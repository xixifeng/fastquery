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

package org.fastquery.filter.generate.query;

import java.lang.reflect.Method;
import java.util.List;

import org.fastquery.core.Placeholder;
import org.fastquery.core.Query;
import org.fastquery.filter.generate.common.MethodFilter;
import org.fastquery.page.NotCount;
import org.fastquery.page.Page;
import org.fastquery.util.TypeUtil;

/**
 * 
 * Page 语法安全检测
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class PageFilter implements MethodFilter {

	@Override
	public Method doFilter(Method method) {
		if(method.getReturnType() == Page.class) {
			
			// 部分校验已经提取到 全局 PageableFilter里去了
			
			// 7). 如果用@NotCount标识,那么@Query中的countId必须是默认值"id",countQuery的值必须为"". 不求总行数,countId和countQuery设置值是没有意义的.
			Query query = method.getAnnotation(Query.class);
			if(method.getAnnotation(NotCount.class)!=null) {
				// 这里query不可能为null的,若为null根本就进入不了该filter
				String countId = query.countField();
				String countQuery = query.countQuery();
				if(!"id".equals(countId) || !"".equals(countQuery)) {
					this.abortWith(method, "如果用@NotCount标识,不能设置@Query中的countId的值和countQuery的值.不求总行数,设置countId和countQuery的值是没有意义的.");
				}
			}
			
			// 8). #{#limit} 禁止重复出现
			String sql = query.value();
			List<String> strs = TypeUtil.matches(sql, Placeholder.LIMIT_RGE);
			if(strs.size()>1) {
				this.abortWith(method, String.format("%s中,禁止重复出现%s",sql,Placeholder.LIMIT));
			}
			
			
		}
		
		return method;
	}

}
