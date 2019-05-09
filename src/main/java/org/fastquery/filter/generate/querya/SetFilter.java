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

package org.fastquery.filter.generate.querya;

import java.lang.reflect.Method;

import org.fastquery.filter.generate.common.MethodFilter;
import org.fastquery.where.Judge;
import org.fastquery.where.Set;

/**
 * Set注解校验
 *  
 * @author mei.sir@aliyun.cn
 */
public class SetFilter implements MethodFilter {

	@Override
	public Method doFilter(Method method) {
		
		// 1). @Set#ignore() 指定的class 必须有一个不带参数且public的构造方法
		// 2). if$ 和 ignoreScript 不能共存
		Set[] sets = method.getAnnotationsByType(Set.class);
		for (Set set : sets) {
				Class<? extends Judge> judge = set.ignore();
				try {
					judge.newInstance();
				} catch (InstantiationException | IllegalAccessException e) {
					this.abortWith(method, set.ignore() + " 必须有一个不带参数并且用public修饰的构造方法");
				}
				
				// > 2)
				if(!"true".equals(set.if$()) && !"false".equals(set.ignoreScript())) {
					this.abortWith(method, "@set中的if$属性和ignoreScript属性不能同时被自定义");
				}

		}
		
		return method;
	}

}
