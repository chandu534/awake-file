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
package org.kawanfw.file.test.parms;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.kawanfw.commons.api.client.HttpProxy;
import org.kawanfw.commons.util.FrameworkSystemUtil;

/**
 * @author Nicolas de Pomereu
 *
 */
public class ProxyLoader {

    private static final String NEOTUNNEL_TXT = "i:\\neotunnel.txt";

    /**
     * 
     */
    public ProxyLoader() {

    }

    public HttpProxy getProxy() throws IOException {
	if (FrameworkSystemUtil.isAndroid()) {
	    return null;
	}

	HttpProxy httpProxy = null;
	if (TestParms.USE_PROXY) {
	    System.out.println("Loading proxy file info...");
	    // System.setProperty("java.net.useSystemProxies", "false");
	    File file = new File(NEOTUNNEL_TXT);
	    if (file.exists()) {
		String proxyValues = FileUtils.readFileToString(file);
		String username = StringUtils.substringBefore(proxyValues, " ");
		String password = StringUtils.substringAfter(proxyValues, " ");
		httpProxy = new HttpProxy("127.0.0.1", 8080, username, password);
		System.out.println("USING PROXY WITH AUTHENTICATION: "
			+ httpProxy);
	    } else {
		throw new FileNotFoundException(
			"proxy values not found. No file " + file);
	    }
	}
	
	return httpProxy;
    }

}
