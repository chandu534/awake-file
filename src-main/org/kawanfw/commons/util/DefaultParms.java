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
package org.kawanfw.commons.util;
/**
 * @author Nicolas de Pomereu
 * 
 *         Store the default values of some parameters used in Kawansoft
 *         frameworks
 *
 */

public class DefaultParms {
    
    /** Do not instantiate */
    protected DefaultParms() {
    }

    /** Defines one kilobyte */
    public static final int KB = 1024;
    /** Defines one megabyte */
    public static final int MB = 1024 * KB;
    /**
     * The default maximum authorized length for a string for upload or download
     */
    public static final int DEFAULT_MAX_LENGTH_FOR_STRING = 2 * MB;
    /** The default buffer size when uploading a file */
    public static final int DEFAULT_UPLOAD_BUFFER_SIZE = 20 * KB;
    /** The default Buffer size for download and copy */
    public static final int DEFAULT_DOWNLOAD_BUFFER_SIZE = 20 * KB;
    /** The default behavior for html encoding */
    public static final boolean DEFAULT_HTML_ENCODING_ON = true;

    /** The default acceptance for self signed SSL certificates */
    public static final boolean ACCEPT_ALL_SSL_CERTIFICATES = false;

    public static final long DEFAULT_DOWNLOAD_CHUNK_LENGTH = 10 * MB;
    public static final long DEFAULT_UPLOAD_CHUNK_LENGTH = 3 * MB;

    public static final int DEFAULT_RETRY_COUNT = 3;
    
    /** Color used by servlet display in all KwanSoft Frameworks */
    public static final String KAWANSOFT_COLOR = "E7403E";


}
