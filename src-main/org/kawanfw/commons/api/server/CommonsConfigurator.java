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
package org.kawanfw.commons.api.server;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Interface that defines the common User Configuration for the KawanSoft
 * Frameworks.
 * <p>
 * All the implemented methods will be called by the Server programs when a
 * client program asks for access to the database from the remote Client side.
 * <p>
 * A concrete implementation should be developed on the Server side in order to:
 * <ul>
 * <li>Define how to extract a JDBC Connection from a Connection Pool.</li>
 * <li>Define how to authenticate the remote (username, password) couple sent by
 * the client side.</li>
 * <li>Define if the client must be in secured https prior to authentication.</li>
 * <li>Define the list of banned usernames.</li>
 * <li>Define a whitelist of IPs.</li>
 * <li>Define a blacklist of IPs.</li>
 * <li>Define a secret value to reinforce the strength of the hash value used
 * for Authentication Token.</li>
 * <li>Define a password that will be used to encrypt the http request
 * parameters sent by the client side.</li>
 * <li>Code the method that will compute a secure Authentication Token.</li>
 * <li>Define the Logger for internal logging.</li>
 * </ul>.
 * 
 * @see org.kawanfw.commons.api.server.DefaultCommonsConfigurator
 * @see org.kawanfw.commons.api.server.SshAuthCommonsConfigurator
 * 
 * @author Nicolas de Pomereu
 * @since 1.0
 */
public interface CommonsConfigurator {
    /**
     * Allows to define if the host URL must be accessed in secured httpS.
     * 
     * If true, the server side (ServerFileManager or
     * ServerSqlManager Servlet) will ask the client side to convert the url
     * scheme from "http" to secure "https" for all server requests. This will
     * be done automatically on the client side prior to authentication.
     * 
     * @return <code>true</code> if the host URL must be in httpS
     */
    public boolean forceSecureHttp();

    /**
     * Allows to define the set of banned usernames. The server side will
     * refuse access to client programs calling with a username in the set.
     *
     * @return the set of banned usernames that are not allowed to access the
     *         service
     * @throws IOException
     *             if an IOException occurs
     * @throws SQLException
     *             if a SQLException occurs
     */
    public Set<String> getBannedUsernames() throws IOException, SQLException;

    
    /**
     * Allows to define the blacklist of banned IP addresses. The server side will
     * refuse access to client programs calling with an address in the list.
     * <br>Note that this blacklist is not used if a whitelist has been defined in {@link #getIPsWhitelist}.
     * <p>
     * Subnet notations are supported: 1.1.1.1/255.255.255.255 or 1.1.1.1/32
     * (CIDR-Notation).
     * 
     * @return the blacklist of banned IP addresses, that are not allowed to access
     *         the service
     * @throws IOException
     *             if an IOException occurs
     * @throws SQLException
     *             if a SQLException occurs
     */
    public List<String> getIPsBlacklist() throws IOException, SQLException;
    
    /**
     * Allows to define the whitelist of authorized IP addresses. The server side will
     * grant access only to client programs calling with an address in the list.
     * <br>
     * <br>Note that if the whitelist is empty, all client programs will be authorized to
     * access the server side (ServerFileManager or
     * ServerSqlManager Servlet), except those whose IPs is defined in {@link #getIPsBlacklist()}
     * <p>
     * Subnet notations are supported: 1.1.1.1/255.255.255.255 or 1.1.1.1/32
     * (CIDR-Notation).
     * 
     * @return the whitelist of authorized IP addresses that are allowed to access
     *         the service
     * @throws IOException
     *             if an IOException occurs
     * @throws SQLException
     *             if a SQLException occurs
     */
    public List<String> getIPsWhitelist() throws IOException, SQLException;    
    
    /**
     * Allows to authenticate the remote {@code (usernname, password)} couple sent by the
     * client side.
     * <p>
     * The KawanSoft Server will call the method in order to grant or not client
     * access.
     * <p>
     * Typical usage would be to check the (username, password) couple against a
     * table in a SQL database or against a LDAP, etc.
     * 
     * @param username
     *            the username sent by the client login
     * @param password
     *            the password to connect to the server
     *            
     * @return <code>true</code> if the (login, password) couple is
     *         correct/valid. If false, the client side will not be authorized
     *         to send any command.
     * @throws IOException
     *             if an IOException occurs
     * @throws SQLException
     *             if a SQLException occurs
     */
    public boolean login(String username, char[] password) throws IOException,
	    SQLException;

    /**
     * <p>
     * Attempts to establish a connection with an underlying data source.
     * 
     * @return a <code>Connection</code> to the data source
     * @exception SQLException
     *                if a database access error occurs
     */
    public Connection getConnection() throws SQLException;

    /**
     * Allows to define a secret value that will enforce the security of the
     * authentication defined in <code>computeAuthToken</code>.
     * 
     * @return the secret value to enforce the secure the authentication defined
     *         in <code>computeAuthToken</code>.
     * 
     * @see #computeAuthToken
     * 
     * @throws IOException
     *             if an IOException occurs
     * @throws SQLException
     *             if a SQLException occurs
     */
    public String addSecretForAuthToken() throws IOException, SQLException;

    /**
     * Allows to compute a secret value that will secure the authentication. <br>
     * <br>
     * After login() succeeds, the method uses username and the value returned
     * by {@link #addSecretForAuthToken()} to build and Authentication Token
     * that will be use by each following client call in order to authenticate
     * the calls. <br>
     * <br>
     * The Authentication Token default value built by the default
     * implementation in
     * {@link DefaultCommonsConfigurator#computeAuthToken(String)}
     * is: <br><br>
     * <code>SHA-1(username + secretValue)</code> first 20
     * hexadecimal characters.
     * <br><br>
     * where:
     * <ul>
     * <li>username: the username of the client.</li>
     * <li>secretValue: the value returned by {@link #addSecretForAuthToken()}.</li>
     * </ul>
     * <p>
     * The server side (ServerFileManager or
     * ServerSqlManager Servlet) will use this value to reinforce the strength of the
     * hash value used for Authentication Token at each method call.
     * 
     * @param username
     *            the database user on whose behalf the connection is being made
     * 
     * @return the computed Authentication Token that will be verified and
     *         recomputed at each client call.
     * 
     * @throws Exception
     *             if an Exception occurs
     */
    public String computeAuthToken(String username) throws Exception;

    /**
     * Allows to define the password that is used to encrypt from the Client all
     * the request parameters values for security reason (obfuscation and
     * transport encryption).
     *      
     * @return the password used to encrypt from Client all the request
     *         parameters values for security reason.
     */
    public char[] getEncryptionPassword();

    /**
     * Returns the {@link Logger} that will be used by for logging:
     * <ul>
     * <li>All Exceptions thrown by server side will be logged.</li>
     * <li>Exceptions thrown are logged with <code>Level.WARNING</code>.</li>
     * </ul>
     * <p>
     * It is not necessary nor recommended to implement this method; do it only
     * if you want take control of the logging to modify the default
     * characteristics of {@link DefaultCommonsConfigurator#getLogger()}.
     * 
     * @return the java.util.logging.Logger that will be used for logging
     * @throws IOException
     *             if an IOException occurs
     */
    public Logger getLogger() throws IOException;

}
