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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.kawanfw.commons.json.HttpProtocolParametersGson;
import org.kawanfw.commons.util.DefaultParms;
import org.kawanfw.file.api.client.RemoteInputStream;
import org.kawanfw.file.api.client.RemoteOutputStream;

/**
 * 
 * Allows to define some parameters for the session:
 * <ul>
 * <li>Buffer size when uploading files. Defaults to 20480 (20 Kb).</li>
 * <li>Buffer size when downloading files. Defaults to 20480 (20 Kb).</li>
 * <li>Maximum authorized length for a string for upload or download (in order
 * to avoid OutOfMemoryException on client and server side.) Defaults to 2097152
 * (2 Mb).</li>
 * <li>Boolean to say if client sides allows HTTPS call with all SSL
 * Certificates, including "invalid" or self-signed Certificates. defaults to
 * <code>true</code>.</li>
 * <li>Password to use for encrypting all parameters request between and Host.</li>
 * <li>Boolean to say if Clob upload/download using character stream or ASCII
 * stream must be html encoded. Defaults to <code>true</code>.</li>
 * <li>Download chunk length to be used by
 * {@link RemoteInputStream}. Defaults to 10Mb. 0
 * means files are not chunked.</li>
 * <li>Upload chunk length to be used by
 * {@link RemoteOutputStream} Defaults to 3Mb. 0 means
 * files are not chunked.</li>
 * <li>The number of times an HttpClient method will be retried. Defaults to 3.</li>
 * </ul>
 * <p>
 * Allows also to store http protocol parameters that will be passed to the
 * underlying <code>DefaultHttpClient</code> class of the <a
 * href="http://hc.apache.org/httpcomponents-client-4.2.x">Jakarta HttpClient
 * 4.2</a> library.
 * <p>
 * Use this class to change the default values of the HttpClient library and
 * pass the created instance to <code>RemoteSession</code> or to the {@code info}
 * <code>Properties</code> when creating a <code>RemoteConnection</code> with
 * {@code DriverManager.getConnection(url, info)}.
 * <p>
 * For example, the following change the default connection timeout to 10
 * seconds and the default socket timeout to 60 seconds: 
 * 
 * <blockquote><pre>
 * String url = "https://www.acme.org/ServerFileManager";
 * String username = "myUsername";
 * char [] password = {'m', 'y', 'P', 'a', 's', 's', 'w', 'o', 'r', 'd'}; 
 * &nbsp;        
 * HttpProtocolParameters httpProtocolParameters = new HttpProtocolParameters();
 * &nbsp;
 * // Sets the timeout until a connection is established to 10 seconds
 * httpProtocolParameters.setHttpClientParameter(
 * "http.connection.timeout", new Integer(10 * 1000));
 * &nbsp;
 * // Sets the socket timeout (SO_TIMEOUT) to 60 seconds
 * httpProtocolParameters.setHttpClientParameter("http.socket.timeout",
 * new Integer(60 * 1000));
 * &nbsp;        
 * // We will use no proxy
 * HttpProxy httpProxy = null;
 * &nbsp;        
 * RemoteSession remoteSession 
 * = new RemoteSession(url, username, password, httpProxy, httpProtocolParameters);
 * &nbsp;
 * // Etc.
 * 
 *  </pre></blockquote>
 * 
 * See <a href=
 * "http://hc.apache.org/httpcomponents-client-4.2.x/tutorial/html/fundamentals.html#d5e299"
 * >HttpClient 4.2 Tutorial</a> for more info on HTTP parameters.
 * 
 * @author Nicolas de Pomereu
 * @since 1.0
 */

public class HttpProtocolParameters implements Serializable {

    /**
     * Serial number
     */
    private static final long serialVersionUID = 8900046208199627283L;

    /** The maximum size of a string read from input stream. Should be &le; 2Mb */
    private int maxLengthForString = DefaultParms.DEFAULT_MAX_LENGTH_FOR_STRING;

    /** The buffer size when uploading a file */
    private int uploadBufferSize = DefaultParms.DEFAULT_UPLOAD_BUFFER_SIZE;

    /** Buffer size for download and copy */
    private int downloadBufferSize = DefaultParms.DEFAULT_DOWNLOAD_BUFFER_SIZE;

    /**
     * Says if we want to html Encode the Clob when using chararacter or ASCII
     * stream Default is true
     */
    private boolean htmlEncodingOn = DefaultParms.DEFAULT_HTML_ENCODING_ON;

