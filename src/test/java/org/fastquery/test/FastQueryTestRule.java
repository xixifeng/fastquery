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

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.fastquery.core.Repository;
import org.fastquery.filter.SkipFilter;
import org.fastquery.struct.SQLValue;

/**
 * 
 * @author mei.sir@aliyun.cn
 */
public class FastQueryTestRule implements TestRule {

	private SQLValue sqlValue;
	private List<SQLValue> sqlValues;
	
	private void proxy(Statement base, Description description) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException{
		Object testTarget = getTestTarget(base);
		System.out.println("SkipFilter:"+description.getAnnotation(SkipFilter.class));
		Class<?> clazz = description.getTestClass();
		List<Field> fList = new ArrayList<>();
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			if(Repository.class.isAssignableFrom(field.getType())){
				field.setAccessible(true);
				fList.add(field);	
			}
		}
		fields = clazz.getFields();
		for (Field field : fields) {
			if(Repository.class.isAssignableFrom(field.getType())){
				field.setAccessible(true);
				fList.add(field);	
			}
		}

			for (Field field : fList) {
				Repository repository =  (Repository) field.get(testTarget);
				Class<?> interfaceClazz = repository.getClass().getInterfaces()[0];
				// 代理repository这个对象
				field.set(testTarget, Proxy.newProxyInstance(this.getClass().getClassLoader(),new Class<?>[] {interfaceClazz},new RepositoryInvocationHandler(repository,this,description)));
			}
	}

	private Object getTestTarget(Statement base) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException{
		if(base instanceof org.junit.internal.runners.statements.ExpectException) {
			Field nextField = base.getClass().getDeclaredField("next");
			nextField.setAccessible(true);
			// 获取目标对象
			Object nextBase = nextField.get(base);
			
			Field targetField = nextBase.getClass().getDeclaredField("target");
			targetField.setAccessible(true);
			// 获取目标对象
			Object target = targetField.get(nextBase);
			return target;
		} else {
			Field targetField = base.getClass().getDeclaredField("target");
			targetField.setAccessible(true);
			// 获取目标对象
			Object target = targetField.get(base);
			return target;
			
		}
	}

	@Override
	public Statement apply(Statement base, Description description) {
		
		 return new Statement() {
	            @Override
	            public void evaluate() throws Throwable { // 请特别注意: 这里的 throws Throwable junti会自行处理,不用捕获,不然断言效果出来不了!
	               proxy(base, description);
		           System.out.println(description.getMethodName()+"--------------------------------------------开始执行");
				   base.evaluate();
	               after(description);
	            }
	        };
	}
	
	
	public  SQLValue getSQLValue(){
		return sqlValue;
	}
	public  List<SQLValue> getListSQLValue(){
		return sqlValues;
	}
	
	private void after(Description description){
		System.out.println(description.getMethodName()+"--------------------------------------------已经结束");
	}

}
