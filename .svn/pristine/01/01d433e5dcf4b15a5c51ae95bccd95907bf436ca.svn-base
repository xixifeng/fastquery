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

package org.fastquery.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.fastquery.core.RepositoryException;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class FqClassLoader extends ClassLoader {
	
	private static final Logger LOG = Logger.getLogger(FqClassLoader.class);
	
	private static Map<String, Object> beans = new HashMap<>();

	FqClassLoader(ClassLoader parent){
		super(parent);
	}
	
	final Object defineClassByName(String name, byte[] b, int off, int len){
		Class<?> clazz = defineClass(name, b, off, len);
		Object obj = null;
		try {
			obj = clazz.newInstance();
		} catch (InstantiationException e) {
			throw new RepositoryException(e.getMessage(),e);
		} catch (IllegalAccessException e) {
			LOG.error(e);
			throw new RepositoryException(e.getMessage(),e);
		}
		
		beans.put(clazz.getName(), obj);
		
		return obj;
	}
	
	static final Object findLoadedClassByName(String name) {
		return beans.get(name);
	}
}
