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
import java.util.*;
import java.util.function.LongSupplier;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.fastquery.page.NotCount;
import org.fastquery.page.Page;
import org.fastquery.page.PageIndex;
import org.fastquery.page.PageSize;
import org.fastquery.util.BeanUtil;

/**
 * 查询仓库
 *
 * @author xixifeng (fastquery@126.com)
 */
public interface QueryRepository extends Repository
{ // NO_UCD
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
     * @param entity         实体bean
     * @param dataSourceName 数据源名称
     * @return 主键
     */
    @Id(MethodId.QUERY)
    BigInteger saveToId(Object entity, @Source String dataSourceName);

    /**
     * 保存一个实体,然后将主键值返回(不适用于联合主键).注意:永不返回null,没有找到主键返回-1
     *
     * @param dataSourceName 数据源名称
     * @param dbName         数据库名称
     * @param entity         实体bean
     * @return 主键
     */
    @Id(MethodId.QUERY)
    BigInteger saveToId(@Source String dataSourceName, String dbName, Object entity);

    /**
     * 保存实体集合
     *
     * @param <B>          实体
     * @param ignoreRepeat 是否忽略已经存在的唯一key(有可能是多个字段构成的唯一key)记录
     * @param entities     实体集合,若,传递null或空集合,则会返回0
     * @return 影响行数
     */
    @Id(MethodId.QUERY4)
    <B> int save(boolean ignoreRepeat, Collection<B> entities);

    /**
     * 保存可变数组实体
     *
     * @param ignoreRepeat 是否忽略已经存在的唯一key(有可能是多个字段构成的唯一key)记录
     * @param entities     实体集合,若,传递null或空数组,则会返回0
     * @return 影响行数
     */
    @Id(MethodId.QUERY4)
    int saveArray(boolean ignoreRepeat, Object... entities);

    /**
     * 保存实体集合
     *
     * @param <B>            实体
     * @param ignoreRepeat   是否忽略已经存在的唯一key(有可能是多个字段构成的唯一key)记录
     * @param dataSourceName 数据源名称
     * @param entities       实体集合,若,传递null或空集合,则会返回0
     * @return 影响行数
     */
    @Id(MethodId.QUERY4)
    <B> int save(boolean ignoreRepeat, @Source String dataSourceName, Collection<B> entities);

    /**
     * 保存可变数组实体
     *
     * @param ignoreRepeat   是否忽略已经存在的唯一key(有可能是多个字段构成的唯一key)记录
     * @param dataSourceName 数据源名称
     * @param entities       实体集合,若,传递null或空数组,则会返回0
     * @return 影响行数
     */
    @Id(MethodId.QUERY4)
    int saveArray(boolean ignoreRepeat, @Source String dataSourceName, Object... entities);

    /**
     * 保存实体集合
     *
     * @param <B>            实体
     * @param dataSourceName 数据源名称
     * @param dbName         数据库名称
     * @param entities       实体集合,若,传递null或空集合,则会返回0
     * @param ignoreRepeat   是否忽略已经存在的唯一key(有可能是多个字段构成的唯一key)记录
     * @return 影响行数
     */
    @Id(MethodId.QUERY4)
    <B> int save(boolean ignoreRepeat, @Source String dataSourceName, String dbName, Collection<B> entities);

    /**
     * 保存可变数组实体
     *
     * @param ignoreRepeat   是否忽略已经存在的唯一key(有可能是多个字段构成的唯一key)记录
     * @param dataSourceName 数据源名称
     * @param dbName         数据库名称
     * @param entities       实体集合,若,传递null或空数组,则会返回0
     * @return 影响行数
     */
    @Id(MethodId.QUERY4)
    int saveArray(boolean ignoreRepeat, @Source String dataSourceName, String dbName, Object... entities);

    /**
     * 插入一个实体 <br>
     *
     * @param <E>    实体
     * @param entity 实体
     * @return 影响行数
     */
    @Id(MethodId.QUERY0)
    <E> int insert(E entity);

