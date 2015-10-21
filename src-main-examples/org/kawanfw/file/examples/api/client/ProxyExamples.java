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
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.List;

import org.kawanfw.commons.api.client.InvalidLoginException;
import org.kawanfw.commons.api.client.RemoteException;
import org.kawanfw.file.api.client.FileSession;
import org.kawanfw.file.api.client.RemoteSession;

/**
 * @author Nicolas de Pomereu
 *
 */
public class ProxyExamples {

    /**
     * 
     */
    public ProxyExamples() {
	
    }

    /**
     * @param args
     */
    @SuppressWarnings("unused")
    public static void main(String[] args) throws Exception {

	useSimpleProxy();
	
    }

    public static void useSimpleProxyFileSession() throws Exception{

	String url = "http://www.acme.org/ServerFileManager";
	String username = "login";
	char[] password = "password".toCharArray();

	Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(
		    "proxyHostname", 8080));
	    
	PasswordAuthentication passwordAuthentication = null;
	
	// If proxy require authentication:
	passwordAuthentication = new PasswordAuthentication("proxyUsername", "proxyPassword".toCharArray());
	
	FileSession fileSession = new FileSession(url, username,
		password, proxy, passwordAuthentication);
	// Etc.
    }
    
    public static void useSimpleProxy() throws Exception{

	String url = "http://www.acme.org/ServerFileManager";
	String username = "myUsername";
	char[] password = "myPassword".toCharArray();

	Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(
		"proxyHostname", 8080));

	PasswordAuthentication passwordAuthentication = null;

	// If proxy require authentication:
	passwordAuthentication = new PasswordAuthentication("proxyUsername",
		"proxyPassword".toCharArray());

	RemoteSession remoteSession = new RemoteSession(url, username,
		password, proxy, passwordAuthentication);
	// Etc.
    }

    public static void useNTLMAuthentication() throws Exception{

	String url = "http://www.acme.org/ServerFileManager";
	String username = "myUsername";
	char[] password = "myPassword".toCharArray();

	Proxy proxy = Proxy.NO_PROXY;
	
	// DOMAIN is passed along username:
	PasswordAuthentication passwordAuthentication = new PasswordAuthentication("DOMAIN\\username", "password".toCharArray());
	
	RemoteSession remoteSession = new RemoteSession(url, username,
		password, proxy, passwordAuthentication);
	// Etc.
    }
    
    public static void useNTLMAuthenticationFileSession() throws Exception{

	String url = "http://www.acme.org/ServerFileManager";
	String username = "myUsername";
	char[] password = "myPassword".toCharArray();

	Proxy proxy = Proxy.NO_PROXY;
	
	// DOMAIN is passed along username:
	PasswordAuthentication passwordAuthentication = new PasswordAuthentication("DOMAIN\\WinUsername", "WinPassword".toCharArray());
	
	FileSession fileSession = new FileSession(url, username,
		password, proxy, passwordAuthentication);
	// Etc.
    }

    
    
    public static void proxyHang() throws URISyntaxException,
	    MalformedURLException, UnknownHostException, ConnectException,
	    SocketException, InvalidLoginException, RemoteException,
	    IOException {
	System.setProperty("java.net.useSystemProxies", "true");
	List<Proxy> proxies = ProxySelector.getDefault().select(
		new URI("http://www.google.com/"));

	String url = "http://www.acme.org:9090/ServerFileManager";
	String username = "login";
	char[] password = "password".toCharArray();
	
	Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(
		    "127.0.0.1", 8080));
	    
	// Code will hang!
	RemoteSession remoteSession = new RemoteSession(url, username,
		password, proxy, null);
    }

}
