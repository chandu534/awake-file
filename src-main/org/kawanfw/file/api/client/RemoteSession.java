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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.kawanfw.commons.api.client.HttpProtocolParameters;
import org.kawanfw.commons.api.client.InvalidLoginException;
import org.kawanfw.commons.api.client.RemoteException;
import org.kawanfw.commons.http.HttpTransfer;
import org.kawanfw.commons.http.HttpTransferUtil;
import org.kawanfw.commons.http.SimpleNameValuePair;
import org.kawanfw.commons.json.ListOfStringTransport;
import org.kawanfw.commons.util.ClientLogger;
import org.kawanfw.commons.util.FrameworkDebug;
import org.kawanfw.commons.util.HtmlConverter;
import org.kawanfw.commons.util.StringUtil;
import org.kawanfw.commons.util.Tag;
import org.kawanfw.file.util.parms.Action;
import org.kawanfw.file.util.parms.Parameter;
import org.kawanfw.file.util.parms.ReturnCode;
import org.kawanfw.file.version.FileVersion;

/**
 * Main class for establishing an http session with a remote host.
 * <p>
 * Also allows the execution of some basic operations:
 * <ul>
 * <li>Get the Java version of the servlet container on the remote server.</li>
 * <li>Call remote Java methods.</li>
 * <li>Upload files by wrapping bytes copy from a {@code FileInputStream} to a
 * {@link RemoteOutputStream}.</li>
 * <li>Download files by wrapping bytes copy from a {@link RemoteInputStream} to
 * a {@code FileOutputStream}.</li>
 * <li>Returns with one call the length of a list of files located on the remote host.</li>
 * </ul>
 * <br>
 * Note that main operations on remote files are done using {@link RemoteFile} class whose
 * method names, signatures and roles are equivalent to those of {@code File} class.
 * <p>
 * Example:
 * <blockquote><pre>
* // Define URL of the path to the {@code ServerFileManager} servlet
 * String url = &quot;https://www.acme.org/ServerFileManager&quot;;
 * 
 * // The login info for strong authentication on server side:
 * String username = &quot;myUsername&quot;;
 * char[] password = { 'm', 'y', 'P', 'a', 's', 's', 'w', 'o', 'r', 'd' };
 * 
 * // Establish a session with the remote server
 * RemoteSession remoteSession = new RemoteSession(url, username, password);
 * 
 * // OK: upload a file
 * remoteSession.upload(new File(&quot;c:\\myFile.txt&quot;), &quot;/home/mylogin/myFile.txt&quot;);
 * </pre></blockquote>
 * <p>
 * Communication via an (authenticating) proxy server is done using an
 * {@link org.kawanfw.commons.api.client.HttpProxy} instance: 
 * 
 * <blockquote><pre>
 * HttpProxy httpProxy = new HttpProxy(&quot;myproxyhost&quot;, 8080);
 * String url = &quot;https://www.acme.org/ServerFileManager&quot;;
 * 
 * // The login info for strong authentication on server side:
 * String username = &quot;myUsername&quot;;
 * char[] password = { 'm', 'y', 'P', 'a', 's', 's', 'w', 'o', 'r', 'd' };
 * 
 * RemoteSession remoteSession = new RemoteSession(url, username, password,
 * 	httpProxy);
 * 
 * // Etc.
  </pre></blockquote>
 * 
 * @see org.kawanfw.commons.api.client.HttpProxy
 * @see org.kawanfw.file.api.client.RemoteFile
 * @see org.kawanfw.file.api.client.RemoteInputStream
 * @see org.kawanfw.file.api.client.RemoteOutputStream
 * 
 * @author Nicolas de Pomereu
 * @since 1.0
 */

public class RemoteSession implements Cloneable {

    static final String REMOTE_SESSION_IS_CLOSED = "RemoteSession is closed.";

    /** For debug info */
    private static boolean DEBUG = FrameworkDebug.isSet(RemoteSession.class);

    /** Defines 1 kilobyte */
    public static final int KB = 1024;

    /** Defines 1 megabyte */
    public static final int MB = 1024 * KB;

    /** Defines if we must use base64 or html encode when using call() */
    private static boolean USE_HTML_ENCODING = true;

    /** The url to use to connectet to the Awake FILE Server */
    private String url = null;

    /**
     * The username is stored in static memory to be passed to upload file
     * servlet
     */
    private String username = null;

    /**
     * Token is stored in static to be available during all session and contains
     * SHA-1(userId + ServerClientLogin.SECRET_FOR_LOGIN) computed by server.
     * Token is re-send and checked at each send or recv command to be sure user
     * is authenticated.
     */
    private String authenticationToken = null;

    /** Proxy to use with HttpUrlConnection */
    private Proxy proxy = null;
    
    /** For authenticated proxy */
    private PasswordAuthentication passwordAuthentication = null;    

    /** The Http Parameters instance */
    private HttpProtocolParameters httpProtocolParameters = null;

    /** The http transfer instance */
    private HttpTransfer httpTransfer = null;

    /** The remote Java version */
    private String remoteJavaVersion = null;

    /**
     * Says if we want to use base64 encoding for parameters passed to call() -
     * This is a method for legacy applications prior to v1.0.
     */
    public static void setUseBase64EncodingForCall() {
	USE_HTML_ENCODING = false;
    }

