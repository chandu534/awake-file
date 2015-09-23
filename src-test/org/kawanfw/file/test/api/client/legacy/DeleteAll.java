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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.List;

import org.junit.Test;
import org.kawanfw.commons.api.client.InvalidLoginException;
import org.kawanfw.commons.api.client.RemoteException;
import org.kawanfw.file.api.client.FileSession;
import org.kawanfw.file.test.parms.TestParms;
import org.kawanfw.file.test.util.MessageDisplayer;

/**
 * 
 * Delete all remote files and directories
 * 
 * @author Nicolas de Pomereu
 */

public class DeleteAll {

    public static void main(String[] args) throws Exception {
	new DeleteAll().test();
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
	    InterruptedException, RemoteException,
	    IOException {

	// Create a remote directory;
	MessageDisplayer.display("");
	System.out
		.println("Deleting remote files list in /" + TestParms.MYDIR1 + " with fileSession.listFiles()...");
	List<String> remotefiles = fileSession.listFiles("/" + TestParms.MYDIR1);

	if (remotefiles != null) {
	    for (String remoteFile : remotefiles) {
		String fileStr = "/" + TestParms.MYDIR1 + "/" + remoteFile;
		fileSession.delete(fileStr);
		MessageDisplayer.display(fileStr + " deleted.");
	    }
	}

	MessageDisplayer.display("");
	System.out
		.println("Deleting remote files list in /" + TestParms.MYDIR3 + " with fileSession.listFiles()...");

	remotefiles = fileSession.listFiles("/" + TestParms.MYDIR3);

	if (remotefiles != null) {
	    for (String remoteFile : remotefiles) {
		String fileStr = TestParms.MYDIR3 + remoteFile;
		fileSession.delete(fileStr);
		MessageDisplayer.display(fileStr + " deleted.");
	    }
	}

	boolean deleted = fileSession.delete("/" + TestParms.MYDIR1);
	boolean deleted3 = fileSession.delete("/" + TestParms.MYDIR3);

	if (deleted) {
	    MessageDisplayer.display(TestParms.MYDIR1 + " deleted : " + deleted);
	}

	if (deleted3) {
	    MessageDisplayer.display(TestParms.MYDIR3 + " deleted : " + deleted);    
	}

    }

}
