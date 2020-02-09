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

package org.fastquery.test;

import static org.junit.Assert.assertThat;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import static org.hamcrest.Matchers.*;

import org.fastquery.bean.PManager;
import org.fastquery.example.StudentDBService;
import org.fastquery.service.FQuery;
import org.fastquery.util.TypeUtil;
import org.junit.Test;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class FQueryTest extends FastQueryTest  {

	@Test
	public void testGetRepository() {
		assertThat(FQuery.getRepository(StudentDBService.class), notNullValue());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testReset()
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, IntrospectionException {
		Class<PManager> clazz = PManager.class;
		PManager tempPmanager = FQuery.reset(clazz);
		clazz = (Class<PManager>) tempPmanager.getClass();
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			if (!TypeUtil.isWarrp(field.getType())) {
				continue;
			}

			Object readValue = new PropertyDescriptor(field.getName(), clazz).getReadMethod().invoke(tempPmanager);
			assertThat(readValue, nullValue());
		}

	}

}
