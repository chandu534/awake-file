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

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.kawanfw.commons.api.client.HttpProtocolParameters;
import org.kawanfw.commons.api.client.HttpProxy;
import org.kawanfw.commons.util.FrameworkFileUtil;
import org.kawanfw.file.api.client.FileSession;
import org.kawanfw.file.api.client.UrlSession;
import org.kawanfw.file.test.api.client.legacy.CallTest;
import org.kawanfw.file.test.api.client.legacy.DeleteAll;
import org.kawanfw.file.test.api.client.legacy.DownloadFiles;
import org.kawanfw.file.test.api.client.legacy.MkdirsRemote;
import org.kawanfw.file.test.api.client.legacy.RenameFiles;
import org.kawanfw.file.test.api.client.legacy.UploadFiles;
import org.kawanfw.file.test.parms.FileGenerator;
import org.kawanfw.file.test.parms.ProxyLoader;
import org.kawanfw.file.test.parms.TestParms;
import org.kawanfw.file.test.util.MessageDisplayer;

public class TestRunnerConsole {

    /**
     * @param fileSession
     *            the Awake FILE Session
     * @throws Exception
     * @throws SQLException
     */
    public static void testAll(FileSession fileSession)
	    throws Exception, SQLException {

	MessageDisplayer.display("Java Version: "
		+ System.getProperty("java.version"));
	MessageDisplayer.display("version: " + fileSession.getVersion());
	MessageDisplayer.display("url    : " + fileSession.getUrl());

	FileGenerator.initDeleteLocalDirectories();
	
	new DeleteAll().test(fileSession);
	new CallTest().test(fileSession);
	new MkdirsRemote().test(fileSession);
	new UploadFiles().test(fileSession);	
	new DownloadFiles().test(fileSession);
	new RenameFiles().test(fileSession);
	
    }

    public static void startIt() throws Exception {
	if (SystemUtils.IS_JAVA_1_7) {
	    System.setProperty("java.net.preferIPv4Stack", "true");
	}

//	boolean useProxy = false;
//	HttpProxy httpProxy = null;
//
//	if (SystemUtils.IS_JAVA_1_6) {
//	    DefaultHttpProxyDetector defaultHttpProxyDetector = new DefaultHttpProxyDetector();
//	    if (defaultHttpProxyDetector.getAddress() != null) {
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
//	    httpProxy = new ProxyLoader().getProxy();
//	}
	
	HttpProxy httpProxy = new ProxyLoader().getProxy();

	HttpProtocolParameters httpProtocolParameters = new HttpProtocolParameters();

	while (true) {

	    FileSession fileSession = new FileSession(
		    TestParms.AWAKE_URL, TestParms.REMOTE_USER,
		    TestParms.REMOTE_PASSWORD.toCharArray(), httpProxy,
		    httpProtocolParameters);
	   	    
	    System.out.println(new Date());
	    
	    UrlSession urlSession = new UrlSession(httpProxy);

	    Date begin = new Date();

	    List<String> directories = fileSession.listDirectories("/");
	    MessageDisplayer.display("List / directories: " + directories);

	    List<String> files = fileSession.listFiles("/");
	    MessageDisplayer.display("List / files: " + files);
	    
	    testAll(fileSession);

	    MessageDisplayer.display("");
	    String urlDownload = urlSession.download(new URL(
		    "http://www.google.fr"));
	    MessageDisplayer.display(urlDownload.substring(0, 80));

	    File fileUrl = createAwakeTempFile();
	    urlSession.download(new URL(
		    "http://www.awake-file.org/1.5/RELEASE-NOTES.txt"),
		    fileUrl);
	    urlDownload = FileUtils.readFileToString(fileUrl);
	    MessageDisplayer.display(urlDownload.substring(0, 80));

	    fileUrl.delete();

	    Date end = new Date();

	    MessageDisplayer.display("");
	    MessageDisplayer.display("Begin: " + begin);
	    MessageDisplayer.display("End  : " + end);

	    fileSession.logoff();

	    if (!TestParms.LOOP_MODE) {
		return;
	    }

	}
    }

    /**
     * Create our own Awake temp file
     * 
     * @return the tempfile to create
     */
    private static synchronized File createAwakeTempFile() {
	String unique = FrameworkFileUtil.getUniqueId();
	String tempDir = FrameworkFileUtil.getKawansoftTempDir();
	String tempFile = tempDir + File.separator + "http-transfer-one-"
		+ unique + ".kawanfw.txt";

	return new File(tempFile);
    }

    public static void main(String[] args) throws Exception {

	startIt();
    }
}
