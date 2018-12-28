/*
 * Copyright (c) 2016-2088, fastquery.org and/or its affiliates. All rights reserved.
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

import java.io.BufferedReader;
import java.io.FileReader;
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
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.fastquery.struct.RespUpdate;
import org.fastquery.struct.SQLValue;
import org.fastquery.util.BeanUtil;
import org.fastquery.where.I18n;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * DB 最基本操作
 * 
 * @author mei.sir@aliyun.cn
 */
public class DB {

	private static final Logger LOG = LoggerFactory.getLogger(DB.class);

	private DB() {
	}

	public static List<Map<String, Object>> find(SQLValue sqlValue) {

		String sql = sqlValue.getSql();
		List<Object> objs = sqlValue.getValues();
		List<Map<String, Object>> keyvals = null;
		Connection conn = QueryContext.getConnection();
		PreparedStatement stat = null;
		ResultSet rs = null;
		try {
			QueryContext.addSqls(sql);
			info(sql, objs);
			stat = conn.prepareStatement(sql);
			// 设置sql参数值
			int lenTmp = objs.size(); // objs 源头上已经控制禁止为null
			for (int i = 0; i < lenTmp; i++) {
				stat.setObject(i + 1, objs.get(i));
			}
			// 设置sql参数值 End
			rs = stat.executeQuery();
			keyvals = rs2Map(rs, QueryContext.getMethod());
			stat.close();
		} catch (Exception e) {
			throw new RepositoryException(e.getMessage(), e);
		} finally {
			close(rs, stat);
		}

		return keyvals;
	}

	/**
	 * 
	 * @param sqlValues 待执行的SQL集
	 * @param hasPK 是否需要返回主健
	 * @return 改操作响应数据
	 */
	public static List<RespUpdate> modify(List<SQLValue> sqlValues, boolean hasPK) {
		List<RespUpdate> rus = null;
		Connection conn = QueryContext.getConnection(); // 由QueryContext自动关闭
		try {
			QueryContext.disableAutoCommit(); // 关闭自动提交
			rus = modify(sqlValues, hasPK, conn);
			QueryContext.commit(); // 提交事务
		} catch (Exception e) {
			try {
				QueryContext.rollback();
			} catch (SQLException e1) {
				throw new RepositoryException(e1.getMessage(), e1);
			}
			throw new RepositoryException(e.getMessage(), e);
		}

		return rus;
	}

