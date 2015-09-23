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
package org.kawanfw.file.api.server;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.kawanfw.commons.api.server.CommonsConfigurator;

/**
 * 
 * Interface that defines the User Configuration for the Awake FILE Framework.
 * <p>
 * All the implemented methods will be called by the Awake Server programs when
 * a client program asks for a file operation from the client, or when a client
 * program asks for a RPC call of a Java.
 * <p>
 * A concrete implementation should be developed on the server side in order to:
 * <ul>
 * <li>Define the Awake FILE Server root directory.</li>
 * <li>Define if each client username has his own root directory.</li>
 * <li>Define a specific piece of Java code to analyze the method name and its
 * parameter values before allowing or not it's execution.</li>
 * <li>Execute a specific piece of Java code if the method is not allowed.</li>
 * </ul>
 * <p>
 * Please note that Awake FILE comes with a Default FileConfigurator
 * implementation that is *not* secured and should be extended:
 * {@link DefaultFileConfigurator}.
 * <p>
 * 
 * @author Nicolas de Pomereu
 * @since 1.0
 */

public interface FileConfigurator {

    /**
     * Allows to define the Awake FILE Server root directory for the file
     * storage and access. The returned {@code File} must be absolute. <br>
     * {@code null} value is authorized and means that the server root is the
     * default unit of the file system and that there is no root per username.
     * <p>
     * On Windows, if not {@code null} the returned {@code File} must start with
     * at least 3 characters: windows unit and file separator. (Example:
     * {@code "D:\"}).
     * 
     * @return the path of the Server Root. <code>null</code> if there is no
     *         server root to define (address of files will be absolute when
     *         accessing, uploading, downloading, etc.)
     */
    public File getServerRoot();

    /**
     * Allows to define if the Awake FILE Server must use a root directory per
     * client username for the file storage and access.<br>
     * If <code>true</code>, the name of the username will be used as root
     * directory per user. if <code>false</code>,all clients will have the same
     * root directory.
     * <p>
     * if {@code getServerRoot()} returns {@code null} or {@code "/"}, value
     * is meaningless
     * 
     * @return <code>true</code> if there is one root directory per client
     *         username, <code>false</code> if all clients have same root
     *         directory
     */
    public boolean useOneRootPerUsername();

    /**
     * Allows, for the passed client username, to define a specific piece of
     * Java code to analyze the method name and it's parameter values before
     * allowing or not it's execution. <br>
     * If the analysis defined by the method returns false, the method call
     * won't be executed.
     * 
     * @param username
     *            the client username to check the rule for
     * @param connection
     *            the SQL Connection as configured in
     *            {@link CommonsConfigurator#getConnection()} implementation.
     *            Will be null if {@code getConnection()} was no configured.
     * @param methodName
     *            the full method name to call in the format
     *            <code>org.acme.config.package.MyClass.myMethod</code>
     * @param params
     *            the list of parameters passed to the method. Empty list if
     *            none.
     * 
     * @return <code>true</code> if the analyzed call is validated.
     * 
     * @throws IOException
     *             if an IOException occurs
     * @throws SQLException
     *             if a SQLException occurs
     */
    public boolean allowCallAfterAnalysis(String username,
	    Connection connection, String methodName, List<Object> params)
	    throws IOException, SQLException;

    /**
     * Allows to implement specific a Java rule immediately after a call has
     * been refused to a user and <code>allowCallAfterAnalysis</code> returned
     * <code>false</code>. <br>
     * <br>
     * Examples:
     * <ul>
     * <li>Delete the user from the username SQL table so that he never comes
     * back,</li>
     * <li>Log the IP address,</li>
     * <li>Log the info,</li>
     * <li>Send an alert message/email to a Security Officer,</li>
     * <li>Etc.</li>
     * </ul>
     * <p>
     * 
     * @param username
     *            the client username
     * @param connection
     *            the SQL Connection as configured in
     *            {@link CommonsConfigurator#getConnection()} implementation.
     *            Will be null if {@code getConnection()} was no configured.
     * @param ipAddress
     *            the IP address of the client user
     * @param methodName
     *            the full method name to call in the format
     *            <code>org.acme.config.package.MyClass.myMethod</code>
     * @param params
     *            the list of parameters passed to the method
     * 
     * @throws IOException
     *             if an IOException occurs
     * @throws SQLException
     *             if a SQLException occurs
     */
    public void runIfCallRefused(String username, Connection connection,
	    String ipAddress, String methodName, List<Object> params)
	    throws IOException, SQLException;

}
