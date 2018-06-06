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

package org.fastquery.jersey;

import org.fastquery.core.RepositoryException;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class FQueryBinder extends AbstractBinder {

	private FqClassLoader fqClassLoader;

	private FQueryBinder(ClassLoader webClassLoader) {
		this.fqClassLoader = new FqClassLoader(webClassLoader, this);
		new GenerateRepositoryImpl(fqClassLoader).persistent();
	}

	@Override
	protected void configure() {
		//
	}

	public static void bind(ResourceConfig resource) { // NO_UCD (unused code)
		FQueryBinder fb = new FQueryBinder(resource.getClassLoader());
		resource.register(fb);
		try {
			resource.registerClasses(fb.fqClassLoader.getResourceClasses());
		} catch (ClassNotFoundException e) {
			throw new RepositoryException("没有找到类", e);
		}
	}
}
