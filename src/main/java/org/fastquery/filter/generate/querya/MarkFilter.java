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
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.fastquery.core.Param;
import org.fastquery.core.Placeholder;
import org.fastquery.core.Query;
import org.fastquery.filter.generate.common.MethodFilter;
import org.fastquery.util.TypeUtil;
import org.fastquery.where.Condition;

/**
 * 如果Query,Condition中出现有":name" 表达式, 那么当前方法必须存在Parm注解并且保持一致. <br>
 * Parm("") 命名时禁止用""字符串
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class MarkFilter implements MethodFilter {

	@Override
	public Method doFilter(Method method) {
		
		Set<String> params = new HashSet<>();
		Parameter[] parameters = method.getParameters();
		for (Parameter parameter : parameters) {
			Param param = parameter.getAnnotation(Param.class);
			if(param != null) {
				params.add(param.value());
				if("".equals(param.value().trim())){
					this.abortWith(method, "@Param(\""+param.value()+"\")这个小括号里面的值不能为空字符串");
				}
			}
		}
		
		String slreg = Placeholder.SL_REG;
		String preg = Placeholder.P_REG;
		
		// 把能与SL_REG和preg匹配的表达式收集起来
		Set<String> ps = new HashSet<>();
		Query[] queries = method.getAnnotationsByType(Query.class);
		for (Query query : queries) {
			// 为什么要把","替换成" ,",请看SL_REG的注释
			ps.addAll( TypeUtil.matchesNotrepeat(query.value().replace(",", " ,"), slreg) );
			ps.addAll( TypeUtil.matchesNotrepeat(query.countField().replace(",", " ,"), slreg) );
			ps.addAll( TypeUtil.matchesNotrepeat(query.countQuery().replace(",", " ,"), slreg) );
			
			ps.addAll( TypeUtil.matchesNotrepeat(query.value(), preg) );
			ps.addAll( TypeUtil.matchesNotrepeat(query.countField(), preg) );
			ps.addAll( TypeUtil.matchesNotrepeat(query.countQuery(), preg) );
				
		}
		
		Condition[] conditions = method.getAnnotationsByType(Condition.class);
		for (Condition condition : conditions) {
			ps.addAll(TypeUtil.matchesNotrepeat(condition.value(), slreg));
		}
		
		for (String p : ps) {
			String s;
			if(Pattern.matches(slreg, p)) {
				s = p.replaceFirst(":","");
			} else {
				s = p.replace("${", "").replace("}","");
			} 
			if(!params.contains(s)) {
				this.abortWith(method, String.format("发现存在%s,而从参数中没有找到@Param(\"%s\"),这种语法是不被允许的.", p,s));
			}
		}
		
		return method;
	}

}
