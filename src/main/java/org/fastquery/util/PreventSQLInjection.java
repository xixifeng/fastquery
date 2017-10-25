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

package org.fastquery.util;

/**
 * 
 * @author mei.sir@aliyun.cn
 */
public class PreventSQLInjection {

	private PreventSQLInjection() {
	}

	/**
	 * 判断是否是注入SQL
	 * 
	 * @param str
	 *            待检测的字符串
	 * @return 如果是敏感字符串就返回true,反之false
	 */
	public static boolean isInjectStr(String str) {
		if (str == null || "".equals(str.trim())) {
			return false;
		}

		String s = str.toLowerCase();

		// 敏感sql关键字
		String[] keys = new String[] { "'", "and", "exec", "execute", "insert", "select", "delete", "update", "count",
				"drop", "*", "%", "chr", "mid", "master", "truncate", "char", "declare", "sitename", "net user",
				"xp_cmdshell", ";", "or", "-", "+", ",", "like'", "and", "exec", "execute", "insert", "create", "drop",
				"table", "from", "grant", "use", "group_concat", "column_name", "information_schema.columns",
				"table_schema", "union", "where", "select", "delete", "update", "order", "by", "count", "*", "chr",
				"mid", "master", "truncate", "char", "declare", "or", ";", "-", "--", "+", ",", "like", "//", "/", "%",
				"#" };
		for (String key : keys) {
			if (s.trim().indexOf(key) != -1) {
				return true;
			}
		}
		return false;
	}
}
