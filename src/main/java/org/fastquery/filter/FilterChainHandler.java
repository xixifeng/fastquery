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

package org.fastquery.filter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.fastquery.core.Repository;
import org.fastquery.core.RepositoryException;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class FilterChainHandler {
	
	/**
	 * 
	 * @param method
	 * @param args
	 * @return
	 */
	public static <R extends Repository> Object bindBeforeFilterChain(Class<?> iclazz, R repository,Method method, Object[] args) {
		SkipFilter skipFilter = method.getAnnotation(SkipFilter.class);
		List<Before> befores = new ArrayList<>();
		if(skipFilter==null){
			befores = new ArrayList<>(Arrays.asList(iclazz.getAnnotationsByType(Before.class))); // 获取当前类级别的before's	
		}
		befores.addAll(Arrays.asList(method.getAnnotationsByType(Before.class))); // 获取当前方法上的before's
		
		BeforeFilterChain<R> beforeFilterChain = new BeforeFilterChain<>();
		for (Before before : befores) {
			// Type safety: 如下是把 Class<? extends BeforeFilter<? extends Repository>>[] 转化成了 Class<BeforeFilter<R>>[]
			// 分析后,属于安全转化.
			@SuppressWarnings("unchecked")
			Class<BeforeFilter<R>>[] clazzs  =  (Class<BeforeFilter<R>>[]) before.value();
			for (Class<BeforeFilter<R>> clazz : clazzs) {
				try {
					beforeFilterChain.addFilter(clazz.newInstance());
				} catch (Exception e) {
					throw new RepositoryException(e.getMessage(),e);
				} 
			}
		}
		return beforeFilterChain.start(repository,method, args);
	}
	
	public static <R extends Repository> Object bindAfterFilterChain(Class<?> iclazz,R repository,Method method,Object[] args,Object object) {
		SkipFilter skipFilter = method.getAnnotation(SkipFilter.class);
		List<After> afters = new ArrayList<>();
		if(skipFilter==null){
			afters = new ArrayList<>(Arrays.asList(iclazz.getAnnotationsByType(After.class))); // 获取当前类级别的after's	
		}
		afters.addAll(Arrays.asList(method.getAnnotationsByType(After.class)));// 获取当前方法上的after's
		AfterFilterChain<R> afterFilterChain = new AfterFilterChain<>();
		for (After after : afters) {
			@SuppressWarnings("unchecked")
			Class<AfterFilter<R>>[] clazzs = (Class<AfterFilter<R>>[]) after.value();
			for (Class<? extends AfterFilter<R>> clazz : clazzs) {
				try {
					afterFilterChain.addFilter(clazz.newInstance());
				} catch (Exception e) {
					throw new RepositoryException(e.getMessage(),e);
				} 
			}
		}
		return afterFilterChain.doFilter(repository,method,args,object);
	}
}
