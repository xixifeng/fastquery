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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;

import org.fastquery.dialect.DialectScheduler;
import org.fastquery.mapper.QueryPool;
import org.fastquery.page.PageDialect;
import org.fastquery.page.Pageable;
import org.fastquery.struct.ParamMap;
import org.fastquery.struct.SQLValue;
import org.fastquery.util.TypeUtil;

/**
 * 
 * @author mei.sir@aliyun.cn
 */
public class QueryParser {
	
	private QueryParser() {
	}

	/**
	 * 改操作分析
	 * 
	 * @param queries
	 * @return
	 */
	static List<SQLValue> modifyParser() {
		MethodInfo method = QueryContext.getMethodInfo();
		Object[] args = QueryContext.getArgs();
		Query[] queries = method.getQueries();

		Modifying modifying = method.getModifying();
		String id = modifying.id(); // 不可能为null
		String table = modifying.table();

		List<String> sqls = TypeUtil.getQuerySQL(method, queries, args);

		int sqlCount = sqls.size();

		List<SQLValue> msvs = new ArrayList<>(sqlCount);

		for (int jk = 0; jk < sqlCount; jk++) {
			// 获取sql
			String sql = sqls.get(jk);
			// 替换SQL中的占位变量符
			sql = sql.replaceAll(Placeholder.TABLE_REG, Matcher.quoteReplacement(table));
			sql = sql.replaceAll(Placeholder.ID_REG, Matcher.quoteReplacement(id));
			msvs.add(inParser(sql));
		}

		return msvs;
	}

	/**
	 * 如果没有标识@Query或@QueryByNamed,则返回null
	 * 
	 * @return
	 */
	static SQLValue queryParser() {
		MethodInfo method = QueryContext.getMethodInfo();
		Query[] queries = method.getQueries();
		String sql = TypeUtil.getQuerySQL(method, queries, QueryContext.getArgs()).get(0);
		return inParser(sql);
	}

	public static List<SQLValue> pageParser() {

		MethodInfo method = QueryContext.getMethodInfo();
		Object[] args = QueryContext.getArgs();
		
		Pageable pageable = QueryContext.getPageable();
		int offset = pageable.getOffset();
		int pageSize = pageable.getPageSize();
		
		Query[] querys = method.getQueries();
		String querySQL = TypeUtil.getQuerySQL(method, querys, args).get(0);
		
		PageDialect pageDialect = DialectScheduler.getCurrentPageDialect();

		String currentPageSQL = pageDialect.getCurrentPageSQL(querySQL, offset, pageSize);
		List<SQLValue> sqlValues = new ArrayList<>(2);// 有3条记录 0.当前页query,1.求和query,2.下一页query
		sqlValues.add(inParser(currentPageSQL));

		if (method.isCount()) { // 表明需要求和
			Query query = querys[0];
			String countField = query.countField();
			String countQuery = query.countQuery();
			String countPageSQL;
			if (countQuery == null || "".equals(countQuery)) { // 表明在声明时没有指定求和语句
				// 那么通过主体查询语句算出count语句,querySQL的where问题已经处理好
				countPageSQL = pageDialect.countSQLInference(querySQL, countField);
			} else {
				countPageSQL = countQuery.replaceFirst(Placeholder.WHERE_REG, Matcher.quoteReplacement(TypeUtil.getWhereSQL(method, args)));
			}
			
			sqlValues.add(inParser(countPageSQL));
		} else {
			offset = offset + pageSize;
			String nextRecordSQL =  pageDialect.getCurrentPageSQL(querySQL, offset, 1); 
			sqlValues.add(inParser(nextRecordSQL));
		}

		return sqlValues;
	}

	public static List<SQLValue> pageParserByNamed() {

		List<SQLValue> sqlValues = new ArrayList<>(2); // 有3条记录 0.当前页query,1.求和query,2.下一页query

		String query = QueryPool.render(true);

		MethodInfo method = QueryContext.getMethodInfo();
		// 获取 pageable
		Pageable pageable = QueryContext.getPageable();
		int offset = pageable.getOffset();
		int pageSize = pageable.getPageSize();

		// 获取sql
		String querySQL = TypeUtil.paramNameFilter(method, query);
		PageDialect pageDialect = DialectScheduler.getCurrentPageDialect();
		String currentPageSQL = pageDialect.getCurrentPageSQL(querySQL, offset, pageSize);
		sqlValues.add(inParser(currentPageSQL));

		if (method.isCount()) { // 需要求和
			String countPageSQL = QueryPool.render(false);
			countPageSQL = TypeUtil.paramNameFilter(method, countPageSQL);
			sqlValues.add(inParser(countPageSQL));
		} else {
			// 获取sql
			offset = offset + pageSize;
			String nextRecordSQL =  pageDialect.getCurrentPageSQL(querySQL, offset, 1); 
			sqlValues.add(inParser(nextRecordSQL));
		}

		return sqlValues;
	}
	
	private static SQLValue inParser(String sql) {
		int[] ints = TypeUtil.getSQLParameter(sql);
		// sql 中的"?"号调整
		// sql中的"?"可能会因为方法参数是一个集合,会变成多个, 举例说明: in (?) 那么这个?的实际个数取决于传递的集合长度
		ParamMap paramMap = TypeUtil.getParamMap(ints);
		Map<Integer, Integer> rpates = paramMap.getRps();
		List<Object> objs = paramMap.getObjs();

		Set<Entry<Integer, Integer>> entities = rpates.entrySet();
		for (Entry<Integer, Integer> entry : entities) {
			sql = TypeUtil.replace(sql, entry.getKey(), entry.getValue());
		}
		return new SQLValue(sql, objs);
	}
}
