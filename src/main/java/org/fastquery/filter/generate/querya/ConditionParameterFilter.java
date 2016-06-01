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
import java.util.regex.Pattern;

import org.fastquery.core.Placeholder;
import org.fastquery.core.Query;
import org.fastquery.filter.generate.common.MethodFilter;
import org.fastquery.util.TypeUtil;
import org.fastquery.where.Condition;

/**
 * 条件参数安全检查
 * @author xixifeng (fastquery@126.com)
 */
public class ConditionParameterFilter implements MethodFilter {

	/*
		@Query("select * from Student #{#where} order by desc")
	// 增加一些条件
	@Condition(l="field1",o=Operator.EQ,r="?1") // ?1的值,如果是null, 该行条件将不参与运算
	@Condition(l="field2",o=Operator.EQ,r="?2")
	@Condition(l="field3",o=Operator.EQ,r="?3",ignoreNull=false) // ?3的值是null,该条件也参与运算.
	@Condition(l="age",o=Operator.IN,r="(?4,?5,?6)") // age in(?4,?5?6)
	@Condition(l="name",o={Operator.NOT,Operator.LIKE},r="?7") // 等效于 name not like ?7
	@Condition(l="info",o=Operator.BETWEEN,r="?8 and ?9") // 等效于 info between ?8 and ?9
	 */
	@Override
	public Method doFilter(Method method) {
		
		// 注意: sql参数与方法参数个数匹配问题,已经在 ParameterFilter里做了安全校验.
		// 0). @Condition 中的l值 不能为空
		// 1). @Query中的value值,有且只能出现一次#{#where} (允许不出现). 换言之,出现"#{#where}"的个数不能大于1
		// 2). 如果有条件注解,那么@Query中的value值,必须有#where
		// 3). 第1个条件(@Condition)的条件连接符号必须为"",其余条件必须有条件连接符号.并且不能为""
		// 4). 条件运算符如果是Operator.IN,那么r()的值必须符合正则: "(?4,?5,?6)"
		// 5). 条件运算符如果是Operator.BETWEEN,那么r()的值必须符合正则: "?8 and ?9"
		// 6). 条件运算符如果不是Operator.BETWEEN又不是Operator.BETWEEN,那么r()的值必须符合正则: "?8"
		Query[] queries = method.getAnnotationsByType(Query.class);
		if(queries.length==0){
			return method;
		}
		
		int countWhere = TypeUtil.matches(queries[0].value(),Placeholder.WHERE_REG).size(); // 
		// >1).
		if( countWhere >1 ) {
			this.abortWith(method,"@Query中的value值,有且只能出现一次#{#where}");
		}
		
		Condition[] conditions = method.getAnnotationsByType(Condition.class);
		int countCondition = conditions.length;
		// >2).
		// 已经在 QueryFilterHelper 里做校验了
		// 如果存在条件语句 并且 @Query中的value值,没有出现"#{#where}"
		
		if( countCondition > 0 ){
	     	// >3).
			if( !"".equals(conditions[0].c().getVal()) ){
				this.abortWith(method, "第1个条件(@Condition)的条件连接符号必须为默认值:c=COperator.NONE");
			} else {
				checkR(conditions[0].c().getVal(),conditions[0].l(), conditions[0].r(),1, method);
			}
			for (int i = 1; i < countCondition; i++) {
				String c = conditions[i].c().getVal();
				if("".equals(c)){
				   this.abortWith(method, String.format("%s 第%s个条件的条件连接符号不能为默认值:c=COperator.NONE",conditions[i],i+1));
				}
				checkR(c,conditions[i].l(), conditions[i].r(), i+1, method);
			}
		}
		
		return method;
	}

	/**
	 * 
	 * @param c @Condition 中的c值
	 * @param r @Condition 中的r值
	 * @param index 第几个条件(从1开始计数)
	 * @param method 当前方法
	 */
	private void checkR(String c,String l,String r,int index,Method method){
		// >0).
		if("".equals(l)) // l永远不可能为null
		{
			this.abortWith(method, String.format("第%s个条件的l的值不能为\"\"", index));
		}
		
		// >4).
		if( "IN".equalsIgnoreCase(c) && !Pattern.matches(Placeholder.INV_REG, r)){
			this.abortWith(method, "条件运算符如果是Operator.IN,那么r()的值必须符合格式: \"(?4,?5,?6)\"");
			// >5).
		} else if("BETWEEN".equalsIgnoreCase(c) && !Pattern.matches(Placeholder.ANDV_REG, r)){
			this.abortWith(method, "条件运算符如果是Operator.BETWEEN,那么r()的值必须符合格式: \"?8 and ?9\"");
			// >6).
		} else if( "".equals(c) && !Pattern.matches(Placeholder.SP2_REG, r) ){
			this.abortWith(method, String.format("第%s个条件的r的值必须符合格式: \"?8\"", index));
		}
	}
	
}
