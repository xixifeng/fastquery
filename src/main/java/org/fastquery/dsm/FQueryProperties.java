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

package org.fastquery.dsm;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

/**
 * basePackage, 数据源的名称, 数据源,这三者的关系映射
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class FQueryProperties {

	// <String,String> 第一个参数是basePackage, 第二个参数是数据源的名字
	private static Map<String, String> dataSourceIndexs = new HashMap<>();

	// 在此用Map,是为了查寻方便, 放在这里我们最终是为了查寻.
	private static Map<String, DataSource> dataSources = new HashMap<>();

	public static void putDataSourceIndex(String key, String value) {
		dataSourceIndexs.put(key, value);
	}

	public static void putDataSource(String key, DataSource value) {
		dataSources.put(key, value);
	}

	/**
	 * 根据basePackage 查找出 数据源的名字的名字
	 * 
	 * @param packageName
	 * @return
	 */
	public static String findDataSourceName(String packageName) {
		return dataSourceIndexs.get(packageName);
	}

	/**
	 * 根据数据源的名字查寻出数据库对象
	 * 
	 * @param sourceName
	 * @return
	 */
	public static DataSource findDataSource(String dataSourceName) {
		return dataSources.get(dataSourceName);
	}
	// 不用提供set方法,如果提供dataSourceIndexs的set方法,就把它覆盖了.
}
