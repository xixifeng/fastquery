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

package org.fastquery.mapper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.fastquery.core.Placeholder;
import org.fastquery.core.QueryByNamed;
import org.fastquery.page.NotCount;
import org.fastquery.page.Page;
import org.fastquery.util.TypeUtil;

import java.util.Set;

/**
 * 所有的代码生成之后,检测query, 尾部集中检测
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class QueryValidator {
	private QueryValidator() {
	}

	public static void check(List<Class<?>> classes) {
		List<String> queries = new ArrayList<>();

		Map<String, String> countQueryMap = QueryPool.getCountQueryMap();
		countQueryMap.forEach((k, v) -> queries.add(v));

		Map<String, Set<QueryMapper>> mapQueryMapper = QueryPool.getMapQueryMapper();
		Set<Entry<String, Set<QueryMapper>>> queryMappers = mapQueryMapper.entrySet();
		for (Entry<String, Set<QueryMapper>> entry : queryMappers) {
			Set<QueryMapper> qms = entry.getValue();
			qms.forEach(q -> queries.add(q.getTemplate()));
		}

		for (String query : queries) {
			// 1). 
			checkQuery(query);

			// 2). 校验微笑表达式中的内容
			checkSmile(query);

			// 3). query装载后,就不能再出现 #{#name} 表达式了
			checkSharp(query);

			// 4). <where> </where> 只能是小写
			checkWhere(query);
		}

		// 10). 检验方法
		for (Class<?> cls : classes) {
			String className = cls.getName();
			Method[] methods = cls.getMethods();
			for (Method method : methods) {
				checkQueryByNamed(mapQueryMapper, className, method);
			}
		}

		queries.clear();
	}

	private static void checkQueryByNamed(Map<String, Set<QueryMapper>> mapQueryMapper, String className, Method method) {
		QueryByNamed queryByNamed = method.getAnnotation(QueryByNamed.class);
		if (queryByNamed != null) {
			String id = queryByNamed.value();
			if ("".equals(id)) {
				id = method.getName();
			}
			
			// m1: 标识有@QueryByNamed的方法,必须有对应的模板
			String tmp = getTemplate(className, id, mapQueryMapper.get(className));
			if (tmp == null) {
				error(method, String.format("从%s.queries.xml里没有找到id为%s的模板,模板文件区分大小写.", className, id));
			} else if ("".equals(tmp.trim())) {
				error(method, String.format("在%s.queries.xml里,id为%s的模板不能为空字符串", className, id));
			} else {
				String key = className + "." + id;
				String countQuery = QueryPool.getCountQuery(key);

				// m2: 如果是分页,并且没有标识@NotCount,必须有求和语句
				Class<?> returnType = method.getReturnType();
				if (returnType == Page.class && (countQuery == null || "".equals(countQuery.trim()))
						&& method.getAnnotation(NotCount.class) == null) {
					error(method, String.format("该方法指明要分页,而在%s.queries.xml里,<query id=\"%s\">下面没有发现count语句.分页而不统计总行数,可以在当前方法上标识@NotCount.", className, id));
				}

				// 注意: 模板中的冒号表达式不必从方法参数中的@Param找到精准匹配, 因为模板是可以共享的, 假设 F1,F2两个方法公用M模板, M中包含:a,:b,而F1只包含@Param("a"),F2只包含@Param("b"),这种情形,当然是允许的.

				// m3	
			}
		}
	}

	private static void checkWhere(String query) {
		Set<String> ss = TypeUtil.matchesNotrepeat(query, "(?i)</?where>");
		ss.forEach(s -> {
			if (!"<where>".equals(s) && !"</where>".equals(s)) {
				error("<where> 或 </where> 只能是小写", query);
			}
		});
	}

	private static void checkSharp(String query) {
		List<String> mts = TypeUtil.matches(query, "#\\{#\\S+\\}");
		mts.remove(Placeholder.LIMIT); // 内置标签不参与校验 
		if (!mts.isEmpty()) {
			error(String.format("没有找到name=\"%s\"的part", mts.get(0).replace("{", "").replace("}", "").replace("#", "")), query);
		}
	}

	private static void checkSmile(String query) {
		List<String> smiles = TypeUtil.matches(query, Placeholder.SMILE_BIG);
		for (String smile : smiles) {
			int len = TypeUtil.matches(smile, Placeholder.EL_OR_COLON).size();
			if (len != 1) {
				error("微笑表达式中的内容必须只能包含一个$表达式或一个冒号表达式,而它包含了" + len + "个表达式", query);
			}
		}
		smiles.clear();
	}
	
	private static void checkQuery(String query) {
		if (!TypeUtil.matches(query, ":\\s+").isEmpty()) {
			error("表达式不符合规范:冒号\":\"后面不能是空白", query);
		}

		if (!TypeUtil.matches(query, "\\$\\{?\\s+").isEmpty()) {
			error("表达式不符合规范:不能出现\"${ \" 或 \"$ \"", query);
		}

		if (!TypeUtil.matches(query, "\\{\\s+").isEmpty()) {
			error("表达式不符合规范:不能出现\"{ \"", query);
		}

		if (!TypeUtil.matches(query, "\\s+\\}").isEmpty()) {
			error("表达式不符合规范:不能出现\" }\"", query);
		}

		if (!TypeUtil.matches(query, "[^\\$#]\\{").isEmpty()) {
			error("表达式不符合规范:\"{\"前必须连接\"$\"或\"#\"", query);
		}
		List<String> mps = TypeUtil.matches(query, Placeholder.EL_REG);
		for (String mp : mps) {
			int c1 = TypeUtil.matches(mp, "\\{").size();
			int c2 = TypeUtil.matches(mp, "\\}").size();
			if (c1 != c2 || c1 > 1) { // "{" 和 "}" 必须成比出现一次
				error(String.format("\"%s\"中的\"{\"和\"}\"分别只能出现一次或都不出现,据分析\"{\"出现%d次,而\"}\"出现%d次", mp, c1, c2), query);
			}
		}
		mps.clear();
	}

	private static void error(Method method, String msg) {
		throw new ExceptionInInitializerError(String.format("%s->: %s ", method.toString(), msg));
	}

	private static void error(String msg, String query) {
		throw new ExceptionInInitializerError(msg + ", 错误位置>>>>>>>>>>>>>>: " + query);
	}

	private static String getTemplate(String className, String id, Set<QueryMapper> queryMappers) {
		if (className == null || id == null) {
			return null;
		}
		if (queryMappers == null) {
			return null;
		}
		for (QueryMapper queryMapper : queryMappers) {
			if (queryMapper.getId().equals(id)) {
				return queryMapper.getTemplate();
			}
		}
		return null;
	}

}
