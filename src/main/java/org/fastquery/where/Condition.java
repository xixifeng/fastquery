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
@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
public @interface Condition {

	/**
	 * 条件
	 * 
	 * @return String
	 */
	String value();

	/**
	 * 默认:允许传递所有的值,都不会使当前条件忽略. <br>
	 * 当前条件所包含的变量值跟allowRule所指定的正则表达式逐个进行匹配,如果其中一个能匹配上就追加这个条件. <br>
	 * 注意: 先 allowRule(允许) 后 ignoreRule(忽略)
	 * 
	 * @return String
	 */
	String[] allowRule() default {};

	/**
	 * 当前条件所包含的变量值跟ignoreRule所指定的正则表达式逐个进行匹配,如果其中一个能匹配上就忽略该条件
	 * 
	 * @return String
	 */
	String[] ignoreRule() default {};

	/**
	 * ignoreNull 为true: 表示该条件中的参数变量如果接受到的值是null,那么该行条件将不参与运算,反之,参与运算(默认:true).
	 * 
	 * @return boolean
	 */
	boolean ignoreNull() default true;

	/**
	 * ignoreEmpty 为true: 表示该条件中的参数变量如果接受到的值是""(空字符串),那么该行条件将不参与运算,反之,参与运算(默认:true).
	 * 
	 * @return boolean
	 */
	boolean ignoreEmpty() default true;
	
	/**
	 * 通过指定一个Judge子类来决定是否忽略该条件
	 * @return 继承于Judge类的Class
	 */
	Class<? extends Judge> ignore() default DefaultJudge.class;
	
	/**
	 * 通过运行java代码脚本,来决定是否保留条件项.脚本运行后的结果如果是true,那么就忽略该条件项,反之,保留条件项,默认脚本是"false",表示保留该条件项. <br>
	 *  注意: 脚本执行后得到的结果必须是布尔类型,否则,项目都启动不起来.  
	 *  
	 * @return 代码脚本
	 */
	String script() default "false";
}
