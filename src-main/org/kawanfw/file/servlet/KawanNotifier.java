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
package org.kawanfw.file.servlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.kawanfw.commons.api.server.CommonsConfigurator;
import org.kawanfw.commons.api.server.util.Sha1;
import org.kawanfw.commons.server.util.ServerLogger;
import org.kawanfw.commons.util.FrameworkDebug;
import org.kawanfw.commons.util.FrameworkFileUtil;
import org.kawanfw.commons.util.Tag;

/**
 * 
 * Thread will notify our remote Kawan servers that a user has succeeded to
 * login. <br>
 * <br>
 * This is done once during the JVM session per client user login, at first
 * login. It is also done in a separated and secured thread: your File
 * Manager Servlet will not be slowed down by the notification and no Exceptions
 * will be thrown. Notification contains only anonymous data that are not
 * reversible and thus can not identify your server: hash value of your server ip
 * address and login count. There are no notifications for localhost or
 * 127.0.0.1 server name. <br>
 * <br>
 * Please note that the notification mechanism is important for us as software
 * editor: it says if our software is used, and the average client users per
 * installation. However, if you *really* don't want our remote Kawan servers to
 * be notified by your server, just create the following file with any content: <br>
 * <code>user.home/.kawansoft/no_notify.txt</code>, where <code>user.home</code> is
 * the one of your Java EE Web server. Notification will be deactivated and at
 * server startup the message <br>
 * "[FRAMEWORK START] Notification to Kawan Servers: OFF" <br>
 * will be inserted in the log defined by
 * {@link CommonsConfigurator#getLogger()} <br>
 * You can check the notification mechanism following source code in class
 * <code>org.kawanfw.sql.servlet.KawanNotifier.java</code>.
 * 
 * @author Nicolas de Pomereu
 */

public class KawanNotifier extends Thread {

    private static boolean DEBUG = FrameworkDebug.isSet(KawanNotifier.class);

    /** All the usernames that have logged */
    private static Set<String> usernames = new HashSet<String>();

    /** the username that has logged */
    private String username = null;

    /** The framwork in use SQL or FILE + Version */
    private String product = null;

    /** The counter of users that successfully logged once */
    private static int usernameCpt = 0;

    /**
     * 
     * @param username
     *            the username that has logged
     * @param product 
     * 		 the product in use
     */
    public KawanNotifier(String username, String product) {
	this.username = username;
	this.product = product;
    }

    /**
     * Notify the Host that a user has done a login - Done once in a server
     * session. <br>
     * This is done in this secured thread: the Server File Manager Servlet will
     * not wait and all thrown <code>Exceptions</code> are trapped
     */
    public void run() {

	BufferedReader bufferedReader = null;

	try {

	    if (!usernames.contains(username)) {
		usernames.add(username);

		// Increment the number of users
		usernameCpt++;

		InetAddress inetAddress = InetAddress.getLocalHost();

		String ip = inetAddress.toString();

		// Make IP address completely anonymous
		Sha1 sha1 = new Sha1();
		ip = sha1.getHexHash(ip.getBytes());
		
		String urlStr = "http://www.kawanfw.org/NotifyNew?ip=" + ip
			+ "&user=" + usernameCpt + "&product=" + product;

		debug("urlStr: " + urlStr);
		
		URL url = new URL(urlStr);
		URLConnection urlConnection = url.openConnection();

		bufferedReader = new BufferedReader(new InputStreamReader(
			urlConnection.getInputStream()));
		String inputLine;
		while ((inputLine = bufferedReader.readLine()) != null) {
		    debug(inputLine);
		}
	    }

	} catch (Exception e) {
	    if (DEBUG) {
		try {
		    ServerLogger.getLogger().log(Level.WARNING, Tag.PRODUCT_EXCEPTION_RAISED
			    + " Notify Exception: " + e.toString());
		} catch (Exception e1) {
		    e1.printStackTrace(System.out);
		}
	    }
	} finally {
	    IOUtils.closeQuietly(bufferedReader);
	}
    }
    
    
    /**
     * Says it the username has already logged once in JVM session
     * @param username
     *            the username that has logged
     * @return	thrue if the username has already logged once in JVM session
     */
    public static boolean usernameAlreadyLogged(String username) {
	
	return usernames.contains(username) ? true : false;
	
    }
    
    /**
     * Says if the file user.home/.kawansoft/no_notify.txt exists.
     * 
     * @return true if user.home/.kawansoft/no_notify.txt exists.
     */
    public static boolean existsNoNotifyTxt() {

	String NoNotifyTxt = FrameworkFileUtil.getUserHomeDotKawansoftDir()  + File.separator
		+ "no_notify.txt";

	boolean noNotifyFileExists = new File(NoNotifyTxt).exists();
	return noNotifyFileExists;

    }

    /**
     * Says if web server is localhost (or 127.0.0.1)
     * 
     * @return true if web server is localhost (or 127.0.0.1)
     */
    public static boolean serverNameIsLocalhost() {

	//RequestInfoStore requestInfoStore = new RequestInfoStore();
	//String serverName = requestInfoStore.getServerName();

	HttpServletRequest httpServletRequest = RequestInfoStore.getHttpServletRequest();
	String serverName = httpServletRequest.getServerName();
	
	if (serverName.toLowerCase().contains("localhost")
		|| serverName.toLowerCase().contains("127.0.0.1")) {
	    debug("localhost serverName: " + serverName);
	    return true;
	} else {
	    return false;
	}

    }

    /**
     * Method called by children Servlet for debug purpose Println is done only
     * if class name name is in debug_list.ini
     */
    public static void debug(String s) {
	if (DEBUG) {
	    ServerLogger.getLogger().log(Level.WARNING, s);
	}
    }

}