    /**
     * Private constructor for clone().
     * 
     * @param url
     *            the URL of the path to the {@code ServerFileManager} Servlet
     * @param username
     *            the username for authentication on the Awake Server (may be
     *            null for call() or downloadUrl())
     * @param authenticationToken
     *            the actual token of the Awake FILE session to clone
	* @param proxy
     *            the proxy to use, null for direct access
     * @param passwordAuthentication
     *            the proxy credentials, null if proxy does not require authentication
     * @param httpProtocolParameters
     *            the http parameters to use
     * @param remoteJavaVersion 
     * 		 the Java version on remote server
     */
    private RemoteSession(String url, String username,
	    String authenticationToken, Proxy proxy,
	    PasswordAuthentication passwordAuthentication,

	    HttpProtocolParameters httpProtocolParameters,
	    String remoteJavaVersion) {
	this.url = url;
	this.username = username;
	this.authenticationToken = authenticationToken;
	this.proxy = proxy;
	this.passwordAuthentication = passwordAuthentication;
	this.httpProtocolParameters = httpProtocolParameters;
	this.remoteJavaVersion = remoteJavaVersion;

	httpTransfer = HttpTransferUtil.HttpTransferFactory(url, proxy,
		passwordAuthentication, httpProtocolParameters);
    }

    /**
     * Creates an Awake FILE session with a proxy and protocol parameters.
     * 
     * @param url
     *            the URL of the path to the {@code ServerFileManager} Servlet
     * @param username
     *            the username for authentication on the Awake Server (may be
     *            null for <code>call()</code>
     * @param password
     *            the user password for authentication on the Awake Server (may
     *            be null)
     * @param proxy
     *            the proxy to use, null for direct access
     * @param passwordAuthentication
     *            the proxy credentials, null if proxy does not require
     *            authentication
     * @param httpProtocolParameters
     *            the http parameters to use (may be null)
     * 
     * @throws MalformedURLException
     *             if the url is malformed
     * @throws UnknownHostException
     *             if host URL (http://www.acme.org) does not exists or no
     *             Internet Connection.
     * @throws ConnectException
     *             if the Host is correct but the {@code ServerFileManager}
     *             Servlet is not reachable
     *             (http://www.acme.org/ServerFileManager) and access failed
     *             with a status != OK (200). (If the host is incorrect, or is
     *             impossible to connect to - Tomcat down - the
     *             {@code ConnectException} will be the sub exception
     *             {@code HttpHostConnectException}.)
     * @throws SocketException
     *             if network failure during transmission
     * @throws InvalidLoginException
     *             the username or password is invalid
     * @throws SecurityException
     *             Scheme is required to be https (SSL/TLS)
     * @throws RemoteException
     *             an exception has been thrown on the server side. This traps
     *             an Awake product failure and should not happen.
     * @throws IOException
     *             for all other IO / Network / System Error
     */
    public RemoteSession(String url, String username, char[] password,
	    Proxy proxy, PasswordAuthentication passwordAuthentication,
	    HttpProtocolParameters httpProtocolParameters)
	    throws MalformedURLException, UnknownHostException,
	    ConnectException, SocketException, InvalidLoginException,
	    RemoteException, SecurityException, IOException {

	if (url == null) {
	    throw new MalformedURLException("url is null!");
	}

	@SuppressWarnings("unused")
	URL asUrl = new URL(url); // Try to raise a MalformedURLException;

	this.username = username;
	this.url = url;

	this.proxy = proxy;
	this.passwordAuthentication = passwordAuthentication;
	this.httpProtocolParameters = httpProtocolParameters;

	// username & password may be null: for call()
	if (username == null) {
	    return;
	}

	// Launch the Servlet
	httpTransfer = HttpTransferUtil.HttpTransferFactory(url, proxy, passwordAuthentication, httpProtocolParameters);
	
	// TestReload if SSL required by host
	if (this.url.toLowerCase().startsWith("http://") && isForceHttps()) {
	    throw new SecurityException(
		    Tag.PRODUCT_SECURITY
			    + " Remote Host requires a SSL url that starts with \"https\" scheme");
	}

	String passwordStr = new String(password);

	// Prepare the request parameters
	List<SimpleNameValuePair> requestParams = new Vector<SimpleNameValuePair>();
	requestParams.add(new SimpleNameValuePair(Parameter.TEST_CRYPTO,
		Parameter.TEST_CRYPTO));
	requestParams.add(new SimpleNameValuePair(Parameter.ACTION,
		Action.LOGIN_ACTION));
	requestParams.add(new SimpleNameValuePair(Parameter.USERNAME, username));
	requestParams.add(new SimpleNameValuePair(Parameter.PASSWORD,
		passwordStr));

	httpTransfer.send(requestParams);

	// If everything is OK, we have in our protocol a response that
	// 1) starts with "OK". 2) Is followed by the Authentication Token
	// else: response starts with "INVALID_LOGIN_OR_PASSWORD".

	String receive = httpTransfer.recv();

	debug("receive: " + receive);

	if (receive.startsWith(ReturnCode.INVALID_LOGIN_OR_PASSWORD)) {
	    throw new InvalidLoginException("Invalid username or password.");
	} else if (receive.startsWith(ReturnCode.OK)) {
	    // OK! We are logged in & and correctly authenticated
	    // Keep in static memory the Authentication Token for next api
	    // commands (First 20 chars)
	    String theToken = receive.substring(ReturnCode.OK.length() + 1);
	    
	    authenticationToken = StringUtils.left(theToken,
		    Parameter.TOKEN_LEFT_SIZE);
	} else {
	    this.username = null;
	    // Should never happen
	    throw new InvalidLoginException(Tag.PRODUCT_PRODUCT_FAIL
		    + " Please contact support.");
	}

    }

