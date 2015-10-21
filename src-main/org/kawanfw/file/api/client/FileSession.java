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
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.kawanfw.commons.api.client.InvalidLoginException;
import org.kawanfw.commons.api.client.RemoteException;
import org.kawanfw.commons.api.client.SessionParameters;
import org.kawanfw.commons.client.http.HttpTransfer;
import org.kawanfw.commons.client.http.HttpTransferUtil;
import org.kawanfw.commons.util.ClientLogger;
import org.kawanfw.commons.util.FrameworkDebug;
import org.kawanfw.commons.util.Tag;
import org.kawanfw.file.api.util.client.PathUtil;
import org.kawanfw.file.version.FileVersion;

/**
 * Main class - before version 3.0 - for executing from a client (Windows, OX S,
 * Linux/Unix and Android) all sorts of operations on remote files or remote
 * classes through an Http or Https session.
 * <p>
 * Class includes methods to:
 * <ul>
 * <li>Get remote files size.</li>
 * <li>Delete remote files or directories.</li>
 * <li>TestReload if a remote file or directory exists.</li>
 * <li>Rename remote files.</li>
 * <li>Create remote directories.</li>
 * <li>Get the list of files of a remote directory.</li>
 * <li>Get the list of sub-directories of a remote directory.</li>
 * <li>Upload files by wrapping bytes copy from a {@code FileInputStream} to a
 * {@link RemoteOutputStream}.</li>
 * <li>Download files by wrapping bytes copy from a {@link RemoteInputStream} to
 * a {@code FileOtputStream}.</li>
 * <li>Call remote Java methods.</li>
 * </ul>
 * <p>
 * Example: <blockquote>
 * 
 * <pre>
 * // Define URL of the path to the {@code ServerFileManager} servlet
 * String url = &quot;https://www.acme.org/ServerFileManager&quot;;
 * 
 * // The login info for strong authentication on server side:
 * String username = &quot;myUsername&quot;;
 * char[] password = { 'm', 'y', 'P', 'a', 's', 's', 'w', 'o', 'r', 'd' };
 * 
 * // Establish a session with the remote server
 * FileSession fileSession = new FileSession(url, username, password);
 * 
 * // OK: upload a file
 * fileSession.upload(new File(&quot;c:\\myFile.txt&quot;), &quot;/home/mylogin/myFile.txt&quot;);
 * 
 * </pre>
 * 
 * </blockquote>
 * 
 * Communication via a proxy server is done using
 * {@code java.net.Proxy} and {@code java.net.PasswordAuthentication} for
 * authentication.
 * 
 * <blockquote>
 * 
 * <pre>
	String url = "http://www.acme.org/ServerFileManager";
	String username = "myUsername";
	char[] password = "myPassword".toCharArray();

	Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(
		    "proxyHostname", 8080));
	    
	PasswordAuthentication passwordAuthentication = null;
	
	// If proxy requires authentication:
	passwordAuthentication = new PasswordAuthentication("proxyUsername", "proxyPassword".toCharArray());
	
	FileSession fileSession = new FileSession(url, username,
		password, proxy, passwordAuthentication);
	// Etc.
 * </pre>
 * 
 * </blockquote>
 * 
 * NTLM authentication is done using {@code PasswordAuthentication}:
 * 
 * <blockquote>
 * 
 * <pre>
	String url = "http://www.acme.org/ServerFileManager";
	String username = "myUsername";
	char[] password = "myPassword".toCharArray();

	Proxy proxy = Proxy.NO_PROXY;
	
	// DOMAIN is passed along username:
	PasswordAuthentication passwordAuthentication = new PasswordAuthentication("DOMAIN\\WinUsername", "WinPassword".toCharArray());
	
	FileSession fileSession = new FileSession(url, username,
		password, proxy, passwordAuthentication);
 * </pre>
 * </blockquote>
 * 
 * @see org.kawanfw.file.api.client.RemoteSession
 * @see org.kawanfw.file.api.client.RemoteFile
 * @see org.kawanfw.file.api.client.RemoteInputStream
 * @see org.kawanfw.file.api.client.RemoteOutputStream
 * 
 * @deprecated As of version 3.0, replaced by {@link RemoteSession} for session
 *             establishment and simple file upload/download and
 *             {@link RemoteFile} for operations on remote files.
 * 
 * @author Nicolas de Pomereu
 * @since 1.0
 */

