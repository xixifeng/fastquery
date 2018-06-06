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

package org.fastquery.struct;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.fastquery.core.Placeholder;
import org.fastquery.core.QueryContext;
import org.fastquery.core.RepositoryException;
import org.fastquery.util.PreventSQLInjection;
import org.fastquery.util.TypeUtil;

/**
 * SQL和值
 * 
 * @author mei.sir@aliyun.cn
 */
public class SQLValue {

	private static final Logger LOG = LoggerFactory.getLogger(SQLValue.class);

	private String sql; // 待执行的sql
	private List<Object> values;// sql语言中"?"对应的实参

	public SQLValue(String sql, List<Object> values) {

		LOG.info("SQL扩展之前:{}", sql);
		Object[] args = QueryContext.getArgs();
		// 1. 处理"% ? % "问题, 对应的正则 "[_\\s*%]+\\?[_\\s*%]+"
		List<String> ssms = TypeUtil.matches(sql, Placeholder.SMILE);
		for (String ssm : ssms) {
			int end = sql.indexOf(ssm);
			// 统计 sql中0-end范围中问号出现的次数
			int count = StringUtils.countMatches(sql.substring(0, end), '?');
			String numStr = TypeUtil.matches(ssm, Placeholder.SEARCH_NUM).get(0);
			ssm = ssm.replaceAll(Placeholder.SP1_REG, "?"); // 这部很重要,不然"?"后面的数字也会融入模板里
			int index = Integer.parseInt(numStr) - 1;
			// values 存储着"?"号对应的值,特别注意: values[i] 表示从左至右第i+1次出现的?号的值,并不代表(?i+1)的值
			values.set(count, ssm.replaceFirst("`-", "").replaceFirst("-`", "").replaceFirst("\\?",
					args[index] != null ? args[index].toString().replaceAll("`", "") : ""));
			Object obj = values.get(count);
			if (obj != null && obj.getClass() == String.class && Pattern.matches(Placeholder.PERCENT, obj.toString())) {
				throw new RepositoryException("这个SQL实参值禁止都是%组成");
			}
		}

		// 2. 防SQL注入
		List<String> ins = TypeUtil.matches(sql, Placeholder.SMILE_BIG);
		for (String in : ins) {
			if (PreventSQLInjection.isInjectStr(in) && TypeUtil.matches(in, Placeholder.SMILE).isEmpty()) {
				String tip = in.replace("`-", "").replace("-`", "") + "中包含有危险关键字,正在尝试SQL注入";
				LOG.error(tip);
				throw new RepositoryException(tip);
			}
		}

		// 3.

		if (!ssms.isEmpty()) {
			this.sql = sql.replaceAll(Placeholder.SMILE, "?");
		} else {
			this.sql = sql;
		}

		this.sql = this.sql.replaceAll(Placeholder.SP1_REG, "?");
		this.values = values;
	}

	public String getSql() {
		return sql;
	}

	public List<Object> getValues() {
		return values;
	}
}
