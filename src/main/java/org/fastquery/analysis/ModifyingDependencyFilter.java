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

import org.fastquery.core.Modifying;
import org.fastquery.core.Query;
import org.fastquery.core.QueryByNamed;

/**
 * 在QueryRepository中 {@link Modifying} 依赖检测 <br>
 * Modifying 要么跟 Query 组合, 要么跟QueryByNamed组合 不能独存
 * 
 * @author xixifeng (fastquery@126.com)
 */
class ModifyingDependencyFilter implements MethodFilter {

	@Override
	public void doFilter(Method method) {
		Modifying m = method.getAnnotation(Modifying.class);
		int queryLen = method.getAnnotationsByType(Query.class).length;
		QueryByNamed queryByNamed = method.getAnnotation(QueryByNamed.class);
		if (m != null && queryLen == 0 && queryByNamed == null) { // m存在 并且 queryLen为0 并且
																	// queryByNamed不存在
			this.abortWith(method, "@Modifying 要么跟 @Query 组合, 要么跟@QueryByNamed组合不能独存!");
		}
	}

}
