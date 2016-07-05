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

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.SQLExec;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.fastquery.dsm.DataSourceManage;
import org.fastquery.handler.ModifyingHandler;
import org.fastquery.handler.QueryHandler;
import org.fastquery.page.NotCount;
import org.fastquery.page.PageImpl;
import org.fastquery.page.Pageable;
import org.fastquery.page.PageableImpl;
import org.fastquery.page.Slice;
import org.fastquery.sql.NativeSpec;
import org.fastquery.sql.Predicate;
import org.fastquery.util.FastQueryJSONObject;
import org.fastquery.util.TypeUtil;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class QueryProcess {
	
	private static final Logger LOG = Logger.getLogger(QueryProcess.class);
	
	private static QueryProcess queryProcess;
	
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
	Object modifying(Method method,Class<?> returnType,Query[] queries,String packageName,Object...args) {
		// 获取数据源
		String sourceName = TypeUtil.findSource(method.getParameters(), args);
		DataSource dataSource = DataSourceManage.getDataSource(sourceName,packageName);
		Connection conn = null;
		PreparedStatement stat = null;
		ResultSet rs = null;
		
		int sqlCount = queries.length;
		int[] effects = new int[sqlCount]; // 影响行数集合
		Primarykey[] primarykeys = new Primarykey[sqlCount];// 主键集合
		
		Modifying modifying = method.getAnnotation(Modifying.class);
		String id = modifying.id(); // 不可能为null
		String table = modifying.table();
		
		
		int effect = -1;
		long autoIncKey = -1;
		String pkey = null;
		
		try {
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);
			// 逐个执行修改操作
			for (int jk = 0; jk < sqlCount; jk++) { 
				// 获取sql
				String sql = queries[jk].value();
				int[] ints = TypeUtil.getSQLParameter(sql);
				sql = sql.replaceAll(Placeholder.SP1_REG, "?");
				// 替换SQL中的占位变量符
				sql = sql.replaceAll(Placeholder.TABLE_REG, table);
				sql = sql.replaceAll(Placeholder.ID_REG, id);
				showArgs(ints,args);
				LOG.info(sql);
				
				try {
					// Statement.RETURN_GENERATED_KEYS
					stat = conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS); // stat 会在下面的finally里关闭.
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
					// XXXXXXXXXXXX End
					
				} finally {
					// rs 在这个for循环中可能会创建多个.
					close(rs, stat, null);  // 这个不能省略
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
				
				effects[jk] = effect;
				primarykeys[jk] = new Primarykey(autoIncKey, pkey);
			}
			// 逐个执行修改操作 End
			conn.commit();
		} catch (Exception e) {
			effects = new int[sqlCount]; // 影响行数集合
			primarykeys = new Primarykey[sqlCount];// 主键集合
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException e1) {
					throw new RepositoryException(e1.getMessage(), e1);
				}
			}
			throw new RepositoryException(e.getMessage(),e);
		} finally {
			// 释放资源
			close(rs, stat, conn);
		}
		
		// 返回类型分析=====================================
		Primarykey pk = primarykeys[0];
		ModifyingHandler mh = ModifyingHandler.getInstance();
		if(returnType==void.class) {
			return mh.voidType();
		} else if(returnType == int.class) {
			return sumIntArray(effects);
		} else if(returnType == int[].class ){
			return effects;
		} else if(returnType == Map.class) { // 如果然会值是Map,那么一定是insert或update,在生成实现的时候已经做安全检测
			return mh.mapType(packageName,sourceName,table,id,pk.getPrimarykey(),pk.getSpecifyPrimarykey());
		} else if(returnType == JSONObject.class) {
			return mh.jsonObjectType(packageName,sourceName,table,id,pk.getPrimarykey(),pk.getSpecifyPrimarykey());
		} else if(returnType == Primarykey.class) {
			return mh.primarykeyType(pk.getPrimarykey(),pk.getSpecifyPrimarykey());
		} else if(returnType == boolean.class) {
			return mh.booleanType(sumIntArray(effects));
		} else { // 把值强制转换成 returnType
			return mh.beanType(packageName,sourceName,table,id,pk.getPrimarykey(),pk.getSpecifyPrimarykey(),returnType);
		}
		// 返回类型分析===================================== End
		
	}
	
	/**
	 * 统计一个int数组,所有元素相加的和
	 * @param ints
	 * @return
	 */
	private int sumIntArray(int[] ints){
		int sum = 0;
		for (int i : ints) {
			sum += i;
		}
		return sum;
	}
	
	// 查操作
	Object query(Method method,Class<?> returnType, String sql,String sourceName, String packageName,Object[] iargs,Object...args) {
		int[] ints = TypeUtil.getSQLParameter(sql);
		showArgs(ints,args);
		sql = sql.replaceAll(Placeholder.SP1_REG, "?");
		LOG.info(sql);
		DataSource dataSource = DataSourceManage.getDataSource(sourceName,packageName);
		List<Map<String, Object>> keyvals = null;
		Connection conn = null;
		PreparedStatement stat = null;
		ResultSet rs  = null;
		try {
			// 获取链接
			conn = dataSource.getConnection();
			stat = conn.prepareStatement(sql); // stat 会在下面的finally里关闭.
			for (int i = 1; i <= ints.length; i++) { // 注意: ints并不是args的长度,而是sql中包含的参数与方法参数的对应关系数组
				stat.setObject(i, args[ints[i-1]-1]);
			}
			rs = stat.executeQuery();
			keyvals = rs2Map(rs);
		} catch (SQLException e) {
			throw new RepositoryException(e.getMessage(),e);
		} finally {
			close(rs, stat, conn);
		}
		
		// 上面的try发生异常了,才会导致keyvals为null, 不过异常一旦捕获到就throw了,因此,程序执行到这里keyvals不可能为null.
		// 返回类型分析=====================================
		QueryHandler qh = QueryHandler.getInstance();
		if(returnType == long.class) {
			return qh.longType(method,keyvals);
		} else if(returnType == boolean.class) {
			return qh.booleanType(keyvals);
		} else if(returnType == Map.class){
			return qh.mapType(method,keyvals);
		} else if(TypeUtil.isListMapSO(method.getGenericReturnType())){
			return qh.listType(keyvals);
		}else if(returnType == List.class){
			return qh.list(keyvals,method,iargs);
		}else if(returnType == JSONObject.class){
			return qh.jsonObjeType(method,keyvals);
		}else if(returnType == JSONArray.class){
			return qh.jsonArrayType(keyvals);
		}else if(isWarrp(returnType)){
			return qh.wrapperType(method,returnType,keyvals);
		}else if(isWarrp(returnType.getComponentType()) || TypeUtil.hasDefaultConstructor(returnType.getComponentType())){
			// 基本类型数组, bean数组
			return qh.wrapperAarryType(method,returnType,keyvals);
		}else {
			return qh.beanType(method,returnType,keyvals);
		}
		// 返回类型分析===================================== End
	}
	
	// 分页查询
	@SuppressWarnings({ "rawtypes", "unchecked" })
	Object queryPage(Method method, Query[] querys, String packageName, Object[] args) {
		// 获取sql
		String sql = TypeUtil.getQuerySQL(method, querys, args).get(0);
		int[] ints = TypeUtil.getSQLParameter(sql);
		Pageable pageable = null;
		for (Object arg : args) {
			if(Pageable.class.isAssignableFrom(arg.getClass())) {
				pageable = (Pageable) arg;
				break;
			}
		}
		Parameter[] parameters = method.getParameters();
		if(pageable == null ) {
			pageable = new PageableImpl(TypeUtil.findPageIndex(parameters, args), TypeUtil.findPageSize(parameters, args));
		}
		
		int firstResult = pageable.getOffset();
		int maxResults = pageable.getPageSize();
		
		LOG.debug("firstResult:"+firstResult+" maxResults:" + maxResults);
		
		// 针对 mysql 分页
		// 获取limit
		StringBuilder sb = new StringBuilder(" limit ");
		sb.append(firstResult);
		sb.append(',');
		sb.append(maxResults);
		String limit = sb.toString();
		
		List<String> strs = TypeUtil.matches(sql, Placeholder.LIMIT_RGE);
		if(strs.isEmpty()) { // 如果没有#{#limit}, 默认在末尾增加.
			sql += Placeholder.LIMIT;
		}
		
		String ssql = new String(sql);
		
		sql = sql.replaceFirst(Placeholder.LIMIT_RGE, limit);
		
		showArgs(ints,args);
		sql = sql.replaceAll(Placeholder.SP1_REG, "?");
		LOG.info(sql);
		// 获取数据源
		String sourceName = TypeUtil.findSource(parameters, args);
		DataSource dataSource = DataSourceManage.getDataSource(sourceName,packageName);
		List<Map<String, Object>> keyvals = null;
		Connection conn = null;
		PreparedStatement stat = null;
		ResultSet rs  = null;
		try {
			// 获取链接
			conn = dataSource.getConnection();
			stat = conn.prepareStatement(sql); // stat 会在下面的finally里关闭.
			for (int i = 1; i <= ints.length; i++) { // 注意: ints并不是args的长度,而是sql中包含的参数与方法参数的对应关系数组
				stat.setObject(i, args[ints[i-1]-1]);
			}
			rs = stat.executeQuery();
			keyvals = rs2Map(rs);
		} catch (SQLException e) {
			throw new RepositoryException(e.getMessage(),e);
		} finally {
			// conn 先不放到连接池里,还需要用
			close(rs, stat, null);
		}
		
		
		Query query = querys[0];

		int size = pageable.getPageSize();      // 每页多少条数据
		long totalElements = -1L;               // 总行数,如果不求和默认-1L
		int totalPages = -1;                    // 总页数,如果不求和默认-1
		int numberOfElements = keyvals.size();  // 每页实际显示多少条数据
		int number = pageable.getPageNumber();  // 当前页码
		boolean hasContent = !keyvals.isEmpty();// 这页有内容吗?
		boolean hasPrevious = (number > 1) && hasContent;// number不是第1页且当前页有数据,就可以断言它有上一页.
		boolean hasNext;                                 // 有下一页吗? 在这里不用给默认值,如下一定会给他赋值.
		boolean isLast;
		
		if(method.getAnnotation(NotCount.class)==null) {
			// 求和 ---------------------------------------------------
			String countField = query.countField();
			// 获取求和sql
			String countQuery = query.countQuery();
			if("".equals(countQuery)) { // 表明在声明时没有指定求和语句
				// 计算求和语句
				// 把select 与 from 之间的 内容变为 count(countField)
				 // (?i)表示正则中不区分大小写 \\b 表示单词的分界
				 int beginIndex = ignoreCaseWordEndIndex("(?i)\\bselect ", sql);
				 // 作为substring中的endIndex
				 int endIndex = ignoreCaseWordStartIndex("(?i) from ", sql);
				 if(beginIndex!=-1 && endIndex!=-1) {
					// 注意: 求和字段默认为 "id"
					// 重要: sqlStr.substring(selectStart+6,fromStart) 的值 很可能包含正则表达式, 因此必须用Pattern.quote
					sql = sql.replaceFirst(Pattern.quote(sql.substring(beginIndex,endIndex)),new StringBuilder("count(").append(countField).append(')').toString());
				 } else {
					 throw new RepositoryException("求和SQL错误:" + sql);
				 }
				 
				 sql = TypeUtil.getCountQuerySQL(method, sql, args);
			} else {
				sql = TypeUtil.getCountQuerySQL(method, countQuery, args);
			}
			
			 // 求和语句不需要order by 和 limit
			 // (?i) : 表示不区分大小写
			 // 过滤order by 后面的字符串(包含本身)
			 sql = sql.replaceFirst("(?i)(order by )(.|\n)+", ""); 
			 // 过滤limit后面的字符串(包含自身)
			 sql = sql.replaceFirst("(?i)(limit )(.|\n)+", "");
			 
			LOG.debug("求和语句: " + sql);
			try {
				close(rs, stat, null); // 在新创建rs,stat 先把之前的关闭掉
				stat = conn.prepareStatement(sql); // stat 会在下面的finally里关闭.
				for (int i = 1; i <= ints.length; i++) { // 注意: ints并不是args的长度,而是sql中包含的参数与方法参数的对应关系数组
					stat.setObject(i, args[ints[i-1]-1]);
				}
				rs = stat.executeQuery();
				rs.next();
				totalElements = rs.getLong(1);
			} catch (SQLException e) {
				throw new RepositoryException(e);
			} finally {
				close(rs, stat, conn);
			}
			
			// 计算总页数
			totalPages = ((int) totalElements) / size;
			if (((int) totalElements) % size != 0) {
				totalPages += 1;
			}
			hasNext = number < totalPages;
			isLast = number == totalPages;
			// 求和 --------------------------------------------------- End
		} else {
			// 在查一下推算出下一页是否有数据, 要不要把下一页的数据存储起来,有待考虑...
			firstResult = pageable.getOffset()+pageable.getPageSize();
			sb = new StringBuilder(" limit ");
			sb.append(firstResult);
			sb.append(',');
			sb.append(maxResults);
			limit = sb.toString();
			sql = ssql.replaceFirst(Placeholder.LIMIT_RGE, limit);
			sql = sql.replaceAll(Placeholder.SP1_REG, "?");
			LOG.debug("下一页的SQL:" + sql);
			try {
				close(rs, stat, null); // 在新创建rs,stat 先把之前的关闭掉
				stat = conn.prepareStatement(sql); // stat 会在下面的finally里关闭.
				for (int i = 1; i <= ints.length; i++) { // 注意: ints并不是args的长度,而是sql中包含的参数与方法参数的对应关系数组
					stat.setObject(i, args[ints[i-1]-1]);
				}
				rs = stat.executeQuery();
				boolean next = rs.next();
				hasNext = next; // 下一页有数据
				isLast = !next; // 下一页没有数据了,表明这是最后一页了.
			} catch (SQLException e) {
				throw new RepositoryException(e.getMessage(),e);
			} finally {
				close(rs, stat, conn);
			}
		}
		
		boolean isFirst = number == 1;
		Slice nextPageable = new Slice((!isLast) ? (number + 1) : number, size);
		Slice previousPageable = new Slice((!isFirst) ? (number - 1) : number, size);
		
		
		List<?> list = keyvals;
		// Page<T> 中的 T如果是一个实体,那么需要把 HashMap 转换成实体
		//method.getGenericReturnType()
		if(!method.getGenericReturnType().getTypeName().contains("Page<java.util.Map<java.lang.String, java.lang.Object>>")){
			// 则说明是一个T是一个实体
			java.lang.reflect.Type type = method.getGenericReturnType();
			if(type instanceof ParameterizedType){
				ParameterizedType parameterizedType = (ParameterizedType) method.getGenericReturnType();
				Class<?> bean = (Class<?>) parameterizedType.getActualTypeArguments()[0];
				list = TypeUtil.listMap2ListBean(keyvals, bean);
			}
		}
						
		return new PageImpl(size, numberOfElements, number, list,totalElements, totalPages, hasContent, hasNext, hasPrevious,isFirst, isLast, nextPageable, previousPageable);
		
	}

	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	Object methodQuery(Method method,String packageName,Object[] iargs) {		
		Id id = method.getAnnotation(Id.class);
		byte methodId = id.value();
		
		NativeSpec spec;
		Predicate predicate;
		String sourceName;
		switch (methodId) {
		case MethodId.QUERY0:
			spec = (NativeSpec) iargs[0];
			predicate = spec.toPredicate(new SelectQuery());
			sourceName = TypeUtil.findSource(method.getParameters(), iargs);
			// 注意: iargs 表示是QueryRepository中的方法的具体实参! 并不是sql需要的实参.
			return query(method, method.getReturnType(), predicate.getSQL(),sourceName,packageName, iargs,predicate.getParameters());
		case MethodId.QUERY1:
			break;
		case MethodId.QUERY2:
			break;
		case MethodId.QUERY3:
			// 获取数据源
			sourceName = TypeUtil.findSource(method.getParameters(), iargs);
			com.mchange.v2.c3p0.ComboPooledDataSource ds = (ComboPooledDataSource) DataSourceManage.getDataSource(sourceName,packageName);
			
			SQLExec sqlExec = new SQLExec();
			
			// 设置数据库参数
			sqlExec.setDriver(ds.getDriverClass());
			sqlExec.setUrl(ds.getJdbcUrl());
			sqlExec.setUserid(ds.getUser());
			sqlExec.setPassword(ds.getPassword());
			
			String basedir = FastQueryJSONObject.getBasedir();
			
			// 要执行的脚本
			sqlExec.setSrc(new File(basedir + (String)iargs[0]));
			
			sqlExec.setOnerror((SQLExec.OnError)(EnumeratedAttribute.getInstance(   
					SQLExec.OnError.class, "abort")));  
			sqlExec.setPrint(true); // 设置是否输出

			// 输出到文件 sql.out 中；不设置该属性，默认输出到控制台
			sqlExec.setOutput(new File(basedir + (String)iargs[1]));
			
			sqlExec.setProject(new Project()); // 要指定这个属性，不然会出错 
			
			try {
				sqlExec.execute();
			} catch (Exception e) {
				LOG.error(String.format("执行%s发生致命错误", iargs[0]),e);
			}
			
			break;
		case MethodId.QUERY4:
			break;
		case MethodId.QUERY5:
			break;
		case MethodId.QUERY6:
			break;
		case MethodId.QUERY7:

			// NativeSpec spec, Pageable pageable,String countField,String countsql,boolean closeCount,String dataSourceName
			spec = (NativeSpec) iargs[0];
			Pageable pageable = (Pageable) iargs[1];
			if(pageable==null) {
				throw new RepositoryException("pageable 不能为null!");
			}
			String countField = (String) iargs[2];
			if(countField==null) {
				countField = "id";
			}
			String countQuery = (String) iargs[3];
			boolean closeCount = (boolean) iargs[4];
			sourceName = TypeUtil.findSource(method.getParameters(), iargs);			
			
			predicate = spec.toPredicate(new SelectQuery());
			String where = predicate.getWhereClause();
			// 获取sql
			String sql = predicate.getSQL();
			
			// SQL需要的实参
			Object[] args = predicate.getParameters();
			int[] ints = TypeUtil.getSQLParameter(sql);

			
			int firstResult = pageable.getOffset();
			int maxResults = pageable.getPageSize();
			
			LOG.debug("firstResult:"+firstResult+" maxResults:" + maxResults);
			
			// 针对 mysql 分页
			// 获取limit
			StringBuilder sb = new StringBuilder(" limit ");
			sb.append(firstResult);
			sb.append(',');
			sb.append(maxResults);
			String limit = sb.toString();
			
			List<String> strs = TypeUtil.matches(sql, Placeholder.LIMIT_RGE);
			if(strs.isEmpty()) { // 如果没有#{#limit}, 默认在末尾增加.
				sql += Placeholder.LIMIT;
			}
			
			String ssql = new String(sql);
			
			sql = sql.replaceFirst(Placeholder.LIMIT_RGE, limit);
			
			showArgs(ints,args);
			sql = sql.replaceAll(Placeholder.SP1_REG, "?");
			LOG.info(sql);
			DataSource dataSource = DataSourceManage.getDataSource(sourceName,packageName);
			List<Map<String, Object>> keyvals = null;
			Connection conn = null;
			PreparedStatement stat = null;
			ResultSet rs  = null;
			try {
				// 获取链接
				conn = dataSource.getConnection();
				stat = conn.prepareStatement(sql); // stat 会在下面的finally里关闭.
				for (int i = 1; i <= ints.length; i++) { // 注意: ints并不是args的长度,而是sql中包含的参数与方法参数的对应关系数组
					stat.setObject(i, args[ints[i-1]-1]);
				}
				rs = stat.executeQuery();
				keyvals = rs2Map(rs);
			} catch (SQLException e) {
				throw new RepositoryException(e.getMessage(),e);
			} finally {
				// conn 先不放到连接池里,还需要用
				close(rs, stat, null);
			}
						
			int size = pageable.getPageSize();      // 每页多少条数据
			long totalElements = -1L;               // 总行数,如果不求和默认-1L
			int totalPages = -1;                    // 总页数,如果不求和默认-1
			int numberOfElements = keyvals.size();  // 每页实际显示多少条数据
			int number = pageable.getPageNumber();  // 当前页码
			boolean hasContent = !keyvals.isEmpty();// 这页有内容吗?
			boolean hasPrevious = (number > 1) && hasContent;// number不是第1页且当前页有数据,就可以断言它有上一页.
			boolean hasNext;                                 // 有下一页吗? 在这里不用给默认值,如下一定会给他赋值.
			boolean isLast;
			
			if(!closeCount) {
				if(countQuery == null || "".equals(countQuery)) { // 表明在声明时没有指定求和语句
					// 计算求和语句
					// 把select 与 from 之间的 内容变为 count(countField)
					 // (?i)表示正则中不区分大小写 \\b 表示单词的分界
					 int beginIndex = ignoreCaseWordEndIndex("(?i)\\bselect ", sql);
					 // 作为substring中的endIndex
					 int endIndex = ignoreCaseWordStartIndex("(?i) from ", sql);
					 if(beginIndex!=-1 && endIndex!=-1) {
						// 注意: 求和字段默认为 "id"
						// 重要: sqlStr.substring(selectStart+6,fromStart) 的值 很可能包含正则表达式, 因此必须用Pattern.quote
						sql = sql.replaceFirst(Pattern.quote(sql.substring(beginIndex,endIndex)),new StringBuilder("count(").append(countField).append(')').toString());
					 } else {
						 throw new RepositoryException("求和SQL错误:" + sql);
					 }
					 
					sql = sql + where;
				} else {
					sql = countQuery + where;
				}
				
				 // 求和语句不需要order by 和 limit
				 // (?i) : 表示不区分大小写
				 // 过滤order by 后面的字符串(包含本身)
				 sql = sql.replaceFirst("(?i)(order by )(.|\n)+", ""); 
				 // 过滤limit后面的字符串(包含自身)
				 sql = sql.replaceFirst("(?i)(limit )(.|\n)+", "");
				 
				LOG.debug("求和语句: " + sql);
				try {
					close(rs, stat, null); // 在新创建rs,stat 先把之前的关闭掉
					stat = conn.prepareStatement(sql); // stat 会在下面的finally里关闭.
					for (int i = 1; i <= ints.length; i++) { // 注意: ints并不是args的长度,而是sql中包含的参数与方法参数的对应关系数组
						stat.setObject(i, args[ints[i-1]-1]);
					}
					rs = stat.executeQuery();
					rs.next();
					totalElements = rs.getLong(1);
				} catch (SQLException e) {
					throw new RepositoryException(e);
				} finally {
					close(rs, stat, conn);
				}
				
				// 计算总页数
				totalPages = ((int) totalElements) / size;
				if (((int) totalElements) % size != 0) {
					totalPages += 1;
				}
				hasNext = number < totalPages;
				isLast = number == totalPages;
				// 求和 --------------------------------------------------- End
			} else {
				// 在查一下推算出下一页是否有数据, 要不要把下一页的数据存储起来,有待考虑...
				firstResult = pageable.getOffset()+pageable.getPageSize();
				sb = new StringBuilder(" limit ");
				sb.append(firstResult);
				sb.append(',');
				sb.append(maxResults);
				limit = sb.toString();
				sql = ssql.replaceFirst(Placeholder.LIMIT_RGE, limit);
				sql = sql.replaceAll(Placeholder.SP1_REG, "?");
				LOG.debug("下一页的SQL:" + sql);
				try {
					close(rs, stat, null); // 在新创建rs,stat 先把之前的关闭掉
					stat = conn.prepareStatement(sql); // stat 会在下面的finally里关闭.
					for (int i = 1; i <= ints.length; i++) { // 注意: ints并不是args的长度,而是sql中包含的参数与方法参数的对应关系数组
						stat.setObject(i, args[ints[i-1]-1]);
					}
					rs = stat.executeQuery();
					boolean next = rs.next();
					hasNext = next; // 下一页有数据
					isLast = !next; // 下一页没有数据了,表明这是最后一页了.
				} catch (SQLException e) {
					throw new RepositoryException(e.getMessage(),e);
				} finally {
					close(rs, stat, conn);
				}
			}
			
			boolean isFirst = number == 1;
			Slice nextPageable = new Slice((!isLast) ? (number + 1) : number, size);
			Slice previousPageable = new Slice((!isFirst) ? (number - 1) : number, size);
			
			return new PageImpl(size, numberOfElements, number, keyvals,totalElements, totalPages, hasContent, hasNext, hasPrevious,isFirst, isLast, nextPageable, previousPageable);
		default:
			break;
		}
		return null;
	}
	
	
	/**
	 * 将 rs 的结果集 转换成 List<Map>, rs没有结果则返回空对象(该方法永不返回null).
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	public List<Map<String, Object>> rs2Map(ResultSet rs) throws SQLException {

		List<Map<String, Object>> keyvals = new ArrayList<>();
		Map<String, Object> keyval;
		// 获取列信息
		ResultSetMetaData resultSetMetaData;
		// 总列数
		int columnCount;

		String key;
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
		return keyvals;
	}
		
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
			throw new RepositoryException(e.getMessage(),e);
		} finally {
			try {
				if (stat != null) {
					stat.close();
				}
			} catch (SQLException e) {
				LOG.error(e.getMessage(),e);
			} finally {
				try {
					if (conn != null) {
						conn.close();
					}
				} catch (SQLException e) {
					LOG.error(e.getMessage(),e);
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
	
	
	/**
	 * 根据regex在target中首次匹配到的开始索引, 没有匹配到返回-1
	 * @param regex 正则表达式
	 * @return
	 */
	private static int ignoreCaseWordStartIndex(String regex,String target){
		Matcher m = matcherFind(regex,target); 
		if( m == null ){
			return -1;
		} else {
			return m.start();
		}
	}
	
	/**
	 * 根据regex在target中首次匹配到的结束索引, 没有匹配到返回-1
	 * @param regex 正则表达式
	 * @return
	 */
	private static int ignoreCaseWordEndIndex(String regex,String target){
		Matcher m = matcherFind(regex,target); 
		if( m == null ){
			return -1;
		} else {
			return m.end();
		}
	}
	
	private static Matcher matcherFind(String regex,String target){
		Pattern pattern = Pattern.compile(regex);
		 Matcher m = pattern.matcher(target);
		 if(m.find()) {
			 return m;
		 }
		 return null;
	}
}
