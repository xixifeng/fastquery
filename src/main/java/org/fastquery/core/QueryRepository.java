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

import java.math.BigInteger;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public interface QueryRepository extends Repository {
	/**
	 * 保存一个实体,然后将主键值返回(不适用于联合主键).注意:永不返回null,没有找到主键返回-1
	 * @param entity 实体bean
	 * @return
	 */
	@Id(MethodId.QUERY)
	BigInteger saveToId(Object entity);
	
	/**
	 * 保存一个实体,然后将主键值返回(不适用于联合主键).注意:永不返回null,没有找到主键返回-1
	 * @param entity 实体bean
	 * @param dataSourceName 数据源名称
	 * @return
	 */
	@Id(MethodId.QUERY)
	BigInteger saveToId(Object entity,@Source String dataSourceName);
	
	/**
	 * 保存一个实体,然后将主键值返回(不适用于联合主键).注意:永不返回null,没有找到主键返回-1
	 * @param dataSourceName 数据源名称
	 * @param dbName 数据库名称
	 * @param entity 实体bean
	 * @return
	 */
	@Id(MethodId.QUERY)
	BigInteger saveToId(@Source String dataSourceName,String dbName,Object entity);
	
	/**
	 * 保存一个实体,这个实体必须有一个自增长的主键 <br>
	 * 注意: 如果该实体没有主键,则返回null
	 * @param entity
	 * @return
	 */
	@Id(MethodId.QUERY0)
	<E> E  save(E entity);
	
	/**
	 * 往指定的数据源里保存一个实体<br>
	 * 注意: 如果该实体没有主键,则返回null
	 * @param entity
	 * @param dataSourceName
	 * @return
	 */
	@Id(MethodId.QUERY0)
	<E> E  save(E entity,@Source String dataSourceName);
	
	/**
	 * 往指定的数据源里保存一个实体,并且指定数据库名称<br>
	 * 注意: 如果该实体没有主键,则返回null
	 * @param dataSourceName
	 * @param dbName
	 * @param entity
	 * @return
	 */
	@Id(MethodId.QUERY0)
	<E> E  save(@Source String dataSourceName,String dbName,E entity);
		
	/**
	 * 更新实体,实体需要包含主键值 <br>
	 * 注意: 修改失败,或违规操作则返回null
	 * @param entity
	 * @return
	 */
	@Id(MethodId.QUERY1)
	<E> E update(E entity);
	
	/**
	 * 根据主键更新实体 <br>
	 * 注意: 修改失败,或违规操作则返回null
	 * @param dataSourceName
	 * @param entity
	 * @return
	 */
	@Id(MethodId.QUERY1)
	<E> E update(@Source String dataSourceName,E entity);

	/**
	 * 根据主键更新实体 <br>
	 * 注意: 修改失败,或违规操作则返回null
	 * @param dataSourceName
	 * @param dbName
	 * @param entity
	 * @return
	 */
	@Id(MethodId.QUERY1)
	<E> E update(@Source String dataSourceName,String dbName,E entity);
	
	
	@Id(MethodId.QUERY3)
	int update(Object entity,String where);
	@Id(MethodId.QUERY3)
	int update(@Source String dataSourceName,Object entity,String where);
	@Id(MethodId.QUERY3)
	int update(@Source String dataSourceName,String dbName,Object entity,String where);
	
	/**
	 * 保存或者更新实体,实体需要包含主键值否则报错 (如果不存在就存储,存在就更新)
	 * @param entity
	 * @return
	 */
	@Id(MethodId.QUERY2)
	<E> E saveOrUpdate(E entity);
	@Id(MethodId.QUERY2)
	<E> E saveOrUpdate(@Source String dataSourceName,E entity);
	@Id(MethodId.QUERY2)
	<E> E saveOrUpdate(@Source String dataSourceName,String dbName,E entity);
	
	/**
	 * 执行SQL文件
	 * @param sqlName 基准目录下的SQL文件名称 注意: 基准目录在fastquery.json里配置
	 * @param output 指定执行SQL后的输出将放在哪个文件里. 注意: 会在基准目录里寻找output
	 */
	@Id(MethodId.QUERY6)
	void executeBatch(String sqlName,String output);
	
	/**
	 * 执行SQL文件
	 * @param sqlName 基准目录下的SQL文件名称 注意: 基准目录在fastquery.json里配置
	 * @param output 指定执行SQL后的输出,放在哪个文件里. 注意: 会在基准目录里寻找output
	 * @param dataSourceName 数据源的名称
	 * @return 执行所影响的行数
	 */
	@Id(MethodId.QUERY6)
	void executeBatch(String sqlName,String output,@Source String dataSourceName);
	
	/**
	 * 根据id 删除表记录, 返回影响行数(每什么可删除的返回0),反之将抛出异常
	 * @param tableName
	 * @param id
	 * @return
	 */
	int delete(String tableName,int id);
	
	/**
	 * 根据id 删除表记录, 返回影响行数(每什么可删除的返回0),反之将抛出异常
	 * @param tableName
	 * @param id
	 * @return
	 */
	int delete(String tableName,int id,@Source String dataSourceName);
}










