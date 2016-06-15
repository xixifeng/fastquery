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

package org.fastquery.test;

import org.fastquery.dao2.UserInfoDBService2;
import org.fastquery.service.FQuery;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertThat;

import java.util.Map;
import java.util.Properties;

import static org.hamcrest.Matchers.*;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class UserInfoDBServiceTest2 {
	
	private UserInfoDBService2 userInfoDBService;
	
	@BeforeClass
	public static void beforeClass(){
		// 数据源名称
		String dataSourceName = "xk1";
		
		// 连接池配置
		Properties properties = new Properties();
		properties.setProperty("driverClass", "com.mysql.jdbc.Driver");
		properties.setProperty("jdbcUrl", "jdbc:mysql://192.168.8.10:3306/xk1");
		properties.setProperty("user", "xk1");
		properties.setProperty("password", "abc1");
		
		// 创建一个数据源
		FQuery.createDataSource(dataSourceName, properties);
	}
	
	@Before
	public void before() throws ClassNotFoundException{
		userInfoDBService = FQuery.getRepository(UserInfoDBService2.class);
	}
	
	@Test
	public void findOne(){
		int age = 1000;
		// 数据库中age没有大于1千的记录
		// 断言: 查询返回的值应该是一个空对象,不是null.
		Map<String, Object> map = userInfoDBService.findOne(age,"xk-c3p0");
		assertThat(map, notNullValue());
		assertThat(map.isEmpty(), is(true));
	}
	
	@Test
	public void testUpdateBatch() {
		int effect = userInfoDBService.updateBatch("小张张", 26, 1,"xk1");
		assertThat("断言该行修改操作一共影响了3行",effect, equalTo(3));
	}
	
	
}












