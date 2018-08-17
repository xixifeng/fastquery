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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class FQueryResourceImpl implements Resource {

	private ClassLoader classLoader;

	public FQueryResourceImpl(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		if (!exist(name)) {
			return null;
		}
		
		String fcd = System.getProperty("fastquery.config.dir");
		if(fcd != null) {
			File file = new File(fcd,name);
			if(file.exists()) {
				if("c3p0-config.xml".equals(name)) {
					System.setProperty("com.mchange.v2.c3p0.cfg.xml",file.getAbsolutePath());  
				}
				try {
					return new FileInputStream(file);
				} catch (FileNotFoundException e) {
					throw new RepositoryException(e);
				}
			}
		}
		
		return classLoader.getResourceAsStream(name);
	}

	@Override
	public boolean exist(String name) {
		
		if (name == null || name.charAt(0) == '/') {
			return false;
		}
		
		String fcd = System.getProperty("fastquery.config.dir");
		if(fcd != null) {
			File file = new File(fcd,name);
			if(file.exists()) {
				return true;
			}
		}
		
		return classLoader.getResource(name) != null;
	}
}