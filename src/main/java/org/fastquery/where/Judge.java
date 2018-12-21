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

package org.fastquery.where;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Objects;

import org.fastquery.core.Param;
import org.fastquery.core.QueryContext;
import org.fastquery.core.RepositoryException;

/**
 * 
 * @author mei.sir@aliyun.cn
 */
public abstract class Judge { // NO_UCD
	
	public abstract boolean ignore();

	/**
	 * 根据参数的名称获取它的值
	 * @param <T> 参数的类型
	 * @param name 参数名称(即:@Param的value)
	 * @param clazz 参数的类型
	 * @return 参数的值
	 */
	@SuppressWarnings("unchecked")
	protected <T> T getParameter(String name,Class<T> clazz) {
		
		Objects.requireNonNull(name);
		Objects.requireNonNull(clazz);
		
		// "@Param(\""+name+"\")标记在"+t.getName()+"类型的参数上,而调用者要求将其值转换成" + clazz.getName()+"类型,"+t.getName()+" cannot be cast to " + clazz.getName()
		// ClassCastException 在此捕获不到已经验证
		return (T) getParameter(name);
	}
	
	protected Object getParameter(String name) {
		Objects.requireNonNull(name);
		int paramIndex = getParamIndex(name);
		if(paramIndex != -1) {
			return QueryContext.getArgs()[paramIndex];
		} else {
			throw new RepositoryException("从发生方法中没有找到@Param(\""+name+"\")");
		}
	}
	
	// 没有找到返回-1
	private static int getParamIndex(String paramName) {
		Annotation[][] annotations = QueryContext.getMethod().getParameterAnnotations();
		int len = annotations.length;
		for (int i = 0; i < len; i++) {
			Annotation[] anns = annotations[i];
			for (Annotation ann : anns) {
				if (ann.annotationType() == Param.class) {
					Param param = (Param) ann;
					if(param.value().equals(paramName)) {
						return i;
					}
				}
			}
		}
		return -1;
	}

	public static Class<?> getParamType(String paramName,Method method) {
		Annotation[][] annotations = method.getParameterAnnotations();
		int len = annotations.length;
		for (int i = 0; i < len; i++) {
			Annotation[] anns = annotations[i];
			for (Annotation ann : anns) {
				if (ann.annotationType() == Param.class) {
					Param param = (Param) ann;
					if(param.value().equals(paramName)) {
						return method.getParameterTypes()[i];
					}
				}
			}
		}
		throw new RepositoryException("根据@Param(\"" + paramName+"\") 没有找到对应的参数");
	}
}
