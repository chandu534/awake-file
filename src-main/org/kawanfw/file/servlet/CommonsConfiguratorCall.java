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
package org.kawanfw.file.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import org.kawanfw.commons.api.server.CommonsConfigurator;

/**
 * 
 * Calls CommonsConfigurator methods using reflection
 * 
 * @author Nicolas de Pomereu
 * 
 */
public class CommonsConfiguratorCall {

    /**
     * Return the result of forceSecureHttp method of CommonsConfigurator
     * 
     * @param CommonsConfigurator
     *            the CommonsConfigurator instance
     * @return true if https must be forced, else false
     * 
     * @throws SQLException
     *             if any Exception occurs, it is wrapped into an SQLException
     */
    public static boolean forceSecureHttp(
	    CommonsConfigurator commonsConfigurator)
	    throws SQLException {
	return commonsConfigurator.forceSecureHttp();
    }

    /**
     * Return the result of getBannedUsernames method of
     * CommonsConfigurator
     * 
     * @param CommonsConfigurator
     *            the CommonsConfigurator instance
     * @return the list of banned usernames else empty list
     * 
     * @throws SQLException
     *             if any Exception occurs, it is wrapped into an SQLException
     */
    public static Set<String> getBannedUsernames(
	    CommonsConfigurator commonsConfigurator)
	    throws IOException, SQLException {
	return commonsConfigurator.getBannedUsernames();
    }

    /**
     * Return the result of getIPsBlacklist method of CommonsConfigurator
     * 
     * @param CommonsConfigurator
     *            the CommonsConfigurator instance
     * @return the list of blacklisted IPs else empty list
     * 
     * @throws SQLException
     *             if any Exception occurs, it is wrapped into an SQLException
     */

    public static List<String> getIPsBlacklist(
	    CommonsConfigurator commonsConfigurator)
	    throws IOException, SQLException {
	
	List<String> ipsBlacklist = commonsConfigurator.getIPsBlacklist();	
	return ipsBlacklist;
    }

    /**
     * Return the result of getIPsWhitelist method of CommonsConfigurator
     * 
     * @param CommonsConfigurator
     *            the CommonsConfigurator instance
     * @return the list of whilelisted IPs else empty list
     * 
     * @throws SQLException
     *             if any Exception occurs, it is wrapped into an SQLException
     */

    public static List<String> getIPsWhitelist(
	    CommonsConfigurator commonsConfigurator)
	    throws IOException, SQLException {
	List<String> ipsWhitelist = commonsConfigurator.getIPsWhitelist();
	return ipsWhitelist;
    }

    /**
     * Return the result of computeAuthToken(String username) method of
     * CommonsConfigurator
     * 
     * @param CommonsConfigurator
     *            the CommonsConfigurator instance
     * @return the result of computeAuthToken
     * 
     * @throws SQLException
     *             if any Exception occurs, it is wrapped into an SQLException
     */
    public static String computeAuthToken(
	    CommonsConfigurator commonsConfigurator, String username)
	    throws Exception {
	return commonsConfigurator.computeAuthToken(username);
    }

    /**
     * Return the result of computeAuthToken(String username) method of
     * CommonsConfigurator
     * 
     * @param CommonsConfigurator
     *            the CommonsConfigurator instance
     * @return the list of banned IPs else null
     * 
     * @throws SQLException
     *             if any Exception occurs, it is wrapped into an SQLException
     */
    public static char[] getEncryptionPassword(
	    CommonsConfigurator commonsConfigurator)
	    throws SQLException {
	return commonsConfigurator.getEncryptionPassword();
    }

}
