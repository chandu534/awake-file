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
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.Authenticator;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.kawanfw.commons.api.client.HttpProtocolParameters;
import org.kawanfw.commons.api.client.RemoteException;
import org.kawanfw.commons.util.ClientLogger;
import org.kawanfw.commons.util.DefaultParms;
import org.kawanfw.commons.util.FrameworkDebug;
import org.kawanfw.commons.util.FrameworkFileUtil;
import org.kawanfw.commons.util.TransferStatus;

/**
 * HttpTransferTwo - Http operations done without any library, only using
 * HttpUrlConnection.
 * 
 * Please note that in implementation, result is read in the send method to be
 * sure to release ASAP the server. <br>
 * 
 * Note that HttpUrlConnection.disconect() is never call, see:
 * http://kingori.co/minutae/2013/04/httpurlconnection-disconnect/
 */

public class HttpTransferOne implements HttpTransfer {

    private static final String GET = "GET";
    private static final String POST = "POST";

    /** The debug flag */
    private static boolean DEBUG = FrameworkDebug.isSet(HttpTransferOne.class);

    /** Universal and clean line separator */
    public static String CR_LF = System.getProperty("line.separator");

    public final static String HTTP_WWW_GOOGLE_COM = "http://www.google.com";

    /** Response from http server container */
    private String m_responseBody = null;

    //
    // Server Parameter
    //
    /** The url to the main controler servlet session */
    private String url = null;

    /** The Http Parameters instance */
    private HttpProtocolParameters httpProtocolParameters = null;

    /** If true, all results will be received in a temp file */
    private boolean doReceiveInFile = false;

    /** The file that contains the result of a http request send() */
    private File receiveFile = null;

    /** The Http Status code of the last send() */
    private int statusCode = 0;

    /** The common HttpURLConnection use through all session */
    private HttpURLConnection conn = null;

    /** Connect Timeout to use */
    private int connectTimeout = 0;

    /** Read Timeout to use */
    private int readTimeout = 0;

    /** Proxy to use with HttpUrlConnection */
    private Proxy proxy = null;
    
    /** For authenticated proxy */
    private PasswordAuthentication passwordAuthentication = null;

    /**
     * Default constructor.&nbsp;
     * <p>
     * 
     * @param url
     *            the URL path to the Sql Manager Servlet
     * @param proxy
     *            the proxy to use, null for direct access
     * @param passwordAuthentication
     *            the proxy credentials, null if proxy does not require
     *            authentication
     * @param httpProtocolParameters
     *            the http protocol supplementary parameters (may be null for
     *            default settings)
     */
    public HttpTransferOne(String url, Proxy proxy,
	    PasswordAuthentication passwordAuthentication,
	    HttpProtocolParameters httpProtocolParameters) {

	if (url == null) {
	    throw new IllegalArgumentException("url is null!");
	}

	this.url = url;
	this.proxy = proxy;
	this.passwordAuthentication = passwordAuthentication;
	this.httpProtocolParameters = httpProtocolParameters;


	if (httpProtocolParameters != null) {
	    this.connectTimeout = httpProtocolParameters.getConnectTimeout();
	    this.readTimeout = httpProtocolParameters.getReadTimeout();

	    if (httpProtocolParameters.isAcceptAllSslCertificates()) {
		acceptSelfSignedSslCert();
	    }

	}

	setProxyCredentials();

    }

