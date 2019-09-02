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

package org.fastquery.analysis;

import java.lang.reflect.Method;

import org.fastquery.core.Query;
import org.fastquery.core.QueryBuilder;
import org.fastquery.util.TypeUtil;

/**
 * SQL 安全检测
 * 
 * @author xixifeng (fastquery@126.com)
 */
class SQLFilter implements MethodFilter {

	@Override
	public void doFilter(Method method) {
		Query[] queries = method.getAnnotationsByType(Query.class);
		boolean b = TypeUtil.hasType(QueryBuilder.class, method.getParameters());
		for (Query query : queries) {
			String sql = query.value();

			if ("".equals(sql) && !b) { // 如果@Query的value为"",并且又没有传递QueryBuilder(用于构建SQL)
				this.abortWith(method, sql + "该方法,没有标注任何SQL语句. 帮定SQL又多种方式:通过@Query;采用xml模板;用QueryBuilder,或参数传入...");
			}else if (sql.length() < 2 && !b) {
				this.abortWith(method, sql + "SQL语法错误");
			}
		}

	}

}
