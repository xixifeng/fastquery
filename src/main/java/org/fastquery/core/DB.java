/*
 * Copyright (c) 2016-2017, fastquery.org and/or its affiliates. All rights reserved.
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.fastquery.struct.RespUpdate;
import org.fastquery.struct.SQLValue;
import org.fastquery.util.BeanUtil;
import org.fastquery.util.TypeUtil;
import org.fastquery.where.I18n;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * DB 最基本操作
 * 
 * @author mei.sir@aliyun.cn
 */
public class DB {

	private static final Logger LOG = Logger.getLogger(DB.class);

	private DB() {
	}
	

	public static List<Map<String, Object>> find(SQLValue sqlValue) {
		
		String sql = sqlValue.getSql();
		List<Object> objs = sqlValue.getValues();
		List<Map<String, Object>> keyvals = null;
		List<String> ssms = TypeUtil.matches(sql, Placeholder.Q_MATCH);
		sql = sql.replaceAll(Placeholder.Q_MATCH, " ? ");
		Connection conn = QueryContext.getConnection();
		PreparedStatement stat = null;
		ResultSet rs = null;
		try {			
			System.out.println("执行:" + sql);
			stat = conn.prepareStatement(sql);			
			// 设置sql参数值
			int lenTmp = objs.size();
			int ssmlen = ssms.size();
			for (int i = 0; i < lenTmp; i++) {
				
				String tpl = "?";
				
				if(ssmlen >= i+1) {
					// 注意: ssms.get(i) 至少包含一个字符 因此不存在 "".trim()问题!
					tpl = ssms.get(i).trim();
				}
				
				if(!"?".equals(tpl) ) {
					LOG.info(String.format("实际给第%d个?设置的值是'%s'%n", i+1,tpl.replaceAll("\\?", objs.get(i).toString())));
					stat.setObject(i+1, tpl.replaceAll("\\?", objs.get(i).toString()));
				} else {
					stat.setObject(i+1, objs.get(i));
				}
			}
			// 设置sql参数值 End
			rs = stat.executeQuery();
			keyvals = rs2Map(rs,QueryContext.getMethod());
			stat.close();
		} catch(Exception e ){
			throw new RepositoryException(e.getMessage(),e);
		} finally {
			close(rs, stat);
		}
		
		return keyvals;
	}
	
	/**
	 * 
	 * @param sqlValues
	 * @param hasEffect 是否需要返回影响行数
	 * @param hasPK 是否需要返回主健
	 * @return
	 */
	public static List<RespUpdate> modify(List<SQLValue> sqlValues, boolean hasEffect, boolean hasPK) {

		List<RespUpdate> rus = null;
		Connection conn = QueryContext.getConnection(); // 由QueryContext自动关闭
		try {
			conn.setAutoCommit(false); // 关闭自动提交
			rus = modify(sqlValues, hasEffect, hasPK, conn);
			conn.commit(); // 提交事务
		} catch (Exception e) {
				try {
					conn.rollback();
				} catch (SQLException e1) {
					throw new RepositoryException(e1.getMessage(), e1);
				}
			throw new RepositoryException(e.getMessage(),e);
		}
		
		return rus;
	}

	private static List<RespUpdate> modify(List<SQLValue> sqlValues, boolean hasEffect, boolean hasPK, Connection conn) throws SQLException {
		List<RespUpdate> rus = new ArrayList<>();
		for (SQLValue sqlValue : sqlValues) {
			ResultSet rs = null;
			PreparedStatement stat = null;
			RespUpdate ru = new RespUpdate();
			try {
				if (hasPK) {
					stat = conn.prepareStatement(sqlValue.getSql(), Statement.RETURN_GENERATED_KEYS);
				} else {
					stat = conn.prepareStatement(sqlValue.getSql());
				}
				List<Object> values = sqlValue.getValues();
				int len = values.size();
				for (int i = 0; i < len; i++) {
					// 设置sql参数值
					stat.setObject(i + 1, values.get(i));
				}

				if (hasEffect) {
					ru.setEffect(stat.executeUpdate());
				}

				if (hasPK) {
					rs = stat.getGeneratedKeys();
					if (rs.next()) {
						ru.setPk(rs.getLong(1));
					}
				}
				stat.close();
			} catch (SQLException e) {
				throw new SQLException(e);
			} finally {
				close(rs, stat);
			}
			
			rus.add(ru);
		}
		
		return rus;
	}
	
