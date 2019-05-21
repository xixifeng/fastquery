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

import java.lang.reflect.Method;
import java.util.Map;

import javax.sql.DataSource;

import org.fastquery.util.XMLParse;


/**
 * 连接池提供者
 * 
 * @author mei.sir@aliyun.cn
 */
public interface ConnectionPoolProvider {
	
	/**
	 * 获取提供的数据源
	 * @param fqueryResource 资源获取
	 * @param dataSourceName 数据源的名称
	 * @return 数据源
	 */
	DataSource getDataSource(Resource fqueryResource,String dataSourceName);
	
	/**
	 * 如果不采用任何连接池,那么就直接使用jdbc实现方,提供数据源.
	 * @param ds jdbc驱动方的数据源class
	 * @param resource 用于读取环境资源
	 * @param dataSourceName 数据源名称
	 * @return 用于提供连接的数据源
	 */
	default DataSource jdbc(Class<? extends DataSource> clazz,Resource resource,String dataSourceName) {
		
		if (!resource.exist("jdbc-config.xml")) {
			throw new RepositoryException("根据fastquery.json 配置文件得知依赖jdbc-config.xml配置文件,可是没有找到.");
		}
		
		Map<String, String> map = XMLParse.toMap(resource, "jdbc-config.xml", dataSourceName,"named-config");
		if(map.isEmpty()) {
			throw new RepositoryException("fastquery.json 配置文件中, 指定了数据源为" + dataSourceName + ",而在jdbc-config.xml中,找不到对该数据源的配置.");
		} else {
			String databaseName = map.get("databaseName");
			String password = map.get("password");
			String portNumber = map.get("portNumber");
			String serverName = map.get("serverName");
			String user = map.get("user");
			String url = map.get("url");
			DataSource ds = null;
			
			try {
				ds = clazz.newInstance();
				Method setDatabaseNameMethod = clazz.getMethod("setDatabaseName",String.class);
				Method setPasswordMethod = clazz.getMethod("setPassword",String.class);
				Method setPortNumberMethod = clazz.getMethod("setPortNumber",int.class);
				Method setServerNameMethod = clazz.getMethod("setServerName",String.class);
				Method setUserNameMethod = clazz.getMethod("setUser",String.class);
				Method setURLNameMethod = clazz.getMethod("setURL",String.class);
				
				setDatabaseNameMethod.invoke(ds, databaseName);
				setPasswordMethod.invoke(ds, password);
				if (portNumber != null) {
					setPortNumberMethod.invoke(ds, Integer.parseInt(portNumber));
				}
				setServerNameMethod.invoke(ds, serverName);
				setUserNameMethod.invoke(ds, user);
				if (url != null) {
					setURLNameMethod.invoke(ds, url);
				}
			} catch (Exception e) {
				throw new ExceptionInInitializerError(e);
			}
			
			
			return ds;
		}
	}
}
