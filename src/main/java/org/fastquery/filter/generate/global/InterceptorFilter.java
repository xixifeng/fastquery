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

package org.fastquery.filter.generate.global;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.fastquery.core.QueryRepository;
import org.fastquery.core.Repository;
import org.fastquery.filter.After;
import org.fastquery.filter.AfterFilter;
import org.fastquery.filter.Before;
import org.fastquery.filter.BeforeFilter;
import org.fastquery.filter.generate.common.MethodFilter;

/**
 * 对Before/After 拦截器安全校验
 * @author xixifeng (fastquery@126.com)
 */
public class InterceptorFilter implements MethodFilter {

	@Override
	public Method doFilter(Method method) {
		
		// 接口的Class
		Class<?> iclazz = method.getDeclaringClass();
		
		// 当前类中所有的@Before
		List<Before> befores = new ArrayList<>(Arrays.asList(iclazz.getAnnotationsByType(Before.class))); // 获取类级别的before's
		befores.addAll(Arrays.asList(method.getAnnotationsByType(Before.class))); // 获取方法上的before's
		
		
		//1). 校验注入的Before是否合法
		// 为了描述方便,把当前获取到的拦截器设为 A<T>, 当前接口为: D<R>
		// 那么,R 必须是T或者是T的子类.
		// 这里非常抽象,请慢慢推敲,自然会明白.
		for (Before before : befores) {
			Class<? extends BeforeFilter<? extends Repository>>[] classz = before.value();
			for (Class<? extends BeforeFilter<? extends Repository>> class1 : classz) {
				// ParameterizedType.class.isAssignableFrom(genericReturnType.getClass())
				// 获得父类的范型.. ................ 如果父类是个接口通过这种方式不行(已经证实)!, 在写这个代码的时候, 明确知道class1的父类是抽象类
				Type type = class1.getGenericSuperclass();
				if(ParameterizedType.class.isAssignableFrom(type.getClass())) { // 如果这个type的实例就是ParameterizedType的子类或就是ParameterizedType
					ParameterizedType parameterizedType = (ParameterizedType) type;
					Type[] tys = parameterizedType.getActualTypeArguments();
					for (Type ty : tys) {
						if(Class.class.isAssignableFrom(ty.getClass())) {
							Class<?> t = (Class<?>) ty; // 范型<>中的类型
							if(Repository.class.isAssignableFrom(t)) {
								if(!t.isAssignableFrom(iclazz)) { // 如果当前iclazz不是T的子类且不就是T.
									this.abortWith(method, "\n"+before+"\n"+parameterizedType.getRawType().getTypeName()+"<"+t.getName()+"> 这个拦截器作用范围不在" + iclazz +"上!.\n"
											+ "解决参考方案: " + parameterizedType.getRawType().getTypeName()+"<" +QueryRepository.class.getSimpleName()+ "> 它的作用范围为: 可以放在QueryRepository的子类上\n"
													+ parameterizedType.getRawType().getTypeName()+"<" +iclazz.getSimpleName()+ "> 它的作用范围为: 只能放在"+iclazz.getName()+"类上\n"
															+ "亲爱的伙伴,您明白了吗?");
								}
							}
						} else {
							this.abortWith(method, before.toString()+"错误!");
						}
					}
				} else {
					this.abortWith(method, before.toString()+"错误!");
				}
			}
		}
		
		
		
		// 当前类中所有的@After
		List<After> afters = new ArrayList<>(Arrays.asList(iclazz.getAnnotationsByType(After.class))); // 获取类级别的after's
		afters.addAll(Arrays.asList(method.getAnnotationsByType(After.class)));// 获取方法上的after's
		for (After after : afters) {
			Class<? extends AfterFilter<? extends Repository>>[] classz = after.value();
			for (Class<? extends AfterFilter<? extends Repository>> class1 : classz) {
				// ParameterizedType.class.isAssignableFrom(genericReturnType.getClass())
				// 获得父类的范型.. ................ 如果父类是个接口通过这种方式不行(已经证实)!, 在写这个代码的时候, 明确知道class1的父类是抽象类
				Type type = class1.getGenericSuperclass();
				if(ParameterizedType.class.isAssignableFrom(type.getClass())) { // 如果这个type的实例就是ParameterizedType的子类或就是ParameterizedType
					ParameterizedType parameterizedType = (ParameterizedType) type;
					Type[] tys = parameterizedType.getActualTypeArguments();
					for (Type ty : tys) {
						if(Class.class.isAssignableFrom(ty.getClass())) {
							Class<?> t = (Class<?>) ty; // 范型<>中的类型
							if(Repository.class.isAssignableFrom(t)) {
								if(!t.isAssignableFrom(iclazz)) { // 如果当前iclazz不是T的子类且不就是T.
									this.abortWith(method, "\n"+after+"\n"+parameterizedType.getRawType().getTypeName()+"<"+t.getName()+"> 这个拦截器作用范围不在" + iclazz +"上!.\n"
											+ "解决参考方案: " + parameterizedType.getRawType().getTypeName()+"<" +QueryRepository.class.getSimpleName()+ "> 它的作用范围为: 可以放在QueryRepository的子类上\n"
													+ parameterizedType.getRawType().getTypeName()+"<" +iclazz.getSimpleName()+ "> 它的作用范围为: 只能放在"+iclazz.getName()+"类上\n"
															+ "亲爱的伙伴,您明白了吗?");
								}
							}
						} else {
							this.abortWith(method, after.toString()+"错误!");
						}
					}
				} else {
					this.abortWith(method, after.toString()+"错误!");
				}
			}
		}
		
		
		
		
		return method;
	}
	

}







