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

import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.kawanfw.commons.api.client.InvalidLoginException;
import org.kawanfw.commons.api.client.RemoteException;
import org.kawanfw.file.api.client.FileSession;
import org.kawanfw.file.test.parms.TestParms;
import org.kawanfw.file.test.util.MessageDisplayer;

/**
 * 
 * TestReload that a remote directory can be created and deleted
 * @author Nicolas de Pomereu
 */

public class MkdirsRemote {
    
    public static void main(String[] args) throws Exception {
	new MkdirsRemote().test();
    }
    
    @Test
    public void test() throws Exception {

	FileSession fileSession = new FileSession(TestParms.AWAKE_URL,
		TestParms.REMOTE_USER, TestParms.REMOTE_PASSWORD.toCharArray());
		
	test(fileSession);
    }

    /**
     * 
     * @param fileSession the Awake FILE Session
     * 
     * @throws IllegalArgumentException
     * @throws InvalidLoginException
     * @throws UnknownHostException
     * @throws ConnectException
     * @throws RemoteException
     * @throws IOException
     */
    public void test(FileSession fileSession)
	    throws IllegalArgumentException, InvalidLoginException,
	    UnknownHostException, ConnectException, RemoteException,
	    IOException {
	// Create a remote directory
	MessageDisplayer.display("");
	MessageDisplayer.display("creating 3 remote directories with mkdirsRemote()...");
	fileSession.mkdirs("/" + TestParms.MYDIR1);
	fileSession.mkdirs("/" + TestParms.MYDIR2);	
	fileSession.mkdirs("/" + TestParms.MYDIR3);
	fileSession.mkdirs("/" + TestParms.MYDIR4);
	
	// TestReload if the directory really exists
	MessageDisplayer.display("Testing remote directories list with listRemoteDirectories()...");
	List<String> directories= fileSession.listDirectories("/");
	MessageDisplayer.display(directories.toString());
	
	//Assert.assertEquals("3 directories created", 3, directories.size());
	Assert.assertTrue("directories contains " + TestParms.MYDIR1 , directories.contains(TestParms.MYDIR1));
	Assert.assertTrue("directories contains " + TestParms.MYDIR2 , directories.contains(TestParms.MYDIR2));
	Assert.assertTrue("directories contains " + TestParms.MYDIR3 , directories.contains(TestParms.MYDIR3));
	Assert.assertTrue("directories contains " + TestParms.MYDIR4 , directories.contains(TestParms.MYDIR4));
	
	MessageDisplayer.display("Remote directories: " + directories);
	MessageDisplayer.display("");
	
	// Try to delete the directory
	MessageDisplayer.display("Testing remote directory delete with deleteRemoteFile()...");
	fileSession.delete("/" + TestParms.MYDIR2);
	
	directories = fileSession.listDirectories("/");
	
	//Assert.assertEquals("2 directories exists now", directories.size(), 2);
	
	Assert.assertTrue("directories contains " + TestParms.MYDIR1 , directories.contains(TestParms.MYDIR1));
	Assert.assertFalse("directories doe NOT contains " + TestParms.MYDIR2 , directories.contains(TestParms.MYDIR2));
	Assert.assertTrue("directories contains " + TestParms.MYDIR3, directories.contains(TestParms.MYDIR3));
	
	MessageDisplayer.display(TestParms.MYDIR2 + " deleted!");
	MessageDisplayer.display("directories: " + directories);
    }

    @Test(expected = InvalidLoginException.class)
    public void test2() throws Exception {
	
	@SuppressWarnings("unused")
	FileSession fileSession = new FileSession(
		TestParms.AWAKE_URL, "xxxxxxxxxxxx",
		TestParms.REMOTE_PASSWORD.toCharArray());
	
    }

}
