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
package org.kawanfw.file.servlet.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.lang3.SystemUtils;
import org.kawanfw.commons.util.Tag;
import org.kawanfw.file.api.server.FileConfigurator;

/**
 * 
 * Utility class to use with <code>FileConfigurator</code> implementation
 * 
 * @author Nicolas de Pomereu
 * @since 1.0
 * 
 */
public class HttpConfigurationUtil {

    /**
     * Protected constructor
     */
    protected HttpConfigurationUtil() {

    }

    /**
     * Adds the root path to the beginning of path for a remote file.&nbsp;
     * <p>
     * If the server runs on Windows: the method will convert the "/" file
     * separator to "\" if necessary.
     * 
     * @param fileConfigurator
     *            the user http configuration
     * @param username
     *            the client username
     * @param filename
     *            the filename path
     * 
     * @return the new filename path, prefixed with the root path
     */
    public static String addRootPath(FileConfigurator fileConfigurator,
	    String username, String filename) throws IOException {
	if (filename == null) {
	    throw new IllegalArgumentException(Tag.PRODUCT_PRODUCT_FAIL
		    + " filename can not be null!");
	}

	File serverRootFile = fileConfigurator.getServerRoot();

	// TestReload the server root file is well defined and that it is creatable
	testServerRootValidity(serverRootFile);

	if (serverRootFile == null || serverRootFile.toString().equals("/")
		|| serverRootFile.toString().toLowerCase().equals("c:\\")) {

	    if (SystemUtils.IS_OS_WINDOWS) {
		// Replace all "/" with "\" for Windows
		filename = filename.replace("/", File.separator);
	    }

	    return filename;
	}

	boolean doUseOneRootPerUsername = fileConfigurator
		.useOneRootPerUsername();

	if (doUseOneRootPerUsername) {
	    // Add username to the root directory
	    serverRootFile = new File(serverRootFile.toString()
		    + File.separator + username);
	}

	if (SystemUtils.IS_OS_WINDOWS) {
	    if (filename.contains(":\\") && filename.length() >= 3) {
		filename = filename.substring(3);
	    }

	    // Replace all "/" with "\" for Windows
	    filename = filename.replace("/", File.separator);

	    if (filename.startsWith(File.separator) && filename.length() >= 1) {
		filename = filename.substring(1);
	    }

	} else {
	    if (filename.startsWith("/") && filename.length() >= 1) {
		filename = filename.substring(1);
	    }
	}

	filename = serverRootFile.toString() + File.separator + filename;

	// Force path creation for filename if it not exists
	File file = new File(filename);
	if (file.getParent() != null) {
	    file.getParentFile().mkdirs();
	}

	return filename;
    }

    /**
     * Tests that the fileConfigurator.getServerRoot() is valid.
     * 
     * @param serverRootFile
     *            fileConfigurator.getServerRoot() value
     * @throws FileNotFoundException
     *             if it's impossible to create it
     */
    public static void testServerRootValidity(File serverRootFile)
	    throws FileNotFoundException {

	if (serverRootFile == null) {
	    return;
	}

	String serverRoot = serverRootFile.toString();

	if (SystemUtils.IS_OS_WINDOWS) {
	    String unit = serverRoot.substring(0, 3);
	    if (!unit.endsWith(":\\")) {
		throw new FileNotFoundException(
			Tag.PRODUCT_USER_CONFIG_FAIL
				+ " FileConfigurator.getServerRoot() Server root directory does not start"
				+ " with a 3 characters string containing Windows unit and file separator (like \"c:\\\"): "
				+ serverRootFile.toString());
	    }
	}
	else {
	    if (! serverRoot.startsWith("/")) {
		throw new FileNotFoundException(
			Tag.PRODUCT_USER_CONFIG_FAIL
				+ " FileConfigurator.getServerRoot() Server root directory does not "
				+ "start with \"/\" separator: "
				+ serverRootFile.toString());
	    }
	}

	if (!serverRootFile.exists() || !serverRootFile.isDirectory()) {
	    serverRootFile.mkdirs();
	}

	if (!serverRootFile.isDirectory()) {
	    throw new FileNotFoundException(
		    Tag.PRODUCT_USER_CONFIG_FAIL
			    + " FileConfigurator.getServerRoot() Server root directory does not exist and can not be created: "
			    + serverRootFile.toString());
	}

    }

}
