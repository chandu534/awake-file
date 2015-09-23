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
package org.kawanfw.commons.http;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.net.ConnectException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.ChallengeState;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.kawanfw.commons.api.client.HttpProtocolParameters;
import org.kawanfw.commons.api.client.HttpProxy;
import org.kawanfw.commons.api.client.RemoteException;
import org.kawanfw.commons.util.ClientLogger;
import org.kawanfw.commons.util.DefaultParms;
import org.kawanfw.commons.util.FrameworkDebug;
import org.kawanfw.commons.util.FrameworkFileUtil;
import org.kawanfw.commons.util.FrameworkSystemUtil;
import org.kawanfw.commons.util.Tag;
import org.kawanfw.commons.util.TransferStatus;

/**
 * HttpTransferOne - Please note that in implementation, result is read in the
 * send method to be sure to release ASAP the server. <br>
 * 
 */

public class HttpTransferOne implements HttpTransfer {
    /** The debug flag */
    private static boolean DEBUG = FrameworkDebug.isSet(HttpTransferOne.class);

    /** Universal and clean line separator */
    public static String CR_LF = System.getProperty("line.separator");

    public final static String HTTP_WWW_GOOGLE_COM = "http://www.google.com";

    /** Response from http server container */
    private String m_responseBody = null;

    /** The http client in use */
    private DefaultHttpClient httpClient;

    /** Target host. Will be passed to httpClient.execute */
    private HttpHost targetHost = null;

    /** The servlet path. Example "/ServerSqlManager */
    private String servletPath = null;

    /** Http context. Will be passed to httpClient.execute */
    private BasicHttpContext localHttpContext;

    //
    // Server Parameter
    //
    /** The url to the main controler servlet session */
    private String url = null;

    /** The Http Proxy instance */
    private HttpProxy httpProxy = null;

    /** The Http Parameters instance */
    private HttpProtocolParameters httpProtocolParameters = null;

    /** If true, all results will be received in a temp file */
    private boolean doReceiveInFile = false;

    /** The file that contains the result of a http request send() */
    private File receiveFile = null;

    /** The Http Status code of the last send() */
    private int statusCode = 0;


    /**
     * Default constructor.&nbsp;
     * <p>
     * 
     * @param url
     *            the URL path to the Sql Manager Servlet
     * @param httpProxy
     *            the proxy (may be null for default settings)
     * @param httpProtocolParameters
     *            the http protocol supplementary parameters (may be null for
     *            default settings)
     */
    public HttpTransferOne(String url, HttpProxy httpProxy,
	    HttpProtocolParameters httpProtocolParameters) {

	if (url == null) {
	    throw new IllegalArgumentException("url can not be null!");
	}

	this.url = url;
	this.httpProxy = httpProxy;
	this.httpProtocolParameters = httpProtocolParameters;

	debug("Before httpClient = new DefaultHttpClient()");
	
	httpClient = new DefaultHttpClient();
	
	debug("After setProxyAndProtocolParameters(httpClient)");
	
	int retryCount = DefaultParms.DEFAULT_RETRY_COUNT;
	if (httpProtocolParameters != null) {
	    retryCount = httpProtocolParameters.getRetryCount();
	}

	debug("After httpProtocolParameters.getRetryCount()");
	
	HttpRequestRetryHandler MyHttpRequestRetryHandler = new DefaultHttpRequestRetryHandler(
		retryCount, false);
	httpClient.setHttpRequestRetryHandler(MyHttpRequestRetryHandler);

	debug("After httpClient.setHttpRequestRetryHandler(MyHttpRequestRetryHandler)");
	
	servletPath = HttpTransferOneUtil.getServletPathFromUrl(url);

	String httpHost = url;
	httpHost = StringUtils.substringBefore(url, servletPath);

	// debug("url        : " + url);
	// debug("servletPath: " + servletPath);
	// debug("httpHost   : " + httpHost);

	HttpHostPartsExtractor httpHostPartsExtractor = new HttpHostPartsExtractor(
		httpHost);
	targetHost = new HttpHost(httpHostPartsExtractor.getHostName(),
		httpHostPartsExtractor.getPort(),
		httpHostPartsExtractor.getSchemeName());
	
	debug("After targetHost = new HttpHost()");

	// debug("hostname: " + targetHost.getHostName());
	// debug("port    : " + targetHost.getPort());
	// debug("scheme  : " + targetHost.getSchemeName());

	// Will fix later the necessary IOException wrapping

	try {
	    setProxyAndProtocolParameters(httpClient);
	} catch (IOException e) {
	    throw new IllegalArgumentException(e);
	}
	
	debug("After setProxyAndProtocolParameters(httpClient))");
	
	
    }

    /**
     * Constructor to use only for for URL download.
     * <p>
     * 
     * @param httpProxy
     *            the proxy (may be null for default settings)
     * @param httpProtocolParameters
     *            the http protocol supplementary parameters
     */
    public HttpTransferOne(HttpProxy httpProxy,
	    HttpProtocolParameters httpProtocolParameters) {
	this.httpProxy = httpProxy;
	this.httpProtocolParameters = httpProtocolParameters;
    }

