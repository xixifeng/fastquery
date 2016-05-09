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

package org.fastquery.util;

import java.util.Map;
import java.util.Set;

import org.fastquery.core.Resource;
import org.fastquery.dsm.FastQueryJson;
import org.fastquery.dsm.FQueryProperties;
import org.fastquery.dsm.JdbcConfig;
/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class LoadPrperties {
	
	/**
	 * 装载配置并且初始化数据源,该方法的消耗成本较大.把它用在频繁调用的地方,显然是不合理的!
	 * @param fqueryResource
	 * @return
	 */
	public synchronized static Set<FastQueryJson> load(Resource fqueryResource) {
		Set<FastQueryJson> fqProperties = PropertiesUtil.getFQueryProperties(fqueryResource.getResourceAsStream("fastquery.json"),fqueryResource);
		String namedConfig = null;
		Set<String> basePackages = null;
		Map<String, JdbcConfig> jdbcConfigs = PropertiesUtil.getJdbcConfigs(fqueryResource.getResourceAsStream("jdbc-config.xml"));
		JdbcConfig jdbcConfig = null;
		String url = null;
		String config = null;
		
		for (FastQueryJson fQueryPropertie : fqProperties) {
			config = fQueryPropertie.getConfig(); // 获取fastquery.json 中的config属性
			namedConfig = fQueryPropertie.getDataSourceName();

			switch (config) {
			case "c3p0":
				if(FQueryProperties.findDataSource(namedConfig) == null) {  // 如果名称为namedConfig的数据源不存在,才能new!
					FQueryProperties.putDataSource(namedConfig, new com.mchange.v2.c3p0.ComboPooledDataSource(namedConfig));	
				}
				break;

			case "jdbc":
				jdbcConfig = jdbcConfigs.get(namedConfig);

				if (jdbcConfig == null) {
					throw new RuntimeException("配置错误!!!");
				}

				// 根据不同的jdbc的驱动,选择不同的数据源实现
				switch (jdbcConfig.getDriverClass()) {
				case "com.mysql.jdbc.Driver":
					if(FQueryProperties.findDataSource(namedConfig) == null) { // 如果名称为namedConfig的数据源不存在,才能new!
						com.mysql.jdbc.jdbc2.optional.MysqlDataSource cpd = new com.mysql.jdbc.jdbc2.optional.MysqlDataSource();
						cpd.setDatabaseName(jdbcConfig.getDatabaseName());
						cpd.setPassword(jdbcConfig.getPassword());
						cpd.setPortNumber(jdbcConfig.getPortNumber());
						cpd.setServerName(jdbcConfig.getServerName());
						cpd.setUser(jdbcConfig.getUser());
						url = jdbcConfig.getUrl();
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