    /**
     * Says if we accept all SSL Certificates (example: self signed
     * certificates)
     */
    private boolean acceptAllSslCertificates = DefaultParms.ACCEPT_ALL_SSL_CERTIFICATES;

    /**
     * The password to use to encrypt all request parameter names and values.
     * null means no encryption is done
     */
    private char[] encryptionPassword = null;

    /**
     * The chunk length for {@link RemoteInputStream}.
     * Defaults to 10Mb.
     */
    private long downloadChunkLength = DefaultParms.DEFAULT_DOWNLOAD_CHUNK_LENGTH;

    /**
     * The chunk length for {@link RemoteOutputStream}.
     * Defaults to 3Mb.
     */
    private long uploadChunkLength = DefaultParms.DEFAULT_UPLOAD_CHUNK_LENGTH;

    /** The number of times a method will be retried */
    private int retryCount = DefaultParms.DEFAULT_RETRY_COUNT;

    /** Hash map of HTTP parameters that this collection contains */
    private Map<String, Object> httpClientParameters = null;

    /**
     * Constructor.
     */
    public HttpProtocolParameters() {
	httpClientParameters = new HashMap<String, Object>();
    }

    /**
     * Returns the maximum authorized length for a string for upload or download
     * (in order to avoid OutOfMemoryException on client and server side).
     * 
     * @return the maximum authorized length for a string for upload or download
     */
    public int getMaxLengthForString() {
	return maxLengthForString;
    }

    /**
     * Sets the maximum authorized length for a string for upload or download
     * (in order to avoid OutOfMemoryException on client and server side).
     * 
     * @param maxLengthForString
     *            the maximum authorized length for a string for upload or
     *            download
     */
    public void setMaxLengthForString(int maxLengthForString) {
	this.maxLengthForString = maxLengthForString;
    }

    /**
     * Returns the buffer size when uploading files.
     * 
     * @return the buffer size when uploading files
     */
    public int getUploadBufferSize() {
	return this.uploadBufferSize;
    }

    /**
     * Sets the buffer size when uploading files.
     * 
     * @param uploadBufferSize
     *            the buffer size when uploading files
     */
    public void setUploadBufferSize(int uploadBufferSize) {
	this.uploadBufferSize = uploadBufferSize;
    }

    /**
     * Returns the buffer size when downloading files.
     * 
     * @return the buffer size when downloading files
     */
    public int getDownloadBufferSize() {
	return this.downloadBufferSize;
    }

    /**
     * Sets the buffer size when downloading files.
     * 
     * @param downloadBufferSize
     *            the buffer size when downloading files to set
     */
    public void setDownloadBufferSize(int downloadBufferSize) {
	this.downloadBufferSize = downloadBufferSize;
    }

    /**
     * Returns the encryption Password that encrypts http request parameters.
     * 
     * @return the encryption Password that encrypts http request parameters
     */
    public char[] getEncryptionPassword() {
	return this.encryptionPassword;
    }

    /**
     * Sets the encryption Password that encrypts http request parameters.
     * 
     * @param encryptionPassword
     *            the encryption Password that encrypts http request parameters
     */
    public void setEncryptionPassword(char[] encryptionPassword) {
	this.encryptionPassword = encryptionPassword;
    }

    /**
     * Says if the upload/download of Clob using character stream or ASCII
     * stream is html encoded.
     * 
     * @return true if the upload/download of Clob is html encoded
     */
    public boolean isHtmlEncodingOn() {
	return this.htmlEncodingOn;
    }

    /**
     * Says if the upload/download of Clob using character stream or ASCII
     * stream must be html encoded.
     * 
     * @param htmlEncodeOn
     *            true to html encode the upload/download of Clob, else false
     */
    public void setHtmlEncodingOn(boolean htmlEncodeOn) {
	this.htmlEncodingOn = htmlEncodeOn;
    }

    /**
     * Says if client sides allows HTTPS call with all SSL Certificates,
     * including "invalid" or self-signed Certificates.
     * 
     * @return true if client sides allows HTTPS call with all SSL Certificates
     */
    public boolean isAcceptAllSslCertificates() {
	return acceptAllSslCertificates;
    }

    /**
     * Sets if client sides must allow HTTPS call with all SSL Certificates,
     * including "invalid" or self-signed Certificates.
     * 
     * @param acceptAllSslCertificates
     *            true if we want client client sides to allow HTTPS call with
     *            all SSL Certificates
     */
    public void setAcceptAllSslCertificates(boolean acceptAllSslCertificates) {
	this.acceptAllSslCertificates = acceptAllSslCertificates;
    }