    /**
     * Set the proxy values for this session
     * 
     * @param httpClient
     *            the Http Client instance
     */
    private void setProxyAndProtocolParameters(DefaultHttpClient httpClient)
	    throws IOException {

	// Say if we want to allow all certificates (including "bad" and
	// self-signed)
	if (httpProtocolParameters != null
		&& httpProtocolParameters.isAcceptAllSslCertificates()
		&& (url == null || url.toLowerCase().startsWith("https://"))
		&& !FrameworkSystemUtil.isAndroid()) {
	    HttpTransferOneUtil.acceptSelfSignedSslCert(httpClient);
	}

	if (httpProtocolParameters != null) {

	    Set<String> httpParamaterNames = httpProtocolParameters
		    .getHttpClientParameterNames();
	    for (String parameter : httpParamaterNames) {
		Object value = httpProtocolParameters
			.getHttpClientParameter(parameter);

		try {
		    httpClient.getParams().setParameter(parameter, value);
		} catch (Exception e) {

		    String className = "unknown";
		    try {
			className = value.getClass().getName();
		    } catch (Exception e1) {
			ClientLogger.getLogger().log(Level.WARNING,
				e1.toString());
		    }

		    throw new IllegalArgumentException(
			    Tag.PRODUCT_USER_CONFIG_FAIL
				    + " Impossible to call httpClient.getParams().setParameter(parameter, value) for parameter: "
				    + parameter + " and value: " + value
				    + " were value is of Java class: "
				    + className);
		}
	    }
	}

	debug("Before if (httpProxy == null");
	
	// Reset proxy if null and return
	if (httpProxy == null) {
	    try {
		displayErrroMessageIfNoProxySet();
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	    return;
	}
	
	debug("After if (httpProxy == null");

	String httpProxyAddress = httpProxy.getAddress();
	int httpProxyPort = httpProxy.getPort();
	String httpProxyUsername = httpProxy.getUsername();
	String httpProxyPassword = httpProxy.getPassword();

	debug("httpProxyAddress : " + httpProxyAddress);
	debug("httpProxyPort    : " + httpProxyPort);
	debug("httpProxyUsername: " + httpProxyUsername);
	debug("httpProxyPassword: " + httpProxyPassword);

	if (httpProxyAddress != null && httpProxyAddress.length() > 0) {

	    HttpHost proxy = new HttpHost(httpProxyAddress, httpProxyPort);
	    httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
		    proxy);

	    if (httpProxyUsername == null || httpProxyUsername.isEmpty()) {
		localHttpContext = new BasicHttpContext();
	    } else {

		// targetHost may be null if we use UrlSession API
		if (targetHost != null) {
		    // Create AuthCache instance
		    AuthCache authCache = new BasicAuthCache();
		    // Generate BASIC scheme object and add it to the local auth
		    // cache

		    BasicScheme basicAuthScheme = new BasicScheme(
			    ChallengeState.PROXY);

		    if (httpProxy.getWorkstation() == null) {
			basicAuthScheme = new BasicScheme(ChallengeState.PROXY);
		    } else {
			basicAuthScheme = new BasicScheme(ChallengeState.TARGET);
		    }

		    authCache.put(targetHost, basicAuthScheme);

		    localHttpContext = new BasicHttpContext();
		    localHttpContext.setAttribute(ClientContext.AUTH_CACHE,
			    authCache);
		}

		if (httpProxy.getWorkstation() != null) {

		    String workstation = httpProxy.getWorkstation();
		    String domain = httpProxy.getDomain();

		    httpClient.getCredentialsProvider().setCredentials(
			    new AuthScope(httpProxyAddress, httpProxyPort),
			    new NTCredentials(httpProxyUsername,
				    httpProxyPassword, workstation, domain));

		} else {
		    httpClient.getCredentialsProvider().setCredentials(
			    new AuthScope(httpProxyAddress, httpProxyPort),
			    new UsernamePasswordCredentials(httpProxyUsername,
				    httpProxyPassword));
		}

	    }

	}
    }

    /**
     * @return the http status code
     */
    @Override
    public int getHttpStatusCode() {
	return this.statusCode;
    }

    /**
     * Send a String to the HTTP server.
     * 
     * @param requestParams
     *            the request parameters list with (parameter, value)
     * 
     * @throws UnknownHostException
     *             Host url (http://www.acme.org) does not exists or no Internet
     *             Connection.
     * @throws ConnectException
     *             The Host is correct but the Servlet
     *             (http://www.acme.org/Servlet) failed with a status <> OK
     *             (200). (if the host is incorrect, or is impossible to connect
     *             to- Tomcat down - will throw a sub exception
     *             HttpHostConnectException)
     * @throws RemoteException
     *             an exception has been thrown on the server side
     * @throws SecurityException
     *             the url is not secured with https (SSL)
     * @throws IOException
     *             For all other IO / Network / System Error
     */
    @Override
    public void send(List<BasicNameValuePair> requestParams)
	    throws UnknownHostException, ConnectException, RemoteException,
	    IOException

