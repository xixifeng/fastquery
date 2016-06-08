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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
		for (Before before : befores) {
			Class<? extends BeforeFilter<? extends Repository>>[] classz = before.value();
			for (Class<? extends BeforeFilter<? extends Repository>> class1 : classz) {
				compareType(method, iclazz, before, class1);
			}
		}
		
		// 当前类中所有的@After
		List<After> afters = new ArrayList<>(Arrays.asList(iclazz.getAnnotationsByType(After.class))); // 获取类级别的after's
		afters.addAll(Arrays.asList(method.getAnnotationsByType(After.class)));// 获取方法上的after's
		for (After after : afters) {
			Class<? extends AfterFilter<? extends Repository>>[] classz = after.value();
			for (Class<? extends AfterFilter<? extends Repository>> class1 : classz) {
				compareType(method, iclazz, after, class1);
			}
		}

		return method;
	}
	
	// 假设: 有两个类,其class分别为c1和c2. c1的直接父类的范型为X
	//  设: X=<T>
	//  若: (c2就是T 或者 c2是T的子类) 并且T是Repository的子类或者就是Repository. 并且X限制只有一个成员.("<>"中是有可能有多个参数的)
	//  则: 返回true,反之返回false.
	public void compareType(Method method, Class<?> c2, Annotation annotation,Class<?> c1) {
		Type type = c1.getGenericSuperclass(); // 获取c1的直接父类的范型
		// 如果这个type的实例就是ParameterizedType的子类或就是ParameterizedType
		if(ParameterizedType.class.isAssignableFrom(type.getClass())) { // 判断是否是范型
			ParameterizedType parameterizedType = (ParameterizedType) type; // 如果是范型就转换
			Type[] tys = parameterizedType.getActualTypeArguments(); // 范型中有多个类型. 换言只尖括号"<>"中有多个类型

			if(tys.length>1) {
				//当前: X的范型参数个数已大于1
				this.abortWith(method, annotation.toString()+"错误!");
			}
			
			Type ty = tys[0];
			
			if(Class.class.isAssignableFrom(ty.getClass())) { // 如果当前类型是Class的子类或者就是Class
				Class<?> t = (Class<?>) ty; 
				if(Repository.class.isAssignableFrom(t)) { // 如果t是Repository的子类或者t就是Repository
					if(!t.isAssignableFrom(c2)) { 
						//当前: c2不是t的子类且不就是t.
						filterScopeError(method, annotation, parameterizedType, c2, t);
					}
				} else {
					// 当前: T 不是Repository,且不是Repository的子类
					this.abortWith(method, annotation.toString()+"错误!");
				}
			} else {
				// 当前: T 不是Class,且不是Class的子类
				this.abortWith(method, annotation.toString()+"错误!");
			}
			
		} else {
			//当前: c1的直接父类不是范型
			this.abortWith(method, annotation.toString()+"错误!");
		}
	}
	
	/**
	 * 过滤器作用范围错误提示信息
	 */
	private void filterScopeError(Method method,Annotation annotation,ParameterizedType parameterizedType,Class<?> iclazz,Class<?> t){
		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		sb.append(annotation);
		sb.append("\n");
		
		sb.append(parameterizedType.getRawType().getTypeName());
		sb.append("<");
		sb.append(t.getSimpleName());
		sb.append("> 这个拦截器的作用范围不在");
		sb.append(iclazz);
		sb.append("上(也就说放在这个类上是非法的)!\n");
		
		sb.append("它可以放在");
		sb.append(t.getSimpleName());
		sb.append("类上\n");
		
		sb.append("举例说明:若有一个拦截器叫A<T>,那么这个拦截器可以用放在T类或T的子类里.反之是违规操作\n");

		this.abortWith(method,sb.toString());
	}

}







