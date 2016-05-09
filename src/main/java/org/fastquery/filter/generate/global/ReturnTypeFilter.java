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

package org.fastquery.filter.generate.global;

import java.lang.reflect.Method;

import org.fastquery.filter.generate.common.MethodFilter;
import org.fastquery.util.TypeUtil;

/**
 * 过滤返回值,不允许是Object,Objet[] Objet[]...N..[], Boolean
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class ReturnTypeFilter implements MethodFilter {

	@Override
	public Method doFilter(Method method) {

		// 1). 返回值不能是Object
		Class<?> returnType = method.getReturnType();
		if ( returnType == Object.class ) {
			this.abortWith(method, "返回值不能是Object,应用层得到一个Object,显然十分不友好!");
		}
		if(returnType.isArray()) {
			Class<?> comp = returnType.getComponentType();
			if(comp == Object.class) {
				this.abortWith(method, "返回值不能是Object,应用层得到一个Object,显然十分不友好!");
			}
		}

		// 2). 返回值禁止出现二维(包含二维)以上的数组
		// 返回值如果是一维数组,也只能允许是基本类型的数组
		String typeName = returnType.getTypeName();
		// 计算"[]" 出现的个数
		int count = TypeUtil.matches(typeName, "\\[\\]").size();
		if (count > 1) {
			this.abortWith(method, "返回值是" + count
					+ "维数组,针对使用者来说,语义不强,要是没有明文规范每个下标表示什么值,使用者就无法理解,这也是你所不愿意的事情,因此需要禁止,建议使用javax.json.JsonArray 或者List<map>!");
		}
		
		// 3). 返回值禁止是Boolean类型
		// Boolean 至少有三种状态 null,true,false, 布尔在db中层中,无非用来表达,有或无, 对或错,因此永不着其包装类型
		// 没有查询出,可以用false表达,而不是null.
		if(returnType == Boolean.class) {
			this.abortWith(method, "返回值不支持Boolean类型,换成boolean试试看.");
		}

		return method;
	}

}