    {
	this.send(requestParams, (File) null);
    }

    
    /**
     * Sends a String to the HTTP server and uploads a File
     * 
     * @param requestParams
     *            the request parameters list with (parameter, value)
     * @param file
     *            the File to upload
     * 
     * @throws UnknownHostException
     *             Host url (http://www.acme.org) does not exists or no Internet
     *             Connection.
     * @throws ConnectException
     *             The Host is correct but the Servlet
     *             (http://www.acme.org/Servlet) failed with a status <> OK
     *             (200). (if the host is incorrect, or is impossible to connect
     *             to- Tomcat down - will throw a sub exception
     *             HttpHostConnectException)
     * @throws RemoteException
     *             an exception has been thrown on the server side
     * @throws SecurityException
     *             the url is not secured with https (SSL)
     * @throws IOException
     *             For all other IO / Network / System Error
     */
    @Override
    public void send(List<BasicNameValuePair> requestParams, File file)
	    throws UnknownHostException, ConnectException, RemoteException,
	    IOException {
	statusCode = 0; // Reset it!
	m_responseBody = null; // Reset it!

	try {
	    // We need to Html convert & maybe encrypt the parameters
	    BasicNameValuePairConvertor basicNameValuePairConvertor = new BasicNameValuePairConvertor(
		    requestParams, httpProtocolParameters);
	    requestParams = basicNameValuePairConvertor.convert();

	    // debug("requestParams       : " + requestParams);
	    // debug("requestParams.length: " +
	    // requestParams.toString().length());

	    HttpPost httpPost = new HttpPost(servletPath);

	    if (file == null) {
		httpPost.setEntity(new UrlEncodedFormEntity(requestParams,
			"UTF-8"));
	    } else {

		MultipartEntity multipartEntity = new MultipartEntity(
			HttpMultipartMode.BROWSER_COMPATIBLE);

		for (BasicNameValuePair basicNameValuePair : requestParams) {
		    String paramName = basicNameValuePair.getName();
		    String paramValue = basicNameValuePair.getValue();

		    // For usual String parameters
		    multipartEntity.addPart(paramName,
			    new StringBody(paramValue.toString(), "text/plain",
				    Charset.forName("UTF-8")));
		}

		debug("sending complete file with FileBodyForEngine");
		// For File parameters
 
		//debug("useRemoteOutputStream: " + useRemoteOutputStream);
		
		multipartEntity.addPart("file",
			new FileBodyForRemoteOutputStream(file,
				"application/zip", httpProtocolParameters));
		    
		httpPost.setEntity(multipartEntity);

	    }

	    execute(httpPost);

	} finally {

	    // Reset doReceiveInFile
	    doReceiveInFile = false;
	}
    }

