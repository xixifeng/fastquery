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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.fastquery.core.Placeholder;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.ClassMemberValue;

/**
 * 用于生成rest实现类
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class AsmRest {

	private AsmRest() {
	}

	/**
	 * 自动生成Repository接口的实现类并以字节的形式返回, 该方法是线程安全的.
	 * 
	 * @param repositoryClazz repository class
	 * @return 生成的类字节码
	 */
	public static synchronized byte[] generateBytes(Class<?> repositoryClazz) {

		// 生成类
		ClassPool pool = ClassPool.getDefault();
		// web容器中的repository 需要增加classPath
		ClassClassPath classClassPath = new ClassClassPath(repositoryClazz);
		pool.removeClassPath(classClassPath);
		pool.insertClassPath(classClassPath);
		String className = repositoryClazz.getName() + Placeholder.REST_SUF;
		CtClass ctClass = pool.makeClass(className);

		ClassFile ccFile = ctClass.getClassFile();
		ConstPool constpool = ccFile.getConstPool();

		try {
			// 增加接口
			ctClass.setInterfaces(new CtClass[] { pool.get(repositoryClazz.getName()) });

			// 增加字段
			CtClass executor = pool.get(repositoryClazz.getName());
			CtField field = new CtField(executor, "d", ctClass);
			field.setModifiers(Modifier.PRIVATE);
			FieldInfo fieldInfo = field.getFieldInfo();
			// 标识属性注解
			AnnotationsAttribute fieldAttr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
			javassist.bytecode.annotation.Annotation autowired = new javassist.bytecode.annotation.Annotation(
					"javax.inject.Inject", constpool);
			fieldAttr.addAnnotation(autowired);
			fieldInfo.addAttribute(fieldAttr);
			ctClass.addField(field);
			
			AsmRepository.addGetInterfaceClassMethod(repositoryClazz, ctClass);
			
			// 标识类注解
			AnnotationsAttribute classAttr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
			javassist.bytecode.annotation.Annotation singAnn = new javassist.bytecode.annotation.Annotation(
					"javax.inject.Singleton", constpool);
			classAttr.addAnnotation(singAnn);
			ccFile.addAttribute(classAttr);

			// 实现抽象方法
			Method[] methods = repositoryClazz.getMethods();
			for (Method method : methods) {
				if (!method.isDefault()) {
					CtMethod cm = CtMethod.make(AsmRepository.getMethodDef(method) + "{return d."+method.getName()+"($$);}", ctClass);
					// 标识方法注解
					AnnotationsAttribute methodAttr = new AnnotationsAttribute(constpool,
							AnnotationsAttribute.visibleTag);
					javassist.bytecode.annotation.Annotation extendsAnn = new javassist.bytecode.annotation.Annotation(
							"org.fastquery.core.Extends", constpool);
					ArrayMemberValue arrayMemberValue = new ArrayMemberValue(constpool);
					Annotation[] mas = method.getAnnotations();
					ClassMemberValue[] cmvs = new ClassMemberValue[mas.length];
					for (int i = 0; i < mas.length; i++) {
						cmvs[i] = new ClassMemberValue(mas[i].annotationType().getName(), constpool);
					}
					arrayMemberValue.setValue(cmvs);
					extendsAnn.addMemberValue("value", arrayMemberValue);
					methodAttr.addAnnotation(extendsAnn);
					MethodInfo info = cm.getMethodInfo();
					info.addAttribute(methodAttr);
					ctClass.addMethod(cm);
				}
			}
			
			return ctClass.toBytecode();

		} catch (Exception e) {
			throw new ExceptionInInitializerError(e);
		}
	}
}
