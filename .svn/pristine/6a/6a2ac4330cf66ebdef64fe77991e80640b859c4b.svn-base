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

package org.fastquery.filter.generate.modifying;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import org.fastquery.core.Id;
import org.fastquery.filter.generate.common.MethodFilter;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class ArgsFilter implements MethodFilter {

	@Override
	public Method doFilter(Method method) {
		// 1). 校验: 方法的参数如果标识有@Id,那么该参数的类型必须是如下类int,Integer,long,Long,String
		Parameter[] parameters = method.getParameters();
		Class<?> currentType;
		int count = 0; // 统计@Id设置的目标在没有错误的前提下,共出现的次数.(如果出错,程序就中断了,下面的校验都不会执行)
		for (int i = 0; i < parameters.length; i++) {
			currentType = parameters[i].getType();
			if (parameters[i].getAnnotation(Id.class) != null) {
				count += 1;
				if ((currentType != int.class) && (currentType != Integer.class) && (currentType != long.class)
						&& (currentType != Long.class) && (currentType != String.class)) {
					this.abortWith(method,
							String.format("这个方法的第%s个参数用@Id标识了,因此这个参数的可选类型范围为:[int,Integer,long,Long,String],而当前设置的类型是:%s", i + 1,currentType.getName()));
					break;
				}
			}
		}

		// 2). 检验: 截至这里. 统计@Id出现的次数如果大于1,是错误的!
		if(count>1){
			this.abortWith(method, "列表中@Id不能出现多次.");
		}
		return method;
	}

}
