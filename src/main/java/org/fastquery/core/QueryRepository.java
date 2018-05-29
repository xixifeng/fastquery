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

import java.math.BigInteger;
import java.util.Collection;

import org.fastquery.util.BeanUtil;

/**
 * 查询仓库
 * 
 * @author xixifeng (fastquery@126.com)
 */
public interface QueryRepository extends Repository { // NO_UCD (test only)
	/**
	 * 保存一个实体,然后将主键值返回(不适用于联合主键).注意:永不返回null,没有找到主键返回-1
	 * 
	 * @param entity 实体bean
	 * @return 主键
	 */
	@Id(MethodId.QUERY)
	BigInteger saveToId(Object entity);

	/**
	 * 保存一个实体,然后将主键值返回(不适用于联合主键).注意:永不返回null,没有找到主键返回-1
	 * 
	 * @param entity 实体bean
	 * @param dataSourceName 数据源名称
	 * @return 主键
	 */
	@Id(MethodId.QUERY)
	BigInteger saveToId(Object entity, @Source String dataSourceName);

	/**
	 * 保存一个实体,然后将主键值返回(不适用于联合主键).注意:永不返回null,没有找到主键返回-1
	 * 
	 * @param dataSourceName 数据源名称
	 * @param dbName 数据库名称
	 * @param entity 实体bean
	 * @return 主键
	 */
	@Id(MethodId.QUERY)
	BigInteger saveToId(@Source String dataSourceName, String dbName, Object entity);

	/**
	 * 保存实体集合
	 * 
	 * @param <B> 实体
	 * @param ignoreRepeat 忽略重复主键
	 * @param entities 实体集合
	 * @return 影响行数
	 */
	@Id(MethodId.QUERY4)
	<B> int save(boolean ignoreRepeat, Collection<B> entities);

	/**
	 * 保存可变数组实体
	 * 
	 * @param ignoreRepeat 忽略重复主键
	 * @param entities 实体集合
	 * @return 影响行数
	 */
	@Id(MethodId.QUERY4)
	int saveArray(boolean ignoreRepeat, Object... entities);

	/**
	 * 保存实体集合
	 * 
	 * @param <B> 实体
	 * @param ignoreRepeat 忽略重复主键
	 * @param dataSourceName 数据源名称
	 * @param entities 实体集合
	 * @return 影响行数
	 */
	@Id(MethodId.QUERY4)
	<B> int save(boolean ignoreRepeat, @Source String dataSourceName, Collection<B> entities);

	/**
	 * 保存可变数组实体
	 * 
	 * @param ignoreRepeat 忽略重复主键
	 * @param dataSourceName 数据源名称
	 * @param entities 实体集合
	 * @return 影响行数
	 */
	@Id(MethodId.QUERY4)
	int saveArray(boolean ignoreRepeat, @Source String dataSourceName, Object... entities);

	/**
	 * 保存实体集合
	 * 
	 * @param <B> 实体
	 * @param dataSourceName 数据源名称
	 * @param dbName 数据库名称
	 * @param entities 实体集合
	 * @param ignoreRepeat 忽略重复主键
	 * @return 影响行数
	 */
	@Id(MethodId.QUERY4)
	<B> int save(boolean ignoreRepeat, @Source String dataSourceName, String dbName, Collection<B> entities);

	/**
	 * 保存可变数组实体
	 * 
	 * @param ignoreRepeat 忽略重复主键
	 * @param dataSourceName 数据源名称
	 * @param dbName 数据库名称
	 * @param entities 实体集合
	 * @return 影响行数
	 */
	@Id(MethodId.QUERY4)
	int saveArray(boolean ignoreRepeat, @Source String dataSourceName, String dbName, Object... entities);

	/**
	 * 插入一个实体 <br>
	 * 
	 * @param <E> 实体
	 * @param entity 实体
	 * @return 影响行数
	 */
	@Id(MethodId.QUERY0)
	<E> int insert(E entity);

	/**
	 * 往指定的数据源里插入一个实体<br>
	 * 
	 * @param <E> 存储的实例
	 * @param entity 实体
	 * @param dataSourceName 数据源名称
	 * @return 影响行数
	 */
	@Id(MethodId.QUERY0)
	<E> int insert(E entity, @Source String dataSourceName);

