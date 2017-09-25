/*
 * Copyright (c) 2016-2017, fastquery.org and/or its affiliates. All rights reserved.
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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.fastquery.core.QueryProcess;
import org.junit.Test;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 * @date 2017年8月31日
 */
public class QueryProcessTest {

	@Test
	public void mapValueTyep() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {

		Method m = QueryProcess.class.getDeclaredMethod("mapValueTyep", Method.class);
		m.setAccessible(true);

		class A {
			public Map<String, String> todo() {
				return null;
			}
		}

		Class<?> clazz = A.class;
		Method method = clazz.getMethod("todo");
		assertThat(m.invoke(null, method) == String.class, is(true));
	}

	@Test
	public void listMapValueTyep() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {

		Method m = QueryProcess.class.getDeclaredMethod("listMapValueTyep", Method.class);
		m.setAccessible(true);

		class B {
			public List<Map<String, String>> todo() {
				return null;
			}
		}

		Class<?> clazz = B.class;
		Method method = clazz.getMethod("todo");
		assertThat(m.invoke(null, method) == String.class, is(true));
	}

}
