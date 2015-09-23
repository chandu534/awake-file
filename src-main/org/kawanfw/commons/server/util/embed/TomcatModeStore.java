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
package org.kawanfw.commons.server.util.embed;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;


/**
 * Class that store the mode in which we run Tomcat: nativ or embedded. <br>
 * Includes also the data source set/get (must be in Awake FILE because used by
 * DefaultCommonsConfigurator
 * 
 * @author Nicolas de Pomereu
 * 
 */
public class TomcatModeStore {

    /** Value that says we are in stand alone Server with Tomcat Embed */
    private static boolean tomcatEmbedded = false;
    
    /** The (Servlet Name, DataSource) Map */
    private static Map<String, DataSource> dataSourceMap = new HashMap<String, DataSource>();
    
    /** Says if the framework is SQL */    
    private static boolean frameworkSql = false;
    
    /**
     * no instantiation
     */
    private TomcatModeStore() {

    }

    /**
     * @return the tomcatEmbedded
     */
    public static boolean isTomcatEmbedded() {
        return tomcatEmbedded;
    }

    /**
     * @param tomcatEmbedded the tomcatEmbedded to set
     */
    public static void setTomcatEmbedded(boolean tomcatEmbedded) {
        TomcatModeStore.tomcatEmbedded = tomcatEmbedded;
    }
        
        
    /**
     * Says if framework in use is SQL framework
     * @return true if framework is SQL framework 
     */
    public static boolean isFrameworkSql() {
        return frameworkSql;
    }

    /**
     * Says to DefaultCommonsConfigurator if framework in use is SQL framework
     * @param frameworkSql true if framework is SQL framework 
     */
    public static void setFrameworkSql(boolean frameworkSql) {
        TomcatModeStore.frameworkSql = frameworkSql;
    }

    /**
     * Stores a DataSource for a specified servlet.
     * @param servletName the servlet to store the DataSource for
     * @param dataSource the dataSource to set.
     */
    public static void setDataSource(String servletName, DataSource dataSource) {
       dataSourceMap.put(servletName, dataSource);
    }    
    
    /**
     * Returns the DataSource associated to a servlet.
     * @param servletName the servlet to store the DataSource for
     * @return the dataSource	corresponding to the index
     */
    public static DataSource getDataSource(String servletName) {
        return dataSourceMap.get(servletName);
    }    
 
          
}
