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

package org.fastquery.page;

import org.fastquery.core.QueryProcess;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToLongFunction;

/**
 * 分页结果接口
 * @param <E> 每页记录的数据类型
 *
 * @author xixifeng (fastquery@126.com)
 */
public interface Page<E>
{

    /**
     * 每页最大记录数。
     *
     * @return 每页大小
     */
    int getSize();

    /**
     * 当前页实际记录数。
     *
     * @return 当前页包含的记录数
     */
    int getNumberOfElements();

    /**
     * 当前页码（从 1 开始）。
     *
     * @return 当前页码
     */
    int getNumber();

    /**
     * 当前页的数据内容。
     *
     * @return 当前页的记录集合
     */
    List<E> getContent();

    /**
     * 总记录数。
     * 若未统计总数，则返回 -1。
     *
     * @return 总记录数，或 -1 表示未统计
     */
    long getTotalElements();

    /**
     * 总页数。
     * 若未统计总记录数，则返回 -1。
     *
     * @return 总页数，或 -1 表示未统计
     */
    int getTotalPages();

    /**
     * 当前页是否包含数据。
     *
     * @return 如果当前页有记录返回 true，否则返回 false
     */
    boolean isHasContent();

    /**
     * 是否存在下一页。
     *
     * @return 存在下一页返回 true，否则返回 false
     */
    boolean isHasNext();

    /**
     * 是否存在上一页。
     *
     * @return 存在上一页返回 true，否则返回 false
     */
    boolean isHasPrevious();

    /**
     * 当前页是否为第一页。
     *
     * @return 是第一页返回 true，否则返回 false
     */
    boolean isFirst();

    /**
     * 当前页是否为最后一页。
     *
     * @return 是最后一页返回 true，否则返回 false
     */
    boolean isLast();

    /**
     * 下一页的分页信息（不包含数据，仅用于请求下一页）。
     *
     * @return 下一页的 {@link Slice} 对象
     */
    Slice getNextPageable();

    /**
     * 上一页的分页信息（不包含数据，仅用于请求上一页）。
     *
     * @return 上一页的 {@link Slice} 对象
     */
    Slice getPreviousPageable();

    /**
     * 将当前 E 类型的 Page 复制至 T 类型的 Page 上 <br>
     * 注意：T 类型的 page 由内部创建
     *
     * @param targetSupplier 另一个类型的实例化方式
     * @param biConsumer     自定义 t 的哪个属性来源于 e 的哪个属性
     * @param <T>            另一个类型 , 目标对象
     * @return 一个新的 Page 包含 T 类型的实例
     */
    <T> Page<T> copy(Supplier<T> targetSupplier, BiConsumer<T, E> biConsumer);

    /**
     * 将当前类型的 Page 复制给 T 类型的 Page, 返回新分页 <br>
     * @param targetSupplier 另一个类型的实例化方式
     * @return 复制之后的新类型分页对象 Page&lt;T&gt;
     * @param <T> 目标类型
     */
    default <T> Page<T> copy(Supplier<T> targetSupplier)
    {
        return this.copy(targetSupplier, null);
    }

    /**
     * 将这页的数据转换成树形结构
     *
     * @param nodeIdFun          获取当前节点 id 的方式
     * @param parentIdFun        获取当前节点 父级 id 的方式
     * @param storageChildrenFun 存储当前子集的方式
     * @return 树形结构
     */
    default Page<E> tree(ToLongFunction<E> nodeIdFun, ToLongFunction<E> parentIdFun, BiConsumer<E, List<E>> storageChildrenFun)
    {
        return this.tree(nodeIdFun, parentIdFun, 0L, storageChildrenFun);
    }

    /**
     * 将这页的数据转换成树形结构
     *
     * @param nodeIdFun          获取当前节点 id 的方式
     * @param parentIdFun        获取当前节点 父级 id 的方式
     * @param topLevelParentId   指定顶级父级的 id
     * @param storageChildrenFun 存储当前子集的方式
     * @return 树形结构
     */
    Page<E> tree(ToLongFunction<E> nodeIdFun, ToLongFunction<E> parentIdFun, long topLevelParentId, BiConsumer<E, List<E>> storageChildrenFun);

