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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.fastquery.core.Repository;

import java.util.Set;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
class AfterFilterChain<R extends Repository> extends AfterFilter<R> {

	// 在此用map 主要目的是为了去重,相同的class后面覆盖前面的.
	// 用LinkedHashMap而不用hashMap 是为了有顺序
	private Map<Class<?>, AfterFilter<R>> afterFilters = new LinkedHashMap<>();
	
	public AfterFilterChain<R> addFilter(AfterFilter<R> f) {
		afterFilters.put(f.getClass(), f);
		return this;
	}
	
	@Override
	protected Object doFilter(R repository,Method method, Object[] args,Object returnVal) {
		Set<Entry<Class<?>, AfterFilter<R>>> entries = afterFilters.entrySet();
		Object rv = returnVal;
		for (Entry<Class<?>, AfterFilter<R>> entry : entries) {
			rv = entry.getValue().doFilter(repository,method,args,returnVal);
		}
		return rv;
	}
}
