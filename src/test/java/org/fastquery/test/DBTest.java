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

package org.fastquery.test;

import lombok.extern.slf4j.Slf4j;
import org.fastquery.core.DB;
import org.fastquery.example.StudentDBService;
import org.fastquery.service.FQuery;
import org.fastquery.struct.RespUpdate;
import org.fastquery.struct.SQLValue;
import org.junit.Rule;
import org.junit.Test;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author mei.sir@aliyun.cn
 */
@Slf4j
public class DBTest extends TestFastQuery
{
    private final StudentDBService db = FQuery.getRepository(StudentDBService.class);

    // 调用: DB.modify
    @SuppressWarnings("unchecked")
    private static List<RespUpdate> modify(List<SQLValue> sqlValues) throws Exception
    {
        Method method = DB.class.getDeclaredMethod("modify", List.class, boolean.class);
        method.setAccessible(true);
        return (List<RespUpdate>) method.invoke(null, sqlValues, true);
    }

    @Rule
    public FastQueryTestRule rule = new FastQueryTestRule();

    public List<RespUpdate> delete(Object... obs) throws Exception
    {
        List<Object> objs = Arrays.asList(obs);

        List<SQLValue> sqlValues = new ArrayList<>();

        sqlValues.add(new SQLValue("DELETE FROM `userinfo` WHERE id = ?", objs));

        return modify(sqlValues);
    }

    public void update() throws Exception
    {
        List<SQLValue> sqlValues = new ArrayList<>();
        sqlValues.add(new SQLValue("INSERT INTO `userinfo`(`name`, `age`) VALUES (?,?)", Arrays.asList("回家孩子1", 21)));
        sqlValues.add(new SQLValue("INSERT INTO `userinfo`(`name`, `age`) VALUES (?,?)", Arrays.asList("回家孩子2", 22)));
        sqlValues.add(new SQLValue("INSERT INTO `userinfo`(`name`, `age`) VALUES (?,?)", Arrays.asList("回家孩子3", 23)));
        sqlValues.add(new SQLValue("INSERT INTO `userinfo`(`name`, `age`) VALUES (?,?)", Arrays.asList("回家孩子4", 23)));
        sqlValues.add(new SQLValue("INSERT INTO `userinfo`(`name`, `age`) VALUES (?,?)", Arrays.asList("回家孩子5", 24)));
        sqlValues.add(new SQLValue("INSERT INTO `userinfo`(`name`, `age`) VALUES (?,?)", Arrays.asList("回家孩子6", 26)));
        sqlValues.add(new SQLValue("INSERT INTO `userinfo`(`name`, `age`) VALUES (?,?)", Arrays.asList("回家孩子7", 27)));
        sqlValues.add(new SQLValue("INSERT INTO `userinfo`(`name`, `age`) VALUES (?,?)", Arrays.asList("回家孩子8", 28)));
        sqlValues.add(new SQLValue("INSERT INTO `userinfo`(`name`, `age`) VALUES (?,?)", Arrays.asList("回家孩子9", 29)));
        sqlValues.add(new SQLValue("INSERT INTO `userinfo`(`name`, `age`) VALUES (?,?)", Arrays.asList("回家孩子10", 30)));
        sqlValues.add(new SQLValue("INSERT INTO `userinfo`(`name`, `age`) VALUES (?,?)", Arrays.asList("回家孩子11", 31)));

        List<RespUpdate> rus = modify(sqlValues);
        assertThat(rus.size(), is(11));
        rus.forEach(ru -> {
            assertThat(ru.getEffect(), greaterThanOrEqualTo(1));
            assertThat(ru.getPk(), greaterThanOrEqualTo(1L));
            log.debug("正在删除:" + ru.getPk());
            List<RespUpdate> rusx;
            try
            {
                rusx = delete(ru.getPk());
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
            rusx.forEach(r -> {
                assertThat(r.getEffect(), greaterThanOrEqualTo(1));
                assertThat(r.getPk(), nullValue());
            });
            assertThat(rusx.size(), is(1));
        });
    }

    @Test
    public void db() throws Exception
    {
        db.db();
        assertThat(QueryContextHelper.getQueryContext(), notNullValue());
        update();
        assertThat(rule.getListSQLValue(), nullValue());
    }
}
