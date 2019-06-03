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

package org.fastquery.core;

/**
 * 事务的传播行为
 * 
 * @author mei.sir@aliyun.cn
 */
enum Propagation { // NO_UCD 

	/**
	 * PROPAGATION_REQUIRED：如果当前没有事务，就创建一个新事务，如果当前存在事务，就加入该事务，该设置是最常用的设置。
	 */
	REQUIRED,

	/**
	 * PROPAGATION_SUPPORTS：支持当前事务，如果当前存在事务，就加入该事务，如果当前不存在事务，就以非事务执行。
	 */
	SUPPORTS,

	/**
	 * PROPAGATION_MANDATORY：支持当前事务，如果当前存在事务，就加入该事务，如果当前不存在事务，就抛出异常。
	 */
	MANDATORY,

	/**
	 * PROPAGATION_REQUIRES_NEW：创建新事务，无论当前存不存在事务，都创建新事务。
	 */
	REQUIRES_NEW,

	/**
	 * PROPAGATION_NOT_SUPPORTED：以非事务方式执行操作，如果当前存在事务，就把当前事务挂起。
	 */
	NOT_SUPPORTED,

	/**
	 * PROPAGATION_NEVER：以非事务方式执行，如果当前存在事务，则抛出异常。
	 */
	NEVER,

	/**
	 * PROPAGATION_NESTED：如果当前存在事务，则在嵌套事务内执行。如果当前没有事务， 则执行与PROPAGATION_REQUIRED类似的操作。
	 */
	NESTED
}
