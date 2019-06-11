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

package org.fastquery.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.fastquery.core.MethodInfo;
import org.fastquery.core.QueryContext;

/**
 * 仅为测试服务
 * @author mei.sir@aliyun.cn
 */
public class QueryContextUtil {

	private static QueryContext getQueryContext() throws Exception {
		Class<QueryContext> clazz = QueryContext.class;
		Constructor<QueryContext> constructor = clazz.getDeclaredConstructor();
		constructor.setAccessible(true);
		return constructor.newInstance();
	}
	
	@SuppressWarnings("unchecked")
	private static ThreadLocal<QueryContext> getThreadLocal() throws Exception {
		Class<QueryContext> clazz = QueryContext.class;
		Field field = clazz.getDeclaredField("threadLocal");
		field.setAccessible(true);
		return (ThreadLocal<QueryContext>) field.get(null);
	}
	
	public static void setCurrentMethod(Method method) throws Exception {
		QueryContext queryContext = getThreadLocal().get();
		Field mf = QueryContext.class.getDeclaredField("methodInfo");
		mf.setAccessible(true);
		mf.set(queryContext, new MethodInfo(method));
	}
	
	public static void setCurrentArgs(Object...args) throws Exception {
		QueryContext queryContext = getThreadLocal().get();
		Field af = QueryContext.class.getDeclaredField("args");
		af.setAccessible(true);
		af.set(queryContext, args);
	}
	
	public static void startQueryContext() throws Exception {
		getThreadLocal().set(getQueryContext());
	}
	
	public static void clearQueryContext() throws Exception {
		getThreadLocal().remove();
	}
}
