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

package org.fastquery.mapper;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.fastquery.core.Param;
import org.fastquery.core.Placeholder;
import org.fastquery.core.QueryByNamed;
import org.fastquery.core.Repository;
import org.fastquery.page.Page;
import org.fastquery.util.TypeUtil;

import java.util.Set;

/**
 * 所有的代码生成之后,检测query, 尾部集中检测
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
	   
	   for (String query : queries) {
		   // 1). 
		   if(!TypeUtil.matches(query, ":\\s+").isEmpty()){
				error("表达式不符合规范:冒号\":\"后面不能是空白", query);
			}
			
			if(!TypeUtil.matches(query, "\\$\\{?\\s+").isEmpty()){
				error("表达式不符合规范:不能出现\"${ \" 或 \"$ \"", query);
			}
			
			if(!TypeUtil.matches(query, "\\{\\s+").isEmpty()){
				error("表达式不符合规范:不能出现\"{ \"", query);
			}
			
			if(!TypeUtil.matches(query, "\\s+\\}").isEmpty()){
				error("表达式不符合规范:不能出现\" }\"", query);
			}
			
			if(!TypeUtil.matches(query, "[^\\$#]\\{").isEmpty()){
				error("表达式不符合规范:\"{\"前必须连接\"$\"或\"#\"", query);
			}
			List<String> mps = TypeUtil.matches(query, Placeholder.EL_REG);
			for (String mp : mps) {
				int c1 = TypeUtil.matches(mp, "\\{").size();
				int c2 = TypeUtil.matches(mp, "\\}").size();
				if(c1==c2 && c1==0) {
					continue;
				}
				if( c1 != c2 || (c1 == c2 && c1 != 1)) { // "{" 和 "}" 必须成比出现
					error(String.format("\"%s\"中的\"{\"和\"}\"分别只能出现一次或都不出现,据分析\"{\"出现%d次,而\"}\"出现%d次", mp,c1,c2), query);
				}
			}
			mps.clear();
			
			
		   // 2). 校验微笑表达式中的内容 
		   List<String> smiles = TypeUtil.matches(query, Placeholder.SMILE_BIG);
		   for (String smile : smiles) {
			   int len = TypeUtil.matches(smile, Placeholder.EL_OR_COLON).size();
			   if(len!=1){
				   error("微笑表达式中的内容必须只能包含一个$表达式或一个冒号表达式,而它包含了"+len+"个表达式", query);
			   }
		   }
		   smiles.clear();
		   
		   // 3). query装载后,就不能再出现 #{#name} 表达式了
		   List<String> mts = TypeUtil.matches(query, "#\\{#\\S+\\}");
		   if(  !mts.isEmpty() ) {
			   error(String.format("没有找到name=\"%s\"的part", mts.get(0).replace("{", "").replace("}", "").replace("#", "")), query);
		   }
		   
	   }
	   


		// 10). 检验方法
		for (Class<Repository> cls : classes) {
			String className = cls.getName();
			Method[] methods = cls.getMethods();
			for (Method method : methods) {
				QueryByNamed queryByNamed = method.getAnnotation(QueryByNamed.class);
				if (queryByNamed == null) {
					continue;
				}
				
				
				String id = queryByNamed.value();
				if ("".equals(id)) {
					id = method.getName();
				}
				// m1: 标识有@QueryByNamed的方法,必须有对应的模板
				String tmp = getTemplate(className, id, mapQueryMapper.get(className));
				if (tmp == null) {
					error(method, String.format("从%s.queries.xml里没有找到id为%s的模板,模板文件区分大小写,很有可能%s.queries.xml不存在.",
							className, id, className));
					return;
				}
				if ("".equals(tmp.trim())) {
					error(method, String.format("在%s.queries.xml里,id为%s的模板不能为空字符串", className, id));
				}

				String key = className + "." + id;
				// m2: 如果是分页,必须有求和语句
				Class<?> returnType = method.getReturnType();
				String countQuery = QueryPool.getCountQuery(key);
				if (returnType == Page.class && countQuery == null) {
					error(method,
							String.format("该方法指明需要分页. 而在%s.queries.xml里,<query id=\"%s\">下面没有发现求和语句", className, id));
					return;
				}
				if (returnType == Page.class && "".equals(countQuery.trim())) {
					error(method,
							String.format("该方法指明需要分页. 而在%s.queries.xml里,<query id=\"%s\">下面的求和语句是空字符串", className, id));
				}
				
				
				// m3: 模板中的冒号表达式必须从方法参数中的@Param找到精准匹配
				Set<String> params = new HashSet<>();
				Parameter[] parameters = method.getParameters();
				for (Parameter parameter : parameters) {
					Param param = parameter.getAnnotation(Param.class);
					if(param != null) {
						params.add(param.value());
					}
				}
				Set<String> ps = new HashSet<>();
				ps.addAll(TypeUtil.matchesNotrepeat(tmp, Placeholder.COLON_REG));
				ps.addAll(TypeUtil.matchesNotrepeat(countQuery, Placeholder.COLON_REG));
				for (String p : ps) {
					String s = null;
					if(Pattern.matches(Placeholder.COLON_REG, p)) {
						s = p.replaceFirst(":","");
					} 
					if(!params.contains(s)) {
						error(method, String.format("%n%s.queries.xml%n从<query id=\"%s\">节点内的语句中发现存在%s,而从方法参数中没有找到@Param(\"%s\").",className,id,p,s));
					}
				}
				ps.clear();
				
				
				// m4: 
				
				
				
			}
		}
	   
	   queries.clear();
   }
   
   
   
   private static void error(Method method,String msg){
	   throw new ExceptionInInitializerError(String.format("%s->: %s ", method.toString(),msg));
   }
   
   private static void error(String msg,String query){
	   throw new ExceptionInInitializerError(msg+ ", 错误位置>>>>>>>>>>>>>>: " + query);
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
















