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

package org.fastquery.util;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.util.List;
import java.util.ArrayList;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class JarListClass {

	/**
	 * 遍历目录下的所有jar包中的所有class
	 * @param d jar包目录地址
	 * @return
	 */
	public static List<Class<?>> jarClasses(String d) {
		List<Class<?>> list = new ArrayList<>();
		
		File[] fs = new File(d).listFiles();
		for (File f : fs) {
			  if(f.isDirectory() || !f.getAbsolutePath().endsWith(".jar"))
	          continue;
			JarFile jar = null;
			try {
					jar = new JarFile(f.getAbsolutePath());
					Enumeration<JarEntry> entry = jar.entries();
					while (entry.hasMoreElements()) {
						JarEntry jarEntry = entry.nextElement();
						if (jarEntry.isDirectory() || !jarEntry.getName().endsWith(".class"))
							continue;
						String name = jarEntry.getName().replace(".class", "").replace("/", ".");
						//System.out.println(name);
						try {
							if(name.indexOf("sun.")==-1 && name.indexOf("jdk.")==-1){
								list.add(Class.forName(name));	
							}
						} catch (Exception e) {
						}
					}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if(jar!=null){
						jar.close();	
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}
		
		return list;
	}
}
