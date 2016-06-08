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

package org.fastquery.filter.generate.quartza;

import java.lang.reflect.Method;

import org.fastquery.core.Modifying;
import org.fastquery.core.Query;
import org.fastquery.filter.generate.common.MethodFilter;

/**
 * 在QuartzRepository中,检测非法注解, 不允许出现{@link Query} 和 {@link Modifying}
 * @author xixifeng (fastquery@126.com)
 */
public class IllegalAnnotation implements MethodFilter {

	@Override
	public Method doFilter(Method method) {
		if(method.getAnnotationsByType(Query.class).length > 0) {
			this.abortWith(method, "QuartzRepository中禁止出现@Query!");
		}
		
		if(method.getAnnotation(Modifying.class) !=null ) {
			this.abortWith(method, "QuartzRepository中禁止出现@Modifying!");
		}
		
		return method;
	}

}
