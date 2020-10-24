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

package org.fastquery.dao2;

import org.fastquery.core.Source;
import org.fastquery.core.Modifying;
import org.fastquery.core.Param;
import org.fastquery.core.Query;
import org.fastquery.core.QueryRepository;
import org.fastquery.core.Transactional;

/**
 * @author xixifeng (fastquery@126.com)
 */
public interface UserInfoDBService3 extends QueryRepository
{

    @Transactional
    @Modifying
    @Query("update `userinfo` set `name`=?1 where id=?3")
    @Query("update `userinfo` set `age`=?2 where id=?3")
    @Query("update `userinfo` set `name`=?1,`age`=?2 where id=?3")
    int updateBatch(String name, Integer age, Integer id, @Source String dataSource);

    @Query(value = "select name from UserInfo where name like ?1 limit 1")
    int findByName(@Param(value = "name", defaultVal = "%谷子%") String name);
}