	/**
	 * 插入数据返回主键值,如果没有主键,返回null
	 * @param dataSource
	 * @param sql
	 * @return
	 */
	 static Object insert(String sql) {
		Connection conn = null;
		PreparedStatement stat = null;
		ResultSet rs = null;
		Object key = null;
		try {
			conn = QueryContext.getConnection();
			//conn.setAutoCommit(false);
			QueryContext.addSqls(sql);
			stat = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			stat.executeUpdate();
			rs = stat.getGeneratedKeys();
			// 获取主键
			if (rs.next()) {
				key = rs.getObject(1);
			}
			//conn.commit();	
		} catch (SQLException e) {
			/*if(conn !=null ) {
				try {
					conn.rollback();
				} catch (SQLException e1) {
					throw new RepositoryException(e1);
				}
			}*/
			throw new RepositoryException(e);
		} finally {
			close(rs, stat);
		}
		return key;
	}
	
		
		static int insert2(String sql){
			Connection conn = null;
			Statement stat = null;
			ResultSet rs = null;
			try {
				conn = QueryContext.getConnection();
				stat = conn.createStatement();
				QueryContext.addSqls(sql);
				return stat.executeUpdate(sql);
			} catch (SQLException e) {
				throw new RepositoryException(e);
			} finally {
				close(rs, stat);
			}
		}
		
		// 查询一条数据然后转换成一个实体
		static Object select(String sql,Object bean) {
			Connection conn = null;
			Statement stat = null;
			ResultSet rs = null;
			try {
				conn = QueryContext.getConnection();
				stat = conn.createStatement();
				LOG.info(sql);
				QueryContext.addSqls(sql);
				rs = stat.executeQuery(sql);
				List<Map<String, Object>> maps = rs2Map(rs,null);
				if(maps.isEmpty()) {
					return null;
				}
				return JSON.toJavaObject(new JSONObject(maps.get(0)),bean.getClass());
			} catch (Exception e) {
				throw new RepositoryException(e);
			}finally {
				close(rs, stat);
			}
		}
	 
		static boolean exists(String sql,Object bean) {
			Connection conn = null;
			Statement stat = null;
			ResultSet rs = null;
			try {
				conn = QueryContext.getConnection();
				stat = conn.createStatement();
				QueryContext.addSqls(sql);
				rs = stat.executeQuery(sql);
				return rs.next();
			} catch (SQLException e) {
				throw new RepositoryException(e);
			}finally {
				close(rs, stat);
			}
		}
		
		// 更新一条数据,然后返回更新后的数据,返回根据主键查询的sql语句
		static String update(Object bean,String dbName) {
			Connection conn = null;
			PreparedStatement stat = null;
			Object[] updateInfo = BeanUtil.toUpdateSQL(bean, dbName);
			if(updateInfo==null){
				return null;
			}
			String sql = updateInfo[0].toString();
			@SuppressWarnings("unchecked")
			List<Object> args = (List<Object>) updateInfo[1];
			int count = args.size();
			try {
				conn = QueryContext.getConnection();
				//conn.setAutoCommit(false);
				QueryContext.addSqls(sql);
				stat = conn.prepareStatement(sql);
				for (int i = 1; i <= count; i++) {
					stat.setObject(i, args.get(i-1));
				}
				stat.executeUpdate();
				//conn.commit();	
			} catch (SQLException e) {
				/*if(conn !=null ) {
					try {
						conn.rollback();
					} catch (SQLException e1) {
						throw new RepositoryException(e1);
					}
				}*/
				throw new RepositoryException(e);
			} finally {
				close(null, stat);
			}
			return updateInfo[2].toString();
		}
		
