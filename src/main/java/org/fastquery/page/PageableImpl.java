/*
 * Copyright (c) 2016-2017, fastquery.org and/or its affiliates. All rights reserved.
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
public class PageableImpl implements Pageable {

	// 这个给默认值是没有意义的, 当前访问的是第几页和每页显示多少条数据,应该让客户端决定.
	private int page; 
	private int size;
	
	public PageableImpl(int page, int size) {
		
		this.page = page;
		this.size = size;
		
		if (page < 1) {
			//throw new IllegalArgumentException("页码索引不能小于1 !")
			this.page = 1;
		}

		if (size < 1) {
			//throw new IllegalArgumentException("Page size 不能小于1 !")
			this.size = 1;
		}
	}

	@Override
	public int getPageNumber() {
		return page;
	}

	@Override
	public int getPageSize() {
		return size;
	}

	@Override
	public int getOffset() {
		return page * size - size;
	}

}
