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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;

import org.apache.http.entity.mime.content.InputStreamBody;
import org.kawanfw.commons.api.client.HttpProtocolParameters;
import org.kawanfw.commons.util.ClientLogger;
import org.kawanfw.commons.util.DefaultParms;
import org.kawanfw.commons.util.FrameworkDebug;

/**
 * 
 * This class externalizes InputStreamBody only to provide a sendData()
 * implementation. Each write on the output stream set the progress indicator
 * for the file transfer engine.
 * 
 * Note that the tempLen used for progress manager is httpTransferOne.tempLen
 * because input stream may be reused dor subsquent calls.
 * 
 * So, NEVER CLOSE THE inputStream. *
 * 
 * @author Nicolas de Pomereu
 */
public class InputStreamBodyForEngine extends InputStreamBody {
    
    /** For debug info */
    private static boolean DEBUG = FrameworkDebug.isSet(InputStreamBodyForEngine.class);

    /** The http protocol parameters */
    private HttpProtocolParameters httpProtocolParameters;

    /**
     * InputStreamBody Constructor.
     * 
     * @param in
     *            the input stream to us for post. May be reused in subsquents
     *            calls, do not close!
     * @param mimeType
     *            the mime Type
     * @param httpProtocolParameters
     *            the http protocol parameters
     * @throws FileNotFoundException
     *             if the <i>file</i> is not a normal file or if it is not
     *             readable.
     */
    public InputStreamBodyForEngine(InputStream in, String mimeType,
	    HttpProtocolParameters httpProtocolParameters)
	    throws FileNotFoundException {
	super(in, mimeType);

	if (in == null) {
	    throw new IllegalArgumentException("in can not be null!");
	}

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

	byte[] tmp = new byte[uploadBufferSize]; // Keep a huge
						 // size,
						 // otherwise
						 // it's very
						 // slow

	// May be reused in subsquents calls, do not close!
	InputStream instream = super.getInputStream();
	int len;

	while ((len = instream.read(tmp)) >= 0) {
	    //debug("len: " + len);
	    out.write(tmp, 0, len);
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
