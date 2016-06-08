/*
 * Copyright (c) 2016-2016, fastquery.org and/or its affiliates. All rights reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * For more information, please see http://www.fastquery.org/.
 * 
 */

package org.fastquery.core;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.fastquery.dsm.FQueryProperties;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class Session {
	
    private static ThreadLocal<Connection> connection = new ThreadLocal<>();  

    private Session(){
    }
    
    /**
     * 根据 packageName 或 dataSourceName 或类的完整名称获取当前连接
     * @param packageName
     * @param dataSourceName
     * @return
     */
    public static Connection getConnection(String dataSourceName,String packageName,String className)  
    {  
    	// 数据源的名称
    	String dsn;
    	// 根据dataSourceName 查
    	DataSource dataSource = FQueryProperties.findDataSource(dataSourceName);
    	if(dataSource == null) {
    		
    		// 根据 packageName 查
    		// 根据basePackage 查找出数据源的名字
    		dsn = FQueryProperties.findDataSourceName(packageName);
    		// 在根据数据源的名字查寻出数据库对象
    		dataSource=FQueryProperties.findDataSource(dsn);
    		
        	if(dataSource == null) {
        		
        		// 根据 className 查
        		dsn = FQueryProperties.findDataSourceName(className);
        		dataSource=FQueryProperties.findDataSource(dsn);
            	
            	// 截至这儿如果 dataSource 还是为null
            	if(dataSource == null) {
            		throw new  ExceptionInInitializerError(String.format("根据dataSourceName=%s,packageName=%s,className=%s这三个值没有找到数据源.",dataSourceName,packageName,className));
            	}
        	}
        	
    	}

        Connection conn=connection.get();  
        if(conn==null)  
        {  
            try {
				connection.set(dataSource.getConnection());
			} catch (SQLException e) {
				throw new RepositoryException(e);
			}  
        }  
          
        return conn;  
    }  
    
	// 根据 className 查
	public static Connection getConnection(String className) {
		// 数据源的名称
		String dsn = FQueryProperties.findDataSourceName(className);
		DataSource dataSource = FQueryProperties.findDataSource(dsn);

		// 截至这儿如果 dataSource 还是为null
		if (dataSource == null) {
			throw new ExceptionInInitializerError(String.format("根据className=%s这个值没有找到数据源.", className));
		}

		Connection conn = connection.get();
		if (conn == null) {
			try {
				connection.set(dataSource.getConnection());
			} catch (SQLException e) {
				throw new RepositoryException(e);
			}
		}
		return conn;
	}
	
	public static Connection getConnection() throws SQLException {
		Connection conn = connection.get();
		if(conn == null) {
			throw new SQLException("没有获取到conn!");
		}
		return conn;
	}
	
    //开启事务  
    public static void beginTransaction(Connection conn) {  
        try {  
            if (conn != null && conn.getAutoCommit()) {  
                    conn.setAutoCommit(false);
            }  
        }catch(SQLException e) {
        	throw new RepositoryException(e);
        }  
    }  
      
      
    //提交事务  
    public static void commitTransaction(Connection conn) {  
        try {  
            if (conn != null && !conn.getAutoCommit()) {  
                    conn.commit();  
            }  
        }catch(SQLException e) {
        	throw new RepositoryException(e);
        }  
    }  
      
    //回滚事务  
    public static void rollbackTransaction(Connection conn) {  
        try {  
            if (conn != null && !conn.getAutoCommit()) {  
                    conn.rollback();  
            }  
        }catch(SQLException e) {
        	throw new RepositoryException(e);
        }  
    }  
    
    //关闭连接  
    public static void close() {  
        Connection conn = connection.get();  
        if (conn != null) {  
            try {  
                conn.close();  
                //从ThreadLocal中清除Connection  
                connection.remove();  
            } catch (SQLException e) {  
            	throw new RepositoryException(e);
            }     
        }  
    } 
}