    /**
     * Execute the http post
     * 
     * @param httpPost
     * @throws UnknownHostException
     * @throws ConnectException
     * @throws RemoteException
     * @throws IOException
     */
    private void execute(HttpPost httpPost) throws UnknownHostException,
	    ConnectException, RemoteException, IOException {

	BufferedReader bufferedReader = null;
	HttpEntity entity = null;
	File entityContentFile = null;

	try {
	    // Execute the request
	    HttpResponse response = httpClient.execute(targetHost, httpPost,
		    localHttpContext);
//
//	    // Interrupted by user
//	    if (transferProgressManager != null
//		    && transferProgressManager.isCancelled()) {
//		return;
//	    }

	    // Analyse the error after request execution
	    statusCode = response.getStatusLine().getStatusCode();

	    // Get hold of the response entity
	    entity = response.getEntity();

	    if (statusCode != HttpStatus.SC_OK) {
		// The server is up, but the servlet is not accessible
		throw new ConnectException(url + ": Servlet failed: "
			+ response.getStatusLine() + " status: " + statusCode);
	    }

	    // Get immediately the content as input stream
	    // do *NOT* use a buffered stream ==> Will fail with SSL handshakes!
	    entityContentFile = createKawansoftTempFile();

	    saveEntityContentToFile(entityContentFile, entity);

	    debug("entityContentFile: " + entityContentFile);
	    
	    // Read content of first line.
	    bufferedReader = new BufferedReader(new InputStreamReader(
		    new FileInputStream(entityContentFile)));

	    // line 1: Contains the request status - line 2: Contains the datas
	    String responseStatus = bufferedReader.readLine();
	    // debug("responseStatus        : " + responseStatus);

	    if (doReceiveInFile) {
		// Content is saved back into a file, minus the first line
		// status
		receiveFile = createKawansoftTempFile();
		copyResponseIntoFile(bufferedReader, receiveFile);
	    } else {
		int maxLengthForString = DefaultParms.DEFAULT_MAX_LENGTH_FOR_STRING;
		if (httpProtocolParameters != null) {
		    maxLengthForString = httpProtocolParameters
			    .getMaxLengthForString();
		}

		if (entityContentFile.length() > maxLengthForString) {
		    throw new IOException("Response too big for String: > "
			    + maxLengthForString + " bytes.");
		}

		copyResponseIntoString(bufferedReader);

	    }

	    // Analyse applicative response header
	    // "SEND_OK"
	    // "SEND_FAILED"

	    if (responseStatus.startsWith(TransferStatus.SEND_OK)) {
		return; // OK!
	    } else if (responseStatus.startsWith(TransferStatus.SEND_FAILED)) {
		BufferedReader bufferedReaderException = null;

		debug(TransferStatus.SEND_FAILED);

		try {
		    // We must throw the remote exception
		    if (doReceiveInFile) {
			bufferedReaderException = new BufferedReader(
				new FileReader(receiveFile));
			throwTheRemoteException(bufferedReaderException);
		    } else {
			bufferedReaderException = new BufferedReader(
				new StringReader(m_responseBody));
			throwTheRemoteException(bufferedReaderException);
		    }
		} finally {
		    IOUtils.closeQuietly(bufferedReaderException);

		    if (doReceiveInFile && !DEBUG) {
			receiveFile.delete();
		    }
		}
	    } else {

		String message = "The Server response does not start with awaited SEND_OK or SEND_FAILED."
			+ CR_LF
			+ "This could be a configuration failure with an URL that does not correspond to a Kawansoft Servlet."
			+ CR_LF
			+ "URL: "
			+ url
			+ CR_LF
			+ "This could also be a communication failure. Content of server response: "
			+ CR_LF;

		message += FileUtils.readFileToString(entityContentFile);
		throw new IOException(message);
	    }
	} finally {

	    if (entity != null) {
		consume(entity);
	    }

	    IOUtils.closeQuietly(bufferedReader);
	    
	    if (!DEBUG) {
		FileUtils.deleteQuietly(entityContentFile);
	    }

	}

    }

    /**
     * Wrapper for EntityUtils.consume(entity) that does not exist on Android
     * 
     * @param entity
     * @throws IOException
     */
    private void consume(HttpEntity entity) throws IOException {
	if (!FrameworkSystemUtil.isAndroid()) {
	    EntityUtils.consume(entity);
	}
    }

    /**
     * 
     * Throws an Exception
     * 
     * @param bufferedReader
     *            the reader that contains the remote thrown exception
     * 
     * @throws IOException
     * @throws RemoteException
     * @throws SecurityException
     */
    public static void throwTheRemoteException(BufferedReader bufferedReader)
	    throws RemoteException, IOException {

	String exceptionName = bufferedReader.readLine();

	if (exceptionName.equals("null")) {
	    exceptionName = null;
	}

	if (exceptionName == null) {
	    throw new IOException(
		    Tag.PRODUCT_PRODUCT_FAIL
			    + "Remote Exception type/name not found in servlet output stream");
	}

	String message = bufferedReader.readLine();

	if (message.equals("null")) {
	    message = null;
	}

	StringBuffer sb = new StringBuffer();

	String line = null;
	while ((line = bufferedReader.readLine()) != null) {
	    // All subsequent lines contain the result
	    sb.append(line);
	    sb.append(CR_LF);
	}

	String remoteStackTrace = null;

	if (sb.length() > 0) {
	    remoteStackTrace = sb.toString();
	}

	//System.err.println("exceptionName: " + exceptionName);
	
	// Ok, build the authorized Exception
	if (exceptionName.contains(Tag.ClassNotFoundException)) {
	    throw new RemoteException(message, new ClassNotFoundException(
		    message), remoteStackTrace);
	} else if (exceptionName.contains(Tag.InstantiationException)) {
	    throw new RemoteException(message, new InstantiationException(
		    message), remoteStackTrace);
	} else if (exceptionName.contains(Tag.NoSuchMethodException)) {
	    throw new RemoteException(message, new NoSuchMethodException(
		    message), remoteStackTrace);
	} else if (exceptionName.contains(Tag.InvocationTargetException)) {
	    throw new RemoteException(message, new InvocationTargetException(
		    new Exception(message)), remoteStackTrace);
	}
	
	// NIO case the uploaded .class file java version is incompatible with server java version
	else if (exceptionName.contains(Tag.UnsupportedClassVersionError)) {
	    throw new RemoteException(message, new UnsupportedClassVersionError(
		    message), remoteStackTrace);
	}
	
	//
	// SQL Exceptions
	//
	else if (exceptionName.contains(Tag.SQLException)) {
	    throw new RemoteException(message, new SQLException(message),
		    remoteStackTrace);
	} else if (exceptionName.contains(Tag.BatchUpdateException)) {
	    throw new RemoteException(message, new BatchUpdateException(),
		    remoteStackTrace);
	}

	//
	// Security Failure
	//
	else if (exceptionName.contains(Tag.SecurityException)) {
	    // throw new RemoteException(message, new
	    // SecurityException(message), remoteStackTrace);
	    throw new SecurityException(message);
	}

	//
	// IOExceptions
	//
	else if (exceptionName.contains(Tag.FileNotFoundException)) {
	    throw new RemoteException(message, new FileNotFoundException(
		    message), remoteStackTrace);
	} else if (exceptionName.contains(Tag.IOException)) {
	    throw new RemoteException(message, new IOException(message),
		    remoteStackTrace);
	}
	
	//
	// Server Failure: these errors should never be thrown by server :
	// - NullPointerException
	// - IllegalArgumentException
	//
	else if (exceptionName.contains(Tag.NullPointerException)) {
	    throw new RemoteException(message,
		    new NullPointerException(message), remoteStackTrace);
	} else if (exceptionName.contains(Tag.IllegalArgumentException)) {
	    throw new RemoteException(message, new IllegalArgumentException(
		    message), remoteStackTrace);
	} else {
	    // All other cases ==> IOException with no cause
	    throw new RemoteException("Remote " + exceptionName + ": "
		    + message, new IOException(message), remoteStackTrace);
	}

    }

