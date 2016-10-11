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
public interface QueryRepository extends Repository{
	
	/**
	 * 执行SQL文件
	 * @param sqlName 基准目录下的SQL文件名称 注意: 基准目录在fastquery.json里配置
	 * @param output 指定执行SQL后的输出,放在哪个文件里. 注意: 会在基准目录里寻找output
	 * @param dataSourceName 数据源的名称
	 * @return 执行所影响的行数
	 */
	@Id(MethodId.QUERY3)
	void executeBatch(String sqlName,String output,@Source String dataSourceName);
	/**
	 * 执行SQL文件
	 * @param sqlName 基准目录下的SQL文件名称 注意: 基准目录在fastquery.json里配置
	 * @param output 指定执行SQL后的输出将放在哪个文件里. 注意: 会在基准目录里寻找output
	 */
	@Id(MethodId.QUERY3)
	void executeBatch(String sqlName,String output);
	
}










