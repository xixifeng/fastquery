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

package org.fastquery.analysis;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.fastquery.page.Page;
import org.fastquery.page.PageIndex;
import org.fastquery.page.PageSize;
import org.fastquery.page.Pageable;
import org.fastquery.util.TypeUtil;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
class PageableFilter implements MethodFilter {

	// 如果当前方法不是分页, 其参数就不应该出现Pageable对象,更不能出现@PageIndex @PageSize

	@Override
	public void doFilter(Method method) {

		Class<?> returnType = method.getReturnType();
		Parameter[] parameters = method.getParameters();
		if (returnType != Page.class) {
			// 不是分页类型,那么方法的参数不能引用分页控制类
			checkNotPage(method, parameters);
		} else {
			// 1). Page<T> 中的T要么是Map,要么是一个实体.
			Type genericReturnType = method.getGenericReturnType();
			if (!(genericReturnType instanceof ParameterizedType)) {
				this.abortWith(method, "分页Page必须是一个范型");
			} else {
				// 校验分页控制参数的合法性
				checkPageCtrlParm(method, parameters, genericReturnType);
			}

		}
	}
	
	private void checkPageCtrlParm(Method method, Parameter[] parameters, Type genericReturnType) {
		ParameterizedType type = (ParameterizedType) genericReturnType;
		Type[] types = type.getActualTypeArguments();
		Type t = types[0];
		if (ParameterizedType.class.isAssignableFrom(t.getClass())
				&& !("org.fastquery.page.Page<java.util.Map<java.lang.String, java.lang.Object>>".equals(type.getTypeName()))) {
			this.abortWith(method, "Page<T> 中的T要么是Map<String,Object>,要么是一个实体.");
		} else if (Class.class.isAssignableFrom(t.getClass()) && !(TypeUtil.hasDefaultConstructor((Class<?>) t))) {
			this.abortWith(method, "Page<T> 中的T要么是Map<String,Object>,要么是一个实体.");
		} else if (!TypeUtil.hasType(Pageable.class, parameters) && !hasPageAnn(parameters)) { // 2). 方法参数中,要么出现Pageable类型,要么存在@PageIndex和@PageSize
			this.abortWith(method, "这是分页,参数中要么存在Pageable类型的参数(不能是Pageable的子类),要么存在@PageIndex和@PageSize");
		} else if (TypeUtil.hasType(Pageable.class, parameters) && hasPageAnn(parameters)) { // 3). 参数中要么存在Pageable类型的参数,要么存在@PageIndex和@PageSize,不能同时都出现
			this.abortWith(method, "这是分页,参数中要么存在Pageable类型的参数,要么存在@PageIndex和@PageSize,不能同时都出现.");
		} else if (TypeUtil.countRepeated(PageIndex.class, parameters) > 1) {// 4). @PageIndex或@PageSize 最多只能出现一次
			this.abortWith(method, "@PageIndex 最多只能出现一次");
		} else if (TypeUtil.countRepeated(PageSize.class, parameters) > 1) {
			this.abortWith(method, "@PageSize 最多只能出现一次");
		} else if (TypeUtil.countRepeated(PageIndex.class, parameters) + TypeUtil.countRepeated(PageSize.class, parameters) == 1) {// 5). @PageIndex或@PageSize 不能独存
			this.abortWith(method, "@PageIndex或@PageSize 不能独存,要么都不要出现.");
		} else if (TypeUtil.findAnnotationIndex(PageIndex.class, parameters) != -1
				&& TypeUtil.findParameter(PageIndex.class, parameters).getType() != int.class) {// 6). @PageIndex或@PageSize 只能标识在int类型上
			this.abortWith(method, "@PageIndex 只能标识在int类型的参数上");
		} else if (TypeUtil.findAnnotationIndex(PageSize.class, parameters) != -1
				&& TypeUtil.findParameter(PageSize.class, parameters).getType() != int.class) {
			this.abortWith(method, "@PageSize 只能标识在int类型的参数上");
		}
	}

	private void checkNotPage(Method method, Parameter[] parameters) {
		for (int i = 0; i < parameters.length; i++) {
			if (Pageable.class.isAssignableFrom(parameters[i].getType())) {
				this.abortWith(method, String.format("又不是分页,该方法的第%s个参数中为何要用%s类型呢?", i + 1, parameters[i].getType().getSimpleName()));
			} else if (parameters[i].getAnnotation(PageIndex.class) != null) {
				this.abortWith(method, String.format("又不是分页,该方法的第%s个参数中为何要标识@PageIndex呢?", i + 1));
			}else if (parameters[i].getAnnotation(PageSize.class) != null) {
				this.abortWith(method, String.format("又不是分页,该方法的第%s个参数中为何要标识@PageSize呢?", i + 1));
			}
		}
	}

	private boolean hasPageAnn(Parameter[] parameters) {
		return (TypeUtil.findAnnotationIndex(PageIndex.class, parameters) != -1) && TypeUtil.findAnnotationIndex(PageSize.class, parameters) != -1;
	}
}