    /**
     * 左表把右表的一些数据合并进来
     *
     * @param leftFriendshipKey 左表的关系键
     * @param byLeftFriendshipKeyData 根据从左表收集到的键查询出右表数据
     * @param rightFriendshipKey  右表的关系键
     * @param leftSetRight 自定义左表是如何合并右表的数据
     * @param <K> 键的类型
     * @param <T> 右表元素的类型
     * @return 得到右表数据之后的左表
     */
    <K, T> Page<E> join(Function<E, List<K>> leftFriendshipKey, Function<List<K>, List<T>> byLeftFriendshipKeyData, Function<T, K> rightFriendshipKey, BiConsumer<E, List<T>> leftSetRight);

    /**
     * Page 把右表的一些数据合并进来
     *
     * @param leftFriendshipKey       左表的关系键
     * @param byLeftFriendshipKeyData 根据从左表收集到的键查询出右表数据
     * @param rightFriendshipKey      右表的关系键
     * @param leftSetRight            自定义左表是如何合并右表的数据
     * @param copy                    针对右表与左表相同的字段，右表的字段值是否覆盖左表字段值. 如果为 false，ignoreProperties 字段就失去意义
     * @param ignoreProperties        自定义哪些字段不参与字段值的覆盖运算，copy 为 true 才有效
     * @param <K>                     键的类型
     * @param <T>                     右表元素的类型
     * @return 得到右表数据之后的左表 (Page)
     */
    <K, T> Page<E> join(Function<E, K> leftFriendshipKey, Function<List<K>, List<T>> byLeftFriendshipKeyData, Function<T, K> rightFriendshipKey, BiConsumer<E, T> leftSetRight, boolean copy, String... ignoreProperties);

    /**
     * 模型：<br>
     * 部门表 对 员工表 <br>
     * 定义：部门表为【左表】; 员工表为为【右表】<br>
     * 【员工表引用了部门表的id】这个id，称之为【关系键】<br>
     * <p>
     * 左表把右表的一些数据合并进来 <br>
     * oneToManyJoin, one：相当于左表的一条记录，Many：对应右表的多条记录
     *
     * @param leftFriendshipKey       左表的关系键
     * @param byLeftFriendshipKeyData 根据从左表收集到的键查询出右表数据
     * @param rightFriendshipKey      右表的关系键
     * @param leftSetRight            自定义左表是如何合并右表的数据
     * @param <K>                     键的类型
     * @param <B>                     右表元素的类型
     * @return 得到右表数据之后的左表(Page)
     */
    <K, B> Page<E> oneToManyJoin(Function<E, K> leftFriendshipKey, Function<List<K>, List<B>> byLeftFriendshipKeyData, Function<B, K> rightFriendshipKey, BiConsumer<E, List<B>> leftSetRight);

    /**
     * 从 Page 集合中收集指定类型的数据, 装入集合并返回
     *
     * @param function 指定收集的目标
     * @param <R>      需要收集的数据的类型
     * @return 收集之后的数据集合
     */
    default <R> List<R> collected(Function<E, R> function)
    {
        return this.collected(function, false);
    }

    /**
     * 从 Page 集合中收集指定类型的数据, 装入集合并返回
     *
     * @param function 指定收集的目标
     * @param <R>      需要收集的数据的类型
     * @param isDistinct 是否去重
     * @return 收集之后的数据集合
     */
    default <R> List<R> collected(Function<E, R> function, boolean isDistinct)
    {
        List<E> list = this.getContent();
        return StreamUtil.collected(list, function, isDistinct);
    }

    @SuppressWarnings("unchecked")
    static <E> Page<E> empty()
    {
        return (Page<E>) QueryProcess.EMPTY_PAGE;
    }

}
