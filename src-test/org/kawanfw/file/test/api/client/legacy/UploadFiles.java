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
package org.kawanfw.file.test.api.client.legacy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Vector;

import org.junit.Assert;
import org.junit.Test;
import org.kawanfw.commons.api.client.InvalidLoginException;
import org.kawanfw.commons.api.client.RemoteException;
import org.kawanfw.commons.util.FrameworkSystemUtil;
import org.kawanfw.file.api.client.FileSession;
import org.kawanfw.file.test.parms.TestParms;
import org.kawanfw.file.test.util.MessageDisplayer;

/**
 * 
 * TestReload that a local files can be uploaded to the remote server. Then test the
 * remote list, the remot sizes and the remote delete
 * 
 * @author Nicolas de Pomereu
 */

public class UploadFiles {

    public static void main(String[] args) throws Exception {
	new UploadFiles().test();
    }

    @Test
    public void test() throws Exception {
	
	FileSession fileSession = new FileSession(
		TestParms.AWAKE_URL, TestParms.REMOTE_USER,
		TestParms.REMOTE_PASSWORD.toCharArray());

	test(fileSession);

    }

    /**
     * @param fileSession
     *            the Awake FILE Session
     * 
     * @throws IllegalArgumentException
     * @throws InvalidLoginException
     * @throws FileNotFoundException
     * @throws UnknownHostException
     * @throws ConnectException
     * @throws InterruptedException
     * @throws RemoteException
     * @throws IOException
     */
    public void test(FileSession fileSession)
	    throws IllegalArgumentException, InvalidLoginException,
	    FileNotFoundException, UnknownHostException, ConnectException,
	    InterruptedException, RemoteException, IOException, Exception {
	File blob1 = TestParms.getFileFromUserHome(TestParms.BLOB_FILE_TULIPS);
	File blob2 = TestParms.getFileFromUserHome(TestParms.BLOB_FILE_KOALA);
	File blob3 = TestParms.getFileFromUserHome(TestParms.BLOB_FILE_RUSSIAN);
		
	// Create a remote directory;
	MessageDisplayer.display("");
	MessageDisplayer.display("uploading " + blob1);
	
	fileSession.upload(blob1, "/" + TestParms.MYDIR1 + "/" + blob1.getName());
	
	MessageDisplayer.display("uploading " + blob2);

	FileSession fileSessionClone = fileSession.clone();
	fileSessionClone.upload(blob2, "/" + TestParms.MYDIR1 + "/"
		+ blob2.getName());
	fileSessionClone.logoff();

	if (!FrameworkSystemUtil.isAndroid()) {
	    MessageDisplayer.display("uploading " + blob3);
	    fileSession.upload(blob3,
		    "/" + TestParms.MYDIR1 + "/" + blob3.getName());
	}
	
	boolean existsBlob1 = fileSession.exists("/" + TestParms.MYDIR1
		+ "/" + blob1.getName());
	Assert.assertTrue("exists blob1", existsBlob1);

	boolean existsBlob2 = fileSession.exists("/" + TestParms.MYDIR1
		+ "/" + blob2.getName());
	Assert.assertTrue("exists blob2", existsBlob2);

	if (!FrameworkSystemUtil.isAndroid()) {
	    boolean existsBlob3 = fileSession.exists("/" + TestParms.MYDIR1
		    + "/" + blob3.getName());
	    Assert.assertTrue("exists blob3", existsBlob3);
	}

	boolean existsNone = fileSession.exists("/" + TestParms.MYDIR1
		+ "/" + " file-does-not-exists");
	Assert.assertTrue("exists blob2", !existsNone);

	// TestReload if the directory really exists
	MessageDisplayer
		.display("Testing remote files list with listFiles()...");
	List<String> remotefiles = fileSession.listFiles("/"
		+ TestParms.MYDIR1);

	Assert.assertEquals("remotefiles contains " + TestParms.BLOB_FILE_TULIPS,
		remotefiles.contains(TestParms.BLOB_FILE_TULIPS), true);
	Assert.assertEquals("remotefiles contains " + TestParms.BLOB_FILE_KOALA,
		remotefiles.contains(TestParms.BLOB_FILE_KOALA), true);

	MessageDisplayer.display("remotefiles: " + remotefiles);
	MessageDisplayer.display("");

	MessageDisplayer
	.display("Testing remote files size with fileSession.length(files)...");
	long remoteBlob1Length = fileSession.length("/" + TestParms.MYDIR1
		+ "/" + blob1.getName());
	long remoteBlob2Length = fileSession.length("/" + TestParms.MYDIR1
		+ "/" + blob2.getName());
	
	Assert.assertEquals("remote length is ok for " + TestParms.BLOB_FILE_TULIPS,
		blob1.length(), remoteBlob1Length);
	Assert.assertEquals("remote length is ok for " + TestParms.BLOB_FILE_KOALA,
		blob2.length(), remoteBlob2Length);

	MessageDisplayer
		.display("Testing remote files comparison size with fileSession.length(files)...");
	List<String> files = new Vector<String>();
	files.add("/" + TestParms.MYDIR1 + "/" + blob1.getName());
	files.add("/" + TestParms.MYDIR1 + "/" + blob2.getName());
	long totalRemotelength = fileSession.length(files);

	Assert.assertEquals("total remote length is ok for two files",
		remoteBlob1Length + remoteBlob2Length, totalRemotelength);

	MessageDisplayer
		.display("Testing deleting remote files with fileSession.delete...");
	fileSession.delete("/" + TestParms.MYDIR1 + "/" + blob1.getName());

	MessageDisplayer
		.display("Testing remote list files remote files with listFiles()...");
	files = fileSession.listFiles("/" + TestParms.MYDIR1);
	Assert.assertTrue("One or two file in remote " + TestParms.MYDIR1,
		files.size() >= 1);

	MessageDisplayer.display("ReUpload of deleted files...");	
	fileSession.upload(blob1, "/" + TestParms.MYDIR1 + "/"
		+ blob1.getName());	
	
	MessageDisplayer.display("Done.");
    }

}
