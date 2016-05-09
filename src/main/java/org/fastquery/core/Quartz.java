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

package org.fastquery.core;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * 
 * @author xixifeng (fastquery@126.com)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Documented
public @interface Quartz {
	
	/**
	 * 目标作业
	 * @return
	 */
	Class<? extends QuartzJob> value() default QuartzJobDefaultImpl.class;
	
	/**
	 *  计划任务, 默认值 0/3 * * * * ? 表示每隔3秒执行一次
	 * @return
	 */
	String cronExpression() default "0/3 * * * * ?";
	
	/**
	 * 调度开始时间(用时间戳来表达,精确至毫秒) 默认是-1, 表示按默认的开始时间
	 * @return
	 */
	long startAt() default -1;
	
	/**
	 * 循环次数. 默认是-1, 表示重复次数无限制,无限循环下去.
	 * @return
	 */
	int withRepeatCount() default -1;

	/**
	 * 激活状态
	 * @return
	 */
	QuartzStatus.Active status() default QuartzStatus.Active.YES;
	
	/**
	 * 作业是否并发进行
	 * @return
	 */
	boolean concurrent() default false;
}
