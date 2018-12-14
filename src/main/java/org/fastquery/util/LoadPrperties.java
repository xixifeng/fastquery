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

package org.fastquery.util;

import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.fastquery.core.Resource;
import org.fastquery.dsm.FastQueryJson;
import org.fastquery.dsm.FQueryProperties;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class LoadPrperties {

	private static final Logger LOG = LoggerFactory.getLogger(LoadPrperties.class);

	private LoadPrperties() {
	}

	/**
	 * 装载配置并且初始化数据源,该方法消耗大,只能被调用一次
	 * 
	 * @param fqueryResource fquery.json 资源文件
	 * @return set格式 fquery.json
	 */
	public static Set<FastQueryJson> load(Resource fqueryResource) {
		Set<FastQueryJson> fqProperties = PropertiesUtil.getFQueryProperties(fqueryResource);
		String namedConfig;
		Set<String> basePackages;
		String config;

		for (FastQueryJson fQueryPropertie : fqProperties) {
			config = fQueryPropertie.getConfig(); // 获取fastquery.json 中的config属性
			namedConfig = fQueryPropertie.getDataSourceName();

			switch (config) {
			case "c3p0":
				if (FQueryProperties.findDataSource(namedConfig) == null && namedConfig != null) { // 如果名称为namedConfig的数据源不存在,才能new!
					com.mchange.v2.c3p0.ComboPooledDataSource cpds = new com.mchange.v2.c3p0.ComboPooledDataSource(namedConfig);
					FQueryProperties.putDataSource(namedConfig, cpds);
					LOG.debug("创建数据源:{},名称为:{}", cpds, namedConfig);
				}
				break;

			case "druid":
				if (FQueryProperties.findDataSource(namedConfig) == null && namedConfig != null) { // 如果名称为namedConfig的数据源不存在,才能new!
					DataSource druidDS;
					try {
						druidDS = com.alibaba.druid.pool.DruidDataSourceFactory.createDataSource(XMLParse.toMap(fqueryResource, "druid.xml", namedConfig,"bean"));
					} catch (Exception e) {
						throw new ExceptionInInitializerError(e);
					}
					FQueryProperties.putDataSource(namedConfig, druidDS);
					LOG.debug("创建数据源:{},名称为:{}", druidDS, namedConfig);
				}
				break;
				
			case "jdbc":
				
				Map<Object, Object> map = XMLParse.toMap(fqueryResource, "jdbc-config.xml", namedConfig,"named-config");
				
				// 根据不同的jdbc的驱动,选择不同的数据源实现
				switch ( (String) map.get("driverClass")) {
				case "com.mysql.cj.jdbc.Driver":
					if (FQueryProperties.findDataSource(namedConfig) == null) { // 如果名称为namedConfig的数据源不存在,才能new!
						String databaseName = (String) map.get("databaseName");
						String password = (String) map.get("password");
						String portNumber = (String) map.get("portNumber");
						String serverName = (String) map.get("serverName");
						String user = (String) map.get("user");
						String url = (String) map.get("url");

						com.mysql.cj.jdbc.MysqlDataSource cpd = new com.mysql.cj.jdbc.MysqlDataSource();
						cpd.setDatabaseName(databaseName);
						cpd.setPassword(password);
						if (portNumber != null) {
							cpd.setPortNumber(Integer.parseInt(portNumber));
						}
						cpd.setServerName(serverName);
						cpd.setUser(user);
						if (url != null) {
							cpd.setUrl(url);
						}
						FQueryProperties.putDataSource(namedConfig, cpd);

					}
					break;

				default:
					break;
				}
				// 根据不同的jdbc的驱动,选择不同的数据源实现 End

				break;

			default:
				break;
			}

			basePackages = fQueryPropertie.getBasePackages();
			for (String basePackage : basePackages) {
				FQueryProperties.putDataSourceIndex(basePackage, namedConfig);
			}
		}

		return fqProperties;
	}
	
}
