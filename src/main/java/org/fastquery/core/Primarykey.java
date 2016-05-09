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
 * 主键对象
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class Primarykey {

	private long autoIncKey;
	private String pkey;
	
	/**
	 * 主键有2中可能 数字类型(int Integer long Long)可视为long, 另一种是字符串类型
	 * @param autoIncKey 若为-1表示不存在
	 * @param pkey 若为 null 表示不存在
	 */
	public Primarykey(long autoIncKey, String pkey) {
		this.autoIncKey = autoIncKey;
		this.pkey = pkey;
	}

	/*
	// 有待扩展
	// 比如插入一条数据库后返回其primary key
	// 主键目前只支持int,long,Integer,Long 着四种类型
	public <T> T getPrimarykey(Class<T> keyType) {
		if ((keyType != int.class) && (keyType != long.class) && (keyType != Integer.class)
				&& (keyType != Long.class)) {
			throw new RuntimeException("类型不允许!");
		}
		T val = (T) keys.get(0).get("GENERATED_KEY");	
		return val;
	}
	*/
	
	/**
	 * 获取主键
	 * @return
	 */
	public long getPrimarykey() {
		return autoIncKey;
	}
	
	/**
	 * 获取主键
	 * @return
	 */
	public String getSpecifyPrimarykey() {
		return pkey;
	}
	
}
