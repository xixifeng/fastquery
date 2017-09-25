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

package org.fastquery.core;

import java.math.BigInteger;
import java.util.Collection;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public interface QueryRepository extends Repository {
	/**
	 * 保存一个实体,然后将主键值返回(不适用于联合主键).注意:永不返回null,没有找到主键返回-1
	 * @param entity 实体bean
	 * @return 主键
	 */
	@Id(MethodId.QUERY)
	BigInteger saveToId(Object entity);
	
	/**
	 * 保存一个实体,然后将主键值返回(不适用于联合主键).注意:永不返回null,没有找到主键返回-1
	 * @param entity 实体bean
	 * @param dataSourceName 数据源名称
	 * @return 主键
	 */
	@Id(MethodId.QUERY)
	BigInteger saveToId(Object entity,@Source String dataSourceName);
	
	/**
	 * 保存一个实体,然后将主键值返回(不适用于联合主键).注意:永不返回null,没有找到主键返回-1
	 * @param dataSourceName 数据源名称
	 * @param dbName 数据库名称
	 * @param entity 实体bean
	 * @return 主键
	 */
	@Id(MethodId.QUERY)
	BigInteger saveToId(@Source String dataSourceName,String dbName,Object entity);
	
	/**
	 * 保存实体集合
	 * @param <B> 实体
	 * @param ignoreRepeat 忽略重复主键
	 * @param entities 实体集合
	 * @return 影响行数
	 */
	@Id(MethodId.QUERY4)
	<B> int save(boolean ignoreRepeat,Collection<B> entities);
	/**
	 * 保存实体集合
	 * @param ignoreRepeat 忽略重复主键
	 * @param entities 实体集合
	 * @return 影响行数
	 */
	@Id(MethodId.QUERY4)
	int saveArray(boolean ignoreRepeat,Object...entities);
	/**
	 * 保存实体集合
	 * @param <B> 实体
	 * @param ignoreRepeat 忽略重复主键
	 * @param dataSourceName 数据源名称
	 * @param entities 实体集合
	 * @return 影响行数
	 */
	@Id(MethodId.QUERY4)
	<B> int save(boolean ignoreRepeat,@Source String dataSourceName,Collection<B> entities);
	/**
	 * 保存实体集合
	 * @param ignoreRepeat 忽略重复主键
	 * @param dataSourceName 数据源名称
	 * @param entities 实体集合
	 * @return 影响行数
	 */
	@Id(MethodId.QUERY4)
	int saveArray(boolean ignoreRepeat,@Source String dataSourceName,Object...entities);
	/**
	 * 保存实体集合
	 * @param <B> 实体
	 * @param dataSourceName 数据源名称
	 * @param dbName 数据库名称
	 * @param entities 实体集合
	 * @param ignoreRepeat 忽略重复主键
	 * @return 影响行数
	 */
	@Id(MethodId.QUERY4)
	<B> int save(boolean ignoreRepeat,@Source String dataSourceName,String dbName,Collection<B> entities);
	/**
	 * 保存实体集合
	 * @param ignoreRepeat 忽略重复主键
	 * @param dataSourceName 数据源名称
	 * @param dbName 数据库名称
	 * @param entities 实体集合
	 * @return 影响行数
	 */
	@Id(MethodId.QUERY4)
	int saveArray(boolean ignoreRepeat,@Source String dataSourceName,String dbName,Object...entities);
	
	/**
	 * 保存一个实体,这个实体必须有一个自增长的主键 <br>
	 * 注意: 如果该实体没有主键,则返回null
	 * @param <E> 实体
	 * @param entity 实体
	 * @return 实体
	 */
	@Id(MethodId.QUERY0)
	<E> E  save(E entity);
	
	/**
	 * 往指定的数据源里保存一个实体<br>
	 * 注意: 如果该实体没有主键,则返回null
	 * @param <E> 存储的实例
	 * @param entity 实体
	 * @param dataSourceName 数据源名称
	 * @return 存储的实例
	 */
	@Id(MethodId.QUERY0)
	<E> E  save(E entity,@Source String dataSourceName);
	
	/**
	 * 往指定的数据源里保存一个实体,并且指定数据库名称<br>
	 * 注意: 如果该实体没有主键,则返回null
	 * @param <E> 存储的实例
	 * @param dataSourceName 数据源名称
	 * @param dbName 数据库名称
	 * @param entity 实体
	 * @return 存储的实例
	 */
	@Id(MethodId.QUERY0)
	<E> E  save(@Source String dataSourceName,String dbName,E entity);
		
	/**
	 * 更新实体,实体需要包含主键值 <br>
	 * 注意: 修改失败,或违规操作则返回null
	 * @param <E> 存储的实例
	 * @param entity 实体
	 * @return 存储的实例
	 */
	@Id(MethodId.QUERY1)
	<E> E update(E entity);
	
	/**
	 * 根据主键更新实体 <br>
	 * 注意: 修改失败,或违规操作则返回null
	 * @param <E> 更新的实例
	 * @param dataSourceName 数据源名称
	 * @param entity 实体
	 * @return 更新的实例
	 */
	@Id(MethodId.QUERY1)
	<E> E update(@Source String dataSourceName,E entity);

	/**
	 * 根据主键更新实体 <br>
	 * 注意: 修改失败,或违规操作则返回null
	 * @param <E> 更新的实例
	 * @param dataSourceName 数据源名称
	 * @param dbName 数据库名称
	 * @param entity 实体
	 * @return 更新的实例
	 */
	@Id(MethodId.QUERY1)
	<E> E update(@Source String dataSourceName,String dbName,E entity);
	
	/**
	 * 更新实体
	 * @param entity 实体
	 * @param where 自定义条件
	 * @return 影响行数
	 */
	@Id(MethodId.QUERY3)
	int update(Object entity,String where);
	
	/**
	 * 更新实体
	 * @param dataSourceName 数据源名称
	 * @param entity 实体
	 * @param where 自定义条件
	 * @return 影响行数
	 */
	@Id(MethodId.QUERY3)
	int update(@Source String dataSourceName,Object entity,String where);
	
	/**
	 * 更新实体
	 * @param dataSourceName 数据源名称
	 * @param dbName 数据库名称
	 * @param entity 实体
	 * @param where 自定义条件
	 * @return 影响行数
	 */
	@Id(MethodId.QUERY3)
	int update(@Source String dataSourceName,String dbName,Object entity,String where);
	
	/**
	 * 保存或者更新实体,实体需要包含主键值否则报错 (如果不存在就存储,存在就更新)
	 * @param <E> 实体
	 * @param entity 实体
	 * @return 实体
	 */
	@Id(MethodId.QUERY2)
	<E> E saveOrUpdate(E entity);
	
	/**
	 * 保存或者更新实体,实体需要包含主键值否则报错 (如果不存在就存储,存在就更新)
	 * @param <E> 实体
	 * @param dataSourceName 数据源名称
	 * @param entity 实体
	 * @return 实体
	 */
	@Id(MethodId.QUERY2)
	<E> E saveOrUpdate(@Source String dataSourceName,E entity);
	
	/**
	 * 保存或者更新实体,实体需要包含主键值否则报错 (如果不存在就存储,存在就更新)
	 * @param <E> 实体
	 * @param dataSourceName 数据源名称
	 * @param dbName 数据库名称
	 * @param entity 实体
	 * @return 实体
	 */
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
	 */
	@Id(MethodId.QUERY6)
	void executeBatch(String sqlName,String output,@Source String dataSourceName);
}










