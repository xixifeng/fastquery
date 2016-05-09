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

package org.fastquery.filter.generate;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.fastquery.dsm.DataSourceManage;

/**
 * 该类只为fastquery.filter服务
 * @author xixifeng (fastquery@126.com)
 */
public class DBUtils {

	private volatile static DBUtils dbUtils;

	private DBUtils() {
	}

	public static DBUtils getInstance() {
		if (dbUtils == null) {
			synchronized (DBUtils.class) {
				if (dbUtils == null) {
					dbUtils = new DBUtils();
				}
			}
		}
		return dbUtils;
	}
	
	/*
	public List<Map<String, Object>> query(String sql){
		
	} 
	*/
	
	/**
	 * 判断 field 是否是 table 表的主键
	 * @param packageName 基本包地址,根据它获取数据源.
	 * @param table 表名称
	 * @param field 字段名称
	 * @return
	 */
	public boolean findColumnKey(String packageName,String table,String field){
		
		DataSource dataSource = DataSourceManage.getDataSource(packageName);
		Connection conn = null;
		Statement stat = null;
		ResultSet rs = null;
		String sql = null;
		try {
			conn = dataSource.getConnection();
			DatabaseMetaData databaseMetaData = conn.getMetaData(); // 获取数据库信息
			String databaseProductName = databaseMetaData.getDatabaseProductName();
			if(databaseProductName.equals("MySQL")) { 
				sql = "SHOW COLUMNS from "+table+" where `KEY`='PRI'";
				stat = conn.createStatement();
				rs = stat.executeQuery(sql);
				if(rs.next() && rs.getString("Field").equals(field)) {
					return true;
				}
			} else {
				throw new RuntimeException("该方法暂不支持 " + databaseProductName + " 数据库");
			}			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs, stat, conn);
		}
		
		return false;
	}
	
	
	/**
	 * 释放资源
	 * @param rs
	 * @param stat
	 * @param conn
	 */
	private void close(ResultSet rs, Statement stat, Connection conn) {
		try {
			if (rs != null) {
				rs.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stat != null) {
					stat.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				try {
					if (conn != null) {
						conn.close();
					}
				} catch (SQLException e) {
					e.printStackTrace();
				} finally {

				}
			}
		}
	}
}







