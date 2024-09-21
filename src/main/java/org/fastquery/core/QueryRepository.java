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
     * 插入一个实体 <br>
     *
     * @param <E>    实体
     * @param entity 实体
     * @return 影响行数
     */
    @Id(MethodId.QUERY0)
    <E> int insert(E entity);

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
     * 保存或者更新实体,实体需要包含主键值否则报错 (如果不存在就存储,存在就更新)
     *
     * @param <E>    实体
     * @param entity 实体
     * @return 影响行数
     */
    @Id(MethodId.QUERY2)
    <E> int executeSaveOrUpdate(E entity);

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
     * @param sqlName        基准目录下的SQL文件名称 注意: 基准目录在fastquery.json里配置.当然,sqlName为绝对路径也可以.若传递null,什么也不做
     * @param quotes         在SQL文件里可以通过<code>$[N]</code>引用数组的元素,N从0开始计数
     * @return 数组中的每个数对应一条SQL语句执行后所影响的行数, 有些数据库驱动没有实现该功能，成功提交成功后不能返回影响行数,而是返回-2。在JDBC的规范中Statement.SUCCESS_NO_INFO(-2)代表:执行成功,受影响行数不确定.
     */
    @Id(MethodId.QUERY6)
    int[] executeBatch(String sqlName, String[] quotes);

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
     * 保存一个实体,返回实体
     *
     * @param <E>    实体
     * @param entity 实体
     * @return 返回实体
     */
    @SuppressWarnings("unchecked")
    default <E> E save(E entity)
    {
        Objects.requireNonNull(entity, "save 不能设置 null 值");
        long id = saveToId(entity).longValue();
        return (E) find(entity.getClass(), id);
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
        Objects.requireNonNull(entity, "update 不能设置 null 值");
        long effect = executeUpdate(entity);
        if (effect != 1)
        {
            throw new RepositoryException("修改失败了");
        }
        long id = BeanUtil.toId(entity);
        return (E) find(entity.getClass(), id);
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
        Objects.requireNonNull(entity, "saveOrUpdate 不能设置 null 值");
        long effect = executeSaveOrUpdate(entity);
        if (effect != 1)
        {
            throw new RepositoryException("修改失败了");
        }
        long id = BeanUtil.toId(entity);
        return (E) find(entity.getClass(), id);
    }

    /**
     * 事务函数，tx 范围内不支持多数据源
     *
     * @param fun 函数式子
     * @return 返回值等于 fun 的返回值.
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
     * @param entity  实体对象
     * @param contain true:包含 fields，反之，排除 fields
     * @param fields  待包含或待排除的字段列表
     * @param <E>     实体
     * @return 实体
     */
    @Id(MethodId.QUERY11)
    <E> E findOne(E entity, boolean contain, String... fields);

    /**
     * 根据条件判断是否存在
     *
     * @param entity 根据指定的条件判断是否存在，实体属性若为 null 值，则，该属性不参与运算
     * @return 存在返回 true，反之，返回 false
     */
    @Id(MethodId.QUERY12)
    boolean exists(Object entity);

    /**
     * 逐一遍历指定对象的成员属性并查询该属性值是否在，若存在，立马返回当前属性的名称，反之，继续搜寻下一个属性。实体的  null 属性值不参与运算
     *
     * @param entity 条件
     * @return 返回属性名称，如果返回 null 表示传递的属性集，其值均在该表中不存在记录
     */
    @Id(MethodId.QUERY13)
    String existsEachOn(Object entity);

    /**
     * 根据实体条件分页查询
     * @param entity 默认视为 equal 条件集，entity 中的成员属性如果为 null 表示不参与条件运算；如果该实体继承了 Predicate 可以通过它，构建条件集。entity 如果为 null，将返回一个非 null 的空 page 实例对象。
     * @param notCount 分页是否不求和
     * @param pageIndex 页码
     * @param pageSize 每页大小
     * @param contain true:包含 fields，反之，排除 fields
     * @param fields 待包含或待排除的字段列表
     * @param <E> 实体类型
     * @return 分页结构对象
     * @see org.fastquery.struct.Predicate
     */
    @Id(MethodId.QUERY14)
    <E> Page<E> findPageByPredicate(E entity, @NotCount boolean notCount, @PageIndex int pageIndex, @PageSize int pageSize, boolean contain, String... fields);
}