    /**
     * Creates an Awake FILE session with a proxy.
     * 
     * @param url
     *            the URL of the path to the {@code ServerFileManager} Servlet
     * @param username
     *            the username for authentication on the Awake Server (may be
     *            null for <code>call()</code>
     * @param password
     *            the user password for authentication on the Awake Server (may
     *            be null)
	 * @param proxy
     *            the proxy to use, null for direct access
     * @param passwordAuthentication
     *            the proxy credentials, null if proxy does not require authentication
     * 
     * @throws MalformedURLException
     *             if the url is malformed
     * @throws UnknownHostException
     *             if host URL (http://www.acme.org) does not exists or no
     *             Internet Connection.
     * @throws ConnectException
     *             if the Host is correct but the {@code ServerFileManager}
     *             Servlet is not reachable
     *             (http://www.acme.org/ServerFileManager) and access failed
     *             with a status != OK (200). (If the host is incorrect, or is
     *             impossible to connect to - Tomcat down - the
     *             {@code ConnectException} will be the sub exception
     *             {@code HttpHostConnectException}.)
     * @throws SocketException
     *             if network failure during transmission
     * @throws InvalidLoginException
     *             the username or password is invalid
     * @throws SecurityException
     *             scheme is required to be https (SSL/TLS)
     * @throws RemoteException
     *             an exception has been thrown on the server side
     * @throws IOException
     *             for all other IO / Network / System Error
     */
    public RemoteSession(String url, String username, char[] password,
		 Proxy proxy, 
		 PasswordAuthentication passwordAuthentication) throws MalformedURLException,
	    UnknownHostException, ConnectException, SocketException,
	    InvalidLoginException, RemoteException, SecurityException,
	    IOException {
	this(url, username, password, proxy, passwordAuthentication, null);
    }

    /**
     * Creates an Awake FILE session.
     * 
     * @param url
     *            the URL of the path to the {@code ServerFileManager} Servlet
     * @param username
     *            the username for authentication on the Awake Server (may be
     *            null for <code>call()</code>
     * @param password
     *            the user password for authentication on the Awake Server (may
     *            be null)
     * 
     * @throws MalformedURLException
     *             if the url is malformed
     * @throws UnknownHostException
     *             if host URL (http://www.acme.org) does not exists or no
     *             Internet Connection.
     * @throws ConnectException
     *             if the Host is correct but the {@code ServerFileManager}
     *             Servlet is not reachable
     *             (http://www.acme.org/ServerFileManager) and access failed
     *             with a status != OK (200). (If the host is incorrect, or is
     *             impossible to connect to - Tomcat down - the
     *             {@code ConnectException} will be the sub exception
     *             {@code HttpHostConnectException}.)
     * @throws SocketException
     *             if network failure during transmission
     * @throws InvalidLoginException
     *             the username or password is invalid
     * @throws SecurityException
     *             scheme is required to be https (SSL/TLS)
     * @throws RemoteException
     *             an exception has been thrown on the server side
     * @throws IOException
     *             for all other IO / Network / System Error
     */

    public RemoteSession(String url, String username, char[] password)
	    throws MalformedURLException, UnknownHostException,
	    ConnectException, SocketException, InvalidLoginException,
	    RemoteException, IOException, SecurityException {
	this(url, username, password, null, null);
    }

    /**
     * Returns the username of this Awake FILE session
     * 
     * @return the username of this Awake FILE session
     */
    public String getUsername() {
	return this.username;
    }

    /**
     * Returns the {@code HttpProtocolParameters} instance in use for the Awake
     * FILE session.
     * 
     * @return the {@code HttpProtocolParameters} instance in use for the Awake
     *         FILE session
     */
    public HttpProtocolParameters getHttpProtocolParameters() {
	return this.httpProtocolParameters;
    }

    /**
     * Returns the URL of the path to the <code>ServerFileManager</code> Servlet
     * (or <code>ServerSqlManager</code> Servlet if session has been initiated by
     * a <code>RemoteConnection</code>).
     * 
     * @return the URL of the path to the <code>ServerFileManager</code> Servlet
     */
    public String getUrl() {
	return url;
    }

    /**
     * Returns the {@code Proxy} instance in use for this File Session.
     * 
     * @return the {@code Proxy} instance in use for this File Session
     */
    public Proxy getProxy() {
	return this.proxy;
    }
    
    /**
     * Returns the proxy credentials
     * @return the proxy credentials
     */
    public PasswordAuthentication getPasswordAuthentication() {
        return passwordAuthentication;
    }

    /**
     * Returns the http status code of the last executed verb
     * 
     * @return the http status code of the last executed verb
     */
    public int getHttpStatusCode() {
	if (httpTransfer != null) {
	    return httpTransfer.getHttpStatusCode();
	} else {
	    return 0;
	}
    }

