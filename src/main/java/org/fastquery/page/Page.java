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

package org.fastquery.page;

import java.util.List;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public interface Page<E> {

	/**
	 * 每页行数
	 * 
	 * @return 每页行数
	 */
	public int getSize();

	/**
	 * 当前页的真实记录行数
	 * 
	 * @return 当前页的真实记录行数
	 */
	public int getNumberOfElements();

	/**
	 * 当前页码,从0开始
	 * 
	 * @return 当前页码,从0开始
	 */
	public int getNumber();

	/**
	 * 当前页的结果集
	 * 
	 * @return 当前页的结果集
	 */
	public List<E> getContent();

	/**
	 * 总行数,注意:如果在不求和的情况下,返回-1.
	 * 
	 * @return 总行数
	 */
	public long getTotalElements();

	/**
	 * 总页码,注意:如果在不求和的情况下,返回-1.<br>
	 * 不求总行数,总页数没有办法推算出来.
	 * 
	 * @return 总页数
	 */
	public int getTotalPages();

	/**
	 * 是否有结果集
	 * 
	 * @return 是否有结果集
	 */
	public boolean isHasContent();

	/**
	 * 是否有下一页
	 * 
	 * @return 是否有下一页
	 */
	public boolean isHasNext();

	/**
	 * 是否有上一页
	 * 
	 * @return 是否有上一页
	 */
	public boolean isHasPrevious();

	/**
	 * 是否是第一页
	 * 
	 * @return 是否是第一页
	 */
	public boolean isFirst();

	/**
	 * 是否是最后一页
	 * 
	 * @return 是否是最后一页
	 */
	public boolean isLast();

	/**
	 * 下一页的 {@link TenetPageable}
	 * 
	 * @return 下一页的 {@link TenetPageable}
	 */
	public Slice getNextPageable();

	/**
	 * 上一页的 {@link TenetPageable}
	 * 
	 * @return 上一页的 {@link TenetPageable}
	 */
	public Slice getPreviousPageable();
}
