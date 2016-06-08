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

import org.fastquery.core.Query;
import org.fastquery.filter.generate.common.MethodFilter;

/**
 * SQL 安全检测
 * @author xixifeng (fastquery@126.com)
 */
public class SQLFilter implements MethodFilter {

	@Override
	public Method doFilter(Method method) {
		Query[] queries = method.getAnnotationsByType(Query.class);
		for (Query query : queries) {
			String sql = query.value();
			
			if("".equals(sql)) {
				this.abortWith(method, sql+"该方法,没有标注任何SQL语句");
			}
			
			if(sql.length()<6) {
				this.abortWith(method, sql+"SQL语法错误");
			}
		}
		
		return method;
	}

}
