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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.fastquery.handler.ModifyingHandler;
import org.fastquery.handler.QueryHandler;
import org.fastquery.page.NotCount;
import org.fastquery.page.PageImpl;
import org.fastquery.page.Pageable;
import org.fastquery.page.PageableImpl;
import org.fastquery.page.Slice;
import org.fastquery.struct.RespUpdate;
import org.fastquery.struct.SQLValue;
import org.fastquery.util.BeanUtil;
import org.fastquery.util.FastQueryJSONObject;
import org.fastquery.util.TypeUtil;
import org.objectweb.asm.Type;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class QueryProcess {
	
	private static final Logger LOG = LoggerFactory.getLogger(QueryProcess.class);
	
	private static QueryProcess queryProcess;
	
	private QueryProcess(){
		
	}
	
	/**
	 * 获取QueryProcess实例
	 * @return QueryProcess
	 */
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
	Object modifying() {
		Method method = QueryContext.getMethod();
		Class<?> returnType = QueryContext.getReturnType();
		
		// 获取待执行的sql
		List<SQLValue> sqlValues = QueryParser.modifyParser();
		
		// 执行
		List<RespUpdate> respUpdates = DB.modify(sqlValues, true, true);
		
		Long autoIncKey = respUpdates.get(0).getPk();
		
		// 返回类型分析=====================================
		ModifyingHandler mh = ModifyingHandler.getInstance();
		if(returnType==void.class) {
			return mh.voidType();
		} else if(returnType == int.class) {
			return mh.intType(respUpdates);
		} else if(returnType == int[].class ){
			int len = respUpdates.size();
			int[] effects = new int[len];
			for (int i = 0; i < len; i++) {
				effects[i] = respUpdates.get(i).getEffect();
			}
			return effects;
		} else if(returnType == Map.class) { // 如果然会值是Map,那么一定是insert或update,在生成实现的时候已经做安全检测
			return mh.mapType(autoIncKey,TypeUtil.mapValueTyep(method));
		} else if(returnType == JSONObject.class) {
			return mh.jsonObjectType(autoIncKey);
		} else if(returnType == Primarykey.class) {
			return mh.primarykeyType(autoIncKey);
		} else if(returnType == boolean.class) {
			return mh.booleanType(respUpdates);
		} else { // 把值强制转换成 returnType
			return mh.beanType(autoIncKey);
		}
		// 返回类型分析===================================== End
		
	}
	
	// 查操作
	Object query() {
		
		Method method = QueryContext.getMethod();
		Class<?> returnType = QueryContext.getReturnType();
		SQLValue sqlValue =  QueryParser.queryParser();
		List<Map<String, Object>> keyvals = DB.find(sqlValue);
		
		// 上面的try发生异常了,才会导致keyvals为null, 不过异常一旦捕获到就throw了,因此,程序执行到这里keyvals不可能为null.
		// 返回类型分析=====================================
		QueryHandler qh = QueryHandler.getInstance();
		if(returnType == long.class) {
			return qh.longType(keyvals);
		} else if(returnType == int.class) {
			return qh.intType(keyvals);
		} else if(returnType == boolean.class) {
			return qh.booleanType(keyvals);
		} else if(returnType == Map.class){
			return qh.mapType(keyvals,TypeUtil.mapValueTyep(method));
		} else if(TypeUtil.isListMapSO(method.getGenericReturnType())){
			return qh.listType(keyvals,TypeUtil.listMapValueTyep(method));
		}else if(returnType == List.class){
			return qh.list(keyvals);
		}else if(returnType == JSONObject.class){
			return qh.jsonObjeType(keyvals);
		}else if(returnType == JSONArray.class){
			return qh.jsonArrayType(keyvals);
		}else if(TypeUtil.isWarrp(returnType)){
			return qh.wrapperType(method,returnType,keyvals);
		}else if(TypeUtil.isWarrp(returnType.getComponentType()) || TypeUtil.hasDefaultConstructor(returnType.getComponentType())){
			// 基本类型数组, bean数组
			return qh.wrapperAarryType(returnType,keyvals);
		}else {
			return qh.beanType(keyvals);
		}
		// 返回类型分析===================================== End
	}
	
	// 分页查询
	@SuppressWarnings({ "rawtypes", "unchecked" })
	Object queryPage() {
		Method method = QueryContext.getMethod();
		Object[] args = QueryContext.getArgs();
		
		
		Pageable pageable = null;
		for (Object arg : args) {
			if(arg instanceof Pageable) { // 如果当前arg是Pageable接口的一个实例
				pageable = (Pageable) arg;
				break;
			}
		}
		Parameter[] parameters = method.getParameters();
		if(pageable == null ) {
			// 没有传递Pageable,那么必然有 pageIndex, pageSize 不然,不能通过初始化
			pageable = new PageableImpl(TypeUtil.findPageIndex(parameters, args), TypeUtil.findPageSize(parameters, args));
		}

		List<SQLValue> sqlValues = QueryParser.pageParser();
		List<Map<String, Object>> keyvals = DB.find(sqlValues.get(0));
		
		int size = pageable.getPageSize();      // 每页多少条数据
		long totalElements = -1L;               // 总行数,如果不求和默认-1L
		int totalPages = -1;                    // 总页数,如果不求和默认-1
		int numberOfElements = keyvals.size();  // 每页实际显示多少条数据
		int number = pageable.getPageIndex();  // 当前页码
		boolean hasContent = !keyvals.isEmpty();// 这页有内容吗?
		boolean hasPrevious = (number > 1) && hasContent;// number不是第1页且当前页有数据,就可以断言它有上一页.
		boolean hasNext;                                 // 有下一页吗? 在这里不用给默认值,如下一定会给他赋值.
		boolean isLast;
		
		if(method.getAnnotation(NotCount.class)==null) {
			 
			List<Map<String, Object>> results = DB.find(sqlValues.get(1));
			if(!results.isEmpty()) {
				totalElements = (long)results.get(0).values().iterator().next();	
			} else {
				totalElements = 0;
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
				List<Map<String, Object>> nextvalues = DB.find(sqlValues.get(1));
				boolean next = nextvalues.isEmpty();
				hasNext = !next; // 下一页有数据
				isLast = next; // 下一页没有数据了,表明这是最后一页了.
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

	    // 分页查询(仅针对QueryByNamed Page分页查询,不针对Query)
		@SuppressWarnings({ "rawtypes", "unchecked" })
		Object queryByNamedPage() {
			Method method = QueryContext.getMethod();
			Object[] args = QueryContext.getArgs();
			// 获取 pageable
			Pageable pageable = null;
			for (Object arg : args) {
				if(arg instanceof Pageable) { // 如果当前arg是Pageable接口的一个实例
					pageable = (Pageable) arg;
					break;
				}
			}
			Parameter[] parameters = method.getParameters();
			if(pageable == null ) {
				// 没有传递Pageable,那么必然有 pageIndex, pageSize 不然,不能通过初始化
				pageable = new PageableImpl(TypeUtil.findPageIndex(parameters, args), TypeUtil.findPageSize(parameters, args));
			}
			// 获取 pageable End
			
			List<SQLValue> sqlValues = QueryParser.pageParserByNamed();
			List<Map<String, Object>> keyvals = DB.find(sqlValues.get(0));
			
			int size = pageable.getPageSize();      // 每页多少条数据
			long totalElements = -1L;               // 总行数,如果不求和默认-1L
			int totalPages = -1;                    // 总页数,如果不求和默认-1
			int numberOfElements = keyvals.size();  // 每页实际显示多少条数据
			int number = pageable.getPageIndex();  // 当前页码
			boolean hasContent = !keyvals.isEmpty();// 这页有内容吗?
			boolean hasPrevious = (number > 1) && hasContent;// number不是第1页且当前页有数据,就可以断言它有上一页.
			boolean hasNext;                                 // 有下一页吗? 在这里不用给默认值,如下一定会给他赋值.
			boolean isLast;
			
			if(method.getAnnotation(NotCount.class)==null) { // 需要求和
				List<Map<String, Object>> results = DB.find(sqlValues.get(1));
					if(!results.isEmpty()) {
						totalElements = (long)results.get(0).values().iterator().next();	
					} else {
						totalElements = 0;
					}
				
				// 计算总页数
				totalPages = ((int) totalElements) / size;
				if (((int) totalElements) % size != 0) {
					totalPages += 1;
				}
				hasNext = number < totalPages;
				isLast = number == totalPages;
			} else {
					List<Map<String, Object>> nextvalues = DB.find(sqlValues.get(1));
					boolean next = nextvalues.isEmpty();
					hasNext = !next; // 下一页有数据
					isLast = next; // 下一页没有数据了,表明这是最后一页了.
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
	
	@SuppressWarnings("unchecked")
	Object methodQuery(Id id) {
		Method method = QueryContext.getMethod();
		Object[] iargs = QueryContext.getArgs();
		// 检验实体
		Parameter[] parameters = method.getParameters();
		for (int i = 0; i < parameters.length; i++) {
			if( parameters[i].getParameterizedType() instanceof TypeVariable ) { // 这个类型是变量类型吗?
				Field[] fields = iargs[i].getClass().getDeclaredFields();
				for (Field field : fields) {
					if( Type.getType(field.getType()).getSort() != Type.OBJECT ) {
						throw new RepositoryException(String.format("%s这个实体的成员变量%s %s %s不允许是基本类型", iargs[i].getClass().getName(),Modifier.toString(field.getModifiers()),field.getType().getName(),field.getName()));
					}
				}
			}	
		}
		// 检验实体 end
		byte methodId = id.value();
		
		Object bean;
		String sql;
		String dbName = null;
		boolean ignoreRepeat;
		switch (methodId) {
		case MethodId.QUERY:
			if(iargs.length == 3) {
				bean = iargs[2];
				sql = BeanUtil.toInsertSQL(iargs[1].toString(),bean);
				LOG.info(sql);
				Object keyObj = DB.insert(sql);
				if(keyObj==null){
					return new BigInteger("-1");
				} else {
					return new BigInteger(keyObj.toString());
				}
			} else {
				bean = iargs[0];
				sql = BeanUtil.toInsertSQL(bean,false);
				LOG.info(sql);
				Object keyObj = DB.insert(sql);
				if(keyObj==null){
					return new BigInteger("-1");
				} else {
					return new BigInteger(keyObj.toString());
				}
			}
			
		case MethodId.QUERY0:
			if(iargs.length == 3) {
				bean = iargs[2];
				sql = BeanUtil.toInsertSQL(iargs[1].toString(),bean);
				LOG.info(sql);
				Object keyObj = DB.insert(sql);
				if(keyObj==null){
					return null;
				} else {
					sql = BeanUtil.toSelectSQL(bean, keyObj.toString(), iargs[1].toString());
					return DB.select(sql, bean);
				}
			} else {
				bean = iargs[0];
				sql = BeanUtil.toInsertSQL(bean,false);
				LOG.info(sql);
				Object keyObj = DB.insert(sql);
				if(keyObj==null){
					return null;
				} else {
					sql = BeanUtil.toSelectSQL(bean, keyObj.toString(), null);
					return DB.select(sql, bean);	
				}
			}
		
		case MethodId.QUERY1:
			if (iargs.length == 1) {
				bean = iargs[0];
			} else if (iargs.length == 2) {
				bean = iargs[1];
			} else {
				dbName = iargs[1].toString();
				bean = iargs[2];
			}
			sql = DB.update(bean, dbName);
			if(sql==null){
				return null;
			}
			return DB.select(sql, bean);
			
		case MethodId.QUERY2:
			if (iargs.length == 1) {
				bean = iargs[0];
			} else if(iargs.length == 2) {
				bean = iargs[1];
			} else {
				dbName = iargs[1].toString();
				bean = iargs[2];
			}
			sql = BeanUtil.toSelectSQL(bean, null, dbName);
			if(DB.exists(sql, bean)) {
				// 更新
				DB.update(bean, dbName);
			} else {
				// 保存
				DB.insert((iargs.length==3) ? BeanUtil.toInsertSQL(iargs[1].toString(),bean) : BeanUtil.toInsertSQL(bean,false));
			}
			return DB.select(sql, bean);			
		case MethodId.QUERY3:
			if (iargs.length == 2) {
				bean = iargs[0];
			} else if (iargs.length == 3) {
				bean = iargs[1];
			} else {
				dbName = iargs[1].toString();
				bean = iargs[2];
			}
			return DB.update(bean, dbName,(String)iargs[iargs.length-1]);
		case MethodId.QUERY4:
			ignoreRepeat = (boolean) iargs[0];
			Object entitiesObj = iargs[iargs.length-1];
			if(iargs.length == 4) {
				dbName = iargs[2].toString();
			}
			if(entitiesObj.getClass().isArray()) {
				sql = BeanUtil.arr2InsertSQL((Object[])entitiesObj, dbName, ignoreRepeat);
			} else {
				sql = BeanUtil.toInsertSQL((Iterable<Object>)entitiesObj, dbName, ignoreRepeat);
			}
			LOG.info(sql);
			return DB.insert2(sql);
			
		case MethodId.QUERY6:
			String basedir = FastQueryJSONObject.getBasedir();
			String sqlFile = basedir + (String)iargs[0];
			try {
				DB.executeBatch(sqlFile);
			} catch (Exception e) {
				throw new RepositoryException(e);
			}
			
			break;
		default:
			break;
		}
		return null;
	}
	
	Object methodQuery() {
		// 有待扩展
		return null;
	}
}