    /**
     * Sets the proxy credentials
     * 
     */
    private void setProxyCredentials() {

	if (proxy == null) {
	    try {
		displayErrroMessageIfNoProxySet();
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	    return;
	}

	// Sets the proxy
	if (proxy != null) {

	    if (passwordAuthentication != null) {
		final String proxyAuthUsername = passwordAuthentication.getUserName();
		final char [] proxyPassword = passwordAuthentication.getPassword();

		Authenticator authenticator = new Authenticator() {

		    public PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(proxyAuthUsername, proxyPassword);
		    }
		};

		Authenticator.setDefault(authenticator);
	    }
	}
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
    public HttpTransferOne(Proxy proxy,
	    PasswordAuthentication passwordAuthentication,
	    HttpProtocolParameters httpProtocolParameters) {
	
	this.proxy = proxy;
	this.passwordAuthentication = passwordAuthentication;
	this.httpProtocolParameters = httpProtocolParameters;

	if (httpProtocolParameters != null) {
	    this.connectTimeout = httpProtocolParameters.getConnectTimeout();
	    this.readTimeout = httpProtocolParameters.getReadTimeout();
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
     * Builds the http URL connection from URL, with proxy if required.
     * 
     * @param url
     *            the URL path to the Sql Manager Servlet
     * @return the instance with proxy set if necessary
     * @throws IOException
     */
    private HttpURLConnection buildHttpUrlConnection(URL url)
	    throws IOException {
	HttpURLConnection conn;

	if (this.proxy == null) {
	    conn = (HttpURLConnection) url.openConnection();
	} else {
	    conn = (HttpURLConnection) url.openConnection(proxy);
	}

	if (httpProtocolParameters != null) {
	    boolean compressionOn = httpProtocolParameters.isCompressionOn();
	    if (compressionOn) {
		conn.setRequestProperty("Accept-Encoding", "gzip");
	    }
	}

	conn.setConnectTimeout(connectTimeout);
	conn.setReadTimeout(readTimeout);
	conn.setUseCaches(false);

	return conn;
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
    public void send(List<SimpleNameValuePair> requestParams)
	    throws UnknownHostException, ConnectException, RemoteException,
	    IOException

    {
	statusCode = 0; // Reset it!
	m_responseBody = null; // Reset it!

	URL theUrl;
	try {
	    theUrl = new URL(this.url);

	    conn = buildHttpUrlConnection(theUrl);
	    conn.setRequestMethod(POST);
	    conn.setDoOutput(true);

	    // We need to Html convert & maybe encrypt the parameters
	    SimpleNameValuePairConvertor simpleNameValuePairConvertor = new SimpleNameValuePairConvertor(
		    requestParams, httpProtocolParameters);
	    requestParams = simpleNameValuePairConvertor.convert();

	    debug("requestParams: " + requestParams);

	    OutputStream os = conn.getOutputStream();
	    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
		    os, "UTF-8"));
	    writer.write(getPostDataString(requestParams));

	    // writer.flush();
	    writer.close();
	    os.close();

	    getAndAnalyzeResponse(conn);

	} finally {
	    // Reset doReceiveInFile
	    doReceiveInFile = false;
	}

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
    public void send(List<SimpleNameValuePair> requestParams, File file)
	    throws UnknownHostException, ConnectException, RemoteException,
	    IOException {

	statusCode = 0; // Reset it!
	m_responseBody = null; // Reset it!
	URL theUrl;

	try {

	    // We need to Html convert & maybe encrypt the parameters
	    SimpleNameValuePairConvertor simpleNameValuePairConvertor = new SimpleNameValuePairConvertor(
		    requestParams, httpProtocolParameters);
	    requestParams = simpleNameValuePairConvertor.convert();

	    theUrl = new URL(this.url);

	    conn = buildHttpUrlConnection(theUrl);
	    conn.setRequestMethod(POST);
	    conn.setDoOutput(true);

	    final MultipartUtility http = new MultipartUtility(theUrl, conn,
		    httpProtocolParameters);

	    for (SimpleNameValuePair basicNameValuePair : requestParams) {
		http.addFormField(basicNameValuePair.getName(),
			basicNameValuePair.getValue());
	    }

	    http.addFilePart("file", file);
	    http.finish();

	    HttpURLConnection httpUrlConnection = http.getConnection();
	    getAndAnalyzeResponse(httpUrlConnection);

	} finally {
	    // Reset doReceiveInFile
	    doReceiveInFile = false;
	}
    }

    /**
     * Get and analyze the response.
     * 
     * @param conn
     *            the URL connection in use
     * 
     * @throws UnknownHostException
     * @throws ConnectException
     * @throws RemoteException
     * @throws IOException
     */
    private void getAndAnalyzeResponse(HttpURLConnection conn)
	    throws UnknownHostException, ConnectException, RemoteException,
	    IOException {

	BufferedReader reader = null;
	File contentFile = null;

	try {

	    // Analyze the error after request execution
	    statusCode = conn.getResponseCode();

	    if (statusCode != HttpURLConnection.HTTP_OK) {
		// The server is up, but the servlet is not accessible
		throw new ConnectException(url + ": Servlet failed: "
			+ conn.getResponseMessage() + " status: " + statusCode);
	    }

//	    //Get immediately the content as input stream
//	    //do *NOT* use a buffered stream ==> Will fail with SSL handshakes!
//	    contentFile = HttpTransferUtil.createKawansoftTempFile();
//	    saveResponseContentToFile(contentFile, conn);
//	    debug("contentFile: " + contentFile);
//
//	    // Read content of first line.
//	    reader = new BufferedReader(new InputStreamReader(
//		    new FileInputStream(contentFile)));

	    // it's ok to use a buffered stream with SSL with HttpUrlConnection
	    // Check the server sent us back a compressed content
	    if ("gzip".equals(conn.getContentEncoding())) {
		reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(conn.getInputStream())));
	    } else {
		reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	    }
	    
	    // line 1: Contains the request status - line 2: Contains the datas
	    String responseStatus = reader.readLine();
	    // debug("responseStatus        : " + responseStatus);

	    if (doReceiveInFile) {
		// Content is saved back into a file, minus the first line
		// status
		receiveFile = HttpTransferUtil.createKawansoftTempFile();
		copyResponseIntoFile(reader, receiveFile);
	    } else {
		
		// NO more length compute, result send by server is false or -1
		
//		int maxLengthForString = DefaultParms.DEFAULT_MAX_LENGTH_FOR_STRING;
//		if (httpProtocolParameters != null) {
//		    maxLengthForString = httpProtocolParameters
//			    .getMaxLengthForString();
//		}
//		if (contentFile.length() > maxLengthForString) {
//		    throw new IOException("Response too big for String: > "
//			    + maxLengthForString + " bytes.");
//		}

		copyResponseIntoString(reader);

	    }

	    // Analyze applicative response header
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
			HttpTransferUtil
				.throwTheRemoteException(bufferedReaderException);
		    } else {
			bufferedReaderException = new BufferedReader(
				new StringReader(m_responseBody));
			HttpTransferUtil
				.throwTheRemoteException(bufferedReaderException);
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

		message += FileUtils.readFileToString(contentFile);
		throw new IOException(message);
	    }
	} finally {

	    IOUtils.closeQuietly(reader);

	    if (!DEBUG) {
		FileUtils.deleteQuietly(contentFile);
	    }

	}

    }

//    /**
//     * Immediately Save the response content into file and release it
//     * 
//     * @param contentFile
//     *            the file where to save the content
//     * @param conn
//     *            the HttpURLConnection instance
//     * @throws IOException
//     * @throws IllegalStateException
//     * @throws FileNotFoundException
//     */
//    private void saveResponseContentToFile(File contentFile,
//	    HttpURLConnection conn) throws IOException, IllegalStateException,
//	    FileNotFoundException {
//	InputStream in;
//	in = conn.getInputStream();
//	BufferedOutputStream out = new BufferedOutputStream(
//		new FileOutputStream(contentFile));
//
//	try {
//	    IOUtils.copy(in, out);
//	} finally {
//	    IOUtils.closeQuietly(in);
//	    IOUtils.closeQuietly(out);
//	}
//    }

