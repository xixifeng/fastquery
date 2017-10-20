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

package org.fastquery.struct;

import java.util.List;

import org.apache.log4j.Logger;
import org.fastquery.core.Placeholder;
import org.fastquery.util.TypeUtil;

/**
 * 改操作SQL和值
 * 
 * @author mei.sir@aliyun.cn
 */
public class SQLValue {
	
	private static final Logger LOG = Logger.getLogger(SQLValue.class);
	
	private String sql; // 待执行的sql
	private List<Object> values;// sql语言中"?"对应的实参

	public SQLValue(String sql, List<Object> values) {
		
		LOG.info("SQL扩展之前:" + sql);
		
		//1. 处理"% ? % "问题, 对应的正则 "[_\\s*%]+\\?[_\\s*%]+"
		List<String> ssms = TypeUtil.matches(sql, Placeholder.Q_MATCH);
		int ssmlen = ssms.size();
		int valLen = values.size();
		for (int i = 0; i < valLen; i++) {
			
			String tpl = "?";
			if(ssmlen >= i+1) {
				// 注意: ssms.get(i) 至少包含一个字符 因此不存在 "".trim()问题!
				tpl = ssms.get(i).trim();
			}
			if(!"?".equals(tpl) ) {
				values.set(i, tpl.replaceAll("\\?", values.get(i).toString()));
			} 
		}

		//2.
		
		//3.
		
		if(ssmlen>0) {
			this.sql = sql.replaceAll(Placeholder.Q_MATCH, " ? ");
		} else {
			this.sql = sql;
		}
		this.values = values;
	}

	public String getSql() {
		return sql;
	}

	public List<Object> getValues() {
		return values;
	}
}
