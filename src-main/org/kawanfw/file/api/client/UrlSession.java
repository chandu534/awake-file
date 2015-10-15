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
package org.kawanfw.file.api.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.logging.Level;

import org.kawanfw.commons.api.client.HttpProtocolParameters;
import org.kawanfw.commons.http.HttpTransfer;
import org.kawanfw.commons.http.HttpTransferUtil;
import org.kawanfw.commons.util.ClientLogger;
import org.kawanfw.commons.util.FrameworkDebug;
import org.kawanfw.file.version.FileVersion;

/**
 * Main class for executing URL downloads.
 * 
 * @deprecated As of version 3.0: no real advantage on {@code java.net.URLConnection} usage.
 * @since 1.0
 * @author Nicolas de Pomereu
 * 
 */

public class UrlSession {
    /** For debug info */
    private static boolean DEBUG = FrameworkDebug.isSet(UrlSession.class);

    /** Proxy to use with HttpUrlConnection */
    private Proxy proxy = null;
    
    /** For authenticated proxy */
    private PasswordAuthentication passwordAuthentication = null;    


    /** The http transfer instance */
    private HttpTransfer httpTransfer = null;

    /**
     * Constructor that allows to define a proxy and protocol parameters.
     * <p>
     * 
     * @param proxy
     *            the proxy to use, null for direct access
     * @param passwordAuthentication
     *            the proxy credentials, null if proxy does not require
     *            authentication
     * 
     * @param httpProtocolParameters
     *            the http parameters to use
     * 
     */
    public UrlSession(Proxy proxy,
	    PasswordAuthentication passwordAuthentication,
	    HttpProtocolParameters httpProtocolParameters)
	    throws IllegalArgumentException {
	this.proxy = proxy;
	this.passwordAuthentication = passwordAuthentication;

	httpTransfer = HttpTransferUtil.HttpTransferFactory(proxy,
		passwordAuthentication, httpProtocolParameters);
    }

    /**
     * Constructor that allows to define a proxy.
     * <p>
     * 
     * @param proxy
     *            the proxy to use, null for direct access
     * @param passwordAuthentication
     *            the proxy credentials, null if proxy does not require
     *            authentication
     */
    public UrlSession(Proxy proxy, PasswordAuthentication passwordAuthentication) {
	this(proxy, passwordAuthentication, null);
    }

    /**
     * Constructor.
     */
    public UrlSession() {
	this(null, null);
    }


    /**
     * Returns the http status code of the last executed download. Will allow to
     * check, for example, if a proxy is required to access the URL.
     * 
     * @return the http status code of the last executed download
     */
    public int getHttpStatusCode() {
	if (httpTransfer != null) {
	    return httpTransfer.getHttpStatusCode();
	} else {
	    return 0;
	}
    }

    /**
     * Creates a File from an URL.
     * 
     * @param url
     *            the URL
     * @param file
     *            the file to create from the download.
     * 
     * @throws IllegalArgumentException
     *             if url or file is null
     * @throws UnknownHostException
     *             if host URL (http://www.acme.org) does not exists or no
     *             Internet Connection.
     * @throws FileNotFoundException
     *             if it is impossible to connect to the URL.
     * @throws InterruptedException
     *             if the download is interrupted by user
     * @throws IOException
     *             For all other IO / Network / System Error
     * 
     */
    public void download(URL url, File file) throws IllegalArgumentException,
	    UnknownHostException, FileNotFoundException, InterruptedException,
	    IOException {

	if (url == null) {
	    throw new IllegalArgumentException("url can not be null!");
	}

	if (file == null) {
	    throw new IllegalArgumentException("file can not be null!");
	}

	httpTransfer.downloadUrl(url, file);

    }

    /**
     * Creates a String from an URL.
     * 
     * @param url
     *            the URL
     * 
     * @return the content of the url
     * 
     * @throws UnknownHostException
     *             if host URL (http://www.acme.org) does not exists or no
     *             Internet Connection.
     * @throws FileNotFoundException
     *             if it is impossible to connect to the URL.
     * @throws IOException
     *             For all other IO / Network / System Error
     * 
     */
    public String download(URL url) throws UnknownHostException, IOException {
	if (url == null) {
	    throw new IllegalArgumentException("url can not be null!");
	}

	String content = httpTransfer.getUrlContent(url);
	return content;
    }

    /**
     * Returns the Awake FILE Version.
     * 
     * @return the Awake FILE Version
     */
    public String getVersion() {
	return FileVersion.getVersion();
    }

    /**
     * debug tool
     */
    @SuppressWarnings("unused")
    private void debug(String s) {
	if (DEBUG) {
	    ClientLogger.getLogger().log(Level.WARNING, s);
	}
    }

}
