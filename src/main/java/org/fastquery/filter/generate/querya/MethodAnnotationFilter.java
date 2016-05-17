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

import org.fastquery.core.Modifying;
import org.fastquery.core.Query;
import org.fastquery.filter.generate.common.MethodFilter;
import org.fastquery.util.TypeUtil;

/**
 * 检测注解上的SQL 是否是Modifying
 * @author xixifeng (fastquery@126.com)
 */
public class MethodAnnotationFilter implements MethodFilter {

	@Override
	public Method doFilter(Method method) {
		
		
		// SQL Modifying 关键字汇总
		String[] updateFlags = {"update","insert","delete","create","alter"};
		
		// 1). 检测是否是update,如果是,需要加注解Modifying
		// 返回与该元素关联的注释。如果没有与该元素关联的注释，返回值是一个长度为0的数组。 
		Query[] querys = method.getAnnotationsByType(Query.class);
		for (Query query : querys) {
				// 获取sql语句
				String sql = query.value();
				
				if( method.getAnnotation(Modifying.class)==null ) {
					for (String updateFlag : updateFlags) {
						if(TypeUtil.containsIgnoreCase(sql, updateFlag)) {
							this.abortWith(method, query.value()+"中包含有SQL关键字"+updateFlag+",它属于修改操作,必须要配合@Modifying一起使用.");
						}
					}
				}
		}
		
		// 2). 检测... 
		
		return method;
	}

}
