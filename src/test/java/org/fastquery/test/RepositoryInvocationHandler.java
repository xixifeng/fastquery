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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

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

	private Repository repository;
	private FastQueryTestRule rule;
	private  Description description;

	public RepositoryInvocationHandler(Repository repository,FastQueryTestRule rule,Description description) {
		this.repository = repository;
		this.rule = rule;
		this.description = description;
	}
	private void before()  throws Throwable { 
		if(description.getAnnotation(SkipFilter.class)!=null) {
			return ;
		}
			QueryContext.forceClear();
			Class<QueryContext> qcclazz = QueryContext.class;
			Method getQueryContextMethod = qcclazz.getDeclaredMethod("getQueryContext");
			getQueryContextMethod.setAccessible(true);
			QueryContext queryContext = (QueryContext) getQueryContextMethod.invoke(null);
			Field debugField = qcclazz.getDeclaredField("debug");
			debugField.setAccessible(true);
			System.out.println("queryContext-->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>-"+queryContext);
			debugField.get(queryContext);
			if( (boolean)debugField.get(queryContext) != true){
				debugField.setAccessible(true);
				debugField.set(queryContext, true);
			}
	}
	
	
	private void after() throws Throwable {
		if(description.getAnnotation(SkipFilter.class)!=null) {
			return ;
		}
		Class<QueryParser> clazz = QueryParser.class;
		    Method currentMethod = QueryContext.getMethod();
			Method  modifyParserMethod = clazz.getDeclaredMethod("modifyParser");
			Method  queryParserMethod = clazz.getDeclaredMethod("queryParser");
			modifyParserMethod.setAccessible(true);
			queryParserMethod.setAccessible(true);
			System.out.println("modifyParserMethod:" + modifyParserMethod);
			System.out.println("currentMethod:"+currentMethod);
			Modifying modifying = currentMethod.getAnnotation(Modifying.class);
			Class<?> returnType = QueryContext.getReturnType();
			QueryByNamed queryById = currentMethod.getAnnotation(QueryByNamed.class);
			
			Class<FastQueryTestRule> qcclazz = FastQueryTestRule.class;
			Field sqlValueField = qcclazz.getDeclaredField("sqlValue");
			Field sqlValuesField = qcclazz.getDeclaredField("sqlValues");
			sqlValueField.setAccessible(true);
			sqlValuesField.setAccessible(true);
			if(modifying!=null) {
				sqlValuesField.set(rule,modifyParserMethod.invoke(null));
			} else if(returnType == Page.class && queryById != null) {
				sqlValuesField.set(rule,QueryParser.pageParserByNamed());
			} else if(returnType == Page.class) {
				sqlValuesField.set(rule,QueryParser.pageParser());
			} else if (currentMethod.getAnnotation(Query.class)==null && modifying==null) {
			
			} else {
				System.out.println("queryParserMethod:" + queryParserMethod);
				sqlValueField.set(rule,queryParserMethod.invoke(null));
			}
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws RepositoryException {
		try {
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
