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

package org.fastquery.service;

import static org.junit.Assert.assertThat;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import static org.hamcrest.Matchers.*;

import org.fastquery.bean.PManager;
import org.fastquery.example.StudentDBService;
import org.fastquery.util.TypeUtil;
import org.junit.Ignore;
import org.junit.Test;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class FQueryTest {

	@Test
	public void testGetRepository() {
		assertThat(FQuery.getRepository(StudentDBService.class), notNullValue());
	}

	@Ignore
	@Test
	public void testCreateDataSource() {
		// 数据源名称
		String dataSourceName = "xk100";

		// 连接池配置
		Properties properties = new Properties();
		properties.setProperty("driverClass", "com.mysql.cj.jdbc.Driver");
		properties.setProperty("jdbcUrl", "jdbc:mysql://192.168.8.10:3306/xk1");
		properties.setProperty("user", "xk1");
		properties.setProperty("password", "abc1");

		// 创建一个数据源
		FQuery.createDataSource(dataSourceName, properties);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testReset()
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException, IntrospectionException {
		Class<PManager> clazz = PManager.class;
		PManager tempPmanager = FQuery.reset(clazz);
		clazz = (Class<PManager>) tempPmanager.getClass();
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			if (field.getType().isArray() || !TypeUtil.isWarrp(field.getType())) {
				continue;
			}

			Object readValue = new PropertyDescriptor(field.getName(), clazz).getReadMethod().invoke(tempPmanager);
			assertThat(readValue, nullValue());
		}

	}

}
