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
package org.kawanfw.file.test.api.client.legacy;

import org.junit.Assert;
import org.junit.Test;
import org.kawanfw.commons.api.client.RemoteException;
import org.kawanfw.file.api.client.FileSession;
import org.kawanfw.file.test.parms.TestParms;
import org.kawanfw.file.test.util.MessageDisplayer;

/**
 * 
 * TestReload that a re mote method is callable.
 * 
 * @author Nicolas de Pomereu
 * 
 */
public class CallTest {

    public static void main(String[] args) throws Exception {
	new CallTest().test();
    }

    @Test
    public void test() throws Exception {

	FileSession fileSession = new FileSession(
		TestParms.AWAKE_URL, TestParms.REMOTE_USER,
		TestParms.REMOTE_PASSWORD.toCharArray());

	test(fileSession);

    }

    /**
     * @param fileSession
     *            the Awake FILE Session
     * @throws Exception
     */
    public void test(FileSession fileSession) throws Exception {

	int a = 33;
	int b = 44;

	String resultStr = null;
	int result = -1;

	MessageDisplayer.display("");
	MessageDisplayer.display("Testing call()...");
	resultStr = fileSession
		.call("org.kawanfw.file.test.api.server.CalculatorNotAuthenticated.add",
			a, b);
	result = Integer.parseInt(resultStr);

	MessageDisplayer
		.display("CalculatorNotAuthenticated Result: " + result);

	Assert.assertEquals("a + b = 77", a + b, result);

	resultStr = fileSession.call(
		"org.kawanfw.file.test.api.server.Calculator.add", a, b);
	result = Integer.parseInt(resultStr);

	Assert.assertEquals("a + b must be result", a + b, result);
	MessageDisplayer.display("Calculator Result: " + result);


	// Testing a method not allowed that will be refused by our
	// TestFileConfigurator.allowCallAfterAnalysis method:

	String exceptionMessage = "";

	try {
	    resultStr = fileSession
		    .call("org.kawanfw.file.test.api.server.CalculatorNotAllowed.add",
			    a, b);

	    // This line must not be reached!
	    Assert.assertEquals("line not to be reached.", true, false);

	} catch (Exception e) {
	    exceptionMessage = e.getMessage();
	    System.out
		    .println("org.kawanfw.file.test.api.server.CalculatorNotAllowed.add exception: "
			    + e.getMessage());
	}

	Assert.assertEquals(
		"exceptionMessage must contains org.kawanfw.file.test.api.server.CalculatorNotAllowed.add",
		true,
		exceptionMessage
			.contains("org.kawanfw.file.test.api.server.CalculatorNotAllowed.add"));

    }

    @Test(expected = RemoteException.class)
    public void test2() throws Exception {

	int a = 33;
	int b = 44;

	FileSession fileSession = new FileSession(
		TestParms.AWAKE_URL, TestParms.REMOTE_USER,
		TestParms.REMOTE_PASSWORD.toCharArray());

	String resultStr = fileSession.call(
		"org.kawanfw.test.api.server.CalculatorNotCallable.add", a, b);
	int result = Integer.parseInt(resultStr);

	MessageDisplayer.display("Calculator Result: " + result);

	Assert.assertEquals(a + b, result);
    }

}
