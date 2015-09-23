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
package org.kawanfw.file.test.api.server.config;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.kawanfw.commons.api.server.DefaultCommonsConfigurator;
import org.kawanfw.commons.server.util.ServerLogger;
import org.kawanfw.commons.util.FrameworkDebug;

/**
 * @author Nicolas de Pomereu
 * 
 *         Default definition of the commons User Configuration for the Awake
 *         Framework
 */

public class TestCommonsConfigurator extends
	DefaultCommonsConfigurator {

    /** Debug info */
    private static boolean DEBUG = FrameworkDebug
	    .isSet(TestCommonsConfigurator.class);
    
    /**
     * Our own Acme Company authentication of remote client users. This methods
     * overrides the {@link DefaultCommonsConfigurator#login} method. <br>
     * The (username, password) values are checked against the user_login table.
     * 
     * @param username
     *            the username sent by Awake client side
     * @param password
     *            the user password sent by Awake client side
     * 
     * @return true if access is granted, else false
     */
    @Override
    public boolean login(String username, char[] password) throws IOException,
	    SQLException {
    		
//	Connection connection = null;
//	
//	try {
//	    connection = super.getConnection();
//	    if (connection == null) {
//	        System.out.println("Connection is NULL!");
//	    }
//	    else {
//	        System.out.println("Connection is OK.");  
//	    }
//	} finally {
//	    // Always close or free the Connection so that it is put
//	    // back into the pool for another user
//	    if (connection != null) {
//		connection.close();
//	    }
//	}
	
	// Login must be "login"
	if (username.contains("username")) {
	    return true;
	}
	else {
	    return false;
	}	
	
    }
      

    /**
     * These usernames must not access our databases: user1 & user2 (In real
     * world case we would retrieve the usernames from a database or a file).
     * 
     * @return the usernames that are banned from our server.
     */
    @Override
    public Set<String> getBannedUsernames() throws IOException, SQLException {
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

    @Override
    public List<String> getIPsWhitelist() {
	return null;
	//return Arrays.asList("87.240.95.233", "87.240.95.233", "127.0.0.1");
    }    
    
    /**
     * @return the secret value used to hash the login for authentication code
     *         creation <br>
     *         Default implementation: return null
     * @throws SQLException 
     * @throws IOException 
     */
    @Override
    public String addSecretForAuthToken() throws IOException, SQLException {
	return (super.addSecretForAuthToken() + "my secret value");
    }
    
    /**
     * @return <b><code>null</code></b> It is highly recommended to override
     *         this method in order to set a secret password in order to
     *         reinforce the security of the transport of request parameters.
     */
    @Override
    public char[] getEncryptionPassword() {
	return "testpass".toCharArray();
    }

    /**
     * Print debug info
     * 
     * @param s
     */

    @SuppressWarnings("unused")
    private void debug(String s) {
	if (DEBUG)
	    ServerLogger.getLogger().log(Level.WARNING, s);
    }

}