    /**
     * Immediately Save the entity content into file and release it
     * 
     * @param entityContentFile
     *            the file where to save the entigy content
     * @param entity
     *            the http entity
     * @throws IOException
     * @throws IllegalStateException
     * @throws FileNotFoundException
     */
    private void saveEntityContentToFile(File entityContentFile,
	    HttpEntity entity) throws IOException, IllegalStateException,
	    FileNotFoundException {
	InputStream in;
	in = entity.getContent();
	BufferedOutputStream out = new BufferedOutputStream(
		new FileOutputStream(entityContentFile));

	try {
	    IOUtils.copy(in, out);
	} finally {
	    IOUtils.closeQuietly(in);
	    IOUtils.closeQuietly(out);
	}
    }

    /**
     * Copy the response into a string
     * 
     * @param bufferedReader
     *            the buffered content of the centiy, minus the first line
     * @throws IOException
     */
    private void copyResponseIntoString(BufferedReader bufferedReader)
	    throws IOException {

	StringBuffer sb = new StringBuffer();

	String line = null;
	while ((line = bufferedReader.readLine()) != null) {
	    // All subsequent lines contain the result
	    sb.append(line);
	    sb.append(CR_LF);
	}
	m_responseBody = sb.toString();
	// debug("m_responseBody: " + m_responseBody + ":");
    }

    /**
     * Transform the response stream into a file
     * 
     * @param bufferedReader
     *            the input response stream as line reader
     * @param file
     *            the output file to create
     * 
     * @throws IOException
     *             if any IOException occurs during the process
     */
    private void copyResponseIntoFile(BufferedReader bufferedReader, File file)
	    throws IOException {
	BufferedOutputStream out = null;

	try {
	    out = new BufferedOutputStream(new FileOutputStream(file));
	    String line = null;
	    while ((line = bufferedReader.readLine()) != null) {
		// All subsequent lines contain the result
		out.write((line + CR_LF).getBytes());
	    }
	    out.flush();
	} finally {
	    IOUtils.closeQuietly(out);
	}

    }

