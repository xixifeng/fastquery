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

import javax.sql.DataSource;

/**
 * 数据源管理
 * @author xixifeng (fastquery@126.com)
 */
public class DataSourceManage {
	
	private DataSourceManage(){}
	
	/**
	 * 获取数据源, 注意: 根据dataSourceName查优先
	 * @param packageName
	 * @return
	 */
	public static DataSource getDataSource(String dataSourceName,String className) {
		
    	// 根据dataSourceName 查
    	DataSource dataSource = FQueryProperties.findDataSource(dataSourceName);
    	if(dataSource == null) {
    		dataSource = FQueryFactoryImpl.getInstance().getDataSource(className);
    	}
    	
		// dataSource 为null 什么也做不了
		if(dataSource==null) {
			throw new  ExceptionInInitializerError("没有找到数据源,请键查fastquery.json是否配置正确,或者是没有初始化连接池. \n 连接池的生成有两种模式:\n1).通过配置c3p0-config.xml,jdbc-config.xml \n2).通过FQueryProperties.createDataSource(...)");
		}

		return dataSource;
	}
	
	public static DataSource getDataSource(String className) {
		
    	// 根据dataSourceName 查
        DataSource dataSource = FQueryFactoryImpl.getInstance().getDataSource(className);
    	
		// dataSource 为null 什么也做不了
		if(dataSource==null) {
			throw new  ExceptionInInitializerError("没有找到数据源,请键查fastquery.json是否配置正确,或者是没有初始化连接池. \n 连接池的生成有两种模式:\n1).通过配置c3p0-config.xml,jdbc-config.xml \n2).通过FQueryProperties.createDataSource(...)");
		}
		
		return dataSource;
	}
	
	
}
