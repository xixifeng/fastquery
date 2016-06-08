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

package org.fastquery.filter.generate.common;

import java.lang.reflect.Method;

import org.fastquery.core.RepositoryException;

/**
 * Method 检测过滤器
 * @author xixifeng (fastquery@126.com)
 */
@FunctionalInterface
public interface MethodFilter {

	/**
	 * 过滤
	 * @param method 待检测的Method
	 * @return 检测后的Method,允许中途修改它
	 */
	Method doFilter(Method method);
	
	/**
	 * 终止(扯断链条)
	 * @param method 当前方法
	 * @param errorMsg 终止理由
	 */
	default void abortWith(Method method,String msg){
		throw new RepositoryException(String.format("%s->: %s ", method.toString(),msg));
	}
	
}