    /**
     * Send a String to the HTTP server using receive httpServerProgram Servlet
     * and download a file
     * 
     * @param requestParams
     *            the request parameters list with (parameter, value)
     * @param fileLength
     *            the file length (for the progress indicator). If 0, will not
     *            be used
     * @param file
     *            the file to create on the client side (PC)
     * 
     * @throws IllegalArgumentException
     *             if the file to download is null
     * @throws UnknownHostException
     *             Host url (http://www.acme.org) does not exists or no Internet
     *             Connection.
     * @throws ConnectException
     *             The Host is correct but the Servlet
     *             (http://www.acme.org/Servlet) failed with a status <> OK
     *             (200).
     * @throws IOException
     *             For all other IO / Network / System Error
     */
    /*
    @Override
    public void download(List<BasicNameValuePair> requestParams, File file)
	    throws IllegalArgumentException, UnknownHostException,
	    ConnectException, RemoteException, IOException {

	if (file == null) {
	    throw new IllegalArgumentException(
		    "file to create can not be null!");
	}

	InputStream in = null;
	OutputStream out = null;

	try {
	    // debug("before in = entity.getContent()");

	    // Read the response body.
	    // Do *NOT* use a buffered input stream ==> Will fail with SSL
	    // handshakes!
	    // in = new BufferedInputStream(entity.getContent()) ;

	    in = getInputStream(requestParams);

	    // debug("" + EntityUtils.toByteArray(entity).length);
	    // debug("getContentLength: " + entity.getContentLength());
	    // debug("streaming       : " + entity.isStreaming());
	    // debug("isChunked       : " + entity.isChunked());
	    // debug("isRepeatable    : " + entity.isRepeatable());

	    out = new BufferedOutputStream(new FileOutputStream(file));

	    int downloadBufferSize = DefaultParms.DEFAULT_DOWNLOAD_BUFFER_SIZE;

	    if (httpProtocolParameters != null) {
		downloadBufferSize = httpProtocolParameters
			.getDownloadBufferSize();
	    }

	    byte[] buf = new byte[downloadBufferSize];
	    int len;

	    int tempLen = 0;
	    // setOwnerNote(message);

	    long filesLength = 0;
	    if (transferProgressManager != null) {
		filesLength = transferProgressManager.getLengthToTransfer();
	    }

	    while ((len = in.read(buf)) > 0) {
		tempLen += len;

		if (filesLength > 0
			&& tempLen > filesLength / MAXIMUM_PROGRESS_100) {
		    tempLen = 0;
		    try {
			Thread.sleep(10);
		    } catch (InterruptedException e) {
			e.printStackTrace();
		    }
		    addOneToTransferProgressManager(); // For ProgressMonitor
						       // progress bar
		}

		if (isTransferProgressManagerInterrupted()) {
		    throw new HttpTransferInterruptedException(Tag.PRODUCT
			    + "File download interrupted by user.");
		}

		// totallen += len;
		// debug("out.write(buf, 0, len); - totallen: " + totallen);

		out.write(buf, 0, len);
	    }

	    // debug("before IOUtils.closeQuietly(out);");

	    // Close now, we will reuse file in setStatusAfterDownload
	    IOUtils.closeQuietly(out);

	    // Sets the status after the file download
	    analyseStatusAfterDownload(file);
	} finally {
	    IOUtils.closeQuietly(in);
	    IOUtils.closeQuietly(out);
	}
    }

    /**
     * Send a String to the HTTP server using servlet defined by url and return
     * the corresponding input stream
     * 
     * 
     * @param requestParams
     *            the request parameters list with (parameter, value)
     * @param fileLength
     *            the file length (for the progress indicator). If 0, will not
     *            be used
     * @param file
     *            the file to create on the client side (PC)
     * 
     * @throws IllegalArgumentException
     *             if the file to download is null
     * @throws UnknownHostException
     *             Host url (http://www.acme.org) does not exists or no Internet
     *             Connection.
     * @throws ConnectException
     *             The Host is correct but the Servlet
     *             (http://www.acme.org/Servlet) failed with a status <> OK
     *             (200).
     * @throws IOException
     *             For all other IO / Network / System Error
     */
    @Override
    public InputStream getInputStream(List<BasicNameValuePair> requestParams)
	    throws IllegalArgumentException, UnknownHostException,
	    ConnectException, RemoteException, IOException {

	statusCode = 0; // Reset it!
	m_responseBody = null; // Reset it!

	// DefaultHttpClient httpClient = null;
	HttpPost httpPost = null;

	InputStream in = null;
	HttpEntity entity = null;

	// We need to Html convert & maybe encrypt the parameters
	BasicNameValuePairConvertor basicNameValuePairConvertor = new BasicNameValuePairConvertor(
		requestParams, httpProtocolParameters);
	requestParams = basicNameValuePairConvertor.convert();

	// Create an instance of HttpClient.
	// httpClient = new DefaultHttpClient();

	// Set the Proxy & Socket Values
	// setProxyAndProtocolParameters(httpClient);

	httpPost = new HttpPost(servletPath);
	httpPost.setEntity(new UrlEncodedFormEntity(requestParams, "UTF-8"));

	HttpResponse response = null;

	// Execute the request and analyse the error
	response = httpClient.execute(targetHost, httpPost, localHttpContext);
	statusCode = response.getStatusLine().getStatusCode();

	// Get hold of the response entity
	entity = response.getEntity();

	if (statusCode != HttpStatus.SC_OK) {
	    // The server is up, but the servlet is not accessible
	    throw new ConnectException(url + ": Servlet failed: "
		    + response.getStatusLine() + " status: " + statusCode);
	}

	debug("entity.isStreaming()  : " + entity.isStreaming());
	debug("entity.isRepeatable() : " + entity.isRepeatable());

	// debug("before in = entity.getContent()");
	in = entity.getContent();

	return in;
    }

    /**
     * Closes the http client used for getInputStream
     */

    @Override
    public void close() {
	if (httpClient != null) {
	    httpClient.getConnectionManager().shutdown();
	}
    }

