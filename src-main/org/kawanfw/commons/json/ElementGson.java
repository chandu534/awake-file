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
package org.kawanfw.commons.json;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * @author Nicolas de Pomereu
 * 
 *         Class to format an instance to a JSON String and vice versa using
 *         Gson
 */
public class ElementGson<E> {

    /**
     * Protected
     */
    protected ElementGson() {

    }

    /**
     * Format an Element into a JSON String
     * 
     * @param element the instance to format
     * @return	the JSON string
     */
    public static<E> String toJson(E element) {

	Gson gson = new Gson();
	Type type = new TypeToken<E>() {
	}.getType();
	String jsonString = gson.toJson(element, type);

	return jsonString;
    }

    /**
     * Format a JSON String back to an instance
     * 
     * @param jsonString 	The JSON string containing the HttpProxy
     * @return	the instance rebuilt from JSON format  
     */
    public static<E> E fromJson(String jsonString) {
	Gson gson = new Gson();
	Type type = new TypeToken<E>() {
	}.getType();
	
	E element = gson.fromJson(jsonString, type);
	return element;
    }

}