    /**
     * Returns the Authentication Token. This method is used by the Kawansoft
     * frameworks.
     * 
     * @return the Authentication Token
     */
    public String getAuthenticationToken() {
	return this.authenticationToken;
    }


    /**
     * Calls a remote Java method and (eventually) pass some parameters to it.
     * 
     * @param methodName
     *            the full method name to call in the format
     *            <code>org.acme.config.package.MyClass.myMethod</code>
     * @param params
     *            the array of parameters passed to the method
     * 
     * @return the result of the Java call as {@code String}
     * 
     * @throws IllegalArgumentException
     *             if methodName is null
     * @throws InvalidLoginException
     *             the session has been closed by a {@code logoff()}
     * 
     * @throws UnknownHostException
     *             if host URL (http://www.acme.org) does not exists or no
     *             Internet Connection.
     * @throws ConnectException
     *             if the Host is correct but the {@code ServerFileManager}
     *             Servlet is not reachable
     *             (http://www.acme.org/ServerFileManager) and access failed
     *             with a status != OK (200). (If the host is incorrect, or is
     *             impossible to connect to - Tomcat down - the
     *             {@code ConnectException} will be the sub exception
     *             {@code HttpHostConnectException}.)
     * @throws SocketException
     *             if network failure during transmission
     * @throws RemoteException
     *             an exception has been thrown on the server side
     * @throws IOException
     *             for all other IO / Network / System Error
     * 
     */

    public String call(String methodName, Object... params)
	    throws IllegalArgumentException, InvalidLoginException,
	    UnknownHostException, ConnectException, SocketException,
	    RemoteException, IOException {

	// For legacy methods
	if (!USE_HTML_ENCODING) {
	    return callBase64Encoded(methodName, params);
	}

	// Class and method name can not be null
	if (methodName == null) {
	    throw new IllegalArgumentException("methodName can not be null!");
	}

	// username & Authentication Token may be null
	// because some methods can be called freely

	if (username == null) {
	    username = "null";
	}

	if (authenticationToken == null) {
	    authenticationToken = "null";
	}

	// Build the params types
	List<String> paramsTypes = new Vector<String>();

	// Build the params values
	List<String> paramsValues = new Vector<String>();

	debug("");

	for (int i = 0; i < params.length; i++) {
	    if (params[i] == null) {
		throw new IllegalArgumentException(
			Tag.PRODUCT
				+ " null values are not supported. Please provide a value for all parameters.");
	    } else {
		String classType = params[i].getClass().getName();

		// NO! can alter class name if value is obsfucated
		// classType = StringUtils.substringAfterLast(classType, ".");
		paramsTypes.add(classType);

		String value = params[i].toString();

		debug("");
		debug("classType: " + classType);
		debug("value    : " + value);

		paramsValues.add(value);
	    }
	}

	// ListHolder listHolderTypes = new ListHolder();
	// listHolderTypes.setList(paramsTypes);
	String jsonParamTypes = ListOfStringTransport.toJson(paramsTypes);

	// ListHolder listHolderValues = new ListHolder();
	// listHolderValues.setList(paramsValues);
	String jsonParamValues = ListOfStringTransport.toJson(paramsValues);

	debug("methodName     : " + methodName);
	debug("jsonParamTypes : " + jsonParamTypes);
	debug("jsonParamValues: " + jsonParamValues);

	// Prepare the request parameters
	List<SimpleNameValuePair> requestParams = new Vector<SimpleNameValuePair>();
	requestParams.add(new SimpleNameValuePair(Parameter.ACTION,
		Action.CALL_ACTION_HTML_ENCODED));
	requestParams.add(new SimpleNameValuePair(Parameter.USERNAME, username));
	requestParams.add(new SimpleNameValuePair(Parameter.TOKEN,
		authenticationToken));
	requestParams.add(new SimpleNameValuePair(Parameter.METHOD_NAME,
		methodName));
	requestParams.add(new SimpleNameValuePair(Parameter.PARAMS_TYPES,
		jsonParamTypes));
	requestParams.add(new SimpleNameValuePair(Parameter.PARAMS_VALUES,
		jsonParamValues));

	httpTransfer.send(requestParams);

	// Return the answer
	String response = httpTransfer.recv();

	debug("response: " + response);

	// Content is OK
	if (response.startsWith(ReturnCode.INVALID_LOGIN_OR_PASSWORD)) {
	    throw new InvalidLoginException(REMOTE_SESSION_IS_CLOSED);
	}

	// The response is in Html encode:
	if (!response.isEmpty()) {
	    response = HtmlConverter.fromHtml(response);
	}

	return response;

    }

   
    /**
     * Returns with one call the length of a list of files located on the remote host.
     * <p>
     * This convenient methods is provided for fast compute of the total length
     * of a list of files to download, without contacting the server
     * for each file result. (Case using a progress monitor).
     * <p>
     * The real paths of the remote files depend on the Awake FILE
     * configuration on the server. See User Documentation.
     * 
     * @param pathnames
     *            the list of pathnames on host with "/" as file separator. Must be absolute.
     * 
     * @return the total length in bytes of the files located on the remote host.
     * 
     * @throws IllegalArgumentException
     *             if pathnames is null
     * @throws InvalidLoginException
     *             the session has been closed by a {@code logoff()}
     * 
     * @throws UnknownHostException
     *             if the host URL (http://www.acme.org) does not exists or no
     *             Internet Connection.
     * @throws ConnectException
     *             if the Host is correct but the {@code ServerFileManager}
     *             Servlet is not reachable
     *             (http://www.acme.org/ServerFileManager) and access failed
     *             with a status != OK (200). (If the host is incorrect, or is
     *             impossible to connect to - Tomcat down - the
     *             {@code ConnectException} will be the sub exception
     *             {@code HttpHostConnectException}.)
     * @throws SocketException
     *             if network failure during transmission
     * @throws RemoteException
     *             an exception has been thrown on the server side
     * @throws IOException
     *             for all other IO / Network / System Error
     */
    public long length(List<String> pathnames)
	    throws IllegalArgumentException, InvalidLoginException,
	    UnknownHostException, ConnectException, SocketException,
	    RemoteException, IOException {
	
	if (pathnames == null) {
	    throw new IllegalArgumentException("pathnames can not be null!");
	}

	if (username == null || authenticationToken == null) {
	    throw new InvalidLoginException(REMOTE_SESSION_IS_CLOSED);
	}

	pathnames = HtmlConverter.toHtml(pathnames);
	String jsonString = ListOfStringTransport.toJson(pathnames);

	// Prepare the request parameters
	List<SimpleNameValuePair> requestParams = new Vector<SimpleNameValuePair>();
	requestParams.add(new SimpleNameValuePair(Parameter.ACTION,
		Action.GET_FILE_LENGTH_ACTION));
	requestParams.add(new SimpleNameValuePair(Parameter.USERNAME, username));
	requestParams.add(new SimpleNameValuePair(Parameter.TOKEN,
		authenticationToken));
	requestParams
		.add(new SimpleNameValuePair(Parameter.FILENAME, jsonString));

	httpTransfer.send(requestParams);

	// If everything is OK, we have in our protocol a response that
	// 1) starts with "OK". 2) Is followed by the Authentication Token
	// else: response starts with "INVALID_LOGIN_OR_PASSWORD".

	String response = httpTransfer.recv();

	if (response.startsWith(ReturnCode.INVALID_LOGIN_OR_PASSWORD)) {
	    throw new InvalidLoginException(REMOTE_SESSION_IS_CLOSED);
	} else {
	    try {
		long fileLength = Long.parseLong(response);
		return fileLength;
	    } catch (NumberFormatException nfe) {
		// Build an Awake Exception with the content of the recv stream
		throw new IOException(Tag.PRODUCT_PRODUCT_FAIL + " "
			+ nfe.getMessage(), nfe);
	    }
	}
    }
    
