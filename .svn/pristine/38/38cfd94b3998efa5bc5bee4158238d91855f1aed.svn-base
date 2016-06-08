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

package org.fastquery.handler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.fastquery.core.Primarykey;
import org.fastquery.core.QueryProcess;
import org.fastquery.core.RepositoryException;
import org.fastquery.dsm.DataSourceManage;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public final class ModifyingHandler {
	
	private static ModifyingHandler mh;
	
	private ModifyingHandler() {
	}
	
	public static ModifyingHandler getInstance() {
		if (mh == null) {
			synchronized (ModifyingHandler.class) {
				if (mh == null) {
					mh = new ModifyingHandler();
				}
			}
		}
		return mh;
	}
	

	// 对于改操作可根据其返回值分类如下(也就是说只允许这这些类型,在生成类之前已经做预处理,越界类型是进来不了的)
	// 1). 返回值是void
	// 2). 返回值是int,int[]
	// 3). 返回值是Map<String,Object> 只对insert或update有效
	// 4). 返回值是JSONObject类型 只对insert或update有效
	// 5). 返回值是Primarykey
	// 6). 返回值是boolean
	// 7). 返回值是实体 只对insert或update有效
	// 为什么要分类? 
	// 如果全部集中处理的话,代码堆积会很多,可读性差,不利于扩展.
	// 对于复杂的事情,一定要找适合的模式,尽可能地分化成的小的模块
	public Object voidType(){
		return null;
	}
	
	public Map<String, Object> mapType(String packageName,String dataSourceName,String tableName, String keyFieldName, long autoIncKey, String pkey){
		String sql = null;
		if (autoIncKey != -1) {
			sql = "select * from " + tableName + " where " + keyFieldName + "=" + autoIncKey; // 在这里拼接SQL不会造成SQL注入问题,因为这些变量不是由用户层传递进来的.
		} else if (pkey != null) {
			sql = "select * from " + tableName + " where " + keyFieldName + "='" + pkey + "'";
		} else {
			// 返回map, 是需要主键值的,要而没有,那么就报错.
			throw new RepositoryException("没有找到主键值,请在方法参数中用@Id标识哪个是主键.");
		}
		QueryProcess qp = QueryProcess.getInstance();
		DataSource dataSource = DataSourceManage.getDataSource(dataSourceName,packageName);
		Connection conn = null;
		PreparedStatement stat = null;
		ResultSet rs = null;
		List<Map<String, Object>> keyvals = null;
		Map<String, Object> keyval = null;
		try {
			conn = dataSource.getConnection();
			stat = conn.prepareStatement(sql); // stat会在下面的finally中关闭
			rs = stat.executeQuery();
			keyvals = qp.rs2Map(rs);
			if(keyvals!=null) {
				keyval = keyvals.get(0);
			}
		} catch (SQLException e) {
			throw new RepositoryException(e.getMessage(),e);
		} finally {
			qp.close(rs, stat, conn);
		}		
		return keyval;
	}
	
	public JSONObject jsonObjectType(String packageName,String dataSourceName,String tableName, String keyFieldName, long autoIncKey, String pkey){
		Map<String, Object> map = mapType(packageName,dataSourceName, tableName, keyFieldName, autoIncKey, pkey);
		return new JSONObject(map);
	}

	// 如果不存在主键直接返回null
	public Primarykey primarykeyType(long autoIncKey, String pkey){
		if(( autoIncKey==-1) && (pkey==null) ) {
			return null;
		}
		return new Primarykey(autoIncKey,pkey);
	}
	
	public boolean booleanType(long effect){
		return effect > 0;
	}
	
	
	public Object beanType(String packageName,String dataSourceName,String tableName, String keyFieldName, long autoIncKey, String pkey,Class<?> returnType){
		Map<String, Object> map = mapType(packageName,dataSourceName, tableName, keyFieldName, autoIncKey, pkey);
		return JSON.toJavaObject(new JSONObject(map), returnType);	
	}
	
}
