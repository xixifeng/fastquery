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
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.fastquery.core.Placeholder;
import org.fastquery.core.RepositoryException;
import org.fastquery.util.PreventSQLInjection;
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
		List<String> ssms = TypeUtil.matches(sql, Placeholder.SMILE);
		for (String ssm : ssms) {
			String numStr = TypeUtil.matches(ssm, Placeholder.SEARCH_NUM).get(0); 
			ssm = ssm.replaceAll(Placeholder.SP1_REG, "?"); // 这部很重要,不然"?"后面的数字也会融入模板里
			int index = Integer.parseInt(numStr) - 1;
			values.set(index, ssm.replaceFirst("`-","").replaceFirst("-`","").replaceFirst("\\?", values.get(index)!=null?values.get(index).toString():""));
			Object obj = values.get(index);
			if (obj!=null && obj.getClass()==String.class && Pattern.matches(Placeholder.PERCENT, obj.toString())) {
               throw new RepositoryException("这个SQL实参值禁止都是%组成");
			}
		}

		//2. 防SQL注入
		List<String> ins = TypeUtil.matches(sql, Placeholder.SMILE_BIG); 
		for (String in : ins) {
			if(PreventSQLInjection.isInjectStr(in) && TypeUtil.matches(in, Placeholder.SMILE).isEmpty()){
				String tip = in.replace("`-", "").replace("-`", "")+"是危险关键字,正在尝试SQL注入";
				LOG.fatal(tip);
				throw new RepositoryException(tip);
			}
		}

		//3.
		
		if(!ssms.isEmpty()) {
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
