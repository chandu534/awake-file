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
package org.kawanfw.commons.api.client;

import java.io.Serializable;

import org.kawanfw.commons.json.HttpProxyGson;

/**
 * 
 * Allows to define a proxy with or without authentication. All is done through
 * constructor, because instance should be immutable:
 * 
 * 
 * 
 * <blockquote><pre>
 * String url = &quot;https://www.acme.org/ServerFileManager&quot;;
 * String username = &quot;myUsername&quot;;
 * char[] password = { 'm', 'y', 'P', 'a', 's', 's', 'w', 'o', 'r', 'd' };
 * 
 * HttpProxy httpProxy = null;
 * 
 * // Constructor to use if http proxy is without authentication:
 * httpProxy = new HttpProxy(&quot;myProxyhost&quot;, 8080);
 * 
 * // Constructor to use if http proxy requires authentication:
 * httpProxy = new HttpProxy(&quot;myProxyhost&quot;, 8080, &quot;username&quot;, &quot;password&quot;);
 * 
 * // Constructor to use if http proxy is a NTLM proxy:
 * httpProxy = new HttpProxy(&quot;myProxyhost&quot;, 8080, &quot;username&quot;, &quot;password&quot;,
 * 	&quot;myWorkstation&quot;, &quot;myDomain&quot;);
 * 
 * // Open an Awake FILE Session through a proxy:
 * RemoteSession remoteSession = new RemoteSession(url, username, password, httpProxy);
 * 
 * // Etc.
 *  </pre></blockquote>
 *  
 * @author Nicolas de Pomereu
 * @since 1.0
 */
public class HttpProxy implements Serializable {

    /**
     * Serial number
     */
    private static final long serialVersionUID = 1452095084146324110L;

    /** The http Proxy Address to use */
    private String address = null;
    /** The http Proxy Port to use */
    private int port = 0;
    /** The proxy credential username */
    private String username = null;
    /** The proxy credential password */
    private String password = null;
    /** NTLM Workstation */
    private String workstation = null;
    /** NTLM Domain */
    private String domain = null;

    /**
     * Constructor to use to define a proxy that does not requires
     * authentication.
     * 
     * @param address
     *            the proxy address
     * @param port
     *            the proxy port
     * 
     * @throws IllegalArgumentException
     *             if address is null
     */
    public HttpProxy(String address, int port) {
	if (address == null) {
	    throw new IllegalArgumentException("Proxy address can not be null!");
	}
	this.address = address;
	this.port = port;
    }

    /**
     * Constructor to use to define a proxy that requires authentication
     * 
     * @param address
     *            the proxy address
     * @param port
     *            the proxy port
     * @param username
     *            the username required for authentication
     * @param password
     *            the password for authentication (maybe null if no password is
     *            required)
     * 
     * 
     * @throws IllegalArgumentException
     *             if address or username is null
     */
    public HttpProxy(String address, int port, String username, String password) {
	this(address, port);
	if (username == null) {
	    throw new IllegalArgumentException(
		    "Proxy username can not be null!");
	}
	this.username = username;
	this.password = password;
    }

    /**
     * Constructor to use to define a Microsoft NTLM proxy that requires
     * authentication.
     * 
     * @param address
     *            the NTLM proxy address
     * @param port
     *            the NTLM proxy port
     * @param username
     *            the username required for authentication
     * @param password
     *            the password for authentication (maybe null if no password is
     *            required)
     * @param workstation
     *            the NTLM workstation parameter
     * @param domain
     *            the NTLM domain parameter
     * 
     * @throws IllegalArgumentException
     *             if address or username is null
     */
    public HttpProxy(String address, int port, String username,
	    String password, String workstation, String domain) {
	this(address, port, username, password);
	if (workstation == null) {
	    throw new IllegalArgumentException(
		    "NTLM Proxy workstation can not be null!");
	}
	if (domain == null) {
	    throw new IllegalArgumentException(
		    "NTLM Proxy domain can not be null!");
	}
	this.workstation = workstation;
	this.domain = domain;
    }

    /**
     * Returns the address of the proxy.
     * 
     * @return the address of the proxy
     */
    public String getAddress() {
	return this.address;
    }

    /**
     * Returns the port of the proxy.
     * 
     * @return the port of the proxy
     */
    public int getPort() {
	return this.port;
    }

    /**
     * Returns the username required for authentication. (<code>null</code> if
     * no authentication is required).
     * 
     * @return the username required for authentication
     */
    public String getUsername() {
	return this.username;
    }

    /**
     * Returns the password. (<code>null</code> if no authentication is required
     * or if no password is required for authentication).
     * 
     * @return the password
     */
    public String getPassword() {
	return this.password;
    }

    /**
     * Returns the NTLM workstation parameter.
     * 
     * @return the NTLM workstation parameter
     */
    public String getWorkstation() {
	return this.workstation;
    }

    /**
     * Returns the NTLM domain parameter.
     * 
     * @return the NTLM domain parameter
     */
    public String getDomain() {
	return this.domain;
    }

    /**
     * Returns a JSon representation of the <code>HttpProxy</code> instance. <br>
     * The JSon formated String can be used later to rebuild the instance from
     * the String.
     * 
     * @return a JSon representation of the <code>HttpProxy</code> instance
     */
    @Override
    public String toString() {
	return HttpProxyGson.toJson(this);
    }
}
