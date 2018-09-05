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

import java.lang.reflect.Method;

import org.fastquery.core.QueryContext;
import org.fastquery.core.RepositoryException;
import org.fastquery.where.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author mei.sir@aliyun.cn
 */
public final class SetParser {
	
	private static final Logger LOG = LoggerFactory.getLogger(SetParser.class);
	
	private SetParser() {
	}
	
	public static String process() {
		Method method = QueryContext.getMethod();
		Object[] args = QueryContext.getArgs();
		Set[] sets = method.getAnnotationsByType(Set.class);
		if(sets.length == 0) {
			return null;
		} else {
			StringBuilder sb = new StringBuilder("set ");
			out:for (Set set : sets) {
				String value = set.value();
				value = TypeUtil.paramFilter(method, args, value);
				java.util.Set<String> pars = TypeUtil.matchesNotrepeat(value, "\\?\\d+");
				for (String par : pars) {
					int index = Integer.parseInt(par.replace("?", "")); // 计数是1开始的
					if (ignoreSet(set, args[index - 1])) { //注意:  @Set(....?1...?2) // ?1 都能决定 ?2 该条件忽略.  "?1保留条件" && "?2 不保留条件" = 不保留
						continue out;
					}
				}

				sb.append(value);
				sb.append(",");
				
			}
			if("set ".equals(sb.toString())) {
				throw new RepositoryException("@Set 修改选项全部被忽略了,这是不允许的");
			} else {
				// 去掉最后一个逗号
				sb.deleteCharAt(sb.length()-1);
				sb.append(' ');
				return sb.toString();
			}
		}
	}
	
	
	/**
	 * 裁决是否忽略指定的条件,返回true表示要把这个条件忽略掉
	 * 
	 * @param set 条件
	 * @return y:true/n:false
	 */
	private static boolean ignoreSet(Set set, Object arg) {
		if(arg == null) {
			return set.ignoreNull();
		} else if("".equals(arg.toString())) {
			return set.ignoreEmpty();
		} else {
			try {
				return set.ignore().newInstance().ignore();
			} catch (InstantiationException | IllegalAccessException e) {
				
				// 这个异常其实永远也发生不了,该异常已经通过静态分析,提升到初始化阶段了
				
				LOG.error("{} 必须有一个不带参数且用public修饰的构造方法.反之,作废",set.ignore());
				return false;
			}
		}
	}
}
