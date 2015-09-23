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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.kawanfw.commons.api.server.CommonsConfigurator;
import org.kawanfw.commons.server.util.ServerLogger;
import org.kawanfw.commons.util.FrameworkDebug;
import org.kawanfw.commons.util.IpUtil;
import org.kawanfw.commons.util.Tag;
import org.kawanfw.commons.util.TransferStatus;
import org.kawanfw.file.util.parms.Action;
import org.kawanfw.file.util.parms.Parameter;
import org.kawanfw.file.util.parms.ReturnCode;

/**
 * @author Nicolas de Pomereu
 * 
 *         The method executeRequest() is to to be called from the
 *         ServerClientLogin Servlet and Class. <br>
 *         It will execute a client side request with a
 *         ServerCaller.httpsLogin()
 * 
 */

public class ServerLoginAction extends HttpServlet {
    private static boolean DEBUG = FrameworkDebug.isSet(ServerLoginAction.class);

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    // A space
    public static final String SPACE = " ";

    /**
     * Constructor
     */
    public ServerLoginAction() {

    }

    /**
     * 
     * Execute the login request asked by the main File Servlet
     * 
     * @param request
     *            the http request
     * @param response
     *            the http response
     * @param commonsConfigurator
     *            the Commons Client login specific class
     * @param action
     *            the login action: BEFORE_LOGIN_ACTION or LOGIN_ACTION
     * @throws IOException
     *             if any Servlet Exception occurs
     */
    public void executeAction(HttpServletRequest request,
	    HttpServletResponse response,
	    CommonsConfigurator commonsConfigurator, String action)
	    throws IOException {
	PrintWriter out = response.getWriter();

	try {
	    response.setContentType("text/html");

	    // if the action is BEFORE_LOGIN_ACTION: just test if we must be in
	    // https mode
	    if (action.equals(Action.BEFORE_LOGIN_ACTION)) {
		// Check if we must be in httpS
		// boolean forceHttps =
		// commonsConfigurator.forceSecureHttp();
		boolean forceHttps = CommonsConfiguratorCall
			.forceSecureHttp(commonsConfigurator);

		out.println(TransferStatus.SEND_OK);
		out.println(forceHttps);
		return;
	    }

	    debug("before request.getParameter(Parameter.LOGIN);");

	    String username = request.getParameter(Parameter.USERNAME);
	    username = username.trim();

	    String password = request.getParameter(Parameter.PASSWORD);
	    password = password.trim();

	    // User must provide a user
	    if (username.length() < 1) {
		debug("username.length() < 1!");
		// No login transmitted
		// Redirect to ClientLogin with error message.
		out.println(TransferStatus.SEND_OK);
		out.println(ReturnCode.INVALID_LOGIN_OR_PASSWORD);
		return;
	    }

	    debug("before commonsConfigurator.getBannedUsernames();");

	    // Check the username. Refuse access if username is banned
	    // Set<String> usernameSet =
	    // commonsConfigurator.getBannedUsernames();
	    Set<String> usernameSet = CommonsConfiguratorCall
		    .getBannedUsernames(commonsConfigurator);

	    if (usernameSet.contains(username)) {
		debug("banned username!");
		throw new SecurityException("Username is banned: "
			+ usernameSet);
	    }

	    // Check the IP. Refuse access if IP is banned/blacklisted
	    String ip = request.getRemoteAddr();

	    debug("before commonsConfigurator.getIPsWhitelist();");
	    List<String> whitelistedIpList = CommonsConfiguratorCall
		    .getIPsWhitelist(commonsConfigurator);

	    if (DEBUG) {
		log("Printing whitelisted IPs...");
		for (String whitelistedIp : whitelistedIpList) {
		    log("whitelisted IP: " + whitelistedIp);
		}
	    }

	    if (!IpUtil.isIpWhitelisted(ip, whitelistedIpList)) {
		debug("not whitelisted IP!");
		throw new SecurityException("Client IP is not whitelisted: "
			+ ip);
	    }

	    // use blacklist only if whitelist is empty

	    if (whitelistedIpList == null || whitelistedIpList.isEmpty()) {
		debug("before commonsConfigurator.getIPsBlacklist();");
		List<String> blacklistedIpList = CommonsConfiguratorCall
			.getIPsBlacklist(commonsConfigurator);

		if (DEBUG) {
		    log("Printing blacklisted IPs...");
		    for (String blacklistedIp : blacklistedIpList) {
			log("blacklisted IP: " + blacklistedIp);
		    }
		}

		if (IpUtil.isIpBlacklisted(ip, blacklistedIpList)) {
		    debug("blacklisted IP!");
		    throw new SecurityException("Client IP is blacklisted: "
			    + ip);
		}
	    }

	    debug("calling checkLoginAndPassword");

	    boolean isOk = commonsConfigurator.login(username,
		    password.toCharArray());

	    debug("login isOk: " + isOk + " (login: " + username + ")");

	    if (!isOk) {
		debug("login: invalid login or password");

		// Reduce the login speed
		LoginSpeedReducer loginSpeedReducer = new LoginSpeedReducer(
			username);
		loginSpeedReducer.checkAttempts();

		out.println(TransferStatus.SEND_OK);
		out.println(ReturnCode.INVALID_LOGIN_OR_PASSWORD);
		return;
	    }

	    debug("Login done!");

	    // OK! Now build a token with SHA-1(username + secretValue)
	    String token = CommonsConfiguratorCall.computeAuthToken(
		    commonsConfigurator, username);

	    // out.println(HttpTransfer.SEND_OK + SPACE + ReturnCode.OK + SPACE
	    // + token);
	    out.println(TransferStatus.SEND_OK);
	    out.println(ReturnCode.OK + SPACE + token);

	} catch (Exception e) {

	    out.println(TransferStatus.SEND_FAILED);
	    out.println(e.getClass().getName());
	    out.println(ServerUserThrowable.getMessage(e));
	    out.println(ExceptionUtils.getStackTrace(e)); // stack trace

	    try {
		ServerLogger.getLogger().log(Level.WARNING, Tag.PRODUCT_EXCEPTION_RAISED + " "
			+ ServerUserThrowable.getMessage(e));
		ServerLogger.getLogger().log(Level.WARNING, Tag.PRODUCT_EXCEPTION_RAISED + " "
			+ ExceptionUtils.getStackTrace(e));
	    } catch (Exception e1) {
		e1.printStackTrace();
		e1.printStackTrace(System.out);
	    }

	}
    }

    private void debug(String s) {
	if (DEBUG) {
	    ServerLogger.getLogger().log(Level.WARNING, s);
	}
    }
}