    /**
     * 往指定的数据源里插入一个实体<br>
     *
     * @param <E>            存储的实例
     * @param entity         实体
     * @param dataSourceName 数据源名称
     * @return 影响行数
     */
    @Id(MethodId.QUERY0)
    <E> int insert(E entity, @Source String dataSourceName);

    /**
     * 往指定的数据源里插入一个实体,并且指定数据库名称<br>
     *
     * @param <E>            存储的实例
     * @param dataSourceName 数据源名称
     * @param dbName         数据库名称
     * @param entity         实体
     * @return 影响行数
     */
    @Id(MethodId.QUERY0)
    <E> int insert(@Source String dataSourceName, String dbName, E entity);

    /**
     * 更新实体,根据实体的主键值更新 <br>
     *
     * @param <E>    存储的实例
     * @param entity 实体
     * @return 影响行数
     */
    @Id(MethodId.QUERY1)
    <E> int executeUpdate(E entity);

    /**
     * 更新实体,根据实体的主键值更新 <br>
     *
     * @param <E>            更新的实例
     * @param dataSourceName 数据源名称
     * @param entity         实体
     * @return 影响行数
     */
    @Id(MethodId.QUERY1)
    <E> int executeUpdate(@Source String dataSourceName, E entity);

    /**
     * 更新实体,根据实体的主键值更新 <br>
     *
     * @param <E>            更新的实例
     * @param dataSourceName 数据源名称
     * @param dbName         数据库名称
     * @param entity         实体
     * @return 影响行数
     */
    @Id(MethodId.QUERY1)
    <E> int executeUpdate(@Source String dataSourceName, String dbName, E entity);

    /**
     * 更新实体
     *
     * @param entity          实体
     * @param attachCondition 附加条件,若传递null或"",默认将主健作为条件进行修改
     * @return 影响行数
     */
    @Id(MethodId.QUERY3)
    int update(Object entity, String attachCondition);

    /**
     * 更新实体
     *
     * @param dataSourceName  数据源名称
     * @param entity          实体
     * @param attachCondition 附加条件
     * @return 影响行数
     */
    @Id(MethodId.QUERY3)
    int update(@Source String dataSourceName, Object entity, String attachCondition);

    /**
     * 更新实体
     *
     * @param dataSourceName  数据源名称
     * @param dbName          数据库名称
     * @param entity          实体
     * @param attachCondition 附加条件
     * @return 影响行数
     */
    @Id(MethodId.QUERY3)
    int update(@Source String dataSourceName, String dbName, Object entity, String attachCondition);

    /**
     * 保存或者更新实体,实体需要包含主键值否则报错 (如果不存在就存储,存在就更新)
     *
     * @param <E>    实体
     * @param entity 实体
     * @return 影响行数
     */
    @Id(MethodId.QUERY2)
    <E> int executeSaveOrUpdate(E entity);

    /**
     * 保存或者更新实体,实体需要包含主键值否则报错 (如果不存在就存储,存在就更新)
     *
     * @param <E>            实体
     * @param dataSourceName 数据源名称
     * @param entity         实体
     * @return 影响行数
     */
    @Id(MethodId.QUERY2)
    <E> int executeSaveOrUpdate(@Source String dataSourceName, E entity);

    /**
     * 保存或者更新实体,实体需要包含主键值否则报错 (如果不存在就存储,存在就更新)
     *
     * @param <E>            实体
     * @param dataSourceName 数据源名称
     * @param dbName         数据库名称
     * @param entity         实体
     * @return 影响行数
     */
    @Id(MethodId.QUERY2)
    <E> int executeSaveOrUpdate(@Source String dataSourceName, String dbName, E entity);

    /**
     * 批量更新实体
     *
     * @param <E>      实体
     * @param entities 实体集合，如果传递 null 或者空集合，将直接返回 0
     * @return 影响行数
     */
    @Id(MethodId.QUERY5)
    <E> int update(Collection<E> entities);

    /**
     * 批量更新实体
     *
     * @param <E>            实体
     * @param dataSourceName 数据源名称
     * @param entities       实体集合，如果传递 null 或者空集合，将直接返回 0
     * @return 影响行数
     */
    @Id(MethodId.QUERY5)
    <E> int update(@Source String dataSourceName, Collection<E> entities);