    /**
     * Downloads a file from the remote server. <br>
     * This method simply wraps bytes copy from a {@link RemoteInputStream} to a
     * {@code FileOutputStream}.
     * <p>
     * The real path of the remote file depends on the Awake FILE
     * configuration on the server. See User Documentation.
     * <p>
     * Large files are split in chunks that are downloaded in sequence. The
     * default chunk length is 10Mb. You can change the default value with
     * {@link HttpProtocolParameters#setDownloadChunkLength(long)} before
     * passing {@code HttpProtocolParameters} to this class constructor.
     * <p>
     * Note that file chunking requires that all chunks be downloaded from to
     * the same web server. Thus, file chunking does not support true stateless
     * architecture with multiple identical web servers. If you want to set a
     * full stateless architecture with multiple identical web servers, you must
     * disable file chunking. This is done by setting a 0 download chunk length
     * value using {@link HttpProtocolParameters#setDownloadChunkLength(long)}.
     * <br>
     * <br>
     * A recovery mechanism allows - in case of failure - to start again in the
     * same JVM run the file download from the last non-downloaded chunk. See
     * User Guide for more information. <br>
     * <br>
     * Note that this method can not be used with a progress indicator/monitor
     * and so does not implement any increment mechanism. The reason is dual:
     * <ul>
     * <li>Implementing an increment mechanism would require to add cumbersome
     * API.</li>
     * <li>Wrapped classes {@link RemoteInputStream} and
     * {@code FileOutputStream} allow easy implementation of progress
     * indicators. See Tutorial and included examples.</li>
     * </ul>
     * 
     * @param pathname
     *            the pathname on host with "/" as file separator. Must be absolute.
     * @param file
     *            the file to create on the client side
     * @throws IllegalArgumentException
     *             if file or pathname is null
     * @throws InvalidLoginException
     *             the session has been closed by a {@code logoff()}
     * @throws FileNotFoundException
     *             if the remote file is not found on server
     * @throws UnknownHostException
     *             if host URL (http://www.acme.org) does not exists or no
     *             Internet Connection.
     * @throws ConnectException
     *             if the Host is correct but the {@code ServerFileManager}
     *             Servlet is not reachable
     *             (http://www.acme.org/ServerFileManager) and access failed
     *             with a status != OK (200). (If the host is incorrect, or is
     *             impossible to connect to - Tomcat down - the
     *             {@code ConnectException} will be the sub exception
     *             {@code HttpHostConnectException}.)
     * @throws SocketException
     *             if network failure during transmission
     * @throws RemoteException
     *             an exception has been thrown on the server side
     * @throws IOException
     *             for all other IO / Network / System Error
     */
    public void download(String pathname, File file)
	    throws IllegalArgumentException, InvalidLoginException,
	    FileNotFoundException, UnknownHostException, ConnectException,
	    SocketException, RemoteException, IOException {

	if (pathname == null) {
	    throw new IllegalArgumentException("pathname can not be null!");
	}

	if (file == null) {
	    throw new IllegalArgumentException("file can not be null!");
	}

	if (getUsername() == null || getAuthenticationToken() == null) {
	    throw new InvalidLoginException(RemoteSession.REMOTE_SESSION_IS_CLOSED);
	}

	InputStream in = null;
	OutputStream out = null;

	// (IOUtils is a general IO stream manipulation utilities 
	// provided by Apache Commons IO)
	
	try {
	    in = new RemoteInputStream(this, pathname);
	    out = new BufferedOutputStream(new FileOutputStream(file));
	    IOUtils.copy(in, out);
	    // Cleaner to close in here so that no Exception is thrown in
	    // finally clause
	    in.close();
	} finally {
	    IOUtils.closeQuietly(in);
	    IOUtils.closeQuietly(out);
	}
    }
    
    
    /**
     * Uploads a file on the server. <br>
     * This method simply wraps bytes copy from a {@code FileInputStream} to a
     * {@link RemoteOutputStream}.
     * <p>
     * The real path of the remote file depends on the Awake FILE
     * configuration on the server. See User Documentation.
     * <p>
     * Large files are split in chunks that are uploaded in sequence. The
     * default chunk length is 3Mb. You can change the default value with
     * {@link HttpProtocolParameters#setUploadChunkLength(long)} before passing
     * {@code HttpProtocolParameters} to this class constructor.
     * <p>
     * Note that file chunking requires all chunks to be sent to the same web
     * server that will aggregate the chunks after the last send. Thus, file
     * chunking does not support true stateless architecture with multiple
     * identical web servers. If you want to set a full stateless architecture
     * with multiple identical web servers, you must disable file chunking. This
     * is done by setting a 0 upload chunk length value using
     * {@link HttpProtocolParameters#setUploadChunkLength(long)}. <br>
     * <br>
     * A recovery mechanism allows - in case of failure - to start again in the
     * same JVM run the file upload from the last non-uploaded chunk. See User
     * Guide for more information. <br>
     * <br>
     * Note that this method can not be used with a progress indicator/monitor
     * and so does not implement any increment mechanism. The reason is dual:
     * <ul>
     * <li>Implementing an increment mechanism would require to add cumbersome
     * API.</li>
     * <li>Wrapped classes {@code FileInputStream} and
     * {@link RemoteOutputStream} allow easy implementation of progress
     * indicators. See Tutorial and included examples.</li>
     * </ul>
     * 
     * @param file
     *            the file to upload
     * @param pathname
     *            the pathname on host with "/" as file separator. Must be absolute.
     * @throws IllegalArgumentException
     *             if file or pathname is null
     * @throws InvalidLoginException
     *             the session has been closed by a {@code logoff()}
     * @throws FileNotFoundException
     *             if the file to upload is not found
     * @throws UnknownHostException
     *             if host URL (http://www.acme.org) does not exists or no
     *             Internet Connection.
     * @throws ConnectException
     *             if the Host is correct but the {@code ServerFileManager}
     *             Servlet is not reachable
     *             (http://www.acme.org/ServerFileManager) and access failed
     *             with a status != OK (200). (If the host is incorrect, or is
     *             impossible to connect to - Tomcat down - the
     *             {@code ConnectException} will be the sub exception
     *             {@code HttpHostConnectException}.)
     * @throws SocketException
     *             if network failure during transmission
     * @throws RemoteException
     *             an exception has been thrown on the server side
     * @throws IOException
     *             for all other IO / Network / System Error
     * 
     */
    public void upload(File file, String pathname)
	    throws IllegalArgumentException, InvalidLoginException,
	    FileNotFoundException, UnknownHostException, ConnectException,
	    SocketException, RemoteException, IOException {

	if (pathname == null) {
	    throw new IllegalArgumentException("pathname can not be null!");
	}

	if (file == null) {
	    throw new IllegalArgumentException("file can not be null!");
	}

	if (!file.exists()) {
	    throw new FileNotFoundException("File does not exists: " + file);
	}

	if (getUsername() == null || getAuthenticationToken() == null) {
	    throw new InvalidLoginException(RemoteSession.REMOTE_SESSION_IS_CLOSED);
	}

	InputStream in = null;
	OutputStream out = null;

	// (IOUtils is a general IO stream manipulation utilities
	// provided by Apache Commons IO)

	try {
	    in = new BufferedInputStream(new FileInputStream(file));
	    out = new RemoteOutputStream(this, pathname, file.length());
	    IOUtils.copy(in, out);
	    // Cleaner to close out here so that no Exception is thrown in
	    // finally clause
	    out.close();
	} finally {
	    IOUtils.closeQuietly(in);
	    IOUtils.closeQuietly(out);
	}
    }

    
    /**
     * Returns the Java version of the the servlet container on the remote
     * server <br>
     * (The value of {@code System.getProperty("java.version")}.
     * 
     * @return the Java version of the the servlet container on the remote
     *         server
     * 
     * @throws InvalidLoginException
     *             the session has been closed by a {@code logoff()}
     * 
     * @throws UnknownHostException
     *             if the host URL (http://www.acme.org) does not exists or no
     *             Internet Connection.
     * @throws ConnectException
     *             if the Host is correct but the {@code ServerFileManager}
     *             Servlet is not reachable
     *             (http://www.acme.org/ServerFileManager) and access failed
     *             with a status != OK (200). (If the host is incorrect, or is
     *             impossible to connect to - Tomcat down - the
     *             {@code ConnectException} will be the sub exception
     *             {@code HttpHostConnectException}.)
     * @throws SocketException
     *             if network failure during transmission
     * @throws RemoteException
     *             an exception has been thrown on the server side
     * @throws IOException
     *             for all other IO / Network / System Error
     */
    public String getRemoteJavaVersion() 
	    throws InvalidLoginException,
	    UnknownHostException, ConnectException, SocketException,
	    RemoteException, IOException {
	
	if (username == null || authenticationToken == null) {
	    throw new InvalidLoginException(REMOTE_SESSION_IS_CLOSED);
	}

	// Remote Java verdion is cached
	if (remoteJavaVersion != null) {
	    return remoteJavaVersion;
	}
	
	// Prepare the request parameters
	List<SimpleNameValuePair> requestParams = new Vector<SimpleNameValuePair>();
	requestParams.add(new SimpleNameValuePair(Parameter.ACTION,
		Action.GET_JAVA_VERSION));
	requestParams.add(new SimpleNameValuePair(Parameter.USERNAME, username));
	requestParams.add(new SimpleNameValuePair(Parameter.TOKEN,
		authenticationToken));

	httpTransfer.send(requestParams);

	// If everything is OK, we have in our protocol a response that
	// 1) starts with "OK". 2) Is followed by the Authentication Token
	// else: response starts with "INVALID_LOGIN_OR_PASSWORD".

	String response = httpTransfer.recv();

	if (response.startsWith(ReturnCode.INVALID_LOGIN_OR_PASSWORD)) {
	    throw new InvalidLoginException(REMOTE_SESSION_IS_CLOSED);
	} else {
	    remoteJavaVersion = response;
	    return remoteJavaVersion;
	}
    }
    
