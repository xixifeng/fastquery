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

/**
 * @author xixifeng (fastquery@126.com)
 */
public class PageableImpl implements Pageable
{

    private int page = 1;
    private int size = 1;

    /**
     * 构造分页
     *
     * @param page 指定访问第几页(从1开始计数)
     * @param size 设定每页显示几条数据
     */
    public PageableImpl(int page, int size)
    {
        if (page > 1)
        {
            this.page = page;
        }
        if (size > 1)
        {
            this.size = size;
        }
    }

    @Override
    public int getPageIndex()
    {
        return page;
    }

    @Override
    public int getPageSize()
    {
        return size;
    }

    @Override
    public int getOffset()
    {
        return page * size - size;
    }
}