    /**
     * 批量更新实体
     *
     * @param <E>            实体
     * @param dataSourceName 数据源名称
     * @param dbName         数据库名称
     * @param entities       实体集合，如果传递 null 或者空集合，将直接返回 0
     * @return 影响行数
     */
    @Id(MethodId.QUERY5)
    <E> int update(@Source String dataSourceName, String dbName, Collection<E> entities);

    /**
     * 执行SQL文件,注意: 只支持单行注释 "#...","-- ..."
     *
     * @param sqlName 基准目录下的SQL文件名称 注意: 基准目录在fastquery.json里配置.当然,sqlName为绝对路径也可以.若传递null,什么也不做
     * @return 数组中的每个数对应一条SQL语句执行后所影响的行数, 有些数据库驱动没有实现该功能，成功提交成功后不能返回影响行数,而是返回-2。在JDBC的规范中Statement.SUCCESS_NO_INFO(-2)代表:执行成功,受影响行数不确定.
     */
    @Id(MethodId.QUERY6)
    int[] executeBatch(String sqlName);

    /**
     * 执行SQL文件,注意: 只支持单行注释 "#...","-- ..."
     *
     * @param sqlName 基准目录下的SQL文件名称 注意: 基准目录在fastquery.json里配置.当然,sqlName为绝对路径也可以.若传递null,什么也不做
     * @param quotes  在SQL文件里可以通过<code>$[N]</code>引用数组的元素,N从0开始计数
     * @return 数组中的每个数对应一条SQL语句执行后所影响的行数, 有些数据库驱动没有实现该功能，成功提交成功后不能返回影响行数,而是返回-2。在JDBC的规范中Statement.SUCCESS_NO_INFO(-2)代表:执行成功,受影响行数不确定.
     */
    default int[] executeBatch(String sqlName, String[] quotes)
    {
        return executeBatch(sqlName, null, quotes);
    }

    /**
     * 执行SQL文件,注意: 只支持单行注释 "#...","-- ..."
     *
     * @param sqlName        基准目录下的SQL文件名称 注意: 基准目录在fastquery.json里配置.当然,sqlName为绝对路径也可以.若传递null,什么也不做
     * @param dataSourceName 数据源的名称
     * @return 数组中的每个数对应一条SQL语句执行后所影响的行数, 有些数据库驱动没有实现该功能，成功提交成功后不能返回影响行数,而是返回-2。在JDBC的规范中Statement.SUCCESS_NO_INFO(-2)代表:执行成功,受影响行数不确定.
     */
    @Id(MethodId.QUERY6)
    int[] executeBatch(String sqlName, @Source String dataSourceName);

    /**
     * 执行SQL文件,注意: 只支持单行注释 "#...","-- ..."
     *
     * @param sqlName        基准目录下的SQL文件名称 注意: 基准目录在fastquery.json里配置.当然,sqlName为绝对路径也可以.若传递null,什么也不做
     * @param dataSourceName 数据源的名称
     * @param quotes         在SQL文件里可以通过<code>$[N]</code>引用数组的元素,N从0开始计数
     * @return 数组中的每个数对应一条SQL语句执行后所影响的行数, 有些数据库驱动没有实现该功能，成功提交成功后不能返回影响行数,而是返回-2。在JDBC的规范中Statement.SUCCESS_NO_INFO(-2)代表:执行成功,受影响行数不确定.
     */
    @Id(MethodId.QUERY6)
    int[] executeBatch(String sqlName, @Source String dataSourceName, String[] quotes);

    /**
     * 根据主键查询实体
     *
     * @param <E>     实体
     * @param clazz   查询的类
     * @param id      主键值
     * @param contain true:包含 fields，反之，排除 fields
     * @param fields  待包含或待排除的字段列表
     * @return 返回实体
     */
    @Id(MethodId.QUERY7)
    <E> E find(Class<E> clazz, long id, boolean contain, String... fields);

