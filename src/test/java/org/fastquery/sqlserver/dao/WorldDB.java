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

package org.fastquery.sqlserver.dao;

import java.util.Map;

import org.fastquery.bean.City;
import org.fastquery.core.Param;
import org.fastquery.core.Query;
import org.fastquery.core.QueryByNamed;
import org.fastquery.core.QueryRepository;
import org.fastquery.page.Page;
import org.fastquery.page.PageIndex;
import org.fastquery.page.PageSize;
import org.fastquery.page.Pageable;
import org.fastquery.where.Condition;

/**
 * @author mei.sir@aliyun.cn
 */
public interface WorldDB extends QueryRepository
{

    @Query("select id,code,cityAbb,cityName from City")
    Page<Map<String, Object>> findPage(Pageable pageable);

    @QueryByNamed(render = false)
    Page<City> findPage(@Param("id") Integer id, @Param("cityAbb") String cityAbb, Pageable pageable);

    @Query("select id,code,cityAbb,cityName from City #{#where}")
    @Condition("id > :id")
    @Condition("and cityAbb like :cityAbb")
    Page<City> findPageWithWhere(@Param("id") Integer id, @Param("cityAbb") String cityAbb, @PageIndex int pageIndex, @PageSize int pageSize);

    @Query("select id,code,cityAbb,cityName from City #{#where}")
    @Condition("id > :id")
    @Condition("and cityAbb like :cityAbb")
    Page<City> findPageWithWhere(@Param("id") Integer id, @Param("cityAbb") String cityAbb, Pageable pageable);

}
