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
import java.lang.reflect.Parameter;
import java.util.Set;
import java.util.regex.Pattern;

import org.fastquery.core.Param;
import org.fastquery.core.Placeholder;
import org.fastquery.core.Query;
import org.fastquery.util.TypeUtil;
import org.fastquery.where.Condition;

/**
 * 条件参数安全检查
 * 
 * @author xixifeng (fastquery@126.com)
 */
class ConditionParameterFilter implements MethodFilter {

	@Override
	public void doFilter(Method method) {

		// 注意: sql参数与方法参数个数匹配问题,已经在 ParameterFilter里做了安全校验.
		// 1). @Query中的value值,有且只能出现一次#{#where} (允许不出现). 换言之,出现"#{#where}"的个数不能大于1
		// 2). 如果有条件注解,那么@Query中的value值,必须有#where
		// 3). 第1个@Condition不能包含有条件连接符("and" 或 "or")
		// 4). if$ 和 ignoreScript 不能共存
		// 5).
		// 6).
		Query[] queries = method.getAnnotationsByType(Query.class);
		if (queries.length != 0) {
			int countWhere = TypeUtil.matches(queries[0].value(), Placeholder.WHERE_REG).size(); //
			// >1).
			if (countWhere > 1) {
				this.abortWith(method, "@Query中的value值,有且只能出现一次#{#where}");
			}

			// >2). 已经在 QueryFilterHelper 里做校验了

			// >3)
			Condition[] conditions = method.getAnnotationsByType(Condition.class);
			for (int i = 0; i < conditions.length; i++) {
				// 截取第一个单词
				String value = conditions[i].value();
				Set<String> ps = TypeUtil.matchesNotrepeat(value, Placeholder.EL_REG);
				Parameter[] parameters = method.getParameters();
				ps.forEach(p -> {
					for (Parameter parameter : parameters) {
						Param param = parameter.getAnnotation(Param.class);
						if (param != null && (("${" + param.value() + "}").equals(p) || ("$" + param.value()).equals(p))) {
							return;
						}
					}
					this.abortWith(method, p + " 没有找到匹配的参数.");
				});
				String word = TypeUtil.getFirstWord(value);
				if (i == 0 && ("or".equalsIgnoreCase(word) || "and".equalsIgnoreCase(word))) {
					this.abortWith(method, "第1个@Condition的值,左边加条件连接符\"" + word + "\"干什么,这个条件跟谁相连?去掉吧.");
				}

				if (i != 0 && (!"or".equalsIgnoreCase(word) && !"and".equalsIgnoreCase(word)) && !Pattern.matches("^\\$\\S+(.|\n)*", value.trim()) ) {
						this.abortWith(method, "第" + (i + 1) + "个@Condition的值\"" + value + "\",缺少条件连接符,如果上一个条件存在,用什么跟它相连?");
				}

				// > 4)
				if (!"true".equals(conditions[i].if$()) && !"false".equals(conditions[i].ignoreScript())) {
					this.abortWith(method, "第" + (i + 1) + "个@Condition中的if$属性和ignoreScript属性不能同时被自定义");
				}

			}
		}

	}
}
