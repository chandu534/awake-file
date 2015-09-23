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

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.kawanfw.commons.api.server.util.HttpServletRequestStore;
import org.kawanfw.commons.api.server.util.ServerInfo;
import org.kawanfw.commons.api.server.util.Sha1;
import org.kawanfw.commons.api.server.util.SingleLineFormatter;
import org.kawanfw.commons.server.util.ServerLogger;
import org.kawanfw.commons.server.util.embed.TomcatModeStore;
import org.kawanfw.commons.util.FrameworkFileUtil;
import org.kawanfw.commons.util.Tag;

/**
 * 
 * Default implementation of the commons User Configuration for the KawanSoft
 * Frameworks.
 * <p>
 * This defaults implementation will help for a quick start and to test the
 * KawanSoft Frameworks, but <u><i>please note that is implementation is not secure
 * at all</i></u>. <br>
 * Especially: the <code>login</code> method will always return
 * <code>true</code>.
 * <p>
 * So:
 * <ul>
 * <li>The <code>forceSecureHttp</code> method should be set to true by your
 * implementation in order to prevent the login info and the data to be send in
 * clear over the Internet with http protocol
 * frameworks.</li>
 * <li>The <code>login</code> method should be overridden by your specific
 * implementation or using {@link SshAuthCommonsConfigurator} implementation.</li>
 * </ul>
 * <p>
 * 
 * @see org.kawanfw.commons.api.server.SshAuthCommonsConfigurator
 * @author Nicolas de Pomereu
 * @since 1.0
 */