    /**
     * Sets the status after the file download
     * 
     * @param file
     *            the downloaded file
     */
    /*
    private void analyseStatusAfterDownload(File file) throws RemoteException,
	    IOException {
	LineNumberReader lineNumberReader = null;
	InputStream in = null;

	try {
	    in = new BufferedInputStream(new FileInputStream(file));

	    byte[] statusAsBytes = new byte[TransferStatus.SEND_OK.length()];
	    int readBytes = in.read(statusAsBytes);

	    String readStr = new String(statusAsBytes);

	    // debug("readStr  : " + readStr);
	    // debug("readBytes: " + readBytes);

	    // Nothing to read (should not happen, except for empty files...)
	    if (readBytes < TransferStatus.SEND_OK.length()) {
		return;
	    }

	    // SEND_OK may happen if: 1) Invalid Login 2) FileNotFound

	    if ((readStr != null) && readStr.startsWith(TransferStatus.SEND_OK)) {
		IOUtils.closeQuietly(in);

		BufferedReader bufferedReader = new BufferedReader(
			new FileReader(file));

		try {
		    bufferedReader.readLine(); // Read The status line
		    m_responseBody = bufferedReader.readLine();
		    // debug("m_responseBody: " + m_responseBody);
		} finally {
		    IOUtils.closeQuietly(bufferedReader);

		    if (!DEBUG) {
			file.delete();
		    }
		}

	    }
	    // SEND_FAILED contains thrown Exceptions:
	    else if ((readStr != null)
		    && readStr.startsWith(TransferStatus.SEND_FAILED)) {
		IOUtils.closeQuietly(in);

		BufferedReader bufferedReaderException = new BufferedReader(
			new FileReader(file));

		try {
		    throwTheRemoteException(bufferedReaderException);
		} finally {
		    IOUtils.closeQuietly(bufferedReaderException);

		    if (!DEBUG) {
			file.delete();
		    }
		}

	    }

	} finally {
	    IOUtils.closeQuietly(in);
	    IOUtils.closeQuietly(lineNumberReader);

	}
    }
	*/
    
    /**
     * Create our own Kawansoft temp file
     * 
     * @return the tempfile to create
     */
    public static synchronized File createKawansoftTempFile() {
	String unique = FrameworkFileUtil.getUniqueId();
	String tempDir = FrameworkFileUtil.getKawansoftTempDir();
	String tempFile = tempDir + File.separator + "http-transfer-one-"
		+ unique + ".kawanfw.txt";

	return new File(tempFile);
    }

    /**
     * Create a File from a remote URL.
     * 
     * @param url
     *            the url of the site. Example http://www.yahoo.com
     * @param file
     *            the file to create from the download.
     * 
     * @throws IllegalArgumentException
     *             if the url or the file is null
     * @throws UnknownHostException
     *             Host url (http://www.acme.org) does not exists or no Internet
     *             Connection.
     * @throws FileNotFoundException
     *             Impossible to connect to the Host. May appear if, for
     *             example, the Web server is down. (Tomcat down ,etc.)
     * @throws IOException
     *             For all other IO / Network / System Error
     * @throws InterruptedException
     *             if download is interrupted by user through a
     *             TransferProgressManager
     */
    @Override
    public void downloadUrl(URL url, File file)
	    throws IllegalArgumentException, UnknownHostException,
	    FileNotFoundException, IOException

    {
	if (file == null) {
	    throw new IllegalArgumentException("file can not be null!");
	}

	if (url == null) {
	    throw new IllegalArgumentException("url can not be null!");
	}

	statusCode = 0; // Reset it!
	DefaultHttpClient httpClient = null;
	HttpGet httpget = null;

	InputStream in = null;
	OutputStream out = null;

	LineNumberReader lineNumberReader = null;

	try {
	    // Create an instance of HttpClient.
	    httpClient = new DefaultHttpClient();

	    // Set the Proxy & Socket Values
	    setProxyAndProtocolParameters(httpClient);

	    httpget = new HttpGet(url.toString());

	    HttpResponse response = null;
	    HttpEntity entity = null;

	    // Execute the request and analyse the error

	    response = httpClient.execute(httpget);
	    statusCode = response.getStatusLine().getStatusCode();

	    // Get hold of the response entity
	    entity = response.getEntity();

	    if (statusCode != HttpStatus.SC_OK) {

		throw new FileNotFoundException("URL not found: " + url
			+ " status line: " + response.getStatusLine()
			+ " status: " + statusCode);

	    }

	    // Read the response body.
	    in = entity.getContent();

	    // transferProgressManager.setLengthToTransfer(entity.getContentLength());

	    out = new BufferedOutputStream(new FileOutputStream(file));

	    byte[] buf = new byte[4096];
	    int len;

	    int tempLen = 0;

	    long filesLength = 0;
//	    if (transferProgressManager != null) {
//		filesLength = transferProgressManager.getLengthToTransfer();
//	    }

	    while ((len = in.read(buf)) > 0) {
		tempLen += len;

		if (filesLength > 0
			&& tempLen > filesLength / MAXIMUM_PROGRESS_100) {
		    tempLen = 0;
		    try {
			Thread.sleep(10);
		    } catch (InterruptedException e) {
			e.printStackTrace();
		    }

		    //addOneToTransferProgressManager(); // For ProgressMonitor
						       // progress bar
		}

//		if (isTransferProgressManagerInterrupted()) {
//		    throw new InterruptedException(Tag.PRODUCT
//			    + "URL download interrupted by user.");
//		}

		out.write(buf, 0, len);
	    }

	    // We suppose the download is ok:
	    // m_isSendOk = true;

	} finally {
	    IOUtils.closeQuietly(out);
	    IOUtils.closeQuietly(in);
	    IOUtils.closeQuietly(lineNumberReader);

	    if (httpClient != null) {
		httpClient.getConnectionManager().shutdown();
	    }
	}

    }

