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

package org.fastquery.where;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
@Repeatable(Conditions.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD,ElementType.ANNOTATION_TYPE })
public @interface Condition {
	
	String value();
	
	/**
	 * ignoreNull 为true: 表示该条件中的参数变量如果接受到的值是null,那么该行条件将不参与运算,反之,参与运算.
	 */
	boolean ignoreNull() default true;
	
	/**
	 * 指定哪些参数将参与决定是否忽略当前条件 <br>
	 * 
	 * 如: judge = {"?1","?2"},那么当 ?1(第一个参数) 或 ?2(第二个参数) 传递null时,当前条件将会被忽略.
	 * 
	 */
	String[] judges() default {};
}
