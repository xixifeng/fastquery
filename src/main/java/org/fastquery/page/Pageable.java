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

package org.fastquery.page;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public interface Pageable {

	/**
	 * 或取当前页索引(从1开始计数)
	 * @return 当前页索引
	 */
	int getPageIndex();


	/**
	 * 或取当前页数据条数
	 * @return 记录数
	 */
	int getPageSize();

	/**
	 * 翻页的偏移量
	 * 
	 * @return 偏移量
	 */
	int getOffset();
}
