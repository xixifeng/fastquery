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

import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import org.fastquery.bean.UserInfo;
import org.fastquery.dao.UserInfoDBService;
import org.fastquery.page.Page;
import org.fastquery.page.PageableImpl;
import org.fastquery.service.FQuery;
import org.fastquery.struct.SQLValue;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.Matchers.*;

/**
 * @author xixifeng (fastquery@126.com)
 */
public class PageTest extends TestFastQuery
{

    private final UserInfoDBService userInfoDBService = FQuery.getRepository(UserInfoDBService.class);

    @Rule
    public FastQueryTestRule rule = new FastQueryTestRule();

    @Test
    public void findSome1()
    {

        int pageIndex = 0;
        int size = 0;
        Integer age = 1;
        Integer id = 100;
        Page<UserInfo> page = userInfoDBService.findSome1(age, id, new PageableImpl(pageIndex, size));
        assertThat(page, notNullValue());
        assertThat(page.getNumber(), equalTo(1));
        assertThat(page.getSize(), equalTo(1));

        List<SQLValue> sqlValues = rule.getListSQLValue();

        assertThat(sqlValues.size(), is(2));

        // 分页主体语句
        SQLValue sv1 = sqlValues.get(0);
        assertThat(sv1.getSql(), equalToIgnoringWhiteSpace("select id,name,age from `userinfo` where age > ? and id < ? limit 0,1"));
        assertThat(sv1.getSql(), equalTo("select id,name,age from `userinfo` where age > ? and id < ? limit 0,1"));
        List<Object> ps1 = sv1.getValues();
        assertThat(ps1.size(), is(2));
        assertThat(ps1.get(0).getClass() == Integer.class, is(true));
        assertThat(ps1.get(1).getClass() == Integer.class, is(true));
        assertThat(ps1.get(0), is(age));
        assertThat(ps1.get(1), is(id));

        // 分页求和语句
        SQLValue sv2 = sqlValues.get(1);
        assertThat(sv2.getSql(), equalToIgnoringWhiteSpace("select count(name) from `userinfo` where age > ?  and id < ?"));
        assertThat(sv2.getSql(), equalTo("select count(name) from `userinfo` where age > ? and id < ?"));
        List<Object> ps2 = sv1.getValues();
        assertThat(ps2.size(), is(2));
        assertThat(ps2.get(0).getClass() == Integer.class, is(true));
        assertThat(ps2.get(1).getClass() == Integer.class, is(true));
        assertThat(ps2.get(0), is(age));
        assertThat(ps2.get(1), is(id));

        long count = userInfoDBService.countByAgeAndId(age, id);
        assertThat(page.getTotalElements(), is(count));

        page = userInfoDBService.findSome1(age, -100, new PageableImpl(pageIndex, size));
        assertThat(page.getContent().isEmpty(), is(true));
    }
}
