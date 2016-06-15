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

package org.fastquery.filter.generate.querya;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import org.fastquery.core.Source;
import org.fastquery.dsm.FQueryProperties;
import org.fastquery.filter.generate.common.MethodFilter;
import org.fastquery.util.TypeUtil;

/**
 * 一个QueryRepository必须要在fastquery.json配置basePackages.<br>
 * 
 * 在fastquery.json文件中,如果已经正确配置了basePackages,而没有配置数据源.
 * 那么必须通过@Source来获得相应的数据源. Source注解如果标识在方法的参数上,那么该参数只能是字符串类型.
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class SourceFilter implements MethodFilter {

	@Override
	public Method doFilter(Method method) {
		
		Class<?> clazz = method.getDeclaringClass();
		
		Parameter[] parameters = method.getParameters();
		
		//1). 在fastquery.json文件中,如果已经正确配置了basePackages,而没有配置数据源.
		//那么必须通过@Source来获得相应的数据源. Source注解如果标识在方法的参数上,那么该参数只能是字符串类型.
		String dataSourceName = FQueryProperties.findDataSourceName(clazz.getName());
		if(dataSourceName==null){ // 表明在fastquery.json中没有配置相应的数据源名称
			int index = TypeUtil.findAnnotationIndex(Source.class, parameters);
			if(index == -1) {
				this.abortWith(method, "* 在fastquery.json文件中,如果已经正确配置了basePackages,而没有配置数据源.\n* 那么必须通过@Source来获得相应的数据源. Source注解如果标识在方法的参数上,那么该参数只能是字符串类型.");
			}
			Class<?> ptype = method.getParameterTypes()[index];
			if(ptype!=String.class){
				this.abortWith(method, "Source注解如果标识在方法的参数上,那么该参数只能是String类型.");
			}
		}
		
		// 2). @Source不能重复出现
		if(TypeUtil.countRepeated(Source.class, parameters)>1) {
			this.abortWith(method, "@Source 只能出现一次.");
		}
		
		return method;
	}

}
