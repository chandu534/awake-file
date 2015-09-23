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
package android.content.res;

import java.io.IOException;
import java.io.InputStream;

/**
 * 
 * Android emulation for compilation of dedicated Android classes 
 * located in org.kawanfw.file.util.android package.
 * 
 * @author Nicolas de Pomereu
 * @since 3.0
 */
public class AssetManager {

    /**
     * Return a String array of all the assets at the given path.
     * 
     * @param path A relative path within the assets, i.e., "docs/home.html".
     * 
     * @return String[] Array of strings, one for each asset.  These file
     *         names are relative to 'path'.  You can open the file by
     *         concatenating 'path' and a name in the returned string (via
     *         File) and passing that to open().
     * 
     * @see #open
     */
    public native final String[] list(String path)
        throws IOException;
    
    /**
     * Open an asset using ACCESS_STREAMING mode.  This provides access to
     * files that have been bundled with an application as assets -- that is,
     * files placed in to the "assets" directory.
     * 
     * @param fileName The name of the asset to open.  This name can be
     *                 hierarchical.
     * 
     * @see #open(String, int)
     * @see #list
     */
    public native final InputStream open(String fileName) throws IOException;
    
}
