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
	/**
	 * 上一个条件跟这个条件的关系逻辑运算符. 例如: where name=? and age >10, 那么 "and" 称为是条件"name=?"与条件"age >10"的关系逻辑运算符
	 */
	COperator c() default COperator.NONE;
	/**
	 * 左边
	 */
	String l();
	
	/**
	 * 表达式
	 */
	Operator[] o() default Operator.EQ;
	
	/**
	 * 右边
	 */
	String r();
	
	/**
	 * ignoreNull 为true: 表示该条件的{@link #r()}值如果接受到了null值,那么该条件就不参与运算,反之,参与运算.
	 */
	boolean ignoreNull() default true;
}
