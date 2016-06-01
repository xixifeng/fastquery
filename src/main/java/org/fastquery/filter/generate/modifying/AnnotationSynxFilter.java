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
import java.util.Map;

import org.fastquery.core.Modifying;
import org.fastquery.core.Placeholder;
import org.fastquery.core.Query;
import org.fastquery.filter.generate.DBUtils;
import org.fastquery.filter.generate.common.MethodFilter;
import org.fastquery.util.TypeUtil;

import com.alibaba.fastjson.JSONObject;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class AnnotationSynxFilter implements MethodFilter {

	@Override
	public Method doFilter(Method method) {
		
		Class<?> returnType = method.getReturnType();
		
		Modifying modifying = method.getAnnotation(Modifying.class);
		
		Query[] queries = method.getAnnotationsByType(Query.class);
		
		String id = modifying.id(); // 不可能为null 因此是使用前不用判断是否为null
		String table = modifying.table();
		
		// 当返回值为Map 或 JSONObject 或 bean时, Modifying中的id和table值是必选的
		if(returnType==Map.class || returnType==JSONObject.class || TypeUtil.hasDefaultConstructor(returnType)) {
			for (Query query : queries) {
				String sql = query.value();
				// 1). id 或 table 不能为""
				if("".equals(id) || "".equals(table) ) {
					this.abortWith(method, String.format("返回值是:%s 因此要求:%s中的id或table的值不能为空字符串.", returnType,modifying));
				}
				
				// 替换SQL中的占位变量符
				sql = sql.replaceAll(Placeholder.TABLE_REG, table);
				sql = sql.replaceAll(Placeholder.ID_REG, id);
				
				// 2).检测: table中的值,必须在当前sql语句中出现(证明:指定就是当前正在修改的表)
				if(!TypeUtil.containsIgnoreCase(sql, table)) {
					this.abortWith(method, String.format("返回值是:%s 因此要求:%s中指定当前要修改的表与\"%s\"语句实际要修改的表不一致.", returnType,modifying,sql));
				}
				
				// 3) @Modifying 必须中的id 和 table值,在数据库中是存在的.
				// 参考:SHOW COLUMNS from student where `KEY`='PRI'
				//String packageName = method.getDeclaringClass().getPackage().getName()
				String packageName = method.getDeclaringClass().getName();
				// id 是 table表的主键吗?
				if( !DBUtils.getInstance().findColumnKey(packageName, table, id) ) { // 如果Modifying描述错误!
					this.abortWith(method, String.format("返回值是:%s 因此要求:%s中描述:%s是%s表的主键,与实际不符.", returnType,modifying,id,table));
				}	
			}	
		}
		
		for (Query query : queries) {
			String s = query.value();
			// 4) 如果SQL中存在 #{#table}, 而@Modifying中的table属性为"",这属于语法错误.
			if( (s.indexOf(Placeholder.TABLE) != -1) && "".equals(table)) {
				this.abortWith(method, s+"中存在"+Placeholder.TABLE+", 那么,@Modifying中的table属性为不能为空");
			}
			
			// 5) 如果SQL中存在 #{#id}, 而@Modifying中的id属性为"",这属于语法错误.	
			if( (s.indexOf(Placeholder.ID) != -1) && "".equals(id)) {
				this.abortWith(method, s+"中存在"+Placeholder.ID+", 那么,@Modifying中的id属性为不能为空");
			}
		}
		
		return method;
	}

}
