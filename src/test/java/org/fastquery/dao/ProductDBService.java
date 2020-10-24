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

package org.fastquery.dao;

import java.util.Map;

import org.fastquery.core.Modifying;
import org.fastquery.core.Query;
import org.fastquery.core.QueryRepository;

/**
 * @author mei.sir@aliyun.cn
 */
public interface ProductDBService extends QueryRepository
{

    @Query("SELECT * FROM `product` limit 1;")
    Map<String, Object> findOne();

    @Modifying
    @Query("DELETE FROM `product` WHERE `pid` = 1")
    @Query("INSERT INTO `product` (`pid`, `lid`, `pname`, `description`) VALUES (1, 3, '中国', NULL)")
    @Query("INSERT INTO `product` (`pid`, `lid`, `pname`, `description`) VALUES (1, 2, '伟大', NULL)")
    @Query("INSERT INTO `product` (`pid`, `lid`, `pname`, `description`) VALUES (1, 1, '复兴', NULL)")
    int inserts();

    // 测试不同表事务
    @Modifying
    @Query("DELETE FROM `product` WHERE `pid` = 882")
    @Query("update UserInfo set `name` = '苹果' where id = 1")
    @Query("INSERT INTO `product` (`pid`, `lid`, `pname`, `description`) VALUES (1, 1, NULL, NULL)")
    int updates();

}