		static int update(Object bean,String dbName,String where) {
			int effect = 0;
			Connection conn = null;
			PreparedStatement stat = null;
			Object[] updateInfo = BeanUtil.toUpdateSQL(bean, dbName,where);
			if(updateInfo==null){
				return effect;
			}
			String sql = updateInfo[0].toString();
			LOG.info(sql);
			@SuppressWarnings("unchecked")
			List<Object> args = (List<Object>) updateInfo[1];
			int count = args.size();
			try {
				conn = QueryContext.getConnection();
				//conn.setAutoCommit(false);
				QueryContext.addSqls(sql);
				stat = conn.prepareStatement(sql);
				for (int i = 1; i <= count; i++) {
					stat.setObject(i, args.get(i-1));
				}
				effect = stat.executeUpdate();
				//conn.commit();	
			} catch (SQLException e) {
				/*if(conn !=null ) {
					try {
						conn.rollback();
					} catch (SQLException e1) {
						throw new RepositoryException(e1);
					}
				}*/
				throw new RepositoryException(e);
			} finally {
				close(null, stat);
			}
			return effect;
		}
	/**
	 * 释放资源
	 * @param rs ResultSet实例
	 * @param stat Statement实例
	 */
	private static void close(ResultSet rs, Statement stat) {
		try {
			if (rs != null) {
				rs.close();
			}
		} catch (SQLException e) {
			throw new RepositoryException(e.getMessage(),e);
		} finally {
			try {
				if (stat != null) {
					stat.close();
				}
			} catch (SQLException e) {
				LOG.error(e.getMessage(),e);
			}
		}
	}

	/**
	 * 将 rs 的结果集 转换成 List&lt;Map&gt;,rs没有结果则返回空对象(该方法永不返回null).
	 * @param rs 结果集
	 * @param method 当前方法
	 * @return List map结果集
	 * @throws SQLException SQL异常
	 */
	private static List<Map<String, Object>> rs2Map(ResultSet rs,Method method) throws SQLException {

		List<String> feildNames = null;
		if(method!=null) {
			I18n i18n = method.getAnnotation(I18n.class);
			if(i18n!=null) {
				feildNames = Arrays.asList(i18n.value());	
			}
		}
		
		List<Map<String, Object>> keyvals = new ArrayList<>();
		Map<String, Object> keyval;
		// 获取列信息
		ResultSetMetaData resultSetMetaData;
		// 总列数
		int columnCount;

		String key;
		Object obj;
		while (rs.next()) {
			resultSetMetaData = rs.getMetaData();
			columnCount = resultSetMetaData.getColumnCount();
			keyval = new HashMap<>();
			for (int i = 1; i <= columnCount; i++) {
				//key = resultSetMetaData.getColumnName(i); // 获取列名称
				key = resultSetMetaData.getColumnLabel(i); // 获取列别名,若没有别名那么就获取本身名称(getColumnName)
				obj = rs.getObject(i);
				if(feildNames!=null && feildNames.contains(key)) {
					obj = i18n(obj);
				} 
				keyval.put(key, obj);
			}
			keyvals.add(keyval);
		}
		return keyvals;
	}
	
	private static Object i18n(Object obj) {
		try {
			JSONObject json = (JSONObject) JSON.parse(obj.toString());
			String lang = QueryContext.getLang();
			if(json.containsKey(lang)){ // 这个过程中有可能set,因此QueryContext.getQueryContext().getLang()
				return json.get(QueryContext.getLang());	
			}
			return "i18n error";
		} catch (Exception e) {
			return obj;
		}
	}
	
	

	private static void showArgs(int[] ints,Object[] args){
		StringBuilder sb = new StringBuilder("SQL参数");
		for (int i = 0; i < ints.length; i++) {
			// ints[i] 表示的是第几个参数
			sb.append(String.format("?%s:%s ",ints[i], args[ints[i]-1]));
		}
		LOG.debug(sb.toString());
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*
	public static int modifyEffect(List<Object> objs,String sql) {
		return modify(objs, true, false,sql).get(0).getEffect();
	}
	public static int[] modifyEffects(List<Object> objs,String...queries) {
		List<RespUpdate> rus = modify(objs, true, false, queries);
		int size = rus.size();
		int[] effects = new int[size];
		for (int i = 0; i < size; i++) {
			effects[i] = rus.get(i).getEffect();
		}
		return effects;
	}
	
	public static long modifyPrimarykey(List<Object> objs,String sql) {
		return modify(objs, false, true,sql).get(0).getPk();
	}*/
}
