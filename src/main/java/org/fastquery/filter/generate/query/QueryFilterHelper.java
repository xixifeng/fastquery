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

package org.fastquery.filter.generate.query;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.fastquery.core.Placeholder;
import org.fastquery.core.Query;
import org.fastquery.core.RepositoryException;
import org.fastquery.util.TypeUtil;
import org.fastquery.where.Condition;
import org.fastquery.where.Operator;

/**
 * 该的作用范围仅仅是当前包
 * @author xixifeng (fastquery@126.com)
 */
class QueryFilterHelper {
	
	private QueryFilterHelper(){}
	
	/**
	 * 获取SQL语句,不考虑条件是否参与运算问题.
	 * @param method
	 * @param query
	 * @return
	 */
	static List<String> getQuerySQL(Method method,Query[] queries){
		
		List<String> sqls = new ArrayList<>(queries.length);
		
		for (Query query : queries) {

			String sql = query.value();
			StringBuilder sb = new StringBuilder();
			// 追加条件
			Condition[] conditions = method.getAnnotationsByType(Condition.class);
			for (int i = 0; i < conditions.length; i++) {
				sb.append(' ');
				sb.append(conditions[i].c().getVal());
				sb.append(' ');	
				sb.append(conditions[i].l());
				sb.append(' ');
				Operator[] operators = conditions[i].o();
				for (Operator operator : operators) {
					sb.append(operator.getVal());
					sb.append(' ');
				}
				sb.append(conditions[i].r());
			}
			// 追加条件 End
			
			String where = sb.toString();
			if(!"".equals(where) && TypeUtil.matches(query.value(),Placeholder.WHERE_REG).size()!=1) {
				throw new RepositoryException(method + " 如果有条件注解,那么@Query中的value值,必须存在#{#where},有且只能出现一次");
			}
			sqls.add(sql.replaceFirst(Placeholder.WHERE_REG, sb.toString()));
		}
		
		return sqls;
	}
	
	static List<String> getQuerySQL(Method method){
		return getQuerySQL(method, method.getAnnotationsByType(Query.class));
	}
}
