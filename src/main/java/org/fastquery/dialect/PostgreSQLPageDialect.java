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

import org.fastquery.page.PageDialect;

/**
 * 
 * @author mei.sir@aliyun.cn
 */
public class PostgreSQLPageDialect implements PageDialect {
	
	private PostgreSQLPageDialect(){
	}
	private static class LazyHolder {
		private static final PostgreSQLPageDialect INSTANCE = new PostgreSQLPageDialect();
		private LazyHolder() {
		}
	}
	
	public static PageDialect getInstance() {
		return LazyHolder.INSTANCE;
	}

	@Override
	public String getCurrentPageSQL(String querySQL, int offset, int pageSize) {

		StringBuilder sb = new StringBuilder();
		sb.append(querySQL);
		sb.append(" limit ");
		sb.append(pageSize);
		sb.append(" offset ");
		sb.append(offset);

		return sb.toString();
	}

}