public class FileSession implements Cloneable {

    protected static final String FILE_SESSION_IS_CLOSED = "RemoteSession is closed.";

    /** For debug info */
    private static boolean DEBUG = FrameworkDebug.isSet(FileSession.class);

    /** Defines 1 kilobyte */
    public static final int KB = 1024;

    /** Defines 1 megabyte */
    public static final int MB = 1024 * KB;

    private static final int LIST_FILES_ONLY = 1;
    private static final int LIST_DIRECTORIES_ONLY = 2;

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
    private SessionParameters sessionParameters = null;

    /** The http transfer instance */
    private HttpTransfer httpTransfer = null;

    /** The wrapped Remote Session */
    private RemoteSession remoteSession = null;

    /**
     * Says if we want to use base64 encoding for parameters passed to call() -
     * This is a method for legacy applications prior to v1.0.
     */
    public static void setUseBase64EncodingForCall() {
	RemoteSession.setUseBase64EncodingForCall();
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
     *            the proxy to use, may be null for direct access
     * @param passwordAuthentication
     *            the proxy credentials, null if no proxy or if the proxy does not require authentication
     * @param sessionParameters
     *            the http parameters to use
     * @param remoteSession
     *            the remote session to clone
     */
    private FileSession(String url, String username,
	    String authenticationToken, Proxy proxy,
	    PasswordAuthentication passwordAuthentication,
	    SessionParameters sessionParameters, RemoteSession remoteSession) {
	this.url = url;
	this.username = username;
	this.authenticationToken = authenticationToken;
	this.proxy = proxy;
	this.passwordAuthentication = passwordAuthentication;
	this.sessionParameters = sessionParameters;

	httpTransfer = HttpTransferUtil.HttpTransferFactory(url, proxy,
		passwordAuthentication, sessionParameters);

	this.remoteSession = remoteSession.clone();
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
     *            the proxy to use, may be null for direct access
     * @param passwordAuthentication
     *            the proxy credentials, null if no proxy or if the proxy does
     *            not require authentication
     * @param sessionParameters
     *            the session parameters to use (may be null)
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
     * 
     * @deprecated As of version 3.0, replaced by:
     *             {@link RemoteSession#RemoteSession(String, String, char[])}
     */
    public FileSession(String url, String username, char[] password,
	    Proxy proxy, PasswordAuthentication passwordAuthentication,
	    SessionParameters sessionParameters) throws MalformedURLException,
	    UnknownHostException, ConnectException, SocketException,
	    InvalidLoginException, RemoteException, SecurityException,
	    IOException {

	remoteSession = new RemoteSession(url, username, password, proxy,
		passwordAuthentication, sessionParameters);

	debug(remoteSession.toString());

	// keep a copy of HttpTransfer than can not be accessed via
	// RemoteSession because package protected
	httpTransfer = HttpTransferUtil.HttpTransferFactory(url, proxy,
		passwordAuthentication, sessionParameters);

	this.username = username;
	this.url = url;

	this.proxy = proxy;
	this.passwordAuthentication = passwordAuthentication;
	this.sessionParameters = sessionParameters;

	this.authenticationToken = remoteSession.getAuthenticationToken();

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
     *            the proxy to use, may be null for direct access
     * @param passwordAuthentication
     *            the proxy credentials, null if no proxy or if the proxy does
     *            not require authentication
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
     * 
     * @deprecated As of version 3.0, replaced by:
     *             {@link RemoteSession#RemoteSession(String, String, char[], Proxy, PasswordAuthentication)}
     */
    public FileSession(String url, String username, char[] password,
	    Proxy proxy, PasswordAuthentication passwordAuthentication)
	    throws MalformedURLException, UnknownHostException,
	    ConnectException, SocketException, InvalidLoginException,
	    RemoteException, SecurityException, IOException {
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
     *
     * @deprecated As of version 3.0, replaced by:
     *             {@link RemoteSession#RemoteSession(String, String, char[])}
     */

    public FileSession(String url, String username, char[] password)
	    throws MalformedURLException, UnknownHostException,
	    ConnectException, SocketException, InvalidLoginException,
	    RemoteException, IOException, SecurityException {
	this(url, username, password, null, null);
    }

    /**
     * Returns the wrapped Remote Session
     * 
     * @return the remoteSession the wrapped Remote Session
     * 
     * @since 3.0
     */
    RemoteSession getRemoteSession() {
	return remoteSession;
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
     * Returns the {@code SessionParameters} instance in use for the Awake FILE
     * session.
     * 
     * @return the {@code SessionParameters} instance in use for the Awake FILE
     *         session
     */
    public SessionParameters getSessionParameters() {
	return this.sessionParameters;
    }

    /**
     * Returns the URL of the path to the <code>ServerFileManager</code> Servlet
     * (or <code>ServerSqlManager</code> Servlet if session has been initiated
     * by a <code>RemoteConnection</code>).
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
     * 
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
     * Downloads a file from the remote server. <br>
     * This method simply wraps bytes copy from a {@link RemoteInputStream} to a
     * {@code FileOutputStream}.
     * <p>
     * The real path of the remote file depends on the Awake FILE configuration
     * on the server. See User Documentation.
     * <p>
     * Large files are split in chunks that are downloaded in sequence. The
     * default chunk length is 10Mb. You can change the default value with
     * {@link SessionParameters#setDownloadChunkLength(long)} before passing
     * {@code SessionParameters} to this class constructor.
     * <p>
     * Note that file chunking requires that all chunks be downloaded from to
     * the same web server. Thus, file chunking does not support true stateless
     * architecture with multiple identical web servers. If you want to set a
     * full stateless architecture with multiple identical web servers, you must
     * disable file chunking. This is done by setting a 0 download chunk length
     * value using {@link SessionParameters#setDownloadChunkLength(long)}. <br>
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
     *            the pathname on host
     * 
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
     * 
     * @deprecated As of version 3.0, replaced by:
     *             {@link RemoteSession#download(String, File)}
     */
    public void download(String pathname, File file)
	    throws IllegalArgumentException, InvalidLoginException,
	    FileNotFoundException, UnknownHostException, ConnectException,
	    SocketException, RemoteException, IOException {

	pathname = PathUtil.rewriteToUnixSyntax(pathname);
	remoteSession.download(pathname, file);
    }

    /**
     * Uploads a file on the server. <br>
     * This method simply wraps bytes copy from a {@code FileInputStream} to a
     * {@link RemoteOutputStream}.
     * <p>
     * The real path of the remote file depends on the Awake FILE configuration
     * on the server. See User Documentation.
     * <p>
     * Large files are split in chunks that are uploaded in sequence. The
     * default chunk length is 10Mb. You can change the default value with
     * {@link SessionParameters#setUploadChunkLength(long)} before passing
     * {@code SessionParameters} to this class constructor.
     * <p>
     * Note that file chunking requires all chunks to be sent to the same web
     * server that will aggregate the chunks after the last send. Thus, file
     * chunking does not support true stateless architecture with multiple
     * identical web servers. If you want to set a full stateless architecture
     * with multiple identical web servers, you must disable file chunking. This
     * is done by setting a 0 upload chunk length value using
     * {@link SessionParameters#setUploadChunkLength(long)}. <br>
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
     *            the pathname on host
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
     * @deprecated As of version 3.0, replaced by:
     *             {@link RemoteSession#upload(File, String)}
     */
    public void upload(File file, String pathname)
	    throws IllegalArgumentException, InvalidLoginException,
	    FileNotFoundException, UnknownHostException, ConnectException,
	    SocketException, RemoteException, IOException {

	pathname = PathUtil.rewriteToUnixSyntax(pathname);
	remoteSession.upload(file, pathname);
    }

    /**
     * Returns the length of a list of files located on the remote host.
     * <p>
     * This convenient methods is provided for fast compute of the total length
     * of a list of files to download, without contacting the server for each
     * file result. (Case using a progress monitor).
     * <p>
     * The real paths of the remote files depend on the Awake FILE configuration
     * on the server. See User Documentation.
     * 
     * @param pathnames
     *            the list of pathnames
     * 
     * @return the total length in bytes of the files located on the remote
     *         host.
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
     * 
     * @deprecated As of version 3.0, replaced by:
     *             {@link RemoteSession#length(List)}
     */
    public long length(List<String> pathnames) throws IllegalArgumentException,
	    InvalidLoginException, UnknownHostException, ConnectException,
	    SocketException, RemoteException, IOException {

	if (pathnames == null) {
	    throw new IllegalArgumentException("pathnames can not be null!");
	}

	List<String> remoteFilesUnix = new ArrayList<String>();

	for (String remoteFile : pathnames) {
	    remoteFile = PathUtil.rewriteToUnixSyntax(remoteFile);
	    remoteFilesUnix.add(remoteFile);
	}

	long length = remoteSession.length(remoteFilesUnix);
	return length;
    }

    /**
     * Renames a remote pathname. <br>
     * ({@code File.renameTo(File)} is executed on remote host.) <br>
     * The method allows a previous delete of the file corresponding to the
     * destination file name.
     * <p>
     * The real path of the remote file depends on the Awake FILE configuration
     * on the server. See User Documentation.
     * 
     * @param pathname
     *            A pathname on the host
     * 
     * @param dest
     *            The pathname new name
     * 
     * @param deleteIfExists
     *            If true, the file or directory scorresponding to {@code dest}
     *            will be deleted if it exists with {@code File.delete()} before
     *            the rename
     * 
     * @return {@code true} if and only if the renaming succeeded; {@code false}
     *         otherwise
     * 
     * @throws IllegalArgumentException
     *             if pathname or dest is null
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
     * 
     * @deprecated As of version 3.0, replaced by:
     *             {@link RemoteFile#renameTo(RemoteFile)}
     */
    public boolean rename(String pathname, String dest, boolean deleteIfExists)
	    throws IllegalArgumentException, InvalidLoginException,
	    UnknownHostException, ConnectException, SocketException,
	    RemoteException, IOException {

	pathname = PathUtil.rewriteToUnixSyntax(pathname);
	dest = PathUtil.rewriteToUnixSyntax(dest);

	RemoteFile remotefile = new RemoteFile(remoteSession, pathname);
	RemoteFile remotefileDest = new RemoteFile(remoteSession, dest);

	if (!remotefile.exists()) {
	    return false;
	}

	boolean result = false;

	/* Delete the new file if it exists to avoid rename failure */

	if (deleteIfExists && remotefileDest.exists()) {
	    result = remotefileDest.delete();
	    if (!result) {
		return false;
	    }
	}

	result = remotefile.renameTo(remotefileDest);
	return result;
    }

    /**
     * Returns the length of a file located on the remote host. <br>
     * ({@code File.lenth()} is executed on remote host.) *
     * <p>
     * The real path of the remote file depends on the Awake FILE configuration
     * on the server. See User Documentation.
     * 
     * @param pathname
     *            the pathname on host
     * 
     * @return the length in bytes of the file located on the remote host.
     * 
     * @throws IllegalArgumentException
     *             if pathname is null
     * @throws InvalidLoginException
     *             the session has been closed by a {@code logoff()}
     * 
     * @throws UnknownHostException
     *             if the Host URL (http://www.acme.org) does not exists or no
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
     * @deprecated As of version 3.0, replaced by: {@link RemoteFile#length()}
     */
    public long length(String pathname) throws IllegalArgumentException,
	    InvalidLoginException, UnknownHostException, ConnectException,
	    SocketException, RemoteException, IOException {

	pathname = PathUtil.rewriteToUnixSyntax(pathname);
	long length = new RemoteFile(remoteSession, pathname).length();
	return length;
    }

    /**
     * Deletes a remote file or remote directory on the host. (Directory must be
     * empty to be deleted). <br>
     * ({@code File.delete()} is executed on remote host.)
     * <p>
     * The real path of the remote file depends on the Awake FILE configuration
     * on the server. See User Documentation.
     * 
     * @param pathname
     *            the pathname on host
     * 
     * @return true if the remote file or directory has been deleted
     * 
     * @throws IllegalArgumentException
     *             if pathname is null
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
     * @throws SecurityException
     *             the url is not secured with https (SSL)
     * @throws IOException
     *             for all other IO / Network / System Error
     * 
     * @deprecated As of version 3.0, replaced by: {@link RemoteFile#delete()}
     */
    public boolean delete(String pathname) throws IllegalArgumentException,
	    InvalidLoginException, UnknownHostException, ConnectException,
	    RemoteException, IOException {

	pathname = PathUtil.rewriteToUnixSyntax(pathname);
	boolean result = new RemoteFile(remoteSession, pathname).delete();
	return result;
    }

    /**
     * Says if a remote file or directory exists. <br>
     * ({@code File.exists()} is executed on remote host.)
     * <p>
     * The real path of the remote file depends on the Awake FILE configuration
     * on the server. See User Documentation.
     * 
     * @param pathname
     *            the pathname on host
     * 
     * @return true if the remote file or directory exists
     * 
     * @throws IllegalArgumentException
     *             if pathname is null
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
     * @throws SecurityException
     *             the url is not secured with https (SSL)
     * @throws IOException
     *             for all other IO / Network / System Error
     * 
     * @deprecated As of version 3.0, replaced by: {@link RemoteFile#exists()}
     */
    public boolean exists(String pathname) throws IllegalArgumentException,
	    InvalidLoginException, UnknownHostException, ConnectException,
	    SocketException, RemoteException, IOException {

	pathname = PathUtil.rewriteToUnixSyntax(pathname);
	boolean result = new RemoteFile(remoteSession, pathname).exists();
	return result;
    }

    /**
     * Creates a directory on the remote host. <br>
     * ({@code File.mkdir()} is executed on remote host.)
     * <p>
     * The real path of the remote file depends on the Awake FILE configuration
     * on the server. See User Documentation.
     * 
     * @param pathname
     *            the pathname on host
     * 
     * @return <code>true</code> if and only if the directory was created;
     *         <code>false</code> otherwise
     * 
     * @throws IllegalArgumentException
     *             if pathname is null
     * @throws InvalidLoginException
     *             the session has been closed by a {@code logoff()}
     * 
     * @throws UnknownHostException
     *             if host url (http://www.acme.org) does not exists or no
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
     * @deprecated As of version 3.0, replaced by: {@link RemoteFile#mkdir()}
     */
    public boolean mkdir(String pathname) throws IllegalArgumentException,
	    InvalidLoginException, UnknownHostException, ConnectException,
	    SocketException, RemoteException, IOException {

	pathname = PathUtil.rewriteToUnixSyntax(pathname);
	boolean result = new RemoteFile(remoteSession, pathname).mkdir();
	return result;
    }

    /**
     * Creates a directory on the remote host, including any necessary but
     * nonexistent parent directories. <br>
     * ({@code File.mkdirs()} is executed on remote host.)
     * <p>
     * The real path of the remote file depends on the Awake FILE configuration
     * on the server. See User Documentation.
     * 
     * @param pathname
     *            A pathname on the host
     * 
     * @return <code>true</code> if and only if the directory was created, along
     *         with all necessary parent directories; <code>false</code>
     *         otherwise
     * 
     * @throws IllegalArgumentException
     *             if pathname is null
     * @throws InvalidLoginException
     *             the session has been closed by a {@code logoff()}
     * 
     * @throws UnknownHostException
     *             if host url (http://www.acme.org) does not exists or no
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
     * @deprecated As of version 3.0, replaced by: {@link RemoteFile#mkdirs()}
     */
    public boolean mkdirs(String pathname) throws IllegalArgumentException,
	    InvalidLoginException, UnknownHostException, ConnectException,
	    SocketException, RemoteException, IOException {

	pathname = PathUtil.rewriteToUnixSyntax(pathname);
	boolean result = new RemoteFile(remoteSession, pathname).mkdirs();
	return result;
    }

    /**
     * Lists the files contained in a remote directory. <br>
     * (Only remote files that return {@code true} to {@code File.isFile()} are
     * returned.)
     * <p>
     * The real path of the remote directory depends on the Awake FILE
     * configuration on the server. See User Documentation.
     * 
     * @param pathname
     *            the directory pathname on host
     * 
     * @return the list of files in the remote directory. Will be
     *         <code>null</code> if the remote directory does not exists. Will
     *         be empty if the remote directory exists but has no files.
     * 
     * @throws IllegalArgumentException
     *             if pathname is null
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
     * @deprecated As of version 3.0, replaced by:
     *             {@link RemoteFile#listFiles(FileFilter)}
     */

    public List<String> listFiles(String pathname)
	    throws IllegalArgumentException, InvalidLoginException,
	    UnknownHostException, ConnectException, SocketException,
	    RemoteException, IOException {

	int type = LIST_FILES_ONLY;
	return listFilesOrDirectories(pathname, type);
    }

    /**
     * Lists the sub-directories contained in a remote directory. <br>
     * (Only remote files that return {@code true} to {@code File.isDirectory()}
     * are returned.)
     * <p>
     * The real path of the remote directory depends on the Awake FILE
     * configuration on the server. See User Documentation.
     * 
     * @param pathname
     *            the directory pathname on host
     * 
     * @return the list of directories in the remote directory. Will be
     *         <code>null</code> if the remote directory does not exists. Will
     *         be empty if the remote directory exists but has no
     *         sub-directories.
     * 
     * @throws IllegalArgumentException
     *             if pathname is null
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
     * @deprecated As of version 3.0, replaced by:
     *             {@link RemoteFile#listFiles(FileFilter)}
     */
    public List<String> listDirectories(String pathname)
	    throws IllegalArgumentException, InvalidLoginException,
	    UnknownHostException, ConnectException, SocketException,
	    RemoteException, IOException {

	int type = LIST_DIRECTORIES_ONLY;
	return listFilesOrDirectories(pathname, type);
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
     * @deprecated As of version 3.0, replaced by:
     *             {@link RemoteSession#call(String, Object...)}
     */
    public String call(String methodName, Object... params)
	    throws IllegalArgumentException, InvalidLoginException,
	    UnknownHostException, ConnectException, SocketException,
	    RemoteException, IOException {
	String result = remoteSession.call(methodName, params);
	return result;
    }

    /**
     * Allows to get a copy of the current <code>RemoteSession</code>: use it to
     * do some simultaneous operations in a different thread (in order to avoid
     * conflicts).
     */
    @Override
    public FileSession clone() {
	FileSession fileSession = new FileSession(this.url, this.username,
		this.authenticationToken, this.proxy,
		this.passwordAuthentication, this.sessionParameters,
		remoteSession);
	return fileSession;
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

	remoteSession.logoff();
	username = null;
	authenticationToken = null;

	proxy = null;
	passwordAuthentication = null;
	sessionParameters = null;

	if (httpTransfer != null) {
	    httpTransfer.close();
	    httpTransfer = null;
	}
    }

    /**
     * get the list of files or directories in a directory
     * 
     * @param pathname
     *            the directory pathname to list
     * @param type
     *            the type of File list to return: LIST_FILES_ONLY or
     *            LIST_DIRECTORIES_ONLY
     * @return the files or directories list
     * 
     * @throws InvalidLoginException
     */
    private List<String> listFilesOrDirectories(String pathname, int type)
	    throws InvalidLoginException {

	pathname = PathUtil.rewriteToUnixSyntax(pathname);

	RemoteFile theRemoteFile = new RemoteFile(remoteSession, pathname);

	FileFilter fileFilter = null;

	if (type == LIST_FILES_ONLY) {
	    fileFilter = FileFileFilter.FILE;
	} else if (type == LIST_DIRECTORIES_ONLY) {
	    fileFilter = DirectoryFileFilter.DIRECTORY;
	} else {
	    throw new IllegalArgumentException(
		    Tag.PRODUCT_PRODUCT_FAIL
			    + " Invalid type. Msust be LIST_FILES_ONLY or LIST_DIRECTORIES_ONLY. Is: "
			    + type);
	}

	RemoteFile[] remoteFiles = theRemoteFile.listFiles(fileFilter);

	if (remoteFiles == null) {
	    return null;
	}

	List<String> filenameList = new ArrayList<String>();
	for (RemoteFile remoteFileItem : remoteFiles) {
	    filenameList.add(remoteFileItem.getName());
	}

	return filenameList;
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
