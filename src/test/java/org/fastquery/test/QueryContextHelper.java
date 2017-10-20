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

import java.lang.reflect.Method;

import org.fastquery.core.QueryContext;

import static org.junit.Assert.fail;

/**
 * 
 * @author mei.sir@aliyun.cn
 */
public class QueryContextHelper {

	public static QueryContext getQueryContext() {
		try {
			Method getQueryContextMethod = QueryContext.class.getDeclaredMethod("getQueryContext");
			getQueryContextMethod.setAccessible(true);
			return (QueryContext) getQueryContextMethod.invoke(null);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		return null;
	}
}