    /**
     * Create a File from a remote URL.
     * 
     * @param url
     *            the url of the site. Example http://www.yahoo.com
     * @param file
     *            the file to create from the download.
     * 
     * @throws IllegalArgumentException
     *             if the url or the file is null
     * @throws UnknownHostException
     *             Host url (http://www.acme.org) does not exists or no Internet
     *             Connection.
     * 
     * @throws IOException
     *             For all other IO / Network / System Error
     */
    @Override
    public String getUrlContent(URL url) throws IllegalArgumentException,
	    UnknownHostException, IOException

    {

	if (url == null) {
	    throw new IllegalArgumentException("url can not be null!");
	}

	statusCode = 0; // Reset it!
	DefaultHttpClient httpClient = null;
	HttpGet httpget = null;

	InputStream in = null;

	try {
	    // Create an instance of HttpClient.
	    httpClient = new DefaultHttpClient();

	    // Set the Proxy & Socket Values
	    setProxyAndProtocolParameters(httpClient);

	    httpget = new HttpGet(url.toString());

	    HttpResponse response = null;
	    HttpEntity entity = null;

	    // Execute the request and analyze the error

	    response = httpClient.execute(httpget);
	    statusCode = response.getStatusLine().getStatusCode();

	    // Get hold of the response entity
	    entity = response.getEntity();

	    if (statusCode != HttpStatus.SC_OK) {
		throw new FileNotFoundException("URL not found: " + url
			+ " status line: " + response.getStatusLine()
			+ " status: " + statusCode);

	    }

	    // Read the response body.
	    in = entity.getContent();

	    int downloadBufferSize = DefaultParms.DEFAULT_DOWNLOAD_BUFFER_SIZE;
	    int maxLengthForString = DefaultParms.DEFAULT_MAX_LENGTH_FOR_STRING;
	    if (httpProtocolParameters != null) {
		downloadBufferSize = httpProtocolParameters
			.getDownloadBufferSize();
		maxLengthForString = httpProtocolParameters
			.getMaxLengthForString();
	    }

	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    byte[] buf = new byte[downloadBufferSize];
	    int len = 0;
	    int totalLen = 0;
	    while ((len = in.read(buf)) > 0) {
		totalLen += len;

		if (totalLen > maxLengthForString) {
		    throw new IOException(
			    "URL content is too big for download into a String. "
				    + "Maximum length authorized is: "
				    + maxLengthForString);
		}

		out.write(buf, 0, len);
	    }

	    String content = new String(out.toByteArray());
	    return content;

	} finally {
	    IOUtils.closeQuietly(in);

	    if (httpClient != null) {
		httpClient.getConnectionManager().shutdown();
	    }
	}

    }

    /**
     * displays a message if no proxu used
     */
    private void displayErrroMessageIfNoProxySet() {

	if (existsKawansoftProxyTxt()) {
	    String message = "WARNING: No Proxy in use for this HttpClient Session! "
		    + CR_LF + "Click Yes to exit Java, No to continue.";

	    System.err.println(new Date() + " " + message);

	}
    }

    public String recv() {
	if (m_responseBody == null) {
	    m_responseBody = "";
	}

	m_responseBody = m_responseBody.trim();
	return m_responseBody;
    }

    /**
     * Defines if the result is to be received into a text file <br>
     * Call getReceiveFile() to get the file name <br>
     * Defaults to false.
     * 
     * @param receiveInFile
     *            if true, the result will be defined in a file
     */
    @Override
    public void setReceiveInFile(boolean doReceiveInFile) {
	this.doReceiveInFile = doReceiveInFile;
    }

    /**
     * @return the receiveFile
     */
    @Override
    public File getReceiveFile() {
	return receiveFile;
    }

    /**
     * TestReload if a user.home/kawansoft_proxy.txt file exists
     * 
     * @return true if a user.home/kawansoft_proxy.txt file exists
     */
    private static boolean existsKawansoftProxyTxt() {

	String userHome = FrameworkFileUtil.getUserHome();

	if (!userHome.endsWith(File.separator)) {
	    userHome += File.separator;
	}
	File proxyFile = new File(userHome + "kawansoft_proxy.txt");
	if (proxyFile.exists()) {
	    return true;
	} else {
	    return false;
	}
    }

    /**
     * debug tool
     */
    private static void debug(String s) {
	if (DEBUG) {
	    ClientLogger.getLogger().log(Level.WARNING, s);
	}
    }

}
