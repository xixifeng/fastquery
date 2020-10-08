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

package org.fastquery.asm;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;

import org.fastquery.analysis.GenerateExtends;
import org.fastquery.core.AbstractQueryRepository;
import org.fastquery.core.Placeholder;
import org.fastquery.core.QueryRepository;
import org.fastquery.mapper.QueryValidator;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class AsmRepository {

	private static final Logger LOG = LoggerFactory.getLogger(AsmRepository.class);

	private AsmRepository() {
	}

	private static String getReturnTypeName(Method method) {
		Class<?> returnType = method.getReturnType();
		String rtName = returnType.getName();
		if (returnType.getComponentType() != null) {
			return returnType.getComponentType().getName() + "[]";
		} else {
			return rtName;
		}
	}
	
	static String getMethodDef(Method method) {
		StringBuilder sb = new StringBuilder("public ");
		sb.append(getReturnTypeName(method));
		sb.append(' ');
		sb.append(method.getName());
		sb.append('(');
		sb.append(getParameterDef(method.getParameterTypes()));
		sb.append(')');
		return sb.toString();
	}

	public static String getParameterDef(Class<?>[] parameterTypes) {
		int len = parameterTypes.length;
		if (len > 0) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < len; i++) {
				if (parameterTypes[i].getComponentType() != null) {
					Class<?> clazz = parameterTypes[i].getComponentType();
					sb.append(clazz.getName().replace("[L","").replace("[","").replace(";",""));
					sb.append("[]");
					while ( (clazz = clazz.getComponentType()) != null) {
						sb.append("[]");
					}
				} else {
					sb.append(parameterTypes[i].getName());
				}
				sb.append(' ');
				sb.append("p");
				sb.append(i);
				sb.append(',');
			}
			return sb.deleteCharAt(sb.length() - 1).toString();
		} else {
			return "";
		}
	}

	private static String getParameterNames(Class<?>[] parameterTypes) {
		int len = parameterTypes.length;
		if (len > 0) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < len; i++) {

				Class<?> currentType = parameterTypes[i];

				if (currentType == int.class) {
					sb.append("Integer.valueOf");
				} else if (currentType == double.class) {
					sb.append("Double.valueOf");
				} else if (currentType == long.class) {
					sb.append("Long.valueOf");
				} else if (currentType == short.class) {
					sb.append("Short.valueOf");
				} else if (currentType == byte.class) {
					sb.append("Byte.valueOf");
				} else if (currentType == boolean.class) {
					sb.append("Boolean.valueOf");
				} else if (currentType == char.class) {
					sb.append("Character.valueOf");
				} else if (currentType == float.class) {
					sb.append("Float.valueOf");
				}

				sb.append("(p");
				sb.append(i);
				sb.append("),");
			}
			sb.deleteCharAt(sb.length() - 1);
			return "new Object[] { " + sb.toString() + " }";
		} else {
			return "org.apache.commons.lang3.ArrayUtils.EMPTY_OBJECT_ARRAY";
		}
	}
	
	/**
	 * 自动生成Repository接口的实现类并以字节的形式返回.
	 * 
	 * @param repositoryClazz repository class
	 * @return 生成的类字节码
	 */
	public static byte[] generateBytes(Class<?> repositoryClazz) {
		// 安全检测
		GenerateExtends.safeCheck(repositoryClazz);

		// 生成Judge
		Script2Class.generate(repositoryClazz);

		// 生成类
		ClassPool pool = ClassPool.getDefault();
		// web容器中的repository 需要增加classPath
		ClassClassPath classClassPath = new ClassClassPath(repositoryClazz);
		pool.removeClassPath(classClassPath);
		pool.insertClassPath(classClassPath);
		String className = repositoryClazz.getName() + Placeholder.DB_SUF;
		CtClass ctClass = pool.makeClass(className);
		try {
			if(QueryRepository.class.isAssignableFrom(repositoryClazz)) {
				// 设置父类
				ctClass.setSuperclass(pool.get(AbstractQueryRepository.class.getName()));
			}
			// 增加接口
			ctClass.setInterfaces(new CtClass[] { pool.get(repositoryClazz.getName()) });
			addGetInterfaceClassMethod(repositoryClazz, ctClass);
			makeSingleton(className, ctClass);
			makeMethod(repositoryClazz, ctClass);
			return ctClass.toBytecode();
		} catch (Exception e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	static void addGetInterfaceClassMethod(Class<?> repositoryClazz, CtClass ctClass) throws CannotCompileException {
		// 增加字段
		CtField field = CtField.make("private Class c = "+repositoryClazz.getName()+".class;", ctClass);
		ctClass.addField(field);
		// 覆盖部分default 方法
		CtMethod cm = CtMethod.make("public Class getInterfaceClass() {return c;}", ctClass);
		ctClass.addMethod(cm);
	}
	
	private static void makeMethod(Class<?> repositoryClazz, CtClass ctClass) throws CannotCompileException {
		// 实现抽象方法
		Method[] methods = repositoryClazz.getDeclaredMethods();
		int len = methods.length;
		if (len > 0) {
			// 新增MethodInfo缓存数组
			CtField field = CtField.make("private org.fastquery.core.MethodInfo[] m = new org.fastquery.core.MethodInfo[" + len + "];", ctClass);
			ctClass.addField(field);
			int index = 0;
			for (Method method : methods) {
				Class<?>[] ps = method.getParameterTypes();
				StringBuilder bodyBuilder = new StringBuilder();
				bodyBuilder.append('{');
				bodyBuilder.append("int j = ").append(index++).append(';');
				// 缓存method...
				bodyBuilder.append("if(this.m[j]==null) {");
				bodyBuilder.append("java.lang.reflect.Method m;");
				bodyBuilder.append("try {m = c.getMethod(\"").append(method.getName()).append("\", $sig);} catch (Exception e) {throw new org.fastquery.core.RepositoryException(e);}");
				bodyBuilder.append("this.m[j] = new org.fastquery.core.MethodInfo(m); ");
				bodyBuilder.append("}");
				// 缓存method... End
				bodyBuilder.append("return ($r) org.fastquery.core.Prepared.excute(this.m[j],").append(getParameterNames(ps)).append(", this) ;");
				bodyBuilder.append('}');
				CtMethod cm = CtMethod.make(getMethodDef(method) + bodyBuilder.toString(), ctClass);
				ctClass.addMethod(cm);
			}
		}
	}
	
	private static void makeSingleton(String className, CtClass ctClass) throws CannotCompileException {
		// 创建一个私有的静态变量
		ctClass.addField(CtField.make("private static " + className + " i;", ctClass));

		// 不带参数的私有方法
		CtConstructor privateConstructor = new CtConstructor(new CtClass[] {}, ctClass);
		privateConstructor.setModifiers(Modifier.PRIVATE);
		privateConstructor.setBody("{}");
		ctClass.addConstructor(privateConstructor);

		// 创建getInstance方法
		String getInstanceMethodSrc = "public static " + className + " g() { if(i == null) { i = new " + className + "();}return i;}";
		ctClass.addMethod(CtMethod.make(getInstanceMethodSrc, ctClass));
	}

	/**
	 * 所有的代码生成之后
	 * 
	 * @param classes Repository class 集合
	 */
	public static void after(List<Class<?>> classes) {
		QueryValidator.check(classes);
		classes.clear();

		LOG.debug("\n\n\n\n初始化阶段结束\n");
	}

}
