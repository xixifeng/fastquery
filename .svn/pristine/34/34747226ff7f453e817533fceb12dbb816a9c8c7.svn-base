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
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class FQueryFactoryImpl implements FQueryFactory {

	private static FQueryFactoryImpl instance;
	
	private FQueryFactoryImpl() {
	}

	// 这个方法禁止外界访问
	static FQueryFactory getInstance() {
		if (instance == null) {
			synchronized (FQueryFactoryImpl.class) {
				if (instance == null) {
					instance = new FQueryFactoryImpl();
				}
			}
		}
		return instance;
	}
	
	@Override
	public DataSource getDataSource(String packageName) {
		
		// 根据basePackage 查找出 数据源的名字
		String dataSourceName = FQueryProperties.findDataSourceName(packageName);
		
		// 在根据数据源的名字查寻出数据库对象
		return FQueryProperties.findDataSource(dataSourceName);
		
	}
}