    /**
     * 根据主键查询实体
     *
     * @param <E>            实体
     * @param clazz          查询的类
     * @param id             主键值
     * @param dataSourceName 数据源名称
     * @param contain        true:包含 fields，反之，排除 fields
     * @param fields         待包含或待排除的字段列表
     * @return 返回实体
     */
    @Id(MethodId.QUERY7)
    <E> E find(Class<E> clazz, long id, @Source String dataSourceName, boolean contain, String... fields);

    /**
     * 根据主键查询实体
     *
     * @param <E>            实体
     * @param clazz          查询的类
     * @param id             主键值
     * @param dataSourceName 数据源名称
     * @param dbName         数据库名称
     * @param contain        true:包含 fields，反之，排除 fields
     * @param fields         待包含或待排除的字段列表
     * @return 返回实体
     */
    @Id(MethodId.QUERY7)
    <E> E find(Class<E> clazz, long id, @Source String dataSourceName, String dbName, boolean contain, String... fields);

    /**
     * 根据主键查询实体
     *
     * @param <E>         实体
     * @param entityClass 实体的class
     * @param id          主键值
     * @return 返回实体
     */
    default <E> E find(Class<E> entityClass, long id)
    {
        return this.find(entityClass, id, false);
    }

    /**
     * 根据主键查询实体
     *
     * @param <E>            实体
     * @param entityClass    实体的class
     * @param id             主键值
     * @param dataSourceName 数据源名称
     * @return 返回实体
     */
    default <E> E find(Class<E> entityClass, long id, @Source String dataSourceName)
    {
        return this.find(entityClass, id, dataSourceName, false);
    }

    /**
     * 根据主键查询实体
     *
     * @param <E>            实体
     * @param entityClass    实体的class
     * @param id             主键值
     * @param dataSourceName 数据源名称
     * @param dbName         数据库名称
     * @return 返回实体
     */
    default <E> E find(Class<E> entityClass, long id, @Source String dataSourceName, String dbName)
    {
        return this.find(entityClass, id, dataSourceName, dbName, false);
    }

    /**
     * 根据主键删除实体
     *
     * @param tableName      表名称,若传递null或空字符串,则,立马返回0
     * @param primaryKeyName 主键名称,若传递null或空字符串,则,立马返回0
     * @param id             主键值
     * @return 影响行数
     */
    @Id(MethodId.QUERY8)
    int delete(String tableName, String primaryKeyName, long id);

    /**
     * 根据主键删除实体
     *
     * @param tableName      表名称,若传递null或空字符串,则,立马返回0
     * @param primaryKeyName 主键名称,若传递null或空字符串,则,立马返回0
     * @param id             主键值
     * @param dataSourceName 数据源名称
     * @return 影响行数
     */
    @Id(MethodId.QUERY8)
    int delete(String tableName, String primaryKeyName, long id, @Source String dataSourceName);

    /**
     * 根据主键删除实体
     *
     * @param tableName      表名称,若传递null或空字符串,则,立马返回0
     * @param primaryKeyName 主键名称,若传递null或空字符串,则,立马返回0
     * @param id             主键值
     * @param dataSourceName 数据源名称
     * @param dbName         数据库名称
     * @return 影响行数
     */
    @Id(MethodId.QUERY8)
    int delete(String tableName, String primaryKeyName, long id, @Source String dataSourceName, String dbName);

    /**
     * 保存一个实体,返回实体
     *
     * @param <E>    实体
     * @param entity 实体
     * @return 返回实体
     */
    @SuppressWarnings("unchecked")
    default <E> E save(E entity)
    {
        long id = saveToId(entity).longValue();
        return (E) find(entity.getClass(), id);
    }

    /**
     * 保存一个实体,返回实体
     *
     * @param <E>            实体
     * @param entity         实体
     * @param dataSourceName 数据源名称
     * @return 返回实体
     */
    @SuppressWarnings("unchecked")
    default <E> E save(E entity, String dataSourceName)
    {
        long id = saveToId(entity, dataSourceName).longValue();
        return (E) find(entity.getClass(), id, dataSourceName);
    }

