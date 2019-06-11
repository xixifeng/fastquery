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

package org.fastquery.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

import org.fastquery.page.NotCount;
import org.fastquery.where.Condition;
import org.fastquery.where.Set;

/**
 * 
 * @author mei.sir@aliyun.cn
 */
public class MethodInfo {
	
	private Method method;
	private Modifying modifying;
	private Query[] queries;
	private Parameter[] parameters;
	private Class<?> returnType;
	private Id id;
	private Transactional t;
	private QueryByNamed queryByNamed;
	private NotCount notCount;
	private Condition[] conditions;
	private Annotation[][] parameterAnnotations;
	private Type genericReturnType;
	private String name;
	private Query query;
	private Set[] sets;

	public Transactional getT() {
		return t;
	}
	public MethodInfo(Method method) { // NO_UCD
		this.method = method;
		this.modifying = method.getAnnotation(Modifying.class);
		this.queries = method.getAnnotationsByType(Query.class);
		this.parameters = method.getParameters();
		this.returnType = method.getReturnType();
		this.id = method.getAnnotation(Id.class);
		this.t = method.getAnnotation(Transactional.class);
		this.queryByNamed = method.getAnnotation(QueryByNamed.class);
		this.notCount = method.getAnnotation(NotCount.class);
		this.conditions = method.getAnnotationsByType(Condition.class);
		this.parameterAnnotations =  method.getParameterAnnotations();
		this.genericReturnType = method.getGenericReturnType();
		this.name = method.getName();
		this.query = method.getAnnotation(Query.class);
		this.sets = method.getAnnotationsByType(Set.class);
	}
	Method getMethod() {
		return method;
	}
	public Modifying getModifying() {
		return modifying;
	}
	public Query[] getQueries() {
		return queries;
	}
	public Parameter[] getParameters() {
		return parameters;
	}
	public Class<?> getReturnType() {
	        return returnType;
	}
	public Id getId() {
		return id;
	}
	public QueryByNamed getQueryByNamed() {
		return queryByNamed;
	}
	public NotCount getNotCount() {
		return notCount;
	}
	public Condition[] getConditions() {
		return conditions;
	}
	public Annotation[][] getParameterAnnotations() {
		return parameterAnnotations;
	}
	public Type getGenericReturnType() {
		return genericReturnType;
	}
	
	public String getName() {
		return name;
	}
	public Query getQuery() {
		return query;
	}
	public Set[] getSets() {
		return sets;
	}
	@Override
	public String toString() {
		return this.method.toString();
	}
	public String toGenericString() {
		return this.method.toGenericString();
	}
}
