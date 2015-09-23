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
package org.kawanfw.commons.jdbc.abstracts;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * ResultSetMetaData Wrapper. <br>
 * Implements all the ResultSetMetaData methods. Usage is exactly the same as a
 * ResultSetMetaData.
 * 
 */

public abstract class AbstractResultSetMetaData implements ResultSetMetaData {

    /** Flag that says the caller is ConnectionHttp */
    private boolean isConnectionHttp = false;

    /**
     * Constructor
     */
    public AbstractResultSetMetaData() {
	this.isConnectionHttp = true;
    }

    /**
     * Constructor
     * 
     * @param isConnectionHttp
     *            Flag that says the caller is ConnectionHttp
     */
    public AbstractResultSetMetaData(boolean isConnectionHttp) {
	this.isConnectionHttp = isConnectionHttp;
    }

    /**
     * Will throw a SQL Exception if callin method is not authorized
     **/
    protected void verifyCallAuthorization(String methodName)
	    throws SQLException {

	if (isConnectionHttp) {
	    throw new SQLException(
		    AbstractConnection.FEATURE_NOT_SUPPORTED_IN_THIS_VERSION
			    + methodName);
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.Wrapper#unwrap(java.lang.Class)
     */
    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
	String methodName = new Object() {
	}.getClass().getEnclosingMethod().getName();
	verifyCallAuthorization(methodName);
	return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
     */
    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
	String methodName = new Object() {
	}.getClass().getEnclosingMethod().getName();
	verifyCallAuthorization(methodName);
	return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSetMetaData#getColumnCount()
     */
    @Override
    public int getColumnCount() throws SQLException {
	String methodName = new Object() {
	}.getClass().getEnclosingMethod().getName();
	verifyCallAuthorization(methodName);
	return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSetMetaData#isAutoIncrement(int)
     */
    @Override
    public boolean isAutoIncrement(int column) throws SQLException {
	String methodName = new Object() {
	}.getClass().getEnclosingMethod().getName();
	verifyCallAuthorization(methodName);
	return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSetMetaData#isCaseSensitive(int)
     */
    @Override
    public boolean isCaseSensitive(int column) throws SQLException {
	String methodName = new Object() {
	}.getClass().getEnclosingMethod().getName();
	verifyCallAuthorization(methodName);
	return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSetMetaData#isSearchable(int)
     */
    @Override
    public boolean isSearchable(int column) throws SQLException {
	String methodName = new Object() {
	}.getClass().getEnclosingMethod().getName();
	verifyCallAuthorization(methodName);
	return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSetMetaData#isCurrency(int)
     */
    @Override
    public boolean isCurrency(int column) throws SQLException {
	String methodName = new Object() {
	}.getClass().getEnclosingMethod().getName();
	verifyCallAuthorization(methodName);
	return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSetMetaData#isNullable(int)
     */
    @Override
    public int isNullable(int column) throws SQLException {
	String methodName = new Object() {
	}.getClass().getEnclosingMethod().getName();
	verifyCallAuthorization(methodName);
	return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSetMetaData#isSigned(int)
     */
    @Override
    public boolean isSigned(int column) throws SQLException {
	String methodName = new Object() {
	}.getClass().getEnclosingMethod().getName();
	verifyCallAuthorization(methodName);
	return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSetMetaData#getColumnDisplaySize(int)
     */
    @Override
    public int getColumnDisplaySize(int column) throws SQLException {
	String methodName = new Object() {
	}.getClass().getEnclosingMethod().getName();
	verifyCallAuthorization(methodName);
	return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSetMetaData#getColumnLabel(int)
     */
    @Override
    public String getColumnLabel(int column) throws SQLException {
	String methodName = new Object() {
	}.getClass().getEnclosingMethod().getName();
	verifyCallAuthorization(methodName);
	return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSetMetaData#getColumnName(int)
     */
    @Override
    public String getColumnName(int column) throws SQLException {
	String methodName = new Object() {
	}.getClass().getEnclosingMethod().getName();
	verifyCallAuthorization(methodName);
	return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSetMetaData#getSchemaName(int)
     */
    @Override
    public String getSchemaName(int column) throws SQLException {
	String methodName = new Object() {
	}.getClass().getEnclosingMethod().getName();
	verifyCallAuthorization(methodName);
	return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSetMetaData#getPrecision(int)
     */
    @Override
    public int getPrecision(int column) throws SQLException {
	String methodName = new Object() {
	}.getClass().getEnclosingMethod().getName();
	verifyCallAuthorization(methodName);
	return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSetMetaData#getScale(int)
     */
    @Override
    public int getScale(int column) throws SQLException {
	String methodName = new Object() {
	}.getClass().getEnclosingMethod().getName();
	verifyCallAuthorization(methodName);
	return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSetMetaData#getTableName(int)
     */
    @Override
    public String getTableName(int column) throws SQLException {
	String methodName = new Object() {
	}.getClass().getEnclosingMethod().getName();
	verifyCallAuthorization(methodName);
	return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSetMetaData#getCatalogName(int)
     */
    @Override
    public String getCatalogName(int column) throws SQLException {
	String methodName = new Object() {
	}.getClass().getEnclosingMethod().getName();
	verifyCallAuthorization(methodName);
	return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSetMetaData#getColumnType(int)
     */
    @Override
    public int getColumnType(int column) throws SQLException {
	String methodName = new Object() {
	}.getClass().getEnclosingMethod().getName();
	verifyCallAuthorization(methodName);
	return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSetMetaData#getColumnTypeName(int)
     */
    @Override
    public String getColumnTypeName(int column) throws SQLException {
	String methodName = new Object() {
	}.getClass().getEnclosingMethod().getName();
	verifyCallAuthorization(methodName);
	return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSetMetaData#isReadOnly(int)
     */
    @Override
    public boolean isReadOnly(int column) throws SQLException {
	String methodName = new Object() {
	}.getClass().getEnclosingMethod().getName();
	verifyCallAuthorization(methodName);
	return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSetMetaData#isWritable(int)
     */
    @Override
    public boolean isWritable(int column) throws SQLException {
	String methodName = new Object() {
	}.getClass().getEnclosingMethod().getName();
	verifyCallAuthorization(methodName);
	return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSetMetaData#isDefinitelyWritable(int)
     */
    @Override
    public boolean isDefinitelyWritable(int column) throws SQLException {
	String methodName = new Object() {
	}.getClass().getEnclosingMethod().getName();
	verifyCallAuthorization(methodName);
	return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSetMetaData#getColumnClassName(int)
     */
    @Override
    public String getColumnClassName(int column) throws SQLException {
	String methodName = new Object() {
	}.getClass().getEnclosingMethod().getName();
	verifyCallAuthorization(methodName);
	return null;
    }

}
