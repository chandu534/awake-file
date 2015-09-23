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

import org.junit.Assert;
import org.junit.Test;
import org.kawanfw.commons.api.client.InvalidLoginException;
import org.kawanfw.commons.api.client.RemoteException;
import org.kawanfw.file.api.client.FileSession;
import org.kawanfw.file.test.parms.TestParms;
import org.kawanfw.file.test.util.MessageDisplayer;

/**
 * 
 * TestReload that a remote file can be renamed on the remote server.
 * 
 * @author Nicolas de Pomereu
 */

public class RenameFiles {

    public static void main(String[] args) throws Exception {
	new RenameFiles().test();
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
	
	// Create a remote directory;
	MessageDisplayer.display("");
	MessageDisplayer.display("Uploading " + blob1);
	fileSession.upload(blob1, blob1.getName()); 
		
	boolean existsBlob1 = fileSession.exists(blob1.getName());
	Assert.assertTrue("exists blob1", existsBlob1);

	MessageDisplayer.display("uploading " + blob2);
	fileSession.upload(blob2, blob2.getName());
	
	boolean existsBlob2 = fileSession.exists(blob2.getName());
	Assert.assertTrue("exists blob2", existsBlob2);
		
	//if (true) throw new IllegalAccessError("stop it!");
	
	MessageDisplayer.display("");
	MessageDisplayer.display("Trying to rename remote " + blob1.getName() + " to " + blob2.getName() + ".ren" + ". Dest file exists.");
	boolean renameFail = fileSession.rename(blob1.getName(), blob1.getName() + ".ren", false);
	MessageDisplayer.display("==> " + renameFail);
	
	// No more text on Linux
	if (TestParms.AWAKE_URL.equals("http://www.awake-file.org/awake-file/ServerFileManager")) {
	    MessageDisplayer.display("No more test on " + TestParms.AWAKE_URL);
	    return;
	}
	
//	Assert.assertFalse("renameFail", renameFail);
//	
//	MessageDisplayer.display("");
//	MessageDisplayer.display("Trying to rename remote " + blob1.getName() + " to " + blob2.getName()+ ". Dest file exists and deleteIfExists = true");
//	boolean renameSuccess = fileSession.rename(blob1.getName(), blob2.getName(), true);
//	MessageDisplayer.display("==> " + renameSuccess);
//	Assert.assertTrue("renameSuccess", renameSuccess);
//		
//	boolean deleted = fileSession.delete(blob2.getName());
//	Assert.assertTrue("deleted", deleted);
//	
//	fileSession.upload(blob1, blob1.getName());
//	existsBlob1 = fileSession.exists(blob1.getName());
//	Assert.assertTrue("exists blob1", existsBlob1);
//	
//	MessageDisplayer.display("");
//	MessageDisplayer.display("Trying to rename remote " + blob1.getName() + " to " + blob2.getName()+ ". Dest file does not exists and deleteIfExists = false");
//	renameSuccess = fileSession.rename(blob1.getName(), blob2.getName(), false);
//	MessageDisplayer.display("==> " + renameSuccess);
//	Assert.assertTrue("renameSuccess", renameSuccess);
	
	MessageDisplayer.display("");
	MessageDisplayer.display("Done.");
    }

}
