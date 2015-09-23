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
package org.kawanfw.file.examples.snippets;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.kawanfw.commons.api.client.InvalidLoginException;
import org.kawanfw.commons.api.client.RemoteException;
import org.kawanfw.file.api.client.RemoteFile;
import org.kawanfw.file.api.client.RemoteOutputStream;
import org.kawanfw.file.api.client.RemoteSession;

/**
 * @author Nicolas de Pomereu
 *
 */
public class AwakeFileSnippets {

    /**
     * 
     */
    public AwakeFileSnippets() {

    }

    /**
     * @param args
     */
    public static void uploadExample() throws Exception {

	// Define URL of the path to the ServerFileManager servlet
	String url = "https://www.acme.org/ServerFileManager";

	// The login info for strong authentication on server side:
	String username = "myUsername";
	char[] password = { 'm', 'y', 'P', 'a', 's', 's', 'w', 'o', 'r', 'd' };

	// Establish a session with the remote server
	RemoteSession remoteSession = new RemoteSession(url, username, password);

	// OK: upload a file
	remoteSession.upload(new File("c:\\myFile.txt"),
		"/home/mylogin/myFile.txt");
    }

    /**
     * @param url
     * @param username
     * @param password
     * @param args
     */
    public static void RpcExample(String url, String username, char[] password)
	    throws Exception {

	// Establish a session with the remote server
	RemoteSession remoteSession = new RemoteSession(url, username, password);

	// OK: call the add(int a, int b) remote method that returns a + b:
	String result = remoteSession.call(
		"org.kawanfw.examples.Calculator.add", 33, 44);
	System.out.println("Calculator Result: " + result);

    }

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {

    }

    /**
     * @throws MalformedURLException
     * @throws UnknownHostException
     * @throws ConnectException
     * @throws SocketException
     * @throws InvalidLoginException
     * @throws RemoteException
     * @throws IOException
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws FileNotFoundException
     */
    @SuppressWarnings("unused")
    public static void remoteFileExample() throws MalformedURLException,
	    UnknownHostException, ConnectException, SocketException,
	    InvalidLoginException, RemoteException, IOException,
	    SecurityException, IllegalArgumentException, FileNotFoundException {

	// Define URL of the path to the ServerFileManager servlet
	String url = "https://www.acme.org/ServerFileManager";

	// The login info for strong authentication on server side
	String username = "myUsername";
	char[] password = { 'm', 'y', 'P', 'a', 's', 's', 'w', 'o', 'r', 'd' };

	// Establish a session with the remote server
	RemoteSession remoteSession = new RemoteSession(url, username, password);

	// Create a new RemoteFile that maps a file on remote server
	RemoteFile remoteFile = new RemoteFile(remoteSession, "/Koala.jpg");

	// RemoteFile methods have the same names, signatures and behaviors
	// as java.io.File methods: a RemoteFile method is a File method that
	// is executed on the remote host
	if (remoteFile.exists()) {
	    System.out.println(remoteFile.getName() + " length  : "
		    + remoteFile.length());
	    System.out.println(remoteFile.getName() + " canWrite: "
		    + remoteFile.canWrite());
	}

	// List files on our remote root directory
	remoteFile = new RemoteFile(remoteSession, "/");

	RemoteFile[] files = remoteFile.listFiles();
	for (RemoteFile file : files) {
	    System.out.println("Remote file: " + file);
	}

	// List all text files in out root directory
	// using an Apache Commons IO 2.4 FileFiter
	FileFilter fileFilter = new SuffixFileFilter(".txt");

	files = remoteFile.listFiles(fileFilter);
	for (RemoteFile file : files) {
	    System.out.println("Remote text file: " + file);
	}

    }

    /**
     * @throws MalformedURLException
     * @throws UnknownHostException
     * @throws ConnectException
     * @throws SocketException
     * @throws InvalidLoginException
     * @throws RemoteException
     * @throws IOException
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws FileNotFoundException
     */
    public static void uploadFileVersion1() throws MalformedURLException,
	    UnknownHostException, ConnectException, SocketException,
	    InvalidLoginException, RemoteException, IOException,
	    SecurityException, IllegalArgumentException, FileNotFoundException {

	// Define URL of the path to the ServerFileManager servlet
	String url = "https://www.acme.org/ServerFileManager";

	// The login info for strong authentication on server side:
	String username = "myUsername";
	char[] password = { 'm', 'y', 'P', 'a', 's', 's', 'w', 'o', 'r', 'd' };

	// Establish a session with the remote server
	RemoteSession remoteSession = new RemoteSession(url, username, password);

	// Upload a file using built-in RemoteSession.upload()
	File file = new File("C:\\Users\\Mike\\Koala.jpg");
	String remotePath = "/Koala.jpg";

	remoteSession.upload(file, remotePath);

    }

    /**
     * @throws MalformedURLException
     * @throws UnknownHostException
     * @throws ConnectException
     * @throws SocketException
     * @throws InvalidLoginException
     * @throws RemoteException
     * @throws IOException
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws FileNotFoundException
     */
    public static void uploadFileVersion2() throws MalformedURLException,
	    UnknownHostException, ConnectException, SocketException,
	    InvalidLoginException, RemoteException, IOException,
	    SecurityException, IllegalArgumentException, FileNotFoundException {

	// Define URL of the path to the ServerFileManager servlet
	String url = "https://www.acme.org/ServerFileManager";

	// The login info for strong authentication on server side:
	String username = "myUsername";
	char[] password = { 'm', 'y', 'P', 'a', 's', 's', 'w', 'o', 'r', 'd' };

	// Establish a session with the remote server
	RemoteSession remoteSession = new RemoteSession(url, username, password);

	// Upload a file using a RemoteOutputStream
	File file = new File("C:\\Users\\Mike\\Koala.jpg");
	String remotePath = "/Koala.jpg";

	InputStream in = null;
	OutputStream out = null;

	try {
	    // Get an InputStream from our local file
	    in = new FileInputStream(file);

	    // Create an OutputStream that maps a remote file on the host
	    out = new RemoteOutputStream(remoteSession, remotePath,
		    file.length());

	    // Create the remote file reading the InpuStream and writing
	    // on the OutputStream
	    byte[] buffer = new byte[1024 * 4];
	    int n = 0;
	    while ((n = in.read(buffer)) != -1) {
		out.write(buffer, 0, n);
	    }
	} finally {
	    if (in != null)
		in.close();
	    if (out != null)
		out.close();
	}
    }

}
