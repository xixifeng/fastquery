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

package org.fastquery.filter.generate.queryn;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;

import org.fastquery.filter.generate.common.MethodFilter;
import org.fastquery.util.FastQueryJSONObject;

/**
 * 要来校验,当前拦截到的方法,是否需要*.queries.xml文件
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class QueriesFileFilter implements MethodFilter {

	@Override
	public Method doFilter(Method method) {
		
		// osgi不支持 QueriesFileFilter.class.getClassLoader().getResource 这种写法!
		String className = method.getDeclaringClass().getName();
		
		List<String> pers = FastQueryJSONObject.getQueries();
		pers.add("");
		
		boolean exits = false;
		for (String per : pers) {
			String perxml = new StringBuilder().append(per).append(className).append(".queries.xml").toString();
			URL url = QueriesFileFilter.class.getClassLoader().getResource(perxml);
			if(url != null) {
				exits = true;
				break;
			}
		}
		
		if(!exits){
			this.abortWith(method, "这个方法标识了注解@QueryByNamed,而没有找到文件:" + new StringBuilder().append(className).append(".queries.xml").toString());
		}
		
		return method;
	}

}
