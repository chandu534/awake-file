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
package org.kawanfw.file.test.parms;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.kawanfw.commons.util.FrameworkFileUtil;
import org.kawanfw.commons.util.FrameworkSystemUtil;
import org.kawanfw.commons.util.Tag;
import org.kawanfw.file.test.util.MessageDisplayer;

/**
 * Defines the RemoteSession parameters to test Awake and get a session to a remote server.
 * platform.
 * 
 * @author Nicolas de Pomereu
 *
 */
public class TestParms {
             
    /** if true, a proxy will be used */
    public static boolean USE_PROXY = true;
    
    /** if true, request will be encrypted */
    public static boolean USE_ENCRYPTION_PASSWORD = true;
    
    public static boolean TEST_BIG_FILES = false;
    
    /** If true, The test will run in loop mode (to detect memory leaks) */
    public static boolean LOOP_MODE = false;
       
    public static boolean COMPRESSION_ON = false;
    
    /** Remote parameters */    
    public static String AWAKE_URL = "http://localhost:8080/awake-file/ServerFileManager";
    public static String REMOTE_USER = "username";
    public static String REMOTE_USER_2 = "username_2";
    public static String REMOTE_PASSWORD = "password";

    /** Directories to use */
    public static final String MYDIR1 = "my_rép1";    
    public static final String MYDIR2 = "my_rép2";
    public static final String MYDIR3 = "my_rép3";
    public static final String MYDIR4 = "РAССЫЛОК";
    
    public static String TULIPS = "Tulips.jpg";
    public static String KOALA = "Koala.jpg";
    public static String RUSSIAN = "РAССЫЛОК.txt";    
    
    //public static String BLOB_FILE_1 = TULIPS;
    //public static String BLOB_FILE_2 = KOALA;
    
    public static String BLOB_FILE_TULIPS = TULIPS;
    public static String BLOB_FILE_KOALA = KOALA;
    public static String BLOB_FILE_RUSSIAN= RUSSIAN;
    
    // Android Environment Class  for file manipulation
    private static Class<?> environmentClass;

    // Method Environment Class Android class for file manipulation
    private static Method getExternalStorageDirectoryMethod;

    
    /**
     * @param imageFileName
     *            the name of the image file to use
     * @return The first image file to use as blob for insert and select
     */
    public static File getFileFromUserHome(String imageFileName) {
		
	String imageFileStr = FrameworkFileUtil.getUserHome() + File.separator
		+ "kawanfw-test" + File.separator + imageFileName;

	if (FrameworkSystemUtil.isAndroid()) {
	    
	    // Android code made by reflection to avoid compilation issue
	    // File envStore = Environment.getExternalStorageDirectory();
	    // imageFileStr = envStore.getPath() + File.separator + imageFileName;
	    // MessageDisplayer.display("imageFileStr: " + imageFileStr);

	    try {
		environmentClass = Class.forName("android.os.Environment");
		getExternalStorageDirectoryMethod = environmentClass
			.getMethod("getExternalStorageDirectory");	
		
		File envStore = (File)getExternalStorageDirectoryMethod.invoke(environmentClass);
		imageFileStr = envStore.getPath() + File.separator + imageFileName;
		MessageDisplayer.display("imageFileStr: " + imageFileStr);		
		
	    } catch (Exception e) {
		throw new IllegalArgumentException(Tag.PRODUCT_PRODUCT_FAIL
			+ " Impossible to load method environmentClass. Cause: "
			+ e.toString());
	    }

	}
	
	if (! new File(imageFileStr).exists()) {
	    MessageDisplayer.display(Tag.PRODUCT_WARNING + " Blob/Clob file does not exists: " + imageFileStr);
	}
	
	return new File(imageFileStr);
    }

    public static File FILE_CONFIGURATOR_TXT = new File("c:\\.kawansoft\\FileConfigurator.txt");


    /**
     * Build the server root file from info dynamically stored in FILE_CONFIGURATOR_TXT
     * @return
     */
    public static String getServerRootFromFile() {
    
        try {
            String content = FileUtils.readFileToString(FILE_CONFIGURATOR_TXT);
            
            if (content == null || content.equals("null")) {
        	return "c:\\";
            }
            
            String root = StringUtils.substringBefore(content, "!");
            String withUsername = StringUtils.substringAfter(content, "!");
            
            if (withUsername.equals("true")) {
        	return root + File.separator + "username" + File.separator;
            }
            else {
        	return root + File.separator;
            }
            
            
        } catch (IOException e) {
            
            e.printStackTrace();
        }
        
        return null;
    }

    //public static String C_AWAKE_SERVER_ROOT_FILE_USERNAME = "C:\\.awake-server-root-file\\username\\";
    public static String C_AWAKE_SERVER_ROOT_FILE_USERNAME = getServerRootFromFile();

    
}
