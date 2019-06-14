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

package org.fastquery.analysis;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;

import org.fastquery.core.Modifying;
import org.fastquery.core.Primarykey;
import org.fastquery.core.Query;
import org.fastquery.util.TypeUtil;

import com.alibaba.fastjson.JSONObject;

/**
 * 在QueryRepository中 方法上标识有{@link Modifying}
 * 
 * @author xixifeng (fastquery@126.com)
 */
class ModifyingReturnTypeFilter implements MethodFilter {

	@Override
	public void doFilter(Method method) {

		String errmsg = String.format(
				"为这个方法设置返回值错误!%n该方法允许的返回值类型如下: %n%s \t- 没有返回值;%n%s \t- 用来获取影响行数;%n%s \t- 保存的实体以Map封装后输出;%n%s \t- 保存的实体JSON格式;%n%s \t- 保存的实体Bean(注意:该bean必须有默认不带参数的构造方法);%n%s \t- 获取主键;%n%s \t - 操作是否正确.",
				"void",
				"int",
				"java.util.Map<String, Object>或java.util.Map<String, String>",
				"com.alibaba.fastjson.JSONObject",
				"Bean",
				Primarykey.class.getName(),
				boolean.class);
		
		String errmsg2 = " 该SQL的操作结果不能映射成Map格式";
		Type genericReturnType = method.getGenericReturnType();
		Class<?> returnType = method.getReturnType();

		Query[] queries = method.getAnnotationsByType(Query.class);
		for (Query query : queries) {
			// 如果当前方法没有标记 @Modifying 和 @ Query 是进入不了这个过滤器的. 过滤器已经分成了8类
			String sql = query.value();
			// 如果返回值是Map 并且 没有包含 "insert into" 或 "update"

			// sql中既不含insert又不含update,才会返回true
			boolean nothas = !TypeUtil.containsIgnoreCase(sql, "insert into") && !TypeUtil.containsIgnoreCase(sql, "update");
			if ((returnType == Map.class) && nothas) { // 1). 校验 SQL中存在"insert into" 才能允许其返回值为 Map<String,Object>
				this.abortWith(method, sql + errmsg2);
			} else if ((returnType == JSONObject.class) && nothas) { // 2). 校验:如果返回值是JSONObject,那么SQL必须是insert语句 或 "update"
				this.abortWith(method, sql + errmsg2);
			} else if ((TypeUtil.hasDefaultConstructor(returnType)) && nothas) { // 3). 校验:如果返回值是实体,那么SQL必须是insert 或 "update"
				this.abortWith(method, sql + errmsg2);
			} else if (TypeUtil.containsIgnoreCase(sql, "delete") && (returnType != void.class) && (returnType != int.class) && (returnType != int[].class) && (returnType != boolean.class)) { // 4). 校验:如果是删除操作,那么返回值只能是void 或是 int, 或者int[]
				this.abortWith(method, sql + "该SQL是删除操作,返回值只能是void,int,int[]或boolean类型.");
			} else if(returnType != void.class && 
				returnType != int.class &&
				returnType != int[].class &&
				returnType != boolean.class &&
				!TypeUtil.isMapSO(genericReturnType) &&
				returnType != JSONObject.class &&
				returnType != Primarykey.class &&
				!TypeUtil.hasDefaultConstructor(returnType) // 不是bean
		           ){ // 5). 校验返回值所允许的类型
				this.abortWith(method, errmsg);
	           }
		}
	}
}
