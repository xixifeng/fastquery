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

package org.fastquery.pool;
import java.util.Map;

import javax.sql.DataSource;
import org.fastquery.core.ConnectionPoolProvider;
import org.fastquery.core.RepositoryException;
import org.fastquery.core.Resource;
import org.fastquery.util.XMLParse;

/**
 * 
 * @author mei.sir@aliyun.cn
 */
public class MySQLDriverProvider implements ConnectionPoolProvider {

	@Override
	public DataSource getDataSource(Resource resource, String dataSourceName) {
		
		// 校验是否存在 jdbc-config.xml 文件
		if (!resource.exist("jdbc-config.xml")) {
			throw new RepositoryException("fastquery.json 配置文件中, config设置了mySQLDriver,因此依赖jdbc-config.xml配置文件,可是没有找到.");
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
			
			return cpd;
		}
	}

}