	/**
	 * 往指定的数据源里插入一个实体,并且指定数据库名称<br>
	 * 
	 * @param <E> 存储的实例
	 * @param dataSourceName 数据源名称
	 * @param dbName 数据库名称
	 * @param entity 实体
	 * @return 影响行数
	 */
	@Id(MethodId.QUERY0)
	<E> int insert(@Source String dataSourceName, String dbName, E entity);

	/**
	 * 更新实体,根据实体的主键值更新 <br>
	 * 
	 * @param <E> 存储的实例
	 * @param entity 实体
	 * @return 影响行数
	 */
	@Id(MethodId.QUERY1)
	<E> int executeUpdate(E entity);

	/**
	 * 更新实体,根据实体的主键值更新 <br>
	 * 
	 * @param <E> 更新的实例
	 * @param dataSourceName 数据源名称
	 * @param entity 实体
	 * @return 影响行数
	 */
	@Id(MethodId.QUERY1)
	<E> int executeUpdate(@Source String dataSourceName, E entity);

	/**
	 * 更新实体,根据实体的主键值更新 <br>
	 * 
	 * @param <E> 更新的实例
	 * @param dataSourceName 数据源名称
	 * @param dbName 数据库名称
	 * @param entity 实体
	 * @return 影响行数
	 */
	@Id(MethodId.QUERY1)
	<E> int executeUpdate(@Source String dataSourceName, String dbName, E entity);

	/**
	 * 更新实体
	 * 
	 * @param entity 实体
	 * @param where 自定义条件,若传递null或"",默认将主健作为条件进行修改
	 * @return 影响行数
	 */
	@Id(MethodId.QUERY3)
	int update(Object entity, String where);

	/**
	 * 更新实体
	 * 
	 * @param dataSourceName 数据源名称
	 * @param entity 实体
	 * @param where 自定义条件
	 * @return 影响行数
	 */
	@Id(MethodId.QUERY3)
	int update(@Source String dataSourceName, Object entity, String where);

	/**
	 * 更新实体
	 * 
	 * @param dataSourceName 数据源名称
	 * @param dbName 数据库名称
	 * @param entity 实体
	 * @param where 自定义条件
	 * @return 影响行数
	 */
	@Id(MethodId.QUERY3)
	int update(@Source String dataSourceName, String dbName, Object entity, String where);

	/**
	 * 保存或者更新实体,实体需要包含主键值否则报错 (如果不存在就存储,存在就更新)
	 * 
	 * @param <E> 实体
	 * @param entity 实体
	 * @return 影响行数
	 */
	@Id(MethodId.QUERY2)
	<E> int executeSaveOrUpdate(E entity);

	/**
	 * 保存或者更新实体,实体需要包含主键值否则报错 (如果不存在就存储,存在就更新)
	 * 
	 * @param <E> 实体
	 * @param dataSourceName 数据源名称
	 * @param entity 实体
	 * @return 影响行数
	 */
	@Id(MethodId.QUERY2)
	<E> int executeSaveOrUpdate(@Source String dataSourceName, E entity);

	/**
	 * 保存或者更新实体,实体需要包含主键值否则报错 (如果不存在就存储,存在就更新)
	 * 
	 * @param <E> 实体
	 * @param dataSourceName 数据源名称
	 * @param dbName 数据库名称
	 * @param entity 实体
	 * @return 影响行数
	 */
	@Id(MethodId.QUERY2)
	<E> int executeSaveOrUpdate(@Source String dataSourceName, String dbName, E entity);

	/**
	 * 批量更新实体
	 * 
	 * @param <E> 实体
	 * @param entities 实体集合
	 * @return 影响行数
	 */
	@Id(MethodId.QUERY5)
	<E> int update(Collection<E> entities);

	/**
	 * 批量更新实体
	 * 
	 * @param <E> 实体
	 * @param dataSourceName 数据源名称
	 * @param entities 实体集合
	 * @return 影响行数
	 */
	@Id(MethodId.QUERY5)
	<E> int update(@Source String dataSourceName, Collection<E> entities);

