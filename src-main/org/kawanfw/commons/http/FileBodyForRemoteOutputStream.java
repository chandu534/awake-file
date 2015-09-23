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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;

import org.apache.commons.io.IOUtils;
import org.apache.http.entity.mime.content.FileBody;
import org.kawanfw.commons.api.client.HttpProtocolParameters;
import org.kawanfw.commons.util.ClientLogger;
import org.kawanfw.commons.util.DefaultParms;
import org.kawanfw.commons.util.FrameworkDebug;

/**
 * @author Nicolas de Pomereu
 * 
 *         this class externalize FileBody only to provide a sendData()
 *         implementation for RemoteOutputStream usage.
 */
public class FileBodyForRemoteOutputStream extends FileBody {

    private static boolean DEBUG = FrameworkDebug
	    .isSet(FileBodyForRemoteOutputStream.class);

    /** The http protocol parameters */
    private HttpProtocolParameters httpProtocolParameters;

    /** The file to upload */
    private File file = null;

    /**
     * FilePart Constructor.
     * 
     * @param file
     *            the file to post
     * @param mimeType
     *            the mime Type
     * @param httpProtocolParameters
     *            the http protocol parameters
     * 
     * @throws FileNotFoundException
     *             if the <i>file</i> is not a normal file or if it is not
     *             readable.
     */
    public FileBodyForRemoteOutputStream(File file, String mimeType,
	    HttpProtocolParameters httpProtocolParameters)
	    throws FileNotFoundException {
	super(file, mimeType);

	if (file == null) {
	    throw new IllegalArgumentException("file can not be null!");
	}

	this.file = file;
	this.httpProtocolParameters = httpProtocolParameters;
    }

    /**
     * Write the data in "source" to the specified stream.
     * 
     * @param out
     *            The output stream.
     * @throws IOException
     *             if an IO problem occurs.
     * @see org.apache.commons.httpclient.methods.multipart.Part#sendData(OutputStream)
     */

    @Override
    public void writeTo(final OutputStream out) throws IOException {
	debug("send data!");

	int uploadBufferSize = DefaultParms.DEFAULT_UPLOAD_BUFFER_SIZE;
	if (httpProtocolParameters != null) {
	    uploadBufferSize = httpProtocolParameters.getUploadBufferSize();
	}

	byte[] buffer = new byte[uploadBufferSize]; // Keep a huge
						    // size,
						    // otherwise
						    // it's very
						    // slow

	InputStream in = new BufferedInputStream(new FileInputStream(file));
	
	try {
	    int len;
	    while ((len = in.read(buffer)) >= 0) {
		out.write(buffer, 0, len);
	    }

	} finally {
	    IOUtils.closeQuietly(in);
	}
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