    /**
     * 保存一个实体,返回实体
     *
     * @param <E>            实体
     * @param dataSourceName 数据源名称
     * @param dbName         数据库名称
     * @param entity         实体
     * @return 返回实体
     */
    @SuppressWarnings("unchecked")
    default <E> E save(String dataSourceName, String dbName, E entity)
    {
        long id = saveToId(dataSourceName, dbName, entity).longValue();
        return (E) find(entity.getClass(), id, dataSourceName, dbName);
    }

    /**
     * 更新一个实体,返回实体
     *
     * @param <E>    实体
     * @param entity 实体
     * @return 返回实体
     */
    @SuppressWarnings("unchecked")
    default <E> E update(E entity)
    {
        long effect = executeUpdate(entity);
        if (effect != 1)
        {
            throw new RepositoryException("修改失败了");
        }
        long id = BeanUtil.toId(entity);
        return (E) find(entity.getClass(), id);
    }

    /**
     * 更新一个实体,返回实体
     *
     * @param <E>            实体
     * @param dataSourceName 数据源名称
     * @param entity         实体
     * @return 返回实体
     */
    @SuppressWarnings("unchecked")
    default <E> E update(String dataSourceName, E entity)
    {
        long effect = executeUpdate(dataSourceName, entity);
        if (effect != 1)
        {
            throw new RepositoryException("修改失败了");
        }
        long id = BeanUtil.toId(entity);
        return (E) find(entity.getClass(), id, dataSourceName);
    }

    /**
     * 更新一个实体,返回实体
     *
     * @param <E>            实体
     * @param dataSourceName 数据源名称
     * @param dbName         数据库名称
     * @param entity         实体
     * @return 返回实体
     */
    @SuppressWarnings("unchecked")
    default <E> E update(String dataSourceName, String dbName, E entity)
    {
        long effect = executeUpdate(dataSourceName, dbName, entity);
        if (effect != 1)
        {
            throw new RepositoryException("修改失败了");
        }
        long id = BeanUtil.toId(entity);
        return (E) find(entity.getClass(), id, dataSourceName, dbName);
    }

    /**
     * 保存或更新一个实体,返回实体 <br>
     * 前提条件:这个实体必须包含主键字段,主键值若是null,直接存 <br>
     * 另见: {@link #executeSaveOrUpdate(Object)} 返回影响行数(int类型)
     *
     * @param <E>    实体
     * @param entity 实体
     * @return 返回实体
     */
    @SuppressWarnings("unchecked")
    default <E> E saveOrUpdate(E entity)
    {
        long effect = executeSaveOrUpdate(entity);
        if (effect != 1)
        {
            throw new RepositoryException("修改失败了");
        }
        long id = BeanUtil.toId(entity);
        return (E) find(entity.getClass(), id);
    }

    /**
     * 保存或更新一个实体,返回实体<br>
     * 前提条件:这个实体必须包含主键字段,主键值若是null,直接存 <br>
     * 另见: {@link #executeSaveOrUpdate(String, Object)} 返回影响行数(int类型)
     *
     * @param <E>            实体
     * @param dataSourceName 数据源名称
     * @param entity         实体
     * @return 返回实体
     */
    @SuppressWarnings("unchecked")
    default <E> E saveOrUpdate(String dataSourceName, E entity)
    {
        long effect = executeSaveOrUpdate(dataSourceName, entity);
        if (effect != 1)
        {
            throw new RepositoryException("修改失败了");
        }
        long id = BeanUtil.toId(entity);
        return (E) find(entity.getClass(), id, dataSourceName);
    }

    /**
     * 保存或更新一个实体,返回实体<br>
     * 前提条件:这个实体必须包含主键字段,主键值若是null,直接存 <br>
     * 另见: {@link #executeSaveOrUpdate(String, String, Object)} 返回影响行数(int类型)
     *
     * @param <E>            实体
     * @param dataSourceName 数据源名称
     * @param dbName         数据库名称
     * @param entity         实体
     * @return 返回实体
     */
    @SuppressWarnings("unchecked")
    default <E> E saveOrUpdate(String dataSourceName, String dbName, E entity)
    {
        long effect = executeSaveOrUpdate(dataSourceName, dbName, entity);
        if (effect != 1)
        {
            throw new RepositoryException("修改失败了");
        }
        long id = BeanUtil.toId(entity);
        return (E) find(entity.getClass(), id, dataSourceName, dbName);
    }

