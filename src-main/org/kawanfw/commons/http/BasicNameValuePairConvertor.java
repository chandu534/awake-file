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

import java.io.IOException;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;

import org.apache.http.message.BasicNameValuePair;
import org.kawanfw.commons.api.client.HttpProtocolParameters;
import org.kawanfw.commons.util.ClientLogger;
import org.kawanfw.commons.util.FrameworkDebug;
import org.kawanfw.commons.util.HtmlConverter;
import org.kawanfw.commons.util.convert.Pbe;
import org.kawanfw.file.util.parms.Parameter;
import org.kawanfw.file.version.FileVersionValues;

public class BasicNameValuePairConvertor {
    /** Debug flag */
    private static boolean DEBUG = FrameworkDebug
	    .isSet(BasicNameValuePairConvertor.class);

    /** The request params */
    private List<BasicNameValuePair> requestParams = null;

    /** The http protocol parameters */
    private HttpProtocolParameters httpProtocolParameters = null;

    /**
     * Constructor
     * 
     * @param requestParams
     *            the request parameters list of BasicNameValuePair
     */
    public BasicNameValuePairConvertor(List<BasicNameValuePair> requestParams, 
	    	HttpProtocolParameters httpProtocolParameters) {
	this.requestParams = requestParams;
	this.httpProtocolParameters = httpProtocolParameters;
    }

    /**
     * @return the convert & may be encrypted parameters
     */
    public List<BasicNameValuePair> convert() throws IOException {
	List<BasicNameValuePair> requestParamsConverted = new Vector<BasicNameValuePair>();

	for (BasicNameValuePair basicNameValuePair : requestParams) {
	    String paramName = basicNameValuePair.getName();	    
	    String paramValue = basicNameValuePair.getValue();

	    // Don't do it for STATEMENT_HOLDER ==> already has it's own Html
	    // conversion and encryption
	    if (! paramName.equals(Parameter.STATEMENT_HOLDER))
	    {
		paramValue = HtmlConverter.toHtml(paramValue);
		paramValue = encryptValue(paramName, paramValue);
	    }
		    	  
	    debug("converted param name : " + paramName);
	    debug("converted param value: " + paramValue);

	    BasicNameValuePair basicNameValuePairEnc = new BasicNameValuePair(
		    paramName, paramValue);
	    requestParamsConverted.add(basicNameValuePairEnc);
	}
	
	// Add the Version
	BasicNameValuePair basicNameValuePairEnc = new BasicNameValuePair(
		Parameter.VERSION, FileVersionValues.VERSION);
	requestParamsConverted.add(basicNameValuePairEnc);

	return requestParamsConverted;
    }

    /**
     * Encrypt the parameter value
     * 
     * @param paramName
     *            the parameter name
     * @param paramValue
     *            the parameter value
     * 
     * @return the encrypted parameter value
     * 
     * @throws IOException
     *             if any exception occurs
     */
    private String encryptValue(String paramName, String paramValue)
	    throws IOException {
	
	if (httpProtocolParameters == null) {
	    return paramValue;
	}
	
	char [] password = httpProtocolParameters.getEncryptionPassword();

	if (password == null || password.length <= 1) {
	    return paramValue;
	}
	    	
	try {
	    paramValue = Pbe.KAWANFW_ENCRYPTED
		    + new Pbe().encryptToHexa(paramValue, password);
	} catch (Exception e) {
	    String message = "Impossible to encrypt the value of the parameter "
		    + paramName;

	    throw new IOException(message, e);
	}
	return paramValue;
    }

    /**
     * Displays the given message if DEBUG is set.
     * 
     * @param s
     *            the debug message
     */

    private static void debug(String s) {
	if (DEBUG) {
	    ClientLogger.getLogger().log(Level.WARNING, s);
	}
    }

}