	/**
	 * 批量更新实体
	 * 
	 * @param <E> 实体
	 * @param dataSourceName 数据源名称
	 * @param dbName 数据库名称
	 * @param entities 实体集合
	 * @return 影响行数
	 */
	@Id(MethodId.QUERY5)
	<E> int update(@Source String dataSourceName, String dbName, Collection<E> entities);

	/**
	 * 执行SQL文件,注意: 只支持单行注释 "#...","-- ..."
	 * 
	 * @param sqlName 基准目录下的SQL文件名称 注意: 基准目录在fastquery.json里配置.当然,sqlName为绝对路径也可以.若传递null,什么也不做
	 * @return 数组中的每个数对应一条SQL语句执行后所影响的行数,有些数据库驱动没有实现该功能，成功提交成功后不能返回影响行数,而是返回-2。在JDBC的规范中Statement.SUCCESS_NO_INFO(-2)代表:执行成功,受影响行数不确定.
	 */
	@Id(MethodId.QUERY6)
	int[] executeBatch(String sqlName);

	/**
	 * 执行SQL文件,注意: 只支持单行注释 "#...","-- ..."
	 * 
	 * @param sqlName 基准目录下的SQL文件名称 注意: 基准目录在fastquery.json里配置.当然,sqlName为绝对路径也可以.若传递null,什么也不做
	 * @param dataSourceName 数据源的名称
	 * @return 数组中的每个数对应一条SQL语句执行后所影响的行数,有些数据库驱动没有实现该功能，成功提交成功后不能返回影响行数,而是返回-2。在JDBC的规范中Statement.SUCCESS_NO_INFO(-2)代表:执行成功,受影响行数不确定.
	 */
	@Id(MethodId.QUERY6)
	int[] executeBatch(String sqlName, @Source String dataSourceName);

	/**
	 * 根据主键查询实体
	 * 
	 * @param <E> 实体
	 * @param entityClass 实体的class
	 * @param id 主键值
	 * @return 返回实体
	 */
	@Id(MethodId.QUERY7)
	<E> E find(Class<E> entityClass, long id);

	/**
	 * 根据主键查询实体
	 * 
	 * @param <E> 实体
	 * @param entityClass 实体的class
	 * @param id 主键值
	 * @param dataSourceName 数据源名称
	 * @return 返回实体
	 */
	@Id(MethodId.QUERY7)
	<E> E find(Class<E> entityClass, long id, @Source String dataSourceName);

	/**
	 * 根据主键查询实体
	 * 
	 * @param <E> 实体
	 * @param entityClass 实体的class
	 * @param id 主键值
	 * @param dataSourceName 数据源名称
	 * @param dbName 数据库名称
	 * @return 返回实体
	 */
	@Id(MethodId.QUERY7)
	<E> E find(Class<E> entityClass, long id, @Source String dataSourceName, String dbName);

	/**
	 * 根据主键删除实体
	 * 
	 * @param tableName 表名称
	 * @param primaryKeyName 主键名称
	 * @param id 主键值
	 * @return 影响行数
	 */
	@Id(MethodId.QUERY8)
	int delete(String tableName, String primaryKeyName, long id);

	/**
	 * 根据主键删除实体
	 * 
	 * @param tableName 表名称
	 * @param primaryKeyName 主键名称
	 * @param id 主键值
	 * @param dataSourceName 数据源名称
	 * @return 影响行数
	 */
	@Id(MethodId.QUERY8)
	int delete(String tableName, String primaryKeyName, long id, @Source String dataSourceName);

	/**
	 * 根据主键删除实体
	 * 
	 * @param tableName 表名称
	 * @param primaryKeyName 主键名称
	 * @param id 主键值
	 * @param dataSourceName 数据源名称
	 * @param dbName 数据库名称
	 * @return 影响行数
	 */
	@Id(MethodId.QUERY8)
	int delete(String tableName, String primaryKeyName, long id, @Source String dataSourceName, String dbName);

	/**
	 * 保存一个实体,返回实体
	 * 
	 * @param entity 实体
	 * @return 返回实体
	 */
	@SuppressWarnings("unchecked")
	default <E> E save(E entity) {
		long id = saveToId(entity).longValue();
		return (E) find(entity.getClass(), id);
	}

