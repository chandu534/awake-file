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

import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.kawanfw.commons.api.client.InvalidLoginException;
import org.kawanfw.commons.api.client.RemoteException;
import org.kawanfw.file.api.client.RemoteSession;


/**
 * 
 * Shows how to decode an Exception thrown by {@link RemoteSession}
 * constructors.
 * 
 */
public class RemoteSessionExceptionDecoder {

    /** The FILE Session */
    RemoteSession remoteSession = null;

    /**
     * Constructor.
     * 
     * @param url
     *            url of ServerFileManager servlet
     * @param username
     *            username
     * @param password
     *            password
	 * @param proxy
     *            the proxy to use, may be null for direct access
     * @param passwordAuthentication
     *            the proxy credentials, null if no proxy or if the proxy does not require authentication
     */
    public RemoteSessionExceptionDecoder(String url, String username,
	    char[] password, Proxy proxy, PasswordAuthentication passwordAuthentication) {
	try {
	    // Create the file session to the remote server:
	    remoteSession = new RemoteSession(url, username, password, proxy, 
			  passwordAuthentication);
	    System.out.println("Ok. RemoteSession created!");
	} catch (Exception e) {
	    System.out.println("Failed. Could not create RemoteSession");
	    decodeException(e);
	}

    }

    /**
     * Decodes the Exception thrown when creating a RemoteSession instance. <br>
     * The exception are classified in order from "most local" to "most remote"
     * 
     * @param e
     *            the Exception thrown by RemoteSession when creating an
     *            instance
     */
    public void decodeException(Exception e) {

	//
	// 1) Exceptions thrown before accessing the Internet:
	//
	if (e instanceof MalformedURLException) {
	    System.out.println("The url parameter is malformed.");
	    return;
	}

	if (e instanceof UnknownHostException) {
	    System.out.println("Your Internet connection is down.");
	    return;
	}

	//
	// 2) Exceptions thrown when accessing Internet but
	// before accessing the remote server:
	//

	if (e instanceof ConnectException) {
	    if (remoteSession.getHttpStatusCode() == HttpURLConnection.HTTP_PROXY_AUTH) // 407
	    {
		System.out.println("The proxy requires authentication.");
	    } else {
		System.out
			.println("Impossible to reach ServerFileManager  Servlet: "
				+ e.getMessage());
	    }

	    return;
	}
	
	if (e instanceof SocketException) {
		System.out.println("Network failure during transmission.");
		return;
	}

	//
	// 3) ServerFileManager  Servlet is reached, but the Servlet
	// refuses to grant access to server:
	//

	if (e instanceof InvalidLoginException) {
	    System.out
		    .println("Invalid credential (username, password). Authentication refused.");
	    return;
	}

	//
	// 4) ServerFileManager  Servlet is reached, but the Servlet
	// refuses to grant access to server:
	//

	if (e instanceof SecurityException) {
	    System.out
		    .println("url scheme is not https (SSL/TLS). Authentication refused.");
	    return;
	}
	
	
	// 5) A unexpected remote exception occurred:
	if (e instanceof RemoteException) {
	    RemoteException remoteException = (RemoteException) e;

	    // May be it's a Security exception raised by remote server
	    Throwable cause = remoteException.getCause();
	    if (cause instanceof SecurityException) {
		System.out.println("Remote SecurityException: "
			+ remoteException.getMessage());
		return;
	    }

	    System.out.println("Remote Exception: "
			+ remoteException.getMessage());	    
	    
	    if (remoteException.getRemoteStackTrace() != null) {
		System.out.println("Remote Stack Trace: "
			+ remoteException.getRemoteStackTrace());
	    }
	    
	    return;
	}

	//
	// 6) IO / System Error
	//

	if (e instanceof IOException) {
	    System.out.println("IO / System Error. " + e.toString());
	    

	    if (e.getStackTrace() != null) {
		System.out.println("Stack Trace: "
			+ e.getStackTrace());
	    }	    
	    
	    return;
	}
	
	//
	// 7) Unexpected Exception (should never occur.)
	//

	System.out
		.println("Unexpected Exception Please see http://www.awake-file.org for support: "
			+ e.toString());	
	
	if (e.getStackTrace() != null) {
	    System.out.println("Stack Trace: "
		    + e.getStackTrace());
	}	

    }

}
