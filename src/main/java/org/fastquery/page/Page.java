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

import java.util.List;

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
}
