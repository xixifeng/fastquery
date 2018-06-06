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

package org.fastquery.dsm;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.fastquery.core.RepositoryException;

/**
 * basePackage, 数据源的名称, 数据源,这三者的关系映射
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class FQueryProperties {

	// <String,String> 第一个参数是basePackage, 第二个参数是数据源的名字
	private static Map<String, String> dataSourceIndexs = new HashMap<>();

	// 第一个参数是dataSourceName,在此用Map,是为了查寻方便, 放在这里我们最终是为了查寻.
	private static Map<String, DataSource> dataSources = new HashMap<>();

	private FQueryProperties() {
	}

	public static void putDataSourceIndex(String basePackage, String dataSourceName) {
		dataSourceIndexs.put(basePackage, dataSourceName);
	}

	public static void putDataSource(String dataSourceName, DataSource dataSource) {
		if (dataSources.containsKey(dataSourceName)) {
			throw new RepositoryException(dataSourceName + " 已经存在!");
		}
		dataSources.put(dataSourceName, dataSource);
	}

	/**
	 * 根据basePackage 查找出 数据源的名字的名字
	 * 
	 * @param packageName 包名称
	 * @return 数据源名称,没有找到返回null
	 */
	public static String findDataSourceName(String packageName) {
		String dataSourceName = dataSourceIndexs.get(packageName);
		String pname = packageName;
		int len;
		while (dataSourceName == null && (len = pname.lastIndexOf('.')) != -1) {
			// 注意:可能出现这种情况
			// map中存在 "A.B"
			// packageName 可能是A.B.C
			// 因此需要做如下处理
			pname = pname.substring(0, len);
			dataSourceName = dataSourceIndexs.get(pname);
		}
		return dataSourceName;
	}

	/**
	 * 根据数据源的名字查寻出数据库对象
	 * 
	 * @param dataSourceName 数据源名称
	 * @return 数据源
	 */
	public static DataSource findDataSource(String dataSourceName) {
		return dataSources.get(dataSourceName);
	}

	public static void createDataSource(String dataSourceName, Properties properties) {
		if (dataSourceName == null || "".equals(dataSourceName)) {
			throw new RepositoryException("dataSourceName 不能为\"\"或为null");
		} else {
			if (dataSources.containsKey(dataSourceName)) { // 这里有必要判断. 而不是等连接池创建后在判断.
				throw new RepositoryException(dataSourceName + " 已经存在!");
			}
			com.mchange.v2.c3p0.ComboPooledDataSource cpds = new com.mchange.v2.c3p0.ComboPooledDataSource();
			cpds.setDataSourceName(dataSourceName);
			Class<?> cls = cpds.getClass();
			properties.forEach((k, v) -> {
				try {
					PropertyDescriptor pd = new PropertyDescriptor(k.toString(), cls);
					Method method = pd.getWriteMethod();
					Class<?> returnType = method.getParameterTypes()[0];
					if (returnType == String.class) {
						method.invoke(cpds, v.toString());
					} else if (returnType == int.class) {
						method.invoke(cpds, Integer.parseInt(v.toString()));
					} else if (returnType == boolean.class) {
						method.invoke(cpds, Boolean.parseBoolean(v.toString()));
					}
				} catch (Exception e) {
					throw new RepositoryException(e);
				}
			});
			putDataSource(dataSourceName, cpds);	
		}
	}

	public static void removeDataSource(String dataSourceName) { // NO_UCD (unused code)
		dataSources.remove(dataSourceName);
	}

	public static void clear() {
		dataSourceIndexs.clear();
		dataSources.clear();
	}
}