public class DefaultCommonsConfigurator implements
	CommonsConfigurator {

    /** The Logger to use */
    private static Logger KAWANFW_LOGGER = null;

    /** The data source to use for connection pooling */
    private DataSource dataSource = null;
    
    /** Cache the addSecretForAuthToken() value */
    private String secretForAuthAdded = null;

    /**
     * Default Constructor.
     */
    public DefaultCommonsConfigurator() {

    }

    /**
     * @return <code>false</code>. (Client programs will be allowed to send
     *         unsecured http requests).
     */
    @Override
    public boolean forceSecureHttp() {
	return false;
    }

    /**
     * @return Empty <code>HashSet</code>. (No banned IP usernames.)
     */
    @Override
    public Set<String> getBannedUsernames() throws IOException, SQLException {
	return new HashSet<String>();
    }


    /**
     * @return Empty <code>ArrayList</code>. (No IP addresses are
     *         blacklisted.)
     */
    @Override
    public List<String> getIPsBlacklist() throws IOException, SQLException {
	return new ArrayList<String>(); // Empty ArrayList
    }

    /**
     * @return Empty <code>ArrayList</code>. (All IP addresses are
     *         accepted.)
     */
    @Override
    public List<String> getIPsWhitelist() throws IOException, SQLException {
	return new ArrayList<String>(); // Empty ArrayList
    }

    /**
     * @return <code>true</code>. (Client is always granted access).
     */
    @Override
    public boolean login(String username, char[] password) throws IOException,
	    SQLException {
	return true;
    }

    /**
     * 
     * Returns a {@code Connection} from <a
     * href="http://tomcat.apache.org/tomcat-7.0-doc/jdbc-pool.html" >Tomcat
     * JDBC Connection Pool</a>.<br>
     * <br>
     * How the {@code Connection} is extracted depends on KawanSoft framework in use:
     * <ul>
     * <li>SQL framework: the {@code Connection} is extracted from the 
     * {@code DataSource} created by the embedded Tomcat JDBC Pool. The JDBC
     * parameters used to create the {@code DataSource} are defined in the
     * properties file passed at start-up of the SQL Framework.</li>
     * <li>FILE framework: the {@code Connection} is extracted from the
     * {@code DataSource} created by an <a href=
     * "http://tomcat.apache.org/tomcat-7.0-doc/api/org/apache/tomcat/jdbc/pool/DataSourceFactory.html"
     * ><code>org.apache.tomcat.jdbc.pool.DataSourceFactory</code></a> factory
     * defined as a {@code 'jdbc/kawanfw-default'} Resource in {@code server.xml}
     * or {@code context.xml}.<br>
     * Here is an example: <a href="http://www.kawanfw.org/3.0/src/context.xml">context.xml</a></li>
     * </ul>
     * 
     * 
     * @return the {@code Connection} extracted from Tomcat JDBC Connection
     *         Pool.
     */
    @Override
    public Connection getConnection() throws SQLException {
		
	if (dataSource == null) {
	    String servletName = getServletNameFromServletPath();
		
	    if (TomcatModeStore.isFrameworkSql()) {

		// SQL Software
		
		dataSource = TomcatModeStore.getDataSource(servletName);
		
		if (dataSource == null) {
		    
		    if (TomcatModeStore.isTomcatEmbedded()) {

			String message = Tag.PRODUCT_USER_CONFIG_FAIL
				+ " the \"driverClassName\" property is not defined in the properties file for servlet "
				+ servletName;
			ServerLogger.getLogger().log(Level.WARNING, message);
			throw new SQLException(message);
		    }
		    else {
			String message = Tag.PRODUCT_USER_CONFIG_FAIL
				+ " the \"driverClassName\" property is not defined in the properties file for servlet "
				+ servletName + " or the servlet name does not match the url pattern in your web.xml";
			ServerLogger.getLogger().log(Level.WARNING, message);
			throw new SQLException(message);			
		    }
		}
	    } else {
		
		// FILE Software		
		String defaultResourceName = "jdbc/kawanfw-default";

		try {
		    Context initCtx0 = (Context) new InitialContext()
			    .lookup("java:comp/env");
		    dataSource = (DataSource) initCtx0
			    .lookup(defaultResourceName);

		} catch (NamingException e) {
		    String message = Tag.PRODUCT_USER_CONFIG_FAIL
			    + " Invalid <Resource> configuration. Lookup failed on Resource: "
			    + defaultResourceName + " Reason: "
			    + e.getMessage();

		    throw new SQLException(message, e);
		}
	    }

	}
	
	Connection connection = dataSource.getConnection();
	return connection;
    }

    /**
     * Returns the current executing servlet name extracted from the servlet path
     * @return	the current executing  servlet name extracted from the servlet path
     */
    private  String getServletNameFromServletPath() {
	HttpServletRequestStore httpServletRequestStore = new HttpServletRequestStore();
	HttpServletRequest httpServletRequest=  httpServletRequestStore.getHttpServletRequest();
	
	String servletName = httpServletRequest.getServletPath();
	servletName = servletName.trim();
	servletName = StringUtils.substringAfterLast(servletName, "/");
	return servletName;
    }

    /**
     * Returns a concatenation of server specific values that won't change
     * during the JVM life and that client side can not guess in order to secure
     * {@code computeAuthToken()}.
     * <p>
     * To ensure very strong security: <br>
     * this method should be overidden to
     * return this method return value plus a secret value in your
     * {@code CommonsConfigurator.addSecretForAuthToken()} implementation.
     * <p>
     * Example: <blockquote>
     * 
     * <pre>
     * &#064;Override
     * public String addSecretForAuthToken() throws IOException, SQLException {
     *     return (super.addSecretForAuthToken() + &quot;my secret value&quot;);
     * }
     * </pre>
     * 
     * </blockquote>
     * 
     * @return the concatenation of some server specific values that won't
     *         change during the JVM life and that the client side can not
     *         guess: server MAC address + System properties: {@code user.home +
     *         os.name + os.version + os.arch + java.class.path +
     *         java.endorsed.dirs}.
     */
    @Override
    public String addSecretForAuthToken() throws IOException, SQLException {
	
	if (secretForAuthAdded != null) {
	    return secretForAuthAdded;
	}
	
	StringBuilder sb = new StringBuilder();
	
	appendPropertySecure(sb, "ServerInfo.getMacAddress()");
	
	// Should be authorized by Servlet Security Manager 
	appendPropertySecure(sb, "java.home");
	appendPropertySecure(sb, "os.name");
	appendPropertySecure(sb, "os.version");
	appendPropertySecure(sb, "os.arch");
	
	// Try to add impossible to guess java.class.path & java.endorsed.dirs
	appendPropertySecure(sb, "java.class.path");
	appendPropertySecure(sb, "java.endorsed.dirs");
	
	// Cache it!
	secretForAuthAdded = sb.toString();
	return secretForAuthAdded;
    }

    /**
     * Append to a {@code StringBuffer} a value and log the operation in case an excepton is thrown.
     * 
     * @param sb	the {@code StringBuffer} to append value on
     * @param property	the property surname
     * 
     */
    private void appendPropertySecure(StringBuilder sb, String property) {
	String value = null;
	try {
	    // Our own property
	    if (property.equals("ServerInfo.getMacAddress()")) {
		value = ServerInfo.getMacAddress();
	    }
	    else {
		// System properties
		value = System.getProperty(property);
	    }
	    
	    sb.append(value);
	} catch (Exception e) {
	    System.err.println(Tag.PRODUCT +  " Caught an Exception in addSecretForAuthToken() calling " + property + ": " + value + ". " + e.toString());
	}
    }


    /**
     * 
     * This default method is secure if client side always use SSL/TLS httpS
     * calls. <br>
     * <br>
     * You may override the method if you want to create the Authentication
     * Token with your own security rules (adding random values stored in
     * database, etc.)
     * 
     * @return <code>SHA-1(username + secretValue)</code> first 20 hexadecimal
     *         characters. <br>
     * <br>
     *         where:
     *         <ul>
     *         <li>username: the username of the client.</li>
     *         <li>secretValue: the value returned by
     *         {@link #addSecretForAuthToken()}.</li>
     *         </ul>
     * @throws Exception
     *             if any Exception occurs
     */
    @Override
    public String computeAuthToken(String username) throws Exception {

	return defaultComputeAuthToken(username, addSecretForAuthToken());
    }

    /**
     * The default algorithm to use for computing token
     * 
     * @param username
     *            the client side username
     * @param secretForAuthToken
     *            the secret to add
     * @return <code>SHA-1(username + {@link #addSecretForAuthToken()})</code>
     *         first 20 hexadecimal characters.
     * 
     * @throws Exception if any Exception occurs
     */
    public static String defaultComputeAuthToken(String username,
	    String secretForAuthToken) throws Exception {
	// Add more secret info very hard to find for an external hacker
	StringBuilder tokenBuilder = new StringBuilder();
	tokenBuilder.append(username);

	if (secretForAuthToken != null) {
	    tokenBuilder.append(secretForAuthToken);
	}

	Sha1 hashcode = new Sha1();
	String token = hashcode
		.getHexHash((tokenBuilder.toString()).getBytes());

	token = StringUtils.left(token, 20);

	return token;
    }

    /**
     * @return <b><code>null</code></b>. It is highly recommended to override
     *         this method in order to set a secret password in order to
     *         reinforce the security of the transport of request parameters.
     */

    @Override
    public char[] getEncryptionPassword() {
	return null;
    }

    /**
     * @return a Logger whose pattern is located in
     *         <code>user.home/.kawansoft/log/kawanfw.log</code>, that uses a
     *         {@link SingleLineFormatter} and that logs 50Mb into 4 rotating
     *         files.
     */

    @Override
    public Logger getLogger() throws IOException {

	if (KAWANFW_LOGGER == null) {

	    File logDir = new File(FrameworkFileUtil.getUserHomeDotKawansoftDir() + File.separator + "log");
	    logDir.mkdirs();

	    String logFilePattern = logDir.toString() + File.separator
		    + "kawanfw.log";

	    KAWANFW_LOGGER = Logger.getLogger("KawanfwLogger");
	    int limit = 50 * 1024 * 1024;
	    Handler fh = new FileHandler(logFilePattern, limit, 4, true);
	    fh.setFormatter(new SingleLineFormatter(false));
	    KAWANFW_LOGGER.addHandler(fh);
	}

	return KAWANFW_LOGGER;
    }

}
