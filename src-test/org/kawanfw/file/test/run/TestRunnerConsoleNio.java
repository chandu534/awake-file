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
package org.kawanfw.file.test.run;

import java.sql.SQLException;
import java.util.Date;

import org.apache.commons.lang3.SystemUtils;
import org.kawanfw.commons.api.client.HttpProtocolParameters;
import org.kawanfw.commons.api.client.HttpProxy;
import org.kawanfw.commons.util.FrameworkSystemUtil;
import org.kawanfw.file.api.client.RemoteSession;
import org.kawanfw.file.test.api.client.nio.CallTestNio;
import org.kawanfw.file.test.api.client.nio.DeleteAllNio;
import org.kawanfw.file.test.api.client.nio.DownloadFilesNio;
import org.kawanfw.file.test.api.client.nio.MkdirsRemoteNio;
import org.kawanfw.file.test.api.client.nio.RenameFilesNio;
import org.kawanfw.file.test.api.client.nio.UploadFilesNio;
import org.kawanfw.file.test.api.client.nio.engines.EngineDownloadBigFilesNew;
import org.kawanfw.file.test.api.client.nio.engines.EngineUploadBigFilesNew;
import org.kawanfw.file.test.parms.FileGenerator;
import org.kawanfw.file.test.parms.ProxyLoader;
import org.kawanfw.file.test.parms.TestParms;
import org.kawanfw.file.test.run.filter.BuiltInFilterTest;
import org.kawanfw.file.test.run.remotefiles.RemoteFileTest;
import org.kawanfw.file.test.util.MessageDisplayer;

public class TestRunnerConsoleNio {

    /**
     * @param remoteSession
     *            the Awake FILE Session
     * @throws Exception
     * @throws SQLException
     */
    public static void testAll(RemoteSession remoteSession)
	    throws Exception, SQLException {
	
	TestRunnerConsole.startIt();
	
	MessageDisplayer.display("");
	MessageDisplayer.display("Local Java Version : "
		+ System.getProperty("java.version"));
	MessageDisplayer.display("Remote Java Version: "
		+ remoteSession.getRemoteJavaVersion());
	MessageDisplayer.display("version: " + remoteSession.getVersion());
	MessageDisplayer.display("url    : " + remoteSession.getUrl());
	MessageDisplayer.display("");

	FileGenerator.initDeleteLocalDirectories();
	
	new DeleteAllNio().test(remoteSession);
	new CallTestNio().test(remoteSession);
	new MkdirsRemoteNio().test(remoteSession);
	new UploadFilesNio().test(remoteSession);	
	new DownloadFilesNio().test(remoteSession);
	new RenameFilesNio().test(remoteSession);
	
	// Filters
	new BuiltInFilterTest().test(remoteSession);
	

	// Remote Files
	new RemoteFileTest().test(remoteSession);
	
	if (TestParms.TEST_BIG_FILES && ! FrameworkSystemUtil.isAndroid()) {
	    new EngineUploadBigFilesNew().test(remoteSession);
	    new EngineDownloadBigFilesNew().test(remoteSession);
	}
    }

    public static void startIt() throws Exception {
	if (SystemUtils.IS_JAVA_1_7) {
	    System.setProperty("java.net.preferIPv4Stack", "true");
	}

//	boolean useProxy = false;
//	HttpProxy httpProxy = null;
//
//	MessageDisplayer.display(new Date());
//	MessageDisplayer.display("Proxy Detection...");
//	if (SystemUtils.IS_JAVA_1_6) {
//	    DefaultHttpProxyDetector defaultHttpProxyDetector = new DefaultHttpProxyDetector();
//	    if (defaultHttpProxyDetector.getAddress() != null) {
//		MessageDisplayer.display("useProxy = true...");
//		useProxy = true;
//	    }
//	}
//	else {
//	    
//	    if (!FrameworkSystemUtil.isAndroid()) {
//		useProxy = TestParms.USE_PROXY ? true : false;
//	    }
//	}
//
//	if (useProxy) {
//	    MessageDisplayer.display("new ProxyLoader().getProxy()...");
//	    httpProxy = new ProxyLoader().getProxy();
//	}

	HttpProxy httpProxy = new ProxyLoader().getProxy();
	
	HttpProtocolParameters httpProtocolParameters = new HttpProtocolParameters();

	while (true) {

	    MessageDisplayer.display("new RemoteSession()...");
	    RemoteSession remoteSession = new RemoteSession(
		    TestParms.AWAKE_URL, TestParms.REMOTE_USER,
		    TestParms.REMOTE_PASSWORD.toCharArray(), httpProxy,
		    httpProtocolParameters);
	   	    
	    MessageDisplayer.display(new Date());
	   
	    Date begin = new Date(); 	    
	    testAll(remoteSession);
	    Date end = new Date();

	    MessageDisplayer.display("");
	    MessageDisplayer.display("Begin: " + begin);
	    MessageDisplayer.display("End  : " + end);

	    remoteSession.logoff();

	    if (!TestParms.LOOP_MODE) {
		return;
	    }

	}
    }

    public static void main(String[] args) throws Exception {

	startIt();
    }
}
