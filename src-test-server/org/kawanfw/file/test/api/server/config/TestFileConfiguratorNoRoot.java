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

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.kawanfw.commons.util.FrameworkDebug;
import org.kawanfw.file.api.server.DefaultFileConfigurator;
import org.kawanfw.file.api.server.FileConfigurator;

/**
 * @author Nicolas de Pomereu
 * 
 *         FileConfigurator implementation. Its extends the default
 *         configuration and provides a security mechanism for login.
 */

public class TestFileConfiguratorNoRoot extends DefaultFileConfigurator
	implements FileConfigurator {

    /** Debug info */
    private static boolean DEBUG = FrameworkDebug
	    .isSet(TestFileConfiguratorNoRoot.class);

    /**
     * Default constructor
     */
    public TestFileConfiguratorNoRoot() {

    }

    /**
     * @return the parent value, and add "-file" to the default c:\awake directory name
     */
    @Override
    public File getServerRoot() {
	
	String content = "null";
	try {
	    FileUtils.writeStringToFile(ServerTestParms.FILE_CONFIGURATOR_TXT, content);
	} catch (IOException e) {
	    e.printStackTrace();
	}
	
	return null;
    }

    @Override
    public boolean useOneRootPerUsername() {
	return false;
    }

    /**
     * @return <code><b>true</b></code>: all methods called are ok, 
     *  except org.kawanfw.file.api.server.ClientCallable.CalculatorNotAllowed
     */
    @Override
    public boolean allowCallAfterAnalysis(String username,
	    Connection connection, String methodName, List<Object> params)
	    throws IOException, SQLException {
	
	// Awlays delete the awake-file-event.log file
	File logEvent = new File(System.getProperty("java.io.tmpdir") + File.separator + "awake-file-event.log");
	logEvent.delete();
			      
	if (methodName.equals("org.kawanfw.file.test.api.server.CalculatorNotAllowed.add") && params.size() == 2)
	{
	    return false;
	}
	else {
	    return true;	
	}
    }
    
    /**
     * 
     * The event will be logged as <code>Level.SEVERE</code> in the
     * <code>user.home/.awake/AwakeFile.log</code> file
     */
    @Override
    public void runIfCallRefused(String username, Connection connection,
	    String ipAddress, String methodName, List<Object> params)
	    throws IOException, SQLException {
	
	// Log method
	super.runIfCallRefused(username, connection, ipAddress, methodName, params);
	
	// Create a file with the event
	File logEvent = new File(System.getProperty("java.io.tmpdir") + File.separator + "awake-file-event.log");
	
	FileUtils.writeStringToFile(logEvent, new Date() + " " + ipAddress + " " + methodName + " " +  params.toString());
			
    }
    
    
    
    /**
     * 
     * @param s
     */
    @SuppressWarnings("unused")
    private void debug(String s) {
	if (DEBUG)
	    System.out.println(this.getClass().getName() + " " + new Date()
		    + " " + s);
    }

}
