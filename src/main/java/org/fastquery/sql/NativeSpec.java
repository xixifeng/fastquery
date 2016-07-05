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

package org.fastquery.sql;

import com.healthmarketscience.sqlbuilder.SelectQuery;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public abstract class NativeSpec {
	
	/**
	 * 构建查询限制
	 * @param selectQuery 用做构建select语句
	 * @param args 当前传递的实参
	 * @return SelectQuery
	 */
	public abstract Predicate toPredicate(SelectQuery selectQuery);
	
	protected Predicate build(SelectQuery selectQuery,Object... parameters) {
		return new Predicate(selectQuery, parameters);
	}
}