    /**
     * Allows to get a copy of the current <code>RemoteSession</code>: use it to
     * do some simultaneous operations in a different thread (in order to avoid
     * conflicts).
     */
    @Override
    public RemoteSession clone() {
	RemoteSession remoteSession = new RemoteSession(this.url,
		this.username, this.authenticationToken, this.proxy, this.passwordAuthentication,
		this.httpProtocolParameters, this.remoteJavaVersion);
	return remoteSession;
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
     * Logs off from the remote host.&nbsp; This will purge the authentication
     * values necessary for method calls.
     * <p>
     * <b>Method should be called at the closure of the Client application</b>.
     */
    public void logoff() {
	username = null;
	authenticationToken = null;

	proxy = null;
	passwordAuthentication = null;
	httpProtocolParameters = null;

	if (httpTransfer != null) {
	    httpTransfer.close();
	    httpTransfer = null;
	}
    }

    /**
     * Returns the HttpTransfer instance in use
     * 
     * @return the httpTransfer instance
     */
    HttpTransfer getHttpTransfer() {
	return httpTransfer;
    }

    /**
     * 
     * Tests if remote host requires our URL to be SSL
     * 
     * @return true if the host requires SSL
     * 
     * @throws UnknownHostException
     * @throws ConnectException
     * @throws RemoteException
     * @throws SecurityException
     * @throws IOException
     */
    private boolean isForceHttps() throws UnknownHostException,
	    ConnectException, RemoteException, SecurityException, IOException {

	// Prepare the request parameters
	List<SimpleNameValuePair> requestParams = new Vector<SimpleNameValuePair>();
	requestParams.add(new SimpleNameValuePair(Parameter.ACTION,
		Action.BEFORE_LOGIN_ACTION));

	httpTransfer.send(requestParams);

	// If everything is OK, we have in our protocol a response that
	// 1) starts with "OK". 2) Is followed by the Authentication Token
	// else: response starts with "INVALID_LOGIN_OR_PASSWORD".

	String receive = httpTransfer.recv();

	Boolean isForceHttps = new Boolean(receive);
	return isForceHttps.booleanValue();

    }

    /**
     * Calls a remote Java {@code class.method} and (eventually) pass some
     * parameters to it. This method transforms the values in Base64. It's a
     * legacy method not to be used anymore: use {@code call} instead.
     * 
     * @param methodName
     *            the full method name to call in the format
     *            <code>org.acme.config.package.MyClass.myMethod</code>
     * @param params
     *            the array of parameters passed to the method
     * @return the result of the Java call as string
     * 
     * @throws IllegalArgumentException
     *             if methodName is null
     * @throws InvalidLoginException
     *             the session has been closed by a {@code logoff()}
     * 
     * @throws UnknownHostException
     *             if host URL (http://www.acme.org) does not exists or no
     *             Internet Connection.
     * @throws ConnectException
     *             if the Host is correct but the {@code ServerFileManager}
     *             Servlet is not reachable
     *             (http://www.acme.org/ServerFileManager) and access failed
     *             with a status != OK (200). (If the host is incorrect, or is
     *             impossible to connect to - Tomcat down - the
     *             {@code ConnectException} will be the sub exception
     *             {@code HttpHostConnectException}.)
     * @throws SocketException
     *             if network failure during transmission
     * @throws RemoteException
     *             an exception has been thrown on the server side
     * @throws IOException
     *             for all other IO / Network / System Error
     * 
     */

    private String callBase64Encoded(String methodName, Object... params)
	    throws IllegalArgumentException, InvalidLoginException,
	    UnknownHostException, ConnectException, SocketException,
	    RemoteException, IOException {

	// Class and method name can not be null
	if (methodName == null) {
	    throw new IllegalArgumentException("methodName can not be null!");
	}

	// username & Authentication Token may be null
	// because some methods can be called freely

	if (username == null) {
	    username = "null";
	}

	if (authenticationToken == null) {
	    authenticationToken = "null";
	}

	// Build the params types
	List<String> paramsTypes = new Vector<String>();

	// Build the params values
	List<String> paramsValues = new Vector<String>();

	debug("");

	for (int i = 0; i < params.length; i++) {
	    if (params[i] == null) {
		throw new IllegalArgumentException(
			Tag.PRODUCT
				+ " null values are not supported. Please provide a value for all parameters.");
	    } else {
		String classType = params[i].getClass().getName();

		// NO! can alter class name if value is obsfucated
		// classType = StringUtils.substringAfterLast(classType, ".");
		paramsTypes.add(classType);

		String value = params[i].toString();

		debug("");
		debug("classType: " + classType);
		debug("value    : " + value);

		paramsValues.add(value);
	    }

	}

	// ListHolder listHolderTypes = new ListHolder();
	// listHolderTypes.setList(paramsTypes);
	String jsonParamTypes = ListOfStringTransport.toJson(paramsTypes);

	// ListHolder listHolderValues = new ListHolder();
	// listHolderValues.setList(paramsValues);
	String jsonParamValues = ListOfStringTransport.toJson(paramsValues);

	debug("methodName     : " + methodName);
	debug("jsonParamTypes : " + jsonParamTypes);
	debug("jsonParamValues: " + jsonParamValues);

	// Prepare the request parameters
	List<SimpleNameValuePair> requestParams = new Vector<SimpleNameValuePair>();
	requestParams.add(new SimpleNameValuePair(Parameter.ACTION,
		Action.CALL_ACTION));
	// requestParams.add(new SimpleNameValuePair(Parameter.LOGIN,
	// StringUtil.toBase64(username)));
	requestParams.add(new SimpleNameValuePair(Parameter.USERNAME, username));
	requestParams.add(new SimpleNameValuePair(Parameter.TOKEN,
		authenticationToken));
	requestParams.add(new SimpleNameValuePair(Parameter.METHOD_NAME,
		methodName));
	requestParams.add(new SimpleNameValuePair(Parameter.PARAMS_TYPES,
		StringUtil.toBase64(jsonParamTypes)));
	requestParams.add(new SimpleNameValuePair(Parameter.PARAMS_VALUES,
		StringUtil.toBase64(jsonParamValues)));

	httpTransfer.send(requestParams);

	// Return the answer
	String response = httpTransfer.recv();

	debug("response: " + response);

	// Content is OK
	if (response.startsWith(ReturnCode.INVALID_LOGIN_OR_PASSWORD)) {
	    throw new InvalidLoginException(REMOTE_SESSION_IS_CLOSED);
	}

	// The response is in Base 64
	try {
	    if (!response.isEmpty()) {
		response = StringUtil.fromBase64(response);
	    }
	} catch (Exception e) {
	    debug(response);
	    // May happen till new Awake FILE is not deployed on Server
	    throw new IOException(Tag.PRODUCT_PRODUCT_FAIL
		    + " Response must be and is not base64!", e);
	}

	return response;

    }



    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime
		* result
		+ ((authenticationToken == null) ? 0 : authenticationToken
			.hashCode());
	result = prime * result + ((url == null) ? 0 : url.hashCode());
	result = prime * result
		+ ((username == null) ? 0 : username.hashCode());
	return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	RemoteSession other = (RemoteSession) obj;
	if (authenticationToken == null) {
	    if (other.authenticationToken != null)
		return false;
	} else if (!authenticationToken.equals(other.authenticationToken))
	    return false;
	if (url == null) {
	    if (other.url != null)
		return false;
	} else if (!url.equals(other.url))
	    return false;
	if (username == null) {
	    if (other.username != null)
		return false;
	} else if (!username.equals(other.username))
	    return false;
	return true;
    }

    
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	return "RemoteSession [url=" + url + ", username=" + username
		+ ", proxy=" + proxy + ", passwordAuthentication="
		+ passwordAuthentication + ", httpProtocolParameters="
		+ httpProtocolParameters + "]";
    }

    /**
     * debug tool
     */
    private void debug(String s) {
	if (DEBUG) {
	    ClientLogger.getLogger().log(Level.WARNING, s);
	}
    }


    
}

// End
