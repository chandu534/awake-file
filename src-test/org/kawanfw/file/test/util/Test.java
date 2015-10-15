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
package org.kawanfw.file.test.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.kawanfw.file.api.util.client.JarReader;
import org.kawanfw.file.reflection.ClassFileLocatorNew;

/**
 * @author Nicolas de Pomereu
 *
 */
public class Test {

    String title = null;
    String url = null;
    String cover = null;
    
    @Override
    public String toString() {
	return "Test [title=" + title + ", url=" + url + ", cover=" + cover
		+ "]";
    }

    /**
     * 
     */
    public Test() {
	
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
	
	URL url = new URL("https://www.aceql.com");
	HttpURLConnection con = (HttpURLConnection) url.openConnection();
	con.setRequestProperty("Accept-Encoding", "gzip");
	System.out.println("Length : " + con.getContentLength());

	Reader reader = new InputStreamReader(con.getInputStream());
	while (true) {
	    int ch = reader.read();
	    if (ch == -1) {
		break;
	    }
	    System.out.print((char) ch);
	} 
    }

    /**
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static void ClassFileLocatorNewTest() throws ClassNotFoundException,
	    IOException, FileNotFoundException {
	String classname = "org.kawanfw.file.test.run.filter.StaticFilterTest$ThePublicStaticFileFilter";
	Class<?> clazz = Class.forName(classname);
	ClassFileLocatorNew classFileLocatorNew = new ClassFileLocatorNew(clazz, null);
	byte [] byteArray = classFileLocatorNew.extractClassFileBytecode();
	System.out.println("byteArray.length: " + byteArray.length);
	
	File file = new File("I:\\_dev_awake\\awake-file-3.0\\lib\\kawanfw-filters.jar");
	byte [] b = JarReader.extractClassFileBytecode(new FileInputStream(file), classname);
	System.out.println("b.length: " + b.length);
	
	file = new File("I:\\_dev_awake\\awake-file-android-test\\assets\\kawansoft\\kawanfw-filters.jar");
	b = JarReader.extractClassFileBytecode(new FileInputStream(file), classname);
	System.out.println("b.length: " + b.length);
    }

}