	private static List<RespUpdate> modify(List<SQLValue> sqlValues, boolean hasPK, Connection conn) throws SQLException {
		List<RespUpdate> rus = new ArrayList<>();
		for (SQLValue sqlValue : sqlValues) {
			ResultSet rs = null;
			PreparedStatement stat = null;
			RespUpdate ru = new RespUpdate();
			try {
				String sql = sqlValue.getSql();
				QueryContext.addSqls(sql);
				info(sql, sqlValue.getValues());
				if (hasPK) {
					stat = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				} else {
					stat = conn.prepareStatement(sql);
				}
				List<Object> values = sqlValue.getValues();
				int len = values.size();
				for (int i = 0; i < len; i++) {
					// 设置sql参数值
					stat.setObject(i + 1, values.get(i));
				}

				ru.setEffect(stat.executeUpdate());

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
	 * 改操作,若:isEffect=true,返回影响行数;若:isEffect=false,返回主键值.
	 * 
	 * @param sql 语句
	 * @param isEffect 是否返回影响行数
	 * @return 影响行数 或 主键值
	 */
	static Object update(String sql, boolean isEffect) {
		Connection conn = null;
		PreparedStatement stat = null;
		ResultSet rs = null;
		Object key = null;
		try {
			conn = QueryContext.getConnection();
			QueryContext.addSqls(sql);
			QueryContext.disableAutoCommit();
			if (isEffect) {
				stat = conn.prepareStatement(sql); // 不需要返回主键
			} else {
				stat = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			}
			int e = stat.executeUpdate();
			if (isEffect) {
				QueryContext.commit();
				return e;
			} else {
				rs = stat.getGeneratedKeys();
				// 获取主键
				if (rs.next()) {
					key = rs.getObject(1);
				}
				QueryContext.commit();
				return key;
			}
		} catch (SQLException e) {
			if (conn != null) {
				try {
					QueryContext.rollback();
				} catch (SQLException e1) {
					throw new RepositoryException(e1);
				}
			}
			throw new RepositoryException(e);
		} finally {
			close(rs, stat);
		}
	}

	static int update(Object bean, String dbName, String where) {
		int effect = 0;
		Connection conn = null;
		PreparedStatement stat = null;
		Object[] updateInfo = (where == null || "".equals(where)) ? BeanUtil.toUpdateSQL(bean, dbName, false)
				: BeanUtil.toUpdateSQL(bean, dbName, where);
		if (updateInfo == null) {
			return effect;
		}
		String sql = updateInfo[0].toString();
		LOG.info(sql);
		@SuppressWarnings("unchecked")
		List<Object> args = (List<Object>) updateInfo[1];
		int count = args.size();
		try {
			conn = QueryContext.getConnection();
			QueryContext.disableAutoCommit();
			QueryContext.addSqls(sql);
			info(sql, args);
			stat = conn.prepareStatement(sql);
			for (int i = 1; i <= count; i++) {
				stat.setObject(i, args.get(i - 1));
			}
			effect = stat.executeUpdate();
			QueryContext.commit();
		} catch (SQLException e) {
			if (conn != null) {
				try {
					QueryContext.rollback();
				} catch (SQLException e1) {
					throw new RepositoryException(e1);
				}
			}
			throw new RepositoryException(e);
		} finally {
			close(null, stat);
		}
		return effect;
	}

	// 查询一条数据然后转换成一个实体
	static Object select(String sql, Object bean) {
		Class<?> cls = (bean instanceof Class) ? (Class<?>) bean : bean.getClass();
		Connection conn = null;
		Statement stat = null;
		ResultSet rs = null;
		try {
			conn = QueryContext.getConnection();
			stat = conn.createStatement();
			LOG.info(sql);
			QueryContext.addSqls(sql);
			rs = stat.executeQuery(sql);
			List<Map<String, Object>> maps = rs2Map(rs, null);
			if (maps.isEmpty()) {
				return null;
			}
			return JSON.toJavaObject(new JSONObject(maps.get(0)), cls);
		} catch (Exception e) {
			throw new RepositoryException(e);
		} finally {
			close(rs, stat);
		}
	}

	static boolean exists(String sql) {
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
		} finally {
			close(rs, stat);
		}
	}

	private static Stream<String> parserSQLFile(String name) {
		Builder<String> builder = Stream.builder();

		try (FileReader reader = new FileReader(name); BufferedReader br = new BufferedReader(reader)) {
			StringBuilder buff = new StringBuilder();
			String str;
			while ((str = br.readLine()) != null) {
				str = str.trim();
				if (!"".equals(str)) {
					buff.append(str);
					buff.append(' '); // 有可能一行语句分了多行书写,拼接时行与行要用空格隔开
					if (buff.charAt(buff.length() - 2) == ';') {
						builder.add(buff.toString());
						buff = new StringBuilder();
					}
				}
			}
		} catch (Exception e) {
			throw new RepositoryException(e);
		}

		return builder.build().filter(s -> !s.startsWith("#") && !s.startsWith("-- "));
	}

	static int[] executeBatch(String sqlFile, BiConsumer<Statement, String> consumer) {
		Connection conn = QueryContext.getConnection();
		Statement stat = null;
		Stream<String> stream = parserSQLFile(sqlFile);
		try {
			QueryContext.disableAutoCommit();
			final Statement st = conn.createStatement();
			stat = st;
			stream.forEach(s -> consumer.accept(st, s));
			int[] ints = stat.executeBatch();
			stat.clearBatch();
			QueryContext.commit();
			return ints;
		} catch (Exception e) {
			try {
				QueryContext.rollback();
			} catch (SQLException e1) {
				throw new RepositoryException(e1.getMessage(), e1);
			}
			throw new RepositoryException(e);
		} finally {
			close(null, stat);
		}
	}

	/**
	 * 释放资源
	 * 
	 * @param rs ResultSet实例
	 * @param stat Statement实例
	 */
	private static void close(ResultSet rs, Statement stat) {
		try {
			if (rs != null) {
				rs.close();
			}
		} catch (SQLException e) {
			throw new RepositoryException(e);
		} finally {
			try {
				if (stat != null) {
					stat.close();
				}
			} catch (SQLException e) {
				LOG.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * 将 rs 的结果集 转换成 List&lt;Map&gt;,rs没有结果则返回空对象(该方法永不返回null).
	 * 
	 * @param rs 结果集
	 * @param method 当前方法
	 * @return List map结果集
	 * @throws SQLException SQL异常
	 */
	private static List<Map<String, Object>> rs2Map(ResultSet rs, Method method) throws SQLException {

		List<String> feildNames = null;
		if (method != null) {
			I18n i18n = method.getAnnotation(I18n.class);
			if (i18n != null) {
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
				// key = resultSetMetaData.getColumnName(i); // 获取列名称
				key = resultSetMetaData.getColumnLabel(i); // 获取列别名,若没有别名那么就获取本身名称(getColumnName)
				obj = rs.getObject(i);
				if (feildNames != null && feildNames.contains(key)) {
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
			if (json.containsKey(lang)) { // 这个过程中有可能set,因此QueryContext.getQueryContext().getLang()
				return json.get(QueryContext.getLang());
			}
			return "i18n error";
		} catch (Exception e) {
			return obj;
		}
	}

	/**
	 * 输出执行日志
	 * 
	 * @param sql
	 * @param objs
	 */
	private static void info(String sql, List<Object> objs) {
		if (LOG.isInfoEnabled()) { // 这个输出要做很多事情,在此判断一下很有必要,生产环境通常是warn级别
			StringBuilder sb = new StringBuilder("\n正在准备执行SQL:");
			sb.append(sql);
			sb.append("\n");
			if (objs != null && !objs.isEmpty()) {
				int len = objs.size();
				for (int i = 0; i < len; i++) {
					sb.append(String.format("第%d个\"?\"对应的参数值是:%s;%n", i + 1, objs.get(i)));
				}
			}
			LOG.info(sb.toString());
		}
	}
}
