/*
 * This file is part of Awake FILE. 
 * Awake file: Easy file upload & download over HTTP with Java.                                    
 * Copyright (C) 2015,  KawanSoft SAS
 * (http://www.kawansoft.com). All rights reserved.                                
 *                                                                               
 * Awake FILE is free software; you can redistribute it and/or                 
 * modify it under the terms of the GNU Lesser General Public                    
 * License as published by the Free Software Foundation; either                  
 * version 2.1 of the License, or (at your option) any later version.            
 *                                                                               
 * Awake FILE is distributed in the hope that it will be useful,               
 * but WITHOUT ANY WARRANTY; without even the implied warranty of                
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU             
 * Lesser General Public License for more details.                               
 *                                                                               
 * You should have received a copy of the GNU Lesser General Public              
 * License along with this library; if not, write to the Free Software           
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  
 * 02110-1301  USA
 *
 * Any modifications to this file must keep this entire header
 * intact.
 */
package org.kawanfw.file.examples.api.server;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.kawanfw.commons.api.server.CommonsConfigurator;
import org.kawanfw.commons.api.server.DefaultCommonsConfigurator;
import org.kawanfw.commons.api.server.util.Sha1;

/**
 * 
 * Full example of a concrete implementation of an
 * {@link CommonsConfigurator}.
 * 
 * @author Nicolas de Pomereu
 * @since 1.0
 */

public class CommonsConfiguratorExample extends
	DefaultCommonsConfigurator {

    /**
     * Our own Acme Company authentication of remote client users. This methods
     * overrides the {@link DefaultCommonsConfigurator#login} method. <br>
     * The (username, password) values are checked against the user_login table.
     * 
     * @param username
     *            the username sent by client side
     * @param password
     *            the user password sent by client side
     * 
     * @return true if access is granted, else false
     * 
     * @throws IOException
     *             if an IOException occurs
     * @throws SQLException
     *             if a SQLException occurs
     */
    @Override
    public boolean login(String username, char[] password) throws IOException,
	    SQLException {
	Connection connection = null;

	try {
	    // Extract a Connection from our Pool
	    connection = this.getConnection();

	    // Compute the hash of the password sent by client side.
	    Sha1 sha1 = new Sha1();
	    String hashPassword = null;

	    try {
		hashPassword = sha1.getHexHash(new String(password).getBytes());
	    } catch (Exception e) {
		// Wrap the Exception into a IOException
		throw new IOException(e);
	    }

	    // Check (username, sha1(password)) existence in user_login table
	    String sql = "SELECT username FROM user_login "
		    + "WHERE username = ? AND hash_password = ?";
	    PreparedStatement prepStatement = connection.prepareStatement(sql);
	    prepStatement.setString(1, username);
	    prepStatement.setString(2, hashPassword);

	    ResultSet rs = prepStatement.executeQuery();

	    if (rs.next()) {
		// (username, password) are authenticated!
		return true;
	    }

	    return false;
	} finally {
	    // Always close ot free the Connection so that it is put
	    // back into the pool for another user
	    if (connection != null) {
		connection.close();
	    }

	}
    }

    /**
     * @return <code>true</code>.
     * 
     *         Our client programs will be forced to use https in the url parm
     *         of the RemoteSession constructor
     */
    @Override
    public boolean forceSecureHttp() {
	return true;
    }

    /**
     * These usernames must not access our databases: user1 & user2
     * 
     * @return the usernames that are banned from our server.
     */
    @Override
    public Set<String> getBannedUsernames() {
	Set<String> set = new HashSet<String>();
	set.add("user1");
	set.add("user2");
	return set;
    }

    /**
     * These two IP addresses must not access our databases: 87.240.95.233 &
     * 87.240.95.234!
     * 
     * @return the IPs that are banned from our server.
     */
    @Override
    public List<String> getIPsBlacklist() {
	return Arrays.asList("87.240.95.233", "87.240.95.233");
    }


    /**
     * @return the secret value used to hash the login for authentication code
     *         creation <br>
     *         Default implementation: return null
     */
    @Override
    public String addSecretForAuthToken() {
	return "Hello World is *$345";
    }

    /**
     * @return <b><code>null</code></b> It is highly recommended to override
     *         this method in order to set a secret password in order to
     *         reinforce the security of the transport of request parameters.
     */
    @Override
    public char[] getEncryptionPassword() {
	return new char[] { 'm', 'y', 'p', 'a', 's', 's', 'w', 'o', 'r', 'd' };
    }

}
