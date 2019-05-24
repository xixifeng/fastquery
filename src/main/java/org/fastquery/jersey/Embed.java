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

package org.fastquery.jersey;

import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.fastquery.core.QueryRepository;
import org.fastquery.util.TypeUtil;
import org.slf4j.Logger;

/**
 * 嵌入执行
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class Embed {

	private static final Logger LOG = LoggerFactory.getLogger(Embed.class);

	private Embed() {
	}

	/**
	 * 执行方法
	 * 
	 * @param methodName 方法名称
	 * @param methodDescriptor 方法完整描述(asm)
	 * @param args 方法参数 注意: 此处参数列表的成员,永远都是包装类型(已经验证)
	 * @param target 目标 Repository
	 * @return 执行之后的值
	 */
	public static Object excute(String methodName, String methodDescriptor, Object[] args, QueryRepository target) { // NO_UCD
		LOG.debug("methodName={}, methodDescriptor={}, args={}, target={}", methodName, methodDescriptor, args, target);
		Method method = TypeUtil.getMethod(target.getClass(), methodName, methodDescriptor);
		try {
			return method.invoke(target, args);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			LOG.error("调用db出错", e);
		}
		return null;
	}
}