    /**
     * Formats & URL encode the the post data for POST.
     * 
     * @param params
     *            the parameter names and values
     * @return the formated and URL encoded string for the POST.
     * @throws UnsupportedEncodingException
     */
    private String getPostDataString(List<SimpleNameValuePair> requestParams)
	    throws UnsupportedEncodingException {
	StringBuilder result = new StringBuilder();
	boolean first = true;

	for (SimpleNameValuePair simpleNameValuePair : requestParams) {
	    if (first)
		first = false;
	    else
		result.append("&");

	    if (simpleNameValuePair.getValue() != null) {
		result.append(URLEncoder.encode(simpleNameValuePair.getName(),
			"UTF-8"));
		result.append("=");
		result.append(URLEncoder.encode(simpleNameValuePair.getValue(),
			"UTF-8"));
	    }
	}

	return result.toString();
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
    public InputStream getInputStream(List<SimpleNameValuePair> requestParams)
	    throws IllegalArgumentException, UnknownHostException,
	    ConnectException, RemoteException, IOException {

	InputStream in = null;

	statusCode = 0; // Reset it!
	m_responseBody = null; // Reset it!

	URL theUrl;
	theUrl = new URL(this.url);

	conn = buildHttpUrlConnection(theUrl);
	conn.setRequestMethod(POST);
	conn.setDoOutput(true);

	debug("requestParams: " + requestParams);

	OutputStream os = conn.getOutputStream();
	Writer writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
	writer.write(getPostDataString(requestParams));

	// writer.flush();
	writer.close();
	os.close();

	// Analyze the error after request execution
	statusCode = conn.getResponseCode();

	if (statusCode != HttpURLConnection.HTTP_OK) {
	    // The server is up, but the servlet is not accessible
	    throw new ConnectException(theUrl + ": Servlet failed: "
		    + conn.getResponseMessage() + " status: " + statusCode);
	}

	if ("gzip".equals(conn.getContentEncoding())) {
	    in = new GZIPInputStream(conn.getInputStream());
	} else {
	    in = conn.getInputStream();
	}

	return in;
    }

    /**
     * Closes the url connection
     */

    @Override
    public void close() {
	if (this.conn != null) {
	    this.conn.disconnect();
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

	InputStream in = null;
	OutputStream out = null;

	statusCode = 0; // Reset it!
	m_responseBody = null; // Reset it!

	try {

	    conn = buildHttpUrlConnection(url);
	    conn.setRequestMethod(GET);

	    // Analyze the error after request execution
	    statusCode = conn.getResponseCode();

	    if (statusCode != HttpURLConnection.HTTP_OK) {
		// The server is up, but the servlet is not accessible
		throw new ConnectException(url + ": Servlet failed: "
			+ conn.getResponseMessage() + " status: " + statusCode);
	    }
	    
	    if ("gzip".equals(conn.getContentEncoding())) {
		in = new GZIPInputStream(conn.getInputStream());
	    } else {
		in = conn.getInputStream();
	    }

	    // transferProgressManager.setLengthToTransfer(entity.getContentLength());

	    out = new BufferedOutputStream(new FileOutputStream(file));

	    byte[] buf = new byte[4096];
	    int len;
	    int tempLen = 0;
	    long filesLength = 0;

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

		}

		out.write(buf, 0, len);
	    }

	} finally {
	    IOUtils.closeQuietly(out);
	    IOUtils.closeQuietly(in);
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

	InputStream in = null;
	statusCode = 0; // Reset it!
	m_responseBody = null; // Reset it!

	try {

	    conn = buildHttpUrlConnection(url);
	    conn.setRequestMethod(GET);

	    // Analyze the error after request execution
	    statusCode = conn.getResponseCode();

	    if (statusCode != HttpURLConnection.HTTP_OK) {
		// The server is up, but the servlet is not accessible
		throw new ConnectException(url + ": Servlet failed: "
			+ conn.getResponseMessage() + " status: " + statusCode);
	    }

	    if ("gzip".equals(conn.getContentEncoding())) {
		in = new GZIPInputStream(conn.getInputStream());
	    } else {
		in = conn.getInputStream();
	    }

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
	}

    }

    /**
     * If called, self signed SSL certificates will be accepted
     */
    private void acceptSelfSignedSslCert() {
	// Create a trust manager that does not validate certificate chains
	TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
	    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
		return null;
	    }

	    public void checkClientTrusted(X509Certificate[] certs,
		    String authType) {
	    }

	    public void checkServerTrusted(X509Certificate[] certs,
		    String authType) {
	    }
	} };

	// Install the all-trusting trust manager
	SSLContext sc = null;
	try {
	    sc = SSLContext.getInstance("SSL");

	    sc.init(null, trustAllCerts, new java.security.SecureRandom());

	    HttpsURLConnection
		    .setDefaultSSLSocketFactory(sc.getSocketFactory());

	    // Create all-trusting host name verifier
	    HostnameVerifier allHostsValid = new HostnameVerifier() {
		public boolean verify(String hostname, SSLSession session) {
		    return true;
		}
	    };

	    // Install the all-trusting host verifier
	    HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

	} catch (NoSuchAlgorithmException e) {
	    e.printStackTrace();
	} catch (KeyManagementException e) {
	    e.printStackTrace();
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