    /**
     * 事务函数
     *
     * @param fun 函数式子
     * @return fun函数体中有任何没被捕获的异常或fun函数体返回null或返回-1,就会导致fun里面的全部操作回滚,被回滚后的tx最后会返回-1,除此之外,tx的返回值等于fun的返回值.
     */
    @Id(MethodId.QUERY9)
    long tx(LongSupplier fun);

    /**
     * 根据指定的条件统计总记录数，实体属性若为 null 值，则，该属性不参与运算，反之，参与 and 运算
     *
     * @param entity 实体对象
     * @param <E>    实体
     * @return 总记录
     */
    @Id(MethodId.QUERY10)
    <E> long count(E entity);

    /**
     * 根据指定的条件查询一条记录，实体属性若为 null 值，则，该属性不参与运算，反之，参与 equal 运算，条件与条件之间的关系是 and
     *
     * @param equals  实体对象
     * @param contain true:包含 fields，反之，排除 fields
     * @param fields  待包含或待排除的字段列表
     * @param <E>     实体
     * @return 实体
     */
    @Id(MethodId.QUERY11)
    <E> E findOne(E equals, boolean contain, String... fields);

    /**
     * 根据指定的条件查询一条记录，实体属性若为 null 值，则，该属性不参与运算. <br>
     * 一个 KV 称为一个条件单元.
     *
     * @param entity 条件单元集
     * @param unequal KV 之间的关系是否不相等，true: K != V , false: K = V
     * @param or 条件单元之间的关系是否是 or， true: 表示条件单元之间的关系为 or, 例如 K1 != V1 or K2 != V2. false: 表示条件单元之间的关系为 and.
     * @param contain true:包含 fields，反之，排除 fields
     * @param fields 待包含或待排除的字段列表
     * @param <E> 实体
     * @return 满足条件约束的实体
     */
    @Id(MethodId.QUERY11)
    <E> E findOne(E entity, boolean unequal, boolean or, boolean contain, String... fields);

    /**
     * 根据条件判断是否存在
     *
     * @param entity 根据指定的条件判断是否存在，实体属性若为 null 值，则，该属性不参与运算
     * @param or     true 表示条件与条件之间参与 or 运算，反之，参与 and 运算。实体的属性参与 equal 运算
     * @return 存在返回 true，反之，返回 false
     */
    @Id(MethodId.QUERY12)
    boolean exists(Object entity, boolean or);

    /**
     * 逐一遍历指定对象的成员属性并查询该属性值是否在，若存在，立马返回当前属性的名称，反之，继续搜寻下一个属性。实体的  null 属性值不参与运算
     *
     * @param entity 条件
     * @return 返回属性名称，如果返回 null 表示传递的属性集，其值均在该表中不存在记录
     */
    @Id(MethodId.QUERY13)
    String existsEachOn(Object entity);

    /**
     * 查询分页
     *
     * @param builder   查询构造器
     * @param notCount  true:表示分页时不执行 count 语句.反之,执行 count 语句.
     * @param pageIndex 用来指定当前页索引,从1开始计数,如果传递的值小于1,依然视为1
     * @param pageSize  用来指定当前页应该显示多少条数据,如果传递的值小于1,依然视为1
     * @return 分页结构对象
     */
    Page<Map<String, Object>> findPage(QueryBuilder builder, @NotCount boolean notCount, @PageIndex int pageIndex, @PageSize int pageSize);

