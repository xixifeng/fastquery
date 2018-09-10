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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fastquery.core.Placeholder;
import org.fastquery.core.QueryContext;
import org.fastquery.core.RepositoryException;
import org.fastquery.util.TypeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

/**
 * 
 * @author mei.sir@aliyun.cn
 */
public class Script2Class {
	
	private static final Logger LOG = LoggerFactory.getLogger(Script2Class.class);
	
	private static Map<String, Judge> judges = new HashMap<>();
	
	private Script2Class() {}
	
	// 根据基本类型获得包装类型
		private static Class<?> wrapperType(Class<?> cls) {
			if (cls == int.class) {
				return Integer.class;
			} else if (cls == short.class) {
				return Short.class;
			} else if (cls == long.class) {
				return Long.class;
			} else if (cls == byte.class) {
				return Byte.class;
			} else if (cls == boolean.class) {
				return Boolean.class;
			} else if (cls == char.class) {
				return Character.class;
			} else if (cls == float.class) {
				return Float.class;
			} else if (cls == double.class) {
				return Double.class;
			} else {
				return cls;
			}
		}
		
	/**
	 * 处理脚本中的冒号表达式
	 * @param script 脚本
	 * @param method 脚本的所属方法
	 * @return 被处理后的脚本
	 */
	private static String processParam(String script, Method method) {
		List<String> names = TypeUtil.matches(script, Placeholder.COLON_REG);
		for (String paramName : names) {
			String name = paramName.replace(":", "");
			Class<?> oldC = Judge.getParamType(name, method);
			Class<?> newC = wrapperType(oldC);
			if(oldC == newC) {
				// 包装类型 如果后面跟着
				script = script.replaceAll(paramName, "(("+newC.getName()+")this.getParameter(\""+name+"\"))");
				
			} else { // 基本类型 -> 包装类型 那么就要解包
				// 加上解包方法
				script = script.replaceAll(paramName, "(("+newC.getName()+")this.getParameter(\""+name+"\"))." + oldC.getName() + "Value()");
			}
		}
		return script;
	}
	
	private static String makeClassName(Method method,int index) {
		String src = method.toGenericString() + "$#" + index;
		return Base64.getEncoder().encodeToString(src.getBytes());
	}
	
	public static Judge getJudge(int index) {
		return judges.get(makeClassName(QueryContext.getMethod(),index));
	}
	
	public static void generate(Class<?> clazz) {
		Method[] methods = clazz.getMethods();
		for (Method method : methods) {
			Set[] sets = method.getAnnotationsByType(Set.class);
			for (int i = 0; i < sets.length; i++) {
				String script = sets[i].script();
				if(!script.equals("false")) {
					gr(method, i, script);
				}
			}
			Condition[] conditions = method.getAnnotationsByType(Condition.class);
			for (int i = 0; i < conditions.length; i++) {
				String script = conditions[i].script();
				if(!script.equals("false")) {
					gr(method, i, script);
				}
			}
		}
	}

	private static void gr(Method method, int i, String script) {
		try {
			generate(script, method, i);
		} catch (InstantiationException | IllegalAccessException | CannotCompileException | NotFoundException e) {
			throw new RepositoryException(method + "中的脚本  \"" + script +"  \" 编译错误",e);
		}
	}
	
	private static void generate(String script,Method method,int annotationIndex) throws CannotCompileException, NotFoundException,InstantiationException, IllegalAccessException {
		
		ClassPool pool = ClassPool.getDefault();
		
		String className = makeClassName(method, annotationIndex);
		
		CtClass ctClass = pool.makeClass(className);
		
		// 增加父类
		ctClass.setSuperclass(pool.get("org.fastquery.where.Judge"));
		
		// 增加方法
		CtMethod ctMethod = new CtMethod(CtClass.booleanType,"ignore",new CtClass[]{},ctClass);
		//为自定义方法设置修饰符
		ctMethod.setModifiers(Modifier.PUBLIC);
				
		String code = processParam(script, method);
		StringBuilder body = new StringBuilder();
		body.append("return ");
		body.append(code);
		body.append(";");
		
		LOG.info("生成源代码: {}",body);
		ctMethod.setBody(body.toString());
		ctClass.addMethod(ctMethod);
		
		judges.put(ctClass.getName(), (Judge) ctClass.toClass().newInstance());
		
	}
}
