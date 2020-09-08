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

package org.fastquery.dialect;

import org.fastquery.core.Placeholder;
import org.fastquery.page.PageDialect;
import org.fastquery.util.TypeUtil;

import java.util.List;
import java.util.regex.Matcher;

/**
 * 
 * @author mei.sir@aliyun.cn
 */
class MySQLPageDialect implements PageDialect {
	
	private MySQLPageDialect(){
	}
	private static class LazyHolder {
		private static final MySQLPageDialect INSTANCE = new MySQLPageDialect();
		private LazyHolder() {
		}
	}
	
	static PageDialect getInstance() {
		return LazyHolder.INSTANCE;
	}

	@Override
	public String getCurrentPageSQL(String querySQL,int offset, int pageSize) {
		
		// limit语句
		String limit = " limit " + offset + ',' + pageSize;

		List<String> strs = TypeUtil.matches(querySQL, Placeholder.LIMIT_RGE);
		if (strs.isEmpty()) { // 如果没有#{#limit}, 默认在末尾增加.
			querySQL += Placeholder.LIMIT;
		}
		
		return querySQL.replaceAll(Placeholder.LIMIT_RGE, Matcher.quoteReplacement(limit));
	}
}
