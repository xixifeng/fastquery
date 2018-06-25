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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.fastquery.core.Modifying;
import org.fastquery.core.Query;
import org.fastquery.core.QueryByNamed;
import org.fastquery.core.QueryContext;
import org.fastquery.core.QueryParser;
import org.fastquery.core.Repository;
import org.fastquery.core.RepositoryException;
import org.fastquery.filter.SkipFilter;
import org.fastquery.page.Page;
import org.junit.runner.Description;

/**
 * 
 * @author mei.sir@aliyun.cn
 */
public class RepositoryInvocationHandler implements InvocationHandler {

	private static final Logger LOG = LoggerFactory.getLogger(RepositoryInvocationHandler.class);

	private Repository repository;
	private FastQueryTestRule rule;
	private Description description;

	public RepositoryInvocationHandler(Repository repository, FastQueryTestRule rule, Description description) {
		this.repository = repository;
		this.rule = rule;
		this.description = description;
	}

	private void before() throws Throwable {
		if (description.getAnnotation(SkipFilter.class) != null) {
			return;
		}
		Class<QueryContext> qcclazz = QueryContext.class;
		Method getQueryContextMethod = qcclazz.getDeclaredMethod("getQueryContext");
		getQueryContextMethod.setAccessible(true);
		Field debugField = qcclazz.getDeclaredField("debug");
		debugField.setAccessible(true);
		debugField.set(null, true);
	}

	private void after() throws Throwable {
		if (description.getAnnotation(SkipFilter.class) != null) {
			return;
		}
		Class<QueryParser> clazz = QueryParser.class;
		LOG.debug("RepositoryInvocationHandler:当前线程:{}", Thread.currentThread());
		Method currentMethod = QueryContext.getMethod();
		Method modifyParserMethod = clazz.getDeclaredMethod("modifyParser");
		Method queryParserMethod = clazz.getDeclaredMethod("queryParser");
		modifyParserMethod.setAccessible(true);
		queryParserMethod.setAccessible(true);
		LOG.debug("modifyParserMethod:{}", modifyParserMethod);
		LOG.info("currentMethod:{}", currentMethod);
		Modifying modifying = currentMethod.getAnnotation(Modifying.class);
		Class<?> returnType = QueryContext.getReturnType();
		QueryByNamed queryById = currentMethod.getAnnotation(QueryByNamed.class);
		Query query = currentMethod.getAnnotation(Query.class);

		Class<FastQueryTestRule> qcclazz = FastQueryTestRule.class;
		Field sqlValueField = qcclazz.getDeclaredField("sqlValue");
		Field sqlValuesField = qcclazz.getDeclaredField("sqlValues");
		sqlValueField.setAccessible(true);
		sqlValuesField.setAccessible(true);
		if (modifying != null) {
			// 改操作涉及多条sql语句
			sqlValuesField.set(rule, modifyParserMethod.invoke(null));
		} else if (returnType == Page.class && queryById != null) {
			sqlValuesField.set(rule, QueryParser.pageParserByNamed());
		} else if (returnType == Page.class) {
			sqlValuesField.set(rule, QueryParser.pageParser());
		} else if (queryById != null || query != null) {
			sqlValueField.set(rule, queryParserMethod.invoke(null));
		} else {
			LOG.error("暂时没考虑");
		}
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws RepositoryException {
		try {
			if (method.getDeclaringClass() == Object.class) { // 如果拦截的方法是继承之Object,那么直接放行
				return method.invoke(repository, args);
			}

			before();
			Object result;
			result = method.invoke(repository, args);
			after();
			return result;
		} catch (Throwable e) {
			e.printStackTrace();
			throw new RepositoryException(e);
		}

	}

}
