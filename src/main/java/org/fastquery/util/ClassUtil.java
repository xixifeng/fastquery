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

package org.fastquery.util;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.fastquery.core.Repository;
import org.fastquery.core.RepositoryException;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class ClassUtil {

	private ClassUtil() {
	}

	/**
	 * 从包package中获取所有的Class
	 * 
	 * @param packageName 包地址
	 * @return clazz 集
	 */
	public static List<Class<?>> getClasses(String packageName) {

		// 第一个class类的集合
		List<Class<?>> classes = new ArrayList<>();

		// packageName 很可能是一个完整的接口
		try {
			putInterface(packageName, classes);
		} catch (RepositoryException e) {
			//
		}

		// 获取包的名字 并进行替换
		String packageDirName = packageName.replace('.', '/');

		// 定义一个枚举的集合 并进行循环来处理这个目录下的things
		Enumeration<URL> dirs;
		try {
			dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
			// 循环迭代下去
			while (dirs.hasMoreElements()) {
				// 获取下一个元素
				URL url = dirs.nextElement();
				// 得到协议的名称
				String protocol = url.getProtocol();
				// 如果是以文件的形式保存在服务器上
				if ("file".equals(protocol)) {
					// 获取包的物理路径
					String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
					// 以文件的方式扫描整个包下的文件 并添加到集合中
					findAndAddClassesInPackageByFile(packageName, filePath,classes);
				} else if ("jar".equals(protocol)) {
					packageName = findJarClassInterface(packageName, classes, packageDirName, url);
				}
			}
		} catch (IOException e) {
			throw new ExceptionInInitializerError(e);
		}

		return classes;
	}
	
	private static String findJarClassInterface(String packageName, List<Class<?>> classes, String packageDirName, URL url) throws IOException {
		JarFile jar = ((JarURLConnection) url.openConnection()).getJarFile();
		Enumeration<JarEntry> entries = jar.entries();
		while (entries.hasMoreElements()) {
			JarEntry entry = entries.nextElement();
			String name = entry.getName();
			if (name.charAt(0) == '/') { // 如果是以/开头的
				name = name.substring(1);// 获取后面的字符串
			}
			// 如果前半部分和定义的包名相同
			if (name.startsWith(packageDirName)) {
				int lastIndex = name.lastIndexOf('/');
				if (lastIndex != -1) { // 如果以"/"结尾,就是是一个包
					// 获取包名 把"/"替换成"."
					packageName = name.substring(0, lastIndex).replace('/', '.');
				}
				// 如果是一个.class文件,而且不是目录
				if (lastIndex != -1 && name.endsWith(".class") && !entry.isDirectory()) {
					String className = packageName + '.' + name.substring(packageName.length() + 1, name.length() - 6);
					putInterface(className, classes);
				}
			}
		}
		return packageName;
	}

	/**
	 * 以文件的形式来获取包下的所有Class
	 * 
	 * @param packageName
	 * @param packagePath
	 * @param classes
	 */
	private static void findAndAddClassesInPackageByFile(String packageName, String packagePath,List<Class<?>> classes) {
		// 获取此包的目录 建立一个File
		File dir = new File(packagePath);
		// 如果不存在或者 也不是目录就直接返回
		if (dir.exists() && dir.isDirectory()) {
			// 如果存在 就获取包下的所有文件 包括目录; 自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
			File[] dirfiles = dir.listFiles(file -> file.isDirectory() || file.getName().endsWith(".class"));
			// 循环所有文件
			for (File file : dirfiles) {
				// 如果是目录 则继续扫描
				if (file.isDirectory()) {
					findAndAddClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(),classes);
				} else {
					// 如果是java类文件 去掉后面的.class 只留下类名
					String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
					putInterface(className, classes);
				}
			}
		}
	}
	
	private static void putInterface(String className, List<Class<?>> classes) {
		try {
			Class<?> rcls = Class.forName(className);
			if (rcls.isInterface() && Repository.class.isAssignableFrom(rcls)) {
				// 添加到集合中去
				classes.add(rcls);
			}
		} catch (ClassNotFoundException e) {
			throw new RepositoryException(e);
		}
	}
}
