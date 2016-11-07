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

package org.fastquery.mapper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.fastquery.core.QueryByNamed;
import org.fastquery.core.Repository;
import org.fastquery.page.Page;

import java.util.Set;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class QueryValidator {
   private QueryValidator(){}
   public static void check(List<Class<Repository>> classes){
	   List<String> queries = new ArrayList<>();
	   
	   Map<String, String> countQueryMap = QueryPool.getCountQueryMap();
	   countQueryMap.forEach((k,v) -> queries.add(v));
	   
	   Map<String, Set<QueryMapper>> mapQueryMapper = QueryPool.getMapQueryMapper();
	   Set<Entry<String, Set<QueryMapper>>> queryMappers = mapQueryMapper.entrySet();
	   for (Entry<String, Set<QueryMapper>> entry : queryMappers) {
		   Set<QueryMapper> qms = entry.getValue();
		   qms.forEach(q -> queries.add(q.getTemplate()));
	   }
	   
	   // 1). query中不能出现 ";"
	   for (String query : queries) {
		   if(query.indexOf(';') != -1) {
			   throw new ExceptionInInitializerError("禁止出现\";\"号, 错误位置>>>>>>>>>>>>>>: " + query);
		   }
	   }
	   

	   // 2). 检验方法
	   for (Class<Repository> cls : classes) {
		   String className = cls.getName();
		   Method[] methods = cls.getMethods();
			for (Method method : methods) {
				QueryByNamed queryByNamed = method.getAnnotation(QueryByNamed.class);
				if(queryByNamed!=null) {
					
					// m1: QueryByNamed的值不能为""
					if("".equals(queryByNamed.value())){
						error(method, "@QueryByNamed的值不能为空字符串");
					}
					
					// m2: 标识有@QueryByNamed的方法,必须有对应的模板
					String tmp = getTemplate(className, queryByNamed.value(), mapQueryMapper.get(className));
					if(tmp==null) {
						error(method, String.format("从%s.queries.xml里没有找到id为%s的模板", className,queryByNamed.value()));
					}
					if("".equals(tmp.trim())) {
						error(method, String.format("在%s.queries.xml里,id为%s的模板不能为空字符串", className,queryByNamed.value()));
					}
					
					String key = className + "." + queryByNamed.value();
					
					// m3: 如果是分页,必须有求和语句
					Class<?> returnType = method.getReturnType();
					if(returnType == Page.class && QueryPool.getCountQuery(key)==null) {
						error(method, String.format("该方法指明需要分页. 而在%s.queries.xml里,<query id=\"%s\">下面没有发现求和语句", className,queryByNamed.value()));
					}
					if(returnType == Page.class && "".equals(QueryPool.getCountQuery(key).trim())) {
						error(method, String.format("该方法指明需要分页. 而在%s.queries.xml里,<query id=\"%s\">下面的求和语句是空字符串", className,queryByNamed.value()));
					}
				}
			}
	   }
	   
	   queries.clear();
   }
   
   
   
   private static void error(Method method,String msg){
	   throw new ExceptionInInitializerError(String.format("%s->: %s ", method.toString(),msg));
   }
   
	static String getTemplate(String className,String id,Set<QueryMapper> queryMappers) {
		if(className == null || id == null) {
			return null;
		}
		if(queryMappers == null){
			return null;
		}
		for (QueryMapper queryMapper : queryMappers) {
			if(queryMapper.getId().equals(id)){
				return queryMapper.getTemplate();
			}
		}
		return null;
	}
	
}
