    /**
     * 查找存储的实体集并进行分页
     *
     * @param <E>       实体类型
     * @param equals    实体实例,用于作为查询条件,条件之间是 and 关系.举例说明,若传递的实体为:<br>
     *                  Student student = new Student(); <br>
     *                  student.setDept("计算机");<br>
     *                  student.setName("海猫");<br>
     *                  那么会推导出 SQL 语句的查询条件为:<br>
     *                  where dept = ? and name = ?<br>
     *                  占位符?问号的值通过 PreparedStatement 设置<br>
     *                  注意: 实体的属性若为 null 该属性将不参与任何运算.
     * @param likes     实体对象实例 用于指定 like 运算集
     * @param sort      排序语句，如,设置"order by id desc". 传递null,则采取默认排序
     * @param notCount  true:表示分页时不执行 count 语句.反之,执行 count 语句.
     * @param pageIndex 用来指定当前页索引,从1开始计数,如果传递的值小于1,依然视为1
     * @param pageSize  用来指定当前页应该显示多少条数据,如果传递的值小于1,依然视为1
     * @param contain   true:包含 fields，反之，排除 fields
     * @param fields    待包含或待排除的字段列表
     * @return 分页结构对象
     * @see #findPage(QueryBuilder, boolean, int, int)
     */
    @SuppressWarnings("unchecked")
    default <E> Page<E> findPage(E equals, E likes, String sort, boolean notCount, int pageIndex, int pageSize, boolean contain, String... fields)
    {

        Class<E> type;
        if (equals != null)
        {
            type = (Class<E>) equals.getClass();
        }
        else if (likes != null)
        {
            type = (Class<E>) likes.getClass();
        }
        else
        {
            throw new IllegalArgumentException("entity，likes 其中一个实体必须不为 null");
        }

        if (sort == null)
        {
            sort = StringUtils.EMPTY;
        }

        SelectField<E> selectField = new SelectField<>(type, contain, fields);
        QueryBuilder builder = BeanUtil.toSelectSQL(type, equals, likes, null, sort, selectField.getFields());
        Page<Map<String, Object>> page = this.findPage(builder, notCount, pageIndex, pageSize);
        return page.convert(type);
    }

    /**
     * 指定某个字段进行 in 和 equal 查询
     *
     * @param entity      实体 class
     * @param fieldName   字段
     * @param fieldValues 字段值列表，参与 in 运算
     * @param equals    实体实例,用于作为查询条件,条件之间是 and 关系.举例说明,若传递的实体为:<br>
     *                  Student student = new Student(); <br>
     *                  student.setDept("计算机");<br>
     *                  student.setName("海猫");<br>
     *                  那么会推导出 SQL 语句的查询条件为:<br>
     *                  where dept = ? and name = ?<br>
     *                  占位符?问号的值通过 PreparedStatement 设置<br>
     *                  注意: 实体的属性若为 null 该属性将不参与任何运算.
     *                  in 条件集 与 equals 条件集 之间的关系是 and
     * @param rows        行数
     * @param contain     true:包含 fields，反之，排除 fields
     * @param fields      待包含或待排除的字段列表
     * @param <E>         实体类型
     * @param <F>         参与 in 运算字段的类型
     * @return 实体 list
     */
    @Id(MethodId.QUERY14)
    <E, F> List<E> findByIn(Class<E> entity, String fieldName, List<F> fieldValues, E equals, int rows, boolean contain, String... fields);

    /**
     * 指定 id 集进行 in 和 equal 查询
     *
     * @param entity  实体 class
     * @param ids     id 集
     * @param equals    实体实例,用于作为查询条件,条件之间是 and 关系.举例说明,若传递的实体为:<br>
     *                  Student student = new Student(); <br>
     *                  student.setDept("计算机");<br>
     *                  student.setName("海猫");<br>
     *                  那么会推导出 SQL 语句的查询条件为:<br>
     *                  where dept = ? and name = ?<br>
     *                  占位符?问号的值通过 PreparedStatement 设置<br>
     *                  注意: 实体的属性若为 null 该属性将不参与任何运算.
     *                  in 条件集 与 equals 条件集 之间的关系是 and
     * @param rows    行数
     * @param contain true:包含 fields，反之，排除 fields
     * @param fields  待包含或待排除的字段列表
     * @param <E>     实体类型
     * @return 实体 list
     */
    default <E> List<E> findByIn(Class<E> entity, long[] ids, E equals, int rows, boolean contain, String... fields)
    {
        Objects.requireNonNull(ids, "ids must not be null");
        List<Object> list = Arrays.stream(ids).boxed().collect(Collectors.toList());
        return this.findByIn(entity, "id", list, equals, rows, contain, fields);
    }

}
