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

import java.util.Objects;

/**
 * 
 * @author mei.sir@aliyun.cn
 */
public class MetaData { // NO_UCD (use default)

	private String query; // 为null表示没有设置
	private String countQuery; // 为null表示没有设置
	private String where = "";
	private String countField = "id"; // count字段

	MetaData() {
	}

	private void requireQuery(String query) {
		Objects.requireNonNull(query, "query 语句必须设置正确");
	}

	/**
	 * 设置查询语句,不能设置null
	 * 
	 * @param query 查询语句
	 */
	public void setQuery(String query) {
		requireQuery(query);
		this.query = query;
	}

	public void setCountQuery(String countQuery) {
		if(!"".equals(countQuery)) {
			this.countQuery = countQuery;
		}
	}

	/**
	 * 不设置,默认是"id"
	 * 
	 * @param countField count 字段
	 */
	public void setCountField(String countField) {
		if (countField != null && !"".equals(countField)) {
			this.countField = countField;
		}
	}

	String getQuery() {
		requireQuery(query);
		return query + ' ' + where;
	}

	String getCountQuery() {
		if (countQuery == null) {
			return "";
		}
		return countQuery + ' ' + where;
	}

	String getCountField() {
		return countField;
	}

	void clear() {
		query = null;
		countQuery = null;
		where = null;
		countField = null;
	}

	/**
	 * 设置查询条件. 支持引用问号表达式(?index) , 冒号表达式(:name) <br>
	 * 注意: 要么不设置,要设置就别设置null.
	 * 
	 * @param where 查询条件
	 */
	public void setWhere(String where) {
		Objects.requireNonNull(where);
		this.where = where;
	}
}
