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
package org.kawanfw.file.servlet.convert;

import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.lang3.StringUtils;
import org.kawanfw.commons.api.server.CommonsConfigurator;
import org.kawanfw.commons.server.util.ServerLogger;
import org.kawanfw.commons.util.FrameworkDebug;
import org.kawanfw.commons.util.HtmlConverter;
import org.kawanfw.commons.util.Tag;
import org.kawanfw.commons.util.convert.Pbe;
import org.kawanfw.file.servlet.CommonsConfiguratorCall;
import org.kawanfw.file.util.parms.Parameter;

/**
 * Wrapper/holder for HttpServletRequest that will allow to decrypt correctly
 * the request.getParameter()
 * 
 * @author Nicolas de Pomereu
 * 
 */
public class HttpServletRequestConvertor extends HttpServletRequestWrapper {
    
    private static boolean DEBUG = FrameworkDebug
	    .isSet(HttpServletRequestConvertor.class);
    
    /**
     * The CommonsConfigurator instance. Used to get the password for
     * encryption
     */
    private CommonsConfigurator commonsConfigurator = null;

    /**
     * Constructor
     * 
     * @param request
     *            the underlying HttpServletRequest
     * @param commonsConfigurator
     *            Used to get the password for encryption
     */
    public HttpServletRequestConvertor(HttpServletRequest request,
	    CommonsConfigurator commonsConfigurator) {
	super(request);
	this.commonsConfigurator = commonsConfigurator;
    }

    /**
     * Will decrypt - if necessary - the parameter and return it's decrypted
     * value caller
     * 
     * @param parameterName
     *            the encrypted or not parameter name
     * @return the parameter value, decrypted if necessary.
     */
    @Override
    public String getParameter(String parameterName) {
	String value = super.getParameter(parameterName);

	if (parameterName.equals(Parameter.STATEMENT_HOLDER) || value == null
		|| value.isEmpty()) {
	    return value;
	}

	try {
	    value = decryptValue(parameterName, value);
	} catch (Exception e) {
	    String message = Tag.PRODUCT_USER_CONFIG_FAIL
		    + " Impossible to decrypt the value of the parameter "
		    + parameterName;
	    message += ". Check that password values are the same on client and server side.";

	    throw new IllegalArgumentException(message, e);
	}

	// The values are HTML converted in new version >= v1.0.5
	// This is just for transition when calling call()
	String version = super.getParameter(Parameter.VERSION);
	
	//debug("param name : " + parameterName);
	//debug("param value: " + value);
	
	if (version != null) {
	    // New protocol (implemented for AceQL version >= v1.0.5
	    value = HtmlConverter.fromHtml(value);
	}
		
	return value;
    }

   
    /**
     * Says it the request is encrypted
     * 
     * @param parameterName
     *            the parameter name
     * @return if the request is encrypted
     */
    private boolean isRequestEncrypted(String parameterName) {
	String value = super.getParameter(parameterName);
	if (value != null && !value.isEmpty()
		&& value.startsWith(Pbe.KAWANFW_ENCRYPTED)) {
	    return true;
	} else {
	    return false;
	}
    }

    /**
     * Decrypt the value
     * 
     * @param parameterName
     * @param value
     * @return
     * @throws Exception
     * @throws IllegalArgumentException
     */
    private String decryptValue(String parameterName, String value)
	    throws Exception, IllegalArgumentException {
	
	if (! isRequestEncrypted(parameterName)) {
	    debug("value *not* encrypted: " + value);
	    return value;
	}
	
	value = StringUtils.substringAfter(value, Pbe.KAWANFW_ENCRYPTED);
	debug("");
	debug("value encrypted: " + value);
	
	//value = new Pbe().decryptFromHexa(value,
	//	commonsConfigurator.getEncryptionPassword());

	value = new Pbe().decryptFromHexa(value, CommonsConfiguratorCall.getEncryptionPassword(commonsConfigurator));
	
	debug("value decrypted: " + value);
	
	// Check coherence for known parms and value
	// Parameter.ACTION, Parameter.TEST_CRYPTO

	if (parameterName.equals(Parameter.TEST_CRYPTO)) {
	    if (!value.equals(Parameter.TEST_CRYPTO)) {
		String message = Tag.PRODUCT_USER_CONFIG_FAIL
			+ " Impossible to decrypt correctly the value of the parameter "
			+ parameterName;
		message += ". Check that password values are the same on client and server side.";

		throw new IllegalArgumentException(message);
	    }
	}
	return value;
    }
    
    private void debug(String s) {
	if (DEBUG) {
	    ServerLogger.getLogger().log(Level.WARNING, s);
	}
    }    
}
