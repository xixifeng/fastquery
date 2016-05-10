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

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.fastquery.dsm.DataSourceManage;
import org.fastquery.handler.ModifyingHandler;
import org.fastquery.handler.QueryHandler;
import org.fastquery.util.TypeUtil;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class QueryProcess {
	
	private static final Logger LOG = Logger.getLogger(QueryProcess.class);
	
	private volatile static QueryProcess queryProcess;
	
	private QueryProcess(){
		
	}
	
	public static QueryProcess getInstance(){
		if(queryProcess == null) {
			synchronized (QueryProcess.class) {
				if(queryProcess == null) {
					queryProcess = new QueryProcess();
				}	
			}
		}
		return queryProcess;
	}
	
	// 改操作
	Object modifying(Method method,Class<?> returnType,Query query,String packageName,Object...args) {
		int effect = -1;       // 影响行数
		long autoIncKey = -1;  // 主键
		String pkey = null;    // 字符串类型的主键
		// 获取sql
		String sql = query.value();
		int[] ints = TypeUtil.getSQLParameter(sql);
		sql = sql.replaceAll(Placeholder.SP1_REG, "?");
		Modifying modifying = method.getAnnotation(Modifying.class);
		String id = modifying.id(); // 不可能为null
		String table = modifying.table();
		// 替换SQL中的占位变量符
		sql = sql.replaceAll(Placeholder.TABLE_REG, table);
		sql = sql.replaceAll(Placeholder.ID_REG, id);
		showArgs(ints,args);
		LOG.info(sql);
		
		// 获取数据源
		DataSource dataSource = DataSourceManage.getDataSource(packageName);
		Connection conn = null;
		PreparedStatement stat = null;
		ResultSet rs = null;
		try {
			conn = dataSource.getConnection();
			// Statement.RETURN_GENERATED_KEYS
			stat = conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
			// 注意: preparedStatement的参数索引是从1开始的!
			for (int i = 1; i <= ints.length; i++) { // 注意: ints并不是args的长度,而是sql中包含的参数与方法参数的对应关系数组
				// ints[i-1] 表示当前SQL参数对应方法的第几个参数. 从1开始计数
				// 从args中取出值,数组的值是从0开始的,因此必须减去1
				stat.setObject(i, args[ints[i-1]-1]);
			}
			effect = stat.executeUpdate();
			
			
			
			// XXXXXXXXXXXX
			
			//如果主键不是自动增长,在此处获取不到
			//手动添加主键,需要在参数种标识 @Id(value=table="student") 表示该字段是student表的主键 否则报错
			//如果按照以上形式标识主键了,那么就可以在生成代码之前做检测了
			rs = stat.getGeneratedKeys();
			if (rs.next()) { 
				autoIncKey = rs.getLong(1);
				LOG.debug("通过getGeneratedKeys获得主键" + autoIncKey);
			}
			//return new Primarykey(generatedKeys);
			//待续..args.
			//.args.getClass().getName().
			
			// XXXXXXXXXXXX End
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs, stat, conn);
		}	
		
		// yyyyyyyyyyyyyyyyyyyyyyy
		if(autoIncKey == -1) {
			// 没有获得主键值
			LOG.debug("通过stat.getGeneratedKeys没有获得主键,将在方法参数里找");
			int index = TypeUtil.findId(method.getParameters());
			if( index != -1 ) {
				if(method.getParameters()[index].getType() != String.class) {
					autoIncKey = Long.valueOf(args[index].toString()); // 在此不会出现类型转换问题, 因为在check.filter里做校验了		
				} else {
					pkey = (String) args[index];
				}
			}
		} 
		// yyyyyyyyyyyyyyyyyyyyyyy End

		
		// 返回类型分析=====================================
		ModifyingHandler mh = ModifyingHandler.getInstance();
		if(returnType==void.class) {
			return mh.voidType();
		} else if(returnType == int.class) {
			return effect;
		} else if(returnType == Map.class) { // 如果然会值是Map,那么一定是insert或update,在生成实现的时候已经做安全检测
			return mh.mapType(packageName,table,id,autoIncKey,pkey);
		} else if(returnType == JSONObject.class) {
			return mh.jsonObjectType(packageName,table,id,autoIncKey,pkey);
		} else if(returnType == Primarykey.class) {
			return mh.primarykeyType(autoIncKey,pkey);
		} else if(returnType == boolean.class) {
			return mh.booleanType(effect);
		} else { // 把值强制转换成 returnType
			return mh.beanType(packageName,table,id,autoIncKey,pkey,returnType);
		}
		// 返回类型分析===================================== End
		
	}
	
	
	// 查操作
	Object query(Method method,Class<?> returnType, Query query, String packageName,Object...args) {
		// 获取sql
		//String sql = query.value();
		String sql = TypeUtil.getQuerySQL(method, query, args);
		int[] ints = TypeUtil.getSQLParameter(sql);
		showArgs(ints,args);
		LOG.info(sql);
		sql = sql.replaceAll(Placeholder.SP1_REG, "?");
		// 获取数据源
		DataSource dataSource = DataSourceManage.getDataSource(packageName);
		List<Map<String, Object>> keyvals = null;
		Connection conn = null;
		PreparedStatement stat = null;
		ResultSet rs  = null;
		try {
			// 获取链接
			conn = dataSource.getConnection();
			stat = conn.prepareStatement(sql);
			for (int i = 1; i <= ints.length; i++) { // 注意: ints并不是args的长度,而是sql中包含的参数与方法参数的对应关系数组
				stat.setObject(i, args[ints[i-1]-1]);
			}
			rs = stat.executeQuery();
			keyvals = rs2Map(rs);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs, stat, conn);
		}
		
		if(keyvals==null || keyvals.size() == 0) { // 没有找到返回null. 返回类型已经在生成代码时做了全面检测
			if(returnType == boolean.class){
				return false;
			}
			if(returnType == long.class) {
				return 0;
			}
			return null;
		} 
		
		// 返回类型分析=====================================
		QueryHandler qh = QueryHandler.getInstance();
		if(returnType == long.class) {
			return qh.longType(method,returnType,keyvals);
		} else if(returnType == boolean.class) {
			return qh.booleanType(method,returnType,keyvals);
		} else if(returnType == Map.class){
			return qh.mapType(method,returnType,keyvals);
		}else if(returnType == List.class){
			return qh.listType(method,returnType,keyvals);
		}else if(returnType == JSONObject.class){
			return qh.jsonObjeType(method,returnType,keyvals);
		}else if(returnType == JSONArray.class){
			return qh.jsonArrayType(method,returnType,keyvals);
		}else if(isWarrp(returnType)){
			return qh.wrapperType(method,returnType,keyvals);
		}else if(isWarrp(returnType.getComponentType()) || TypeUtil.hasDefaultConstructor(returnType.getComponentType())){
			// 基本类型数组, bean数组
			return qh.wrapperAarryType(method,returnType,keyvals);
		}else {
			return qh.beanType(method,returnType,keyvals);
		}
		// 返回类型分析===================================== End
		//return conversion(method,returnType,keyvals);
		//return null;
	}
	

	Object methodQuery(Method method,String methodName, Class<?> returnType, Query query, String packageName,Object[] args) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	/**
	 * 将 rs 的结果集 转换成 List<Map>, rs没有结果则返回null
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	public List<Map<String, Object>> rs2Map(ResultSet rs) throws SQLException {

		List<Map<String, Object>> keyvals = new ArrayList<>();
		Map<String, Object> keyval = null;
		// 获取列信息
		ResultSetMetaData resultSetMetaData = null;
		// 总列数
		int columnCount = 0;

		String key = null;
		while (rs.next()) {
			resultSetMetaData = rs.getMetaData();
			columnCount = resultSetMetaData.getColumnCount();
			keyval = new HashMap<>();
			for (int i = 1; i <= columnCount; i++) {
				//key = resultSetMetaData.getColumnName(i); // 获取列名称
				key = resultSetMetaData.getColumnLabel(i); // 获取列别名,若没有别名那么就获取本身名称(getColumnName)
				keyval.put(key, rs.getObject(i));
			}
			keyvals.add(keyval);
		}
		if( keyvals.size() == 0) {
			return null;
		}
		return keyvals;
	}
	
	/*
	private Object conversion(Method method,Class<?> returnType, List<Map<String, Object>> keyvals) {
		if(keyvals==null) {
			return null;
		}
		if(keyvals.size() == 0) { // 没有找到返回null. 返回类型已经在生成代码时做了全面检测
			if(returnType == boolean.class){
				return false;
			}
			return null;
		} else {
			if(returnType.isArray()) { // 如果是数组类型
				return toArray(method, returnType, keyvals);
			} else if(returnType == JSONArray.class) {
				return toJsonArray(keyvals);
			} else if(returnType == JSONObject.class) {
				return toJSONObject(method,keyvals);
			} else if(returnType == boolean.class){
				return true;
			}
			else {
				return toBean(method,returnType,keyvals);
			}
		}
	}
	
	// ===========================================       Query(不含Modifying)结果集类型转换  ===========================================
	// keyvals 长度不可能为0(判断之后才进入该方法的)
	private Object toArray(Method method,Class<?> returnType, List<Map<String, Object>> keyvals){	
		// 该数组元素的类型
		Class<?> componentType = returnType.getComponentType();
		
		boolean hasConstructor = TypeUtil.hasDefaultConstructor(componentType);
		
		// 结果集的条数
		int count = keyvals.size();
		// 创建componentType类型的数组
		Object array = Array.newInstance(componentType, count);
		
		Map<String, Object> map = null;
		Set<String> keys = null;
		String key = null;
		for (int index = 0; index < count; index++) {
			if(hasConstructor) { // 存在默认构造方法,表明是一个bean
				Array.set(array, index, JSON.toJavaObject(new JSONObject(keyvals.get(index)), componentType));
			} else { // 不是bean
				map = keyvals.get(index);
				keys = map.keySet();
				if(keys.size() == 1) { //集合中的单个元素是一对键值
					key = keys.iterator().next();
					Array.set(array, index, map.get(key));	
				}else {
					throw new RuntimeException(method + " 执行的结果集"+keyvals+"不能转换成" + returnType);
				}
			} 
		}

		return array;
	}
	
	
	// keyvals 长度不可能为0(判断之后才进入该方法的)
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private JSONArray toJsonArray(List<Map<String, Object>> keyvals) {
		return new JSONArray((List)keyvals);
	}
	
	// keyvals 长度不可能为0(判断之后才进入该方法的)
	private JSONObject toJSONObject(Method method,List<Map<String, Object>> keyvals) {
		if(keyvals.size() != 1) {
			throw new RuntimeException(String.format("%s不能转换成JSONObject : %s", keyvals.toString(),method.toString()));
		}
		return new JSONObject(keyvals.get(0));
	}
	
	private Object toBean(Method method,Class<?> returnType,List<Map<String, Object>> keyvals) {
		if(keyvals.size() != 1) {
			throw new RuntimeException(String.format("%s不能转换成JSONObject : %s", keyvals.toString(),method.toString()));
		}
		
		if(TypeUtil.hasDefaultConstructor(returnType)) {
			return JSON.toJavaObject(new JSONObject(keyvals.get(0)), returnType);	
		} else {
			return  keyvals.get(0).entrySet().iterator().next().getValue();
		}	
	}
	// ===========================================       Query(不含Modifying)结果集类型转换  =========================================== End
	*/
	
	/**
	 * 释放资源
	 * @param rs
	 * @param stat
	 * @param conn
	 */
	public void close(ResultSet rs, Statement stat, Connection conn) {
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
	
	private void showArgs(int[] ints,Object[] args){
		StringBuilder sb = new StringBuilder("SQL参数");
		for (int i = 0; i < ints.length; i++) {
			// ints[i] 表示的是第几个参数
			sb.append(String.format("?%s:%s ",ints[i], args[ints[i]-1]));
		}
		LOG.debug(sb.toString());
	}
	
	// 判断 returnType 是否是包装类型
	private boolean isWarrp(Class<?> returnType){
		if(returnType==null) {
			return false;
		}
		return (returnType == Integer.class) || (returnType == Double.class) || (returnType == Long.class) || (returnType == Short.class) || (returnType == Byte.class) || (returnType == Character.class) || (returnType == Float.class) || (returnType == String.class);
	}
}
