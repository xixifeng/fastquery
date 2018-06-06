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
import java.lang.reflect.Parameter;

import org.fastquery.core.Placeholder;
import org.fastquery.core.Repository;
import org.fastquery.jersey.Embed;
import org.fastquery.util.TypeUtil;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class AsmRest implements Opcodes {

	private AsmRest() {
	}

	/**
	 * 自动生成Repository接口的实现类并以字节的形式返回, 该方法是线程安全的.
	 * 
	 * @param repositoryClazz repository class
	 * @return 生成的类字节码
	 */
	public static synchronized byte[] generateBytes(Class<? extends Repository> repositoryClazz) {

		// internal Name 格式: org/fastquery/core/QueryRepository
		String internalName = Type.getInternalName(repositoryClazz);

		// 需要生成类的接口集合
		String[] interfaces = new String[] { internalName };

		// 给待生成的实现类取个名字
		String proxyName = internalName + Placeholder.REST_SUF;

		MethodVisitor mv;
		FieldVisitor fv;
		AnnotationVisitor av0;
		// 生成类
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		cw.visit(V1_8, ACC_PUBLIC, proxyName, null, "java/lang/Object", interfaces);

		// 给类标识@Singleton注解
		av0 = cw.visitAnnotation("Ljavax/inject/Singleton;", true);
		av0.visitEnd();

		String dbDes = Type.getDescriptor(repositoryClazz);

		// 注入依赖的db
		fv = cw.visitField(ACC_PRIVATE, "d", dbDes, null, null);
		av0 = fv.visitAnnotation("Ljavax/inject/Inject;", true);
		av0.visitEnd();
		fv.visitEnd();

		// 生成不带参的构造方法
		mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
		mv.visitCode();
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		mv.visitInsn(RETURN);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
		// 生成不带参的构造方法 End

		// 根据接口clazz 生成实现的方法
		Method[] methods = repositoryClazz.getMethods();
		for (Method method : methods) {
			if (Modifier.isAbstract(method.getModifiers())) { // 只针对 abstract 方法
				cw = generateMethod(cw, method, proxyName, dbDes);
			}
		}
		cw.visitEnd();

		return cw.toByteArray();
	}

	/**
	 * 生成方法
	 * 
	 * @param cw
	 * @param method
	 * @param exceptions
	 * @param interfaceClazz
	 */
	private static ClassWriter generateMethod(ClassWriter cw, java.lang.reflect.Method method, String proxyName, String dbDes) {

		org.objectweb.asm.commons.Method m = new org.objectweb.asm.commons.Method(method.getName(), Type.getMethodDescriptor(method));
		GeneratorAdapter mv = new GeneratorAdapter(ACC_PUBLIC, m, null, null, cw);

		mv.visitLdcInsn(method.getName()); // excute的第1参数
		mv.visitLdcInsn(Type.getType(method).getDescriptor()); // excute的第2参数

		// excute的第3参数(是可变参数)
		Parameter[] parameters = method.getParameters();
		setIn(mv, parameters);

		// excute的第4个参数
		mv.visitVarInsn(ALOAD, 0);

		// 调用Prepared中的excute方法
		// INVOKESTATIC
		mv.visitFieldInsn(GETFIELD, proxyName, "d", dbDes);
		mv.visitMethodInsn(INVOKESTATIC, Type.getType(Embed.class).getInternalName(), "excute",
				"(Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;Lorg/fastquery/core/Repository;)Ljava/lang/Object;", false);

		// 返回值处理
		String internalName = Type.getInternalName(method.getReturnType());
		int sort = Type.getReturnType(method).getSort();
		if (sort == 0) { // 如果返回值是 Void
			mv.visitInsn(POP);
			mv.visitInsn(RETURN);
		} else if ((1 <= sort) && (sort <= 8)) { // 如果是基本类型 [1,8]
			Object[] objs = TypeUtil.getTypeInfo(Type.getReturnType(method).getDescriptor());
			String type = objs[0].toString();
			mv.visitTypeInsn(CHECKCAST, type);
			mv.visitMethodInsn(INVOKEVIRTUAL, type, objs[1].toString(), objs[2].toString(), false);
			// Integer.parseInt(objs[3].toString()) 比
			// Integer.valueOf(objs[3].toString()).intValue()更优.
			mv.visitInsn(Integer.parseInt(objs[3].toString()));

		} else { // sort==9 表述数组类型int[]或Integer[], srot=10表示包装类型
			mv.visitTypeInsn(CHECKCAST, internalName);
			mv.visitInsn(ARETURN);
		}

		// mv.visitEnd()
		mv.endMethod();
		return cw;
	}

	private static void setIn(GeneratorAdapter mv, Parameter[] parameters) {
		int size = parameters.length;
		if (size < 6) {
			mv.visitInsn(size + ICONST_0);
			// 注释不要删除
			// 当 size = 0 时,则: mv.visitInsn(3); 3对应 ICONST_0
			// 当 size = 1 时,则: mv.visitInsn(4); 4对应 ICONST_1
			// 当 size = 2 时,则: mv.visitInsn(5); 5对应 ICONST_2
			// 当 size = 3 时,则: mv.visitInsn(6); 6对应 ICONST_3
			// 当 size = 4 时,则: mv.visitInsn(7); 7对应 ICONST_4
			// 当 size = 5 时,则: mv.visitInsn(8); 8对应 ICONST_5
		} else {
			mv.visitIntInsn(BIPUSH, size);
		}
		mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");

		int nextIndex = 1; // 指定下一次?LOAD应该的使用的索引
		for (int i = 0; i < size; i++) {
			mv.visitInsn(DUP);
			if (i < 6) {
				mv.visitInsn(i + ICONST_0);
				// 注释不要删除
				// 当 i = 0 时, 则: mv.visitInsn(3); 3对应ICONST_0
				// 当 i = 1 时, 则: mv.visitInsn(4); 3对应ICONST_1
				// 当 i = 2 时, 则: mv.visitInsn(5); 3对应ICONST_2
				// 当 i = 3 时, 则: mv.visitInsn(6); 3对应ICONST_3
				// 当 i = 4 时, 则: mv.visitInsn(7); 3对应ICONST_4
				// 当 i = 5 时, 则: mv.visitInsn(8); 3对应ICONST_5
			} else {
				mv.visitIntInsn(BIPUSH, i);
			}

			// 注释不要删除
			// int -> ILOAD
			// double -> DLOAD
			// long -> LLOAD
			// short -> ILOAD

			// byte -> ILOAD
			// boolean -> ILOAD
			// char -> ILOAD

			// float -> FLOAD

			// 计算当前遍历的参数的类型
			Class<?> currentType = parameters[i].getType();
			// int","double","long","short","byte","boolean","char","float"
			String valueOf = "valueOf";
			if (currentType == int.class) {
				mv.visitVarInsn(ILOAD, nextIndex);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", valueOf, "(I)Ljava/lang/Integer;", false);
				// 指定下一次?LOAD应该的使用的索引
				nextIndex = nextIndex + 1;
			} else if (currentType == double.class) {
				mv.visitVarInsn(DLOAD, nextIndex);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", valueOf, "(D)Ljava/lang/Double;", false);
				nextIndex = nextIndex + 2;
			} else if (currentType == long.class) {
				mv.visitVarInsn(LLOAD, nextIndex);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", valueOf, "(J)Ljava/lang/Long;", false);
				nextIndex = nextIndex + 2;
			} else if (currentType == short.class) {
				mv.visitVarInsn(ILOAD, nextIndex);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", valueOf, "(S)Ljava/lang/Short;", false);
				nextIndex = nextIndex + 1;
			} else if (currentType == byte.class) {
				mv.visitVarInsn(ILOAD, nextIndex);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", valueOf, "(B)Ljava/lang/Byte;", false);
				nextIndex = nextIndex + 1;
			} else if (currentType == boolean.class) {
				mv.visitVarInsn(ILOAD, nextIndex);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", valueOf, "(Z)Ljava/lang/Boolean;", false);
				nextIndex = nextIndex + 1;
			} else if (currentType == char.class) {
				mv.visitVarInsn(ILOAD, nextIndex);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Character", valueOf, "(C)Ljava/lang/Character;", false);
				nextIndex = nextIndex + 1;
			} else if (currentType == float.class) {
				mv.visitVarInsn(FLOAD, nextIndex);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", valueOf, "(F)Ljava/lang/Float;", false);
				nextIndex = nextIndex + 1;
			} else {
				mv.visitVarInsn(ALOAD, nextIndex);
				nextIndex = nextIndex + 1;
			}
			mv.visitInsn(AASTORE);
		}
	}
}
