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

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public interface QuartzRepository extends Repository{
	
	/**
	 * 增加一个作业
	 * @param status 作业增加时的状态
	 * @param quartzJob 具体的作业
	 * @return 作业jobKey
	 */
	 String addJobDetai(QuartzStatus status,QuartzJob...quartzJob);
	 
	 
	 /**
	  * 动态增加一个作业
	  * @param status
	  * @param quartzJob quartzJob 字符串格式的作业源代码,在服务端会自动编译装载进去.
	  */
	 void addJobDetai(QuartzStatus status,String...quartzJob);
	 
	 /**
	  * 更改指定作业的状态
	  * @param jobKey
	  * @param status
	  */
	 void update(QuartzStatus status,String...jobKey);
	 
	 /**
	  * 获取所有的作业
	  */
	 void findAll();
	 
	 
	 /**
	  * 删除所有作业
	  */
	 void deleteAll();
}
