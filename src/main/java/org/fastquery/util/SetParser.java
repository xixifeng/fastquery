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

package org.fastquery.util;

import org.fastquery.asm.Script2Class;
import org.fastquery.core.MethodInfo;
import org.fastquery.core.QueryContext;
import org.fastquery.core.RepositoryException;
import org.fastquery.where.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author mei.sir@aliyun.cn
 */
final class SetParser {
	
	private static final Logger LOG = LoggerFactory.getLogger(SetParser.class);
	
	private SetParser() {
	}
	
	static String process() {
		MethodInfo method = QueryContext.getMethodInfo();
		Set[] sets = method.getSets();
		int len = sets.length;
		if(len == 0) {
			return null;
		} else {
			Object[] args = QueryContext.getArgs();
			StringBuilder sb = new StringBuilder("set ");
			for (int i = 0; i < len; i++) {
				Set set = sets[i];
				String value = set.value();
				value = TypeUtil.paramFilter(method, args, value);
				if(!ignoreSet(set, value, i)) {
					// if$ , else$ 解析
					ifelse(sb, i, set, value); 
				}
			}

			if("set ".equals(sb.toString())) {
				throw new RepositoryException("@Set 修改选项全部被忽略了,这是不允许的");
			} else {
				// 将最后一个字符替换成 ' '
				sb.setCharAt(sb.length() - 1, ' ');
				return sb.toString();
			}
		}
	}

	private static void ifelse(StringBuilder sb, int i, Set set, String value) {
		if("true".equals(set.if$()) || Script2Class.getJudge(i).ignore()) { // if$绑定的脚本执行之后结果如果是"真"
			sb.append(value);
			sb.append(",");
		} else {
			String elseValue = set.else$();
			if(!"".equals(elseValue)) {
				elseValue = TypeUtil.paramFilter(QueryContext.getMethodInfo(), QueryContext.getArgs(), elseValue);
				sb.append(elseValue);
				sb.append(",");
			} 
		}
	}

	/**
	 * 
	 * @param set 条件
	 * @param value Set.value处理之后的条件值
	 * @param setPosition 条件在方法上的位置索引
	 * @return y:true/n:false
	 */
	private static boolean ignoreSet(Set set, String value, int setPosition) {
		// 忽略因子列表,任何一个都可以导致忽略
		// 这些因子不拿出来定义是有意义的, 多个 || 第一个true,后面的方法就不执行了
		// 像||,&& 这种运算, 多长都不嫌丑
		
		java.util.Set<String> pars = TypeUtil.matchesNotrepeat(value, "\\?\\d+");
		Object[] args = QueryContext.getArgs();
		for (String par : pars) {
			int index = Integer.parseInt(par.replace("?", "")); // 计数是1开始的
			Object arg = args[index - 1];
			if (getFactor1(set, arg) || getFactor2(set, arg)) {
				return true;
			}
		}
				
		return getFactor3(set, setPosition) || getFactor4(set);
	}

	private static boolean getFactor1(Set set, Object arg) {
		return (arg == null) && set.ignoreNull();
	}

	private static boolean getFactor2(Set set, Object arg) {
		return arg!=null && "".equals(arg.toString()) && set.ignoreEmpty();
	}

	private static boolean getFactor3(Set set, int index) {
		return (!set.ignoreScript().equals("false")) && Script2Class.getJudge(index).ignore();
	}

	private static boolean getFactor4(Set set) {
		try {
			return set.ignore().newInstance().ignore();
		} catch (InstantiationException | IllegalAccessException e1) {
			// 这个异常其实永远也发生不了,该异常已经通过静态分析,提升到初始化阶段了
			
			LOG.error("{} 必须有一个不带参数且用public修饰的构造方法.反之,作废",set.ignore());
			return false;
		}
	}
}
