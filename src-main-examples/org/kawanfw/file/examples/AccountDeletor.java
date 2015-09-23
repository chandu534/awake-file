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
package org.kawanfw.file.examples;

import org.kawanfw.file.api.server.ClientCallable;

/**
 * 
 * Exemple of a server class callable by the client side. The client must be
 * authenticated.
 * 
 * @author Nicolas de Pomereu
 * 
 */
public class AccountDeletor implements ClientCallable {

    /**
     * Constructor with no parameters (required by RPC mechanism)
     */
    public AccountDeletor() {
    }

    /**
     * Deletes an account.
     * 
     * @param username
     *            the username of the account to delete
     * @return true if the account is deleted
     */
    public boolean deleteAccount(String username) {

	// code to delete the account.
	// ...

	return true;
    }
}
