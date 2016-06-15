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

package org.fastquery.filter.generate.query;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

import org.fastquery.filter.generate.common.MethodFilter;
import org.fastquery.page.Page;
import org.fastquery.util.TypeUtil;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 查询返回值安全检测,既然是查询,如果方法的返回值是void,显然是不受允许的.
 * @author xixifeng (fastquery@126.com)
 */
public class QueryReturnTypeFilter implements MethodFilter {

	@Override
	public Method doFilter(Method method) {
		String errmsg = String.format("为这个方法设置的返回值错误,其返回值类型支持类型如下:%n%s %n%s %n%s %n%s %n%s %n%s %n%s %n%s %n%s %n%s %n%s %n%s %n",
				"1). long 用于统计总行数",
				"2). boolean 判断是否存在",
				"3). Map<String,Object>", 
				"4). List<Map<String,Object>>",
				"5). List<实体>",
				"6). Page",
				"7). JSONObject",
				"8). JSONArray",
				"9). Integer,Double,Long,Short,Byte,Character,Float,String 八种基本类型(除了Boolean)",
				"10). Integer[],Double[],Long[],Short[],Byte[],Character[],Float[]",
				"11). 自定义实体数组",
				"12).自定义实体,必须包含有默认的构造函数"
				);
		
		Type genericReturnType = method.getGenericReturnType();
		Class<?> returnType = method.getReturnType();

		
		// 返回值所允许的类型
		if(returnType == long.class) {
			return method;
		} else if(returnType == boolean.class) {
			return method;
		} else if(TypeUtil.isMapSO(genericReturnType) || TypeUtil.isListMapSO(genericReturnType)){
			return method;
		} else if(returnType == List.class){ // List<Bean> 类型
			return method;
		} else if(returnType == Page.class){
			return method;
		}
		else if(returnType == JSONObject.class) {
			return method;
		} else if(returnType == JSONArray.class) {
			return method;
		} else if(isWarrp(returnType)) {
			return method;
		} else if(isWarrp(returnType.getComponentType())) { // 包装类型[]
			return method;
		} else if(TypeUtil.hasDefaultConstructor(returnType.getComponentType())){ // bean[]
			return method;
		} else if(TypeUtil.hasDefaultConstructor(returnType)) { // bean
			return method;
		} else {
			this.abortWith(method, errmsg);
		}
		
		return method;
	}

	
	// 判断 returnType 是否是包装类型
	private boolean isWarrp(Class<?> returnType){
		if(returnType==null) {
			return false;
		}
		return (returnType == Integer.class) || (returnType == Double.class) || (returnType == Long.class) || (returnType == Short.class) || (returnType == Byte.class) || (returnType == Character.class) || (returnType == Float.class)|| (returnType == String.class);
	}
}
