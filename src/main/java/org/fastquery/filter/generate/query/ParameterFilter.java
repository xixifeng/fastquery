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
import java.util.List;

import org.fastquery.filter.generate.common.MethodFilter;
import org.fastquery.util.TypeUtil;

/**
 * 检测SQL语句中的参数个数,跟方法的参数个数是否一致,不一致当然不通过!<br>
 * 当然也检测SQL参数指定越界问题,如"?0"是不被允许的.
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class ParameterFilter implements MethodFilter {

	@Override
	public Method doFilter(Method method) {
		StringBuilder sb = new StringBuilder();
		List<String> list = QueryFilterHelper.getQuerySQL(method);
		list.forEach(sb::append);
		String sql = sb.toString();
			
			// 1). 检测SQL参数的书写规则
			// 计算出sql 中出现 "\\?\\d+" 几次, "?"后面进跟数字表明是一个参数
			int sqlParameterCount = TypeUtil.matches(sql, "\\?\\d+").size(); // sql中的参数个数
			// 统计"?"的个数
			int sx = TypeUtil.matches(sql, "\\?").size();
			// 如果SQL中出想的"\\?"出现的个数如果跟 "\\?\\d+" 出现的个数不相等, 则可以断言是sql参数的书写语法没有遵守"?"后面紧跟数字这一法则.
			if (sx != sqlParameterCount) {
				this.abortWith(method, sql+" SQL设置参数错误,SQL中的参数设置必须是\"?\"后紧跟数字!");
			}
			
			
			
			// 2).检测SQL语句中指定的参数,需要方法多少个参数去支持
			// 计算出sql语句中的参数个数,去重.(例如:同时SQL中同时出现两次?1,算一个)
			int sqlParameterCountNoRepeat = TypeUtil.matchesNotrepeat(sql, "\\?\\d+").size();
			int methodParameterCount = method.getParameterCount();
			// 如果sql中的参数个数(去重) > 方法中的参数个数
			if (sqlParameterCountNoRepeat > methodParameterCount) {
				this.abortWith(method,
						String.format("%s SQL语句中的参数已经明确指明,该方法必须需要有%s个参数, 但是方法的参数个数是:%s,注意:SQL中的参数设置必须是\"?\"后紧跟数字!", sql,sqlParameterCountNoRepeat, methodParameterCount));
			}

			// 3). 检测SQL语句中的参数所指定的方法索引是否越界.
			int[] ints = TypeUtil.getSQLParameter(sql);
			int methodIndex; // 计算从1开始
			for (int i = 0; i < ints.length; i++) { 
				// 如果该for sqlParameterCount,可能会导致ints数组越界 因为sqlParameterCount长度有可能大于ints的长度
				// 例如 sql "...?1 ?2 ?1..." 只需要方法带两个参数即可满足 
				
				methodIndex = ints[i];
				if(methodIndex==0) {
					this.abortWith(method, sql+" SQL中禁止出现\"?0\",你是想对应方法中的第一个参数吗?那么请用\"?1\"");
				}
				
				// 获取当前sql参数对应方法中的第几个参数
				if(methodIndex > methodParameterCount) { // 如果sql语句中,指定的方法参数索引已经大于方法参数的个数,显然是错误的.
					this.abortWith(method, String.format("%s SQL中\"?%s\"表示指定该方法的第%s个参数,可是当前方法一共只有%s个参数!", sql,methodIndex,methodIndex,methodParameterCount));
				}
			}
			
		return method;
	}

}