	/**
	 * 保存一个实体,返回实体
	 * 
	 * @param entity 实体
	 * @param dataSourceName 数据源名称
	 * @return 返回实体
	 */
	@SuppressWarnings("unchecked")
	default <E> E save(E entity, String dataSourceName) {
		long id = saveToId(entity, dataSourceName).longValue();
		return (E) find(entity.getClass(), id, dataSourceName);
	}

	/**
	 * 保存一个实体,返回实体
	 * 
	 * @param dataSourceName 数据源名称
	 * @param dbName 数据库名称
	 * @param entity 实体
	 * @return 返回实体
	 */
	@SuppressWarnings("unchecked")
	default <E> E save(String dataSourceName, String dbName, E entity) {
		long id = saveToId(dataSourceName, dbName, entity).longValue();
		return (E) find(entity.getClass(), id, dataSourceName, dbName);
	}

	/**
	 * 更新一个实体,返回实体
	 * 
	 * @param entity 实体
	 * @return 返回实体
	 */
	@SuppressWarnings("unchecked")
	default <E> E update(E entity) {
		long effect = executeUpdate(entity);
		if (effect != 1) {
			throw new RepositoryException("修改失败了");
		}
		long id = BeanUtil.toId(entity);
		return (E) find(entity.getClass(), id);
	}

	/**
	 * 更新一个实体,返回实体
	 * 
	 * @param dataSourceName 数据源名称
	 * @param entity 实体
	 * @return 返回实体
	 */
	@SuppressWarnings("unchecked")
	default <E> E update(String dataSourceName, E entity) {
		long effect = executeUpdate(dataSourceName, entity);
		if (effect != 1) {
			throw new RepositoryException("修改失败了");
		}
		long id = BeanUtil.toId(entity);
		return (E) find(entity.getClass(), id, dataSourceName);
	}

	/**
	 * 更新一个实体,返回实体
	 * 
	 * @param dataSourceName 数据源名称
	 * @param dbName 数据库名称
	 * @param entity 实体
	 * @return 返回实体
	 */
	@SuppressWarnings("unchecked")
	default <E> E update(String dataSourceName, String dbName, E entity) {
		long effect = executeUpdate(dataSourceName, dbName, entity);
		if (effect != 1) {
			throw new RepositoryException("修改失败了");
		}
		long id = BeanUtil.toId(entity);
		return (E) find(entity.getClass(), id, dataSourceName, dbName);
	}

	/**
	 * 保存或更新一个实体,返回实体
	 * 
	 * @param entity 实体
	 * @return 返回实体
	 */
	@SuppressWarnings("unchecked")
	default <E> E saveOrUpdate(E entity) {
		long effect = executeSaveOrUpdate(entity);
		if (effect != 1) {
			throw new RepositoryException("修改失败了");
		}
		long id = BeanUtil.toId(entity);
		return (E) find(entity.getClass(), id);
	}

	/**
	 * 保存或更新一个实体,返回实体
	 * 
	 * @param dataSourceName 数据源名称
	 * @param entity 实体
	 * @return 返回实体
	 */
	@SuppressWarnings("unchecked")
	default <E> E saveOrUpdate(String dataSourceName, E entity) {
		long effect = executeSaveOrUpdate(dataSourceName, entity);
		if (effect != 1) {
			throw new RepositoryException("修改失败了");
		}
		long id = BeanUtil.toId(entity);
		return (E) find(entity.getClass(), id, dataSourceName);
	}

	/**
	 * 保存或更新一个实体,返回实体
	 * 
	 * @param dataSourceName 数据源名称
	 * @param dbName 数据库名称
	 * @param entity 实体
	 * @return 返回实体
	 */
	@SuppressWarnings("unchecked")
	default <E> E saveOrUpdate(String dataSourceName, String dbName, E entity) {
		long effect = executeSaveOrUpdate(dataSourceName, dbName, entity);
		if (effect != 1) {
			throw new RepositoryException("修改失败了");
		}
		long id = BeanUtil.toId(entity);
		return (E) find(entity.getClass(), id, dataSourceName, dbName);
	}
}
