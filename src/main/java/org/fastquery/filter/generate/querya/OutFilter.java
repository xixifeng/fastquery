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

import org.fastquery.core.Query;
import org.fastquery.core.QueryByNamed;
import org.fastquery.filter.generate.common.MethodFilter;

/**
 * 注解排斥校验
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class OutFilter implements MethodFilter {

	@Override
	public Method doFilter(Method method) {
		
		//1). 不能同时标识@Query,@QueryByNamed
		if(method.getAnnotationsByType(Query.class).length>0 && method.getAnnotation(QueryByNamed.class)!=null){
			this.abortWith(method, "该方法不能同时标识@Query和@QueryByNamed");
		}
		
		return method;
	}

}
