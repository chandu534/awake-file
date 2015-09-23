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
package org.kawanfw.file.util.android;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.kawanfw.commons.util.Tag;
import org.kawanfw.file.api.util.client.JarReader;

import android.content.Context;
import android.content.res.AssetManager;

/**
 * 
 * Android utilities that must be compiled with Android.
 * 
 * @author Nicolas de Pomereu
 * @since 3.0
 */
public class AndroidUtil {

    /** Universal and clean line separator */
    public static String CR_LF = System.getProperty("line.separator");
    
    /**
     * Protected class
     */
    protected AndroidUtil() {

    }
    	    
    
    /**
     * Returns the bytecode of a class store in a jar in assets/kawansoft. The
     * Android Context is retrieved statically with
     * {@code KawanfwApp.getContext()} The XML Android Manifest must contain
     * following entry in &lt;application&gt; section
     * 		android:name="org.kawanfw.file.android.KawanfwApp"
     * 
     * @param classname
     *            the classname to search for
     * @return the bytecode of a the Class
     * 
     * @throws FileNotFoundException
     *             if no jar are found assets/kawansoft or if the class is not
     *             found in the jars
     * @throws IOException
     *             if any I/O error
     */
    public static byte[] extractClassFileBytecode(String classname)
	    throws IOException, FileNotFoundException {
	Context context = KawanfwApp.getContext();
	
	if (context == null) {
	    throw new NullPointerException(Tag.PRODUCT + " Impossible to get a Context "
	    	+ "from org.kawanfw.file.util.android.KawanfwApp Application. " + CR_LF 
	    	+ "Please verify that a field android:name=\"org.kawanfw.file.util.android.KawanfwApp\"" + CR_LF 
	    	+ "is defined in the <application> section of your XML Manifest file.");
	}
	
	return extractClassFileBytecode(context, classname);
    }

    /**
     * Return  the bytecode of a class store in a jar in assets/kawansoft.
     * 
     * @param context		the Android Context
     * @param classname		the classname to search for
     * @return
     * 
     * @throws FileNotFoundException	if no jar are found assets/kawansoft
     * @throws IOException	if any I/O error
     */
    public static byte []  extractClassFileBytecode(Context context, String classname) throws IOException, FileNotFoundException {
	
	if (context == null) {
	    throw new IllegalArgumentException("context is null!");
	}
	
	if (classname == null) {
	    throw new IllegalArgumentException("classname is null!");
	}
	
	if (classname.isEmpty()) {
	    throw new IllegalArgumentException("classname is empty!");
	}
	
        AssetManager assetManager= context.getAssets();
        
        String [] files  = assetManager.list("kawansoft");
        if (files == null || files.length == 0) {
            throw new FileNotFoundException(Tag.PRODUCT + " No JAR files found in assets/kawansoft path!");
        }
            
	for (String file : files) {
	    
	    if (! file.endsWith(".jar")) {
		continue;
	    }
	    
	    InputStream in = null;
	    in = assetManager.open("kawansoft/" + file);
	    byte [] byteArray = JarReader.extractClassFileBytecode(in, classname);
	    
	    if (byteArray != null) {
		return byteArray;
	    }
	}
	
	throw new FileNotFoundException(Tag.PRODUCT + " class not found in assets/kawansoft jar files: " + classname);
	
    }
    
//    /**
//    * Method to get all classes in the Android JVM classpath.
//    * (Future usage).
//    * 
//    * @param context
//    * @param packageName
//    * @return
//    * @throws IOException
//    * @throws ClassNotFoundException
//    */
//   public static Set<Class<?>> getClasspathClasses(Context context,
//	    String packageName) throws IOException, ClassNotFoundException {
//	Set<Class<?>> classes = new HashSet<Class<?>>();
//	DexFile dex = new DexFile(context.getApplicationInfo().sourceDir);
//	ClassLoader classLoader = Thread.currentThread()
//		.getContextClassLoader();
//	Enumeration<String> entries = dex.entries();
//	while (entries.hasMoreElements()) {
//	    String entry = entries.nextElement();
//	    if (entry.toLowerCase().startsWith(packageName.toLowerCase()))
//		classes.add(classLoader.loadClass(entry));
//	}
//	return classes;
//   }
    
//  /**
//  * Returns the input stream array corresponding to the jar files located in asset/kawansoft directory.
//  * The input streams must be closed after reading by caller.
//  * <p>
//  * We keep this Android method minimalist so that maximum code is done in main project for easy setup and debug.
//  * 
//  * @return the input stream array corresponding to the jar files located in asset/kawansoft directory. Empty array if no jars found.
//  * @throws NullPointerException if KawanfwApp Application Context can't be found and KawanfwApp.getContext() returns nulls
//  * @throws IOException in any I/O exception occurs
//  * 
//  */
    
// public static List<InputStream> getAssetsJarStreams() throws IOException {
//	
//	Context context = KawanfwApp.getContext();
//	if (context == null) {
//	    throw new NullPointerException(Tag.PRODUCT + " Impossible to get a Context "
//	    	+ "from org.kawanfw.file.api.util.client2.KawanfwApp Application. " + CR_LF 
//	    	+ "Please verify that an android:name=\"org.kawanfw.file.api.util.client2.KawanfwApp\"" + CR_LF 
//	    	+ "field is defined in <application> section in your XML manifest file");
//	}
//	
//     AssetManager assetManager= context.getAssets();
//     String [] files  = assetManager.list("kawansoft");
//	
//     List<InputStream> inputStreams = new ArrayList<InputStream>();
//     if (files == null) {
//         return inputStreams;
//     }
//     
//	for (String file : files) {
//	    
//	    if (! file.endsWith(".jar")) {
//		continue;
//	    }
//	    
//	    InputStream in = assetManager.open("kawansoft/" + file);
//	    inputStreams.add(in);
//	}
//	
//	return inputStreams;
//	
// }

}
