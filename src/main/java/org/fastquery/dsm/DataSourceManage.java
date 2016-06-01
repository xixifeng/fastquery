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
	 * 获取数据源
	 * @param packageName
	 * @return
	 */
	public static DataSource getDataSource(String packageName){
		
		DataSource dataSource = FQueryFactoryImpl.getInstance().getDataSource(packageName);
		
		// dataSource 为null 什么也做不了
		if(dataSource==null) {
			throw new  ExceptionInInitializerError("没有找到数据源,请键查fastquery.json是否配置正确.");
		}
		
		return dataSource;
	}
	
	
}