    /**
     * Returns the chunk length used by
     * {@link RemoteOutputStream}. Defaults to 3Mb. 0
     * means files are not chunked.
     * 
     * @return the chunk length to be used for file upload
     */
    public long getUploadChunkLength() {
	return uploadChunkLength;
    }

    /**
     * Sets the chunk length to be used by
     * {@link RemoteOutputStream}. 0 means files are not
     * chunked.
     * 
     * @param chunkLength
     *            the chunk length to set for file upload
     */
    public void setUploadChunkLength(long chunkLength) {
	this.uploadChunkLength = chunkLength;
    }

    /**
     * Returns the chunk length used by
     * {@link RemoteInputStream}. Defaults to 10Mb. 0
     * means files are not chunked.
     * 
     * @return the chunk length to be used for file download
     */
    public long getDownloadChunkLength() {
	return downloadChunkLength;
    }

    /**
     * Sets the chunk length to be used by
     * {@link RemoteInputStream}. 0 means files are not
     * chunked.
     * 
     * @param chunkLength
     *            the chunk length to set for file download
     */
    public void setDownloadChunkLength(long chunkLength) {
	this.downloadChunkLength = chunkLength;
    }

    /**
     * Returns The number of times an HttpClient method will be retried.
     * Defaults to 3. <br>
     * see <a href=
     * "https://hc.apache.org/httpcomponents-client-4.2.x/tutorial/html/fundamentals.html#d5e249"
     * >HttpClient 4.2 Tutorial - Exception handling</a>.
     * 
     * @return The number of times an HttpClient method will be retried
     */
    public int getRetryCount() {
	return retryCount;
    }

    /**
     * Sets the number of times an HttpClient method will be retried. Defaults
     * to 3. <br>
     * See <a href=
     * "https://hc.apache.org/httpcomponents-client-4.2.x/tutorial/html/fundamentals.html#d5e249"
     * >HttpClient 4.2 Tutorial - Exception handling</a>.
     * 
     * @param retryCount
     *            the number of times an HttpClient method will be retried.
     */
    public void setRetryCount(int retryCount) {
	this.retryCount = retryCount;
    }

    /**
     * Returns the HttpClient Parameters.
     * 
     * @return the HttpClient Parameters
     */
    public Map<String, Object> getHttpClientParameters() {
	return this.httpClientParameters;
    }

    /**
     * Sets the HttpClient Parameters.
     * 
     * @param httpClientParameters
     *            the HttpClient Parameters to set
     */
    public void setHttpClientParameters(Map<String, Object> httpClientParameters) {
	this.httpClientParameters = httpClientParameters;
    }

    /**
     * Sets the value of this HttpClient Library parameter.
     * 
     * @param name
     *            the parameter name
     * @param value
     *            the parameter value
     * 
     * @throws IllegalArgumentException
     *             if name is null
     */
    public synchronized void setHttpClientParameter(final String name,
	    final Object value) {

	if (name == null) {
	    throw new IllegalArgumentException(
		    "parameter name can not be null!");
	}

	this.httpClientParameters.put(name, value);
    }

    /**
     * Gets the value of this HttpClient Library parameter.
     * 
     * @param name
     *            the parameter name *
     * @return the parameter value
     * 
     * @throws IllegalArgumentException
     *             if name is null
     */
    public synchronized Object getHttpClientParameter(final String name) {

	if (name == null) {
	    throw new IllegalArgumentException(
		    "parameter name can not be null!");
	}

	return this.httpClientParameters.get(name);
    }

    /**
     * Returns the new parameters key set.
     * 
     * @return the new parameters key set.
     * @see Map#keySet()
     */
    public synchronized Set<String> getHttpClientParameterNames() {
	return this.httpClientParameters.keySet();
    }

    /**
     * Removes all parameters from this collection.
     */
    public void clearHttpClientParameters() {
	this.httpClientParameters.clear();
    }

    /**
     * Returns a JSon representation of the <code>HttpProtocolParameters</code>
     * instance. <br>
     * The JSon formated String can be used later to rebuild the instance from
     * the String.
     * 
     * @return a JSon representation of the <code>HttpProtocolParameters</code>
     *         instance
     */
    @Override
    public String toString() {
	return HttpProtocolParametersGson.toJson(this);
    }

}
