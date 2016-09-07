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

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public enum Operator {
	
	/**
	 * = 相等 EQ 是 equal的缩写
	 */
	EQ("="),
	
	/**
	 * < LT 是 less than的缩写
	 */
	LT("<"),
	
	/**
	 * > GT 是 greater than的缩写
	 */
	GT(">"),
	
	/**
	 * <> 不等与 NE 是 not equal的缩写 
	 */
	NE("<>"),
	
	/**
	 * != 不等与
	 */
	NEQ("!="),
	
	/**
	 * !>（不大于）not greater than
	 */
	NGT("!>"),
	
	/**
	 * !<（不小于） not less than
	 */
	NLT("!<"),
	
	/**
	 * >=（大于等于） greater or equal
	 */
	GE(">="),
	
	/**
	 * <=（小于等于） less or equal
	 */
	LE("<="),
	
	/**
	 * LIKE 像
	 */
	LIKE("LIKE"),
	
	/**
	 * IN
	 */
	IN("IN"),
	
	/**
	 * NOT
	 */
	NOT("NOT"),
	
	/**
	 * IS
	 */
	IS("IS"),
	
	/**
	 * NULL
	 */
	NULL("NULL"),
	
	/**
	 * BETWEEN
	 */
	BETWEEN("BETWEEN"),
	/**
	 * SOME
	 */
	SOME("SOME"),
	
	NONE("");
	
	// http://www.cnblogs.com/libingql/p/4097460.html
	// http://www.jb51.net/article/57342.htm
	// http://www.w3school.com.cn/sql/sql_in.asp
	
	private String name;
	private Operator(String name) {
		this.name = name;
	}
	
	public String getVal() {
		return name;
	}
}
