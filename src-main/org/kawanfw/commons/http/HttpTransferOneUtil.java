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
import java.lang.reflect.Constructor;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.impl.client.DefaultHttpClient;

public class HttpTransferOneUtil {

    
    private static int SCHEME_NB_PARMS = 3;
    
    /**
     * Extracts the servlet path from an url.
     * Example: if url "http://www.awake-sql:9090/ServerSqlManager", the method will return "/ServerSqlManager"
     * @param url full url with servlet path
     * @return	the servlet path with trailing /
     */
    public static String getServletPathFromUrl(String url) {
	
	if (url == null) {
	    throw new IllegalArgumentException("url can not be null!");
	}
	
	if (! url.startsWith("http://") && ! url.startsWith("https://")) {
	    throw new IllegalArgumentException("url must start with \"http://\" or \"https://\". passed url is invalid: " + url);	    
	}
	
	String servletPath = StringUtils.substringAfter(url, "//");
	servletPath =  "/" + StringUtils.substringAfter(servletPath, "/");
	return servletPath;
    }
    
    /**
     * Code that forces HttpClient to accept any self signed SSL Cert
     * 
     * @param httpClient
     *            the DefaultHttpClient in use
     * @throws IOException
     */
    public static void acceptSelfSignedSslCert(DefaultHttpClient httpClient)
	    throws IOException {
//	
//	try {
//	    ClientConnectionManager ccm = httpClient.getConnectionManager();
//	    SSLSocketFactory sslsf = new SSLSocketFactory(
//		    new TrustSelfSignedStrategy());
//	    Scheme https = new Scheme("https", 444, sslsf);
//	    ccm.getSchemeRegistry().register(https);
//	} catch (Exception e) {
//	    throw new IOException(e.getMessage());
//	}
	
    
	try {
	    ClientConnectionManager ccm = httpClient.getConnectionManager();
	    	 
	    // Create by reflection the following code:
	    //	SSLSocketFactory sslsf = new SSLSocketFactory(
	    //	new TrustSelfSignedStrategy());
	    	    
	    Class<?> trustCls =  Class.forName("org.apache.http.conn.ssl.TrustSelfSignedStrategy");
	    Constructor<?> trustCt = trustCls.getConstructor();	
	    Object trustObject = trustCt.newInstance();
	    
	    Class<?> clsSSLSocketFactory = Class.forName("org.apache.http.conn.ssl.SSLSocketFactory");		    
	    Class<?> sslPartypes[] = new Class[1];
	    int i = 0;
	    sslPartypes[i++] = Class.forName("org.apache.http.conn.ssl.TrustStrategy");
	    	    
	    Constructor<?> shemeSsl = clsSSLSocketFactory.getConstructor(sslPartypes);
	    
	    Object sslObj = shemeSsl.newInstance(trustObject);	    
	  	    
	    // Create by reflection the following code:
	    // Scheme https = new Scheme("https", 444, sslsf);
	    	    
	    Class<?> clsSheme = Class.forName("org.apache.http.conn.scheme.Scheme");
	    
	    Class<?> schemePartypes[] = new Class[SCHEME_NB_PARMS];
	    i = 0;
	    schemePartypes[i++] = String.class;
	    schemePartypes[i++] = int.class;
	    schemePartypes[i++] =  Class.forName("org.apache.http.conn.scheme.SchemeSocketFactory"); // SchemeSocketFactory.class;
	    
	    Constructor<?> shemeCt = clsSheme.getConstructor(schemePartypes);	    

	    Object schemeArglist[] = new Object[SCHEME_NB_PARMS];
	    i = 0;
	    schemeArglist[i++] = new String("https");
	    schemeArglist[i++] = new Integer(444);
	    schemeArglist[i++] = sslObj;
	    Object schemeObj = shemeCt.newInstance(schemeArglist);
	            	    
	    Scheme https = (Scheme)schemeObj;	    
	    ccm.getSchemeRegistry().register(https);
	    
	} catch (Exception e) {
	    e.printStackTrace();
	    throw new IOException(e.getMessage());
	}
    }
    
}
