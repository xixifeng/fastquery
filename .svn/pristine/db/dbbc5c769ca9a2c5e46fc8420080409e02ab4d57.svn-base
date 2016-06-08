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
public class PageImpl<E> implements Page<E> {
	
	private int size;             // 每页行数
	private int number;           // 当前页码,从0开始
	private int numberOfElements; // 当前页的真实记录行数
	private List<E> content;      // 当前页的结果集

	private long totalElements;   // 总行数
	private int totalPages;       // 总页码
	private boolean hasContent;   // 是否有结果集

	private boolean isFirst;      // 是否是第一页
	private boolean isLast;       // 是否是最后一页

	private boolean hasNext;      // 是否有下一页
	private boolean hasPrevious;  // 是否有上一页

	private Slice nextPageable;      // 下一页的Pageable对象
	private Slice previousPageable;  // 上一页的Pageable对象

	public PageImpl(int size, int numberOfElements, int number, List<E> content,
			long totalElements, int totalPages, boolean hasContent, boolean hasNext,
			boolean hasPrevious, boolean isFirst, boolean isLast, Slice nextPageable,
			Slice previousPageable) {
		this.size = size;
		this.numberOfElements = numberOfElements;
		this.number = number;
		this.content = content;
		this.totalElements = totalElements;
		this.totalPages = totalPages;
		this.hasContent = hasContent;
		this.hasNext = hasNext;
		this.hasPrevious = hasPrevious;
		this.isFirst = isFirst;
		this.isLast = isLast;
		this.nextPageable = nextPageable;
		this.previousPageable = previousPageable;
	}

	@Override
	public int getSize() {
		return size;
	}

	@Override
	public int getNumberOfElements() {
		return numberOfElements;
	}

	@Override
	public int getNumber() {
		return number;
	}

	@Override
	public List<E> getContent() {
		return content;
	}

	@Override
	public long getTotalElements() {
		return totalElements;
	}

	@Override
	public int getTotalPages() {
		return totalPages;
	}

	@Override
	public boolean isHasContent() {
		return hasContent;
	}

	@Override
	public boolean isHasNext() {
		return hasNext;
	}

	@Override
	public boolean isHasPrevious() {
		return hasPrevious;
	}

	@Override
	public boolean isFirst() {
		return isFirst;
	}

	@Override
	public boolean isLast() {
		return isLast;
	}

	@Override
	public Slice getNextPageable() {
		return nextPageable;
	}

	@Override
	public Slice getPreviousPageable() {
		return previousPageable;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public void setNumberOfElements(int numberOfElements) {
		this.numberOfElements = numberOfElements;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public void setContent(List<E> content) {
		this.content = content;
	}

	public void setTotalElements(long totalElements) {
		this.totalElements = totalElements;
	}

	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}

	public void setHasContent(boolean hasContent) {
		this.hasContent = hasContent;
	}

	public void setHasNext(boolean hasNext) {
		this.hasNext = hasNext;
	}

	public void setHasPrevious(boolean hasPrevious) {
		this.hasPrevious = hasPrevious;
	}

	public void setFirst(boolean isFirst) {
		this.isFirst = isFirst;
	}

	public void setLast(boolean isLast) {
		this.isLast = isLast;
	}

	public void setNextPageable(Slice nextPageable) {
		this.nextPageable = nextPageable;
	}

	public void setPreviousPageable(Slice previousPageable) {
		this.previousPageable = previousPageable;
	}
}
