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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kawanfw.commons.api.server.DefaultCommonsConfigurator;

/**
 * Default implementation of server side configuration for the Awake FILE
 * Framework:
 * <ul>
 * <li>The Awake FILE root directory will be
 * {@code user.home/.awake-server-root}, where {@code user.home} is the one of
 * the Servlet container.</li>
 * <li>The Awake FILE Manager will use one root directory per username.</li>
 * <li>RPC Code is not analysed before execution and
 * {@link #allowCallAfterAnalysis(String, Connection, String, List)} always
 * returns {@code true}.</li>
 * </ul>
 * <p>
 * 
 * @author Nicolas de Pomereu
 * @since 1.0
 */
public class DefaultFileConfigurator implements FileConfigurator {

    /**
     * Constructor.
     */
    public DefaultFileConfigurator() {

    }

    /**
     * @return <code>user.home/.awake-server-root</code>. ({@code user.home} is
     *         the one of the servlet container).
     */
    @Override
    public File getServerRoot() {
	String userHome = System.getProperty("user.home");
	if (!userHome.endsWith(File.separator)) {
	    userHome += File.separator;
	}
	userHome += ".awake-server-root";
	return new File(userHome);
    }

    /**
     * @return <b><code>true</code></b>: each user have it's own root directory.
     *         (The root directory name is the login username).
     */
    @Override
    public boolean useOneRootPerUsername() {
	return true;
    }

    /**
     * @return <code><b>true</b></code>: all methods called are always allowed
     *         for all client usernames
     */
    @Override
    public boolean allowCallAfterAnalysis(String username,
	    Connection connection, String methodName, List<Object> params)
	    throws IOException, SQLException {
	return true;
    }

    /**
     * 
     * The event will be logged as <code>Level.WARNING</code> in the
     * <code>user.home/.kawansoft/log/kawanfw.log</code> file
     */
    @Override
    public void runIfCallRefused(String username, Connection connection,
	    String ipAddress, String methodName, List<Object> params)
	    throws IOException, SQLException {

	Logger logger = new DefaultCommonsConfigurator().getLogger();

	logger.log(Level.WARNING, "Client " + username + "(IP: " + ipAddress
		+ ") has been denied executing method: " + methodName
		+ " with parameters: " + params);
    }

}
