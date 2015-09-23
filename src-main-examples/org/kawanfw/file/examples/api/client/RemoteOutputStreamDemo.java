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
package org.kawanfw.file.examples.api.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.kawanfw.file.api.client.RemoteOutputStream;
import org.kawanfw.file.api.client.RemoteSession;

/**
 * 
 * TestReload the download of remote files and that the hash values match hash
 * values of the original local files
 * 
 * @author Nicolas de Pomereu
 */

public class RemoteOutputStreamDemo {

    public static void main(String[] args) throws Exception {

	// Create a RemoteSession instance and username on the remote server
	// using the URL of the path to the ServerFileManager Servlet
	String url = "https://www.acme.org/ServerFileManager";

	// The login info for strong authentication on server side:
	String username = "myUsername";
	char[] password = { 'm', 'y', 'P', 'a', 's', 's', 'w', 'o', 'r', 'd' };

	// Establish a session with the remote server
	RemoteSession remoteSession = new RemoteSession(url, username, password);

	File file = new File("C:\\Users\\Mike\\Koala.jpg");
	String remoteFile = "/Koala.jpg";

	InputStream in = null;
	OutputStream out = null;

	try {

	    // Get an InputStream from our local file
	    in = new FileInputStream(file);

	    // Create an OutputStream that maps a remote file on the shost
	    out = new RemoteOutputStream(remoteSession, remoteFile,
		    file.length());

	    // Create the remote file reading the InpuStream and writing
	    // on the OutputStream
	    byte[] buffer = new byte[1024 * 4];
	    int n = 0;
	    while ((n = in.read(buffer)) != -1) {
		out.write(buffer, 0, n);
	    }

	    // It is better to also close out before finally
	    // because RemoteOutputStream.close() sends data to the server and
	    // is thus prone to IOException
	    out.close();

	} catch (IOException e) {
	    // Treat IOException, including those thrown by out.close()
	    e.printStackTrace();
	    // Etc.

	} finally {
	    if (in != null)
		in.close();
	    if (out != null)
		out.close();
	}
    }

}
