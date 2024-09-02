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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;
import org.fastquery.bean.UserInfo;
import org.fastquery.dao.UserInfoDBService;
import org.fastquery.page.*;
import org.fastquery.service.FQuery;
import org.fastquery.struct.SQLValue;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.Matchers.*;

/**
 * @author xixifeng (fastquery@126.com)
 */
@Slf4j
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
        assertThat(sv1.getSql(), equalToCompressingWhiteSpace("select id,name,age from `userinfo` where age > ? and id < ? limit 0,1"));
        assertThat(sv1.getSql(), equalTo("select id,name,age from `userinfo` where age > ? and id < ? limit 0,1"));
        List<Object> ps1 = sv1.getValues();
        assertThat(ps1.size(), is(2));
        assertThat(ps1.get(0).getClass() == Integer.class, is(true));
        assertThat(ps1.get(1).getClass() == Integer.class, is(true));
        assertThat(ps1.get(0), is(age));
        assertThat(ps1.get(1), is(id));

        // 分页求和语句
        SQLValue sv2 = sqlValues.get(1);
        assertThat(sv2.getSql(), equalToCompressingWhiteSpace("select count(name) from `userinfo` where age > ?  and id < ?"));
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

    @Test
    public void findPageByIn1()
    {
        Class<UserInfo> entity = UserInfo.class;
        String fieldName = "id";
        List<Long> fieldValues = Arrays.asList(1L,2L,3L,4L,36L,37L,38L,39L,40L,41L,42L);
        UserInfo equals = null;
        boolean notCount = false;
        int pageIndex = 1;
        int pageSize = 3;
        boolean contain = true;
        String[] fields = {"id","name","age"};

        Page<UserInfo> page = userInfoDBService.findPageByIn(entity,fieldName,fieldValues,equals,notCount,pageIndex,pageSize,contain,fields);
        assertThat(page.getTotalElements(), is(11L));
        assertThat(page.getTotalPages(), is(4));

        // 断言执行过的 sql
        List<String> sqls = rule.getExecutedSQLs();
        assertThat(sqls.get(0), equalTo("select id,name,age from UserInfo where id in (?,?,?,?,?,?,?,?,?,?,?)  limit 0,3"));
        assertThat(sqls.get(1), equalTo("select count(id) from UserInfo where id in (?,?,?,?,?,?,?,?,?,?,?) "));
    }


    @Test
    public void findPageByIn2()
    {
        Class<UserInfo> entity = UserInfo.class;
        String fieldName = "id";
        List<Long> fieldValues = Stream.of(1L,2L,3L,4L,36L,37L,38L,39L,40L,41L,42L).collect(Collectors.toList());
        UserInfo equals = new UserInfo();
        equals.setName("Jsxxv");
        boolean notCount = false;
        int pageIndex = 1;
        int pageSize = 3;
        boolean contain = true;
        String[] fields = {"id","name","age"};

        Page<UserInfo> page = userInfoDBService.findPageByIn(entity,fieldName,fieldValues,equals,notCount,pageIndex,pageSize,contain,fields);
        assertThat(page.getTotalElements(), is(6L));
        assertThat(page.getTotalPages(), is(2));

        // 断言执行过的 sql
        List<String> sqls = rule.getExecutedSQLs();
        assertThat(sqls.get(0), equalTo("select id,name,age from UserInfo where id in (?,?,?,?,?,?,?,?,?,?,?)  and name = ?  limit 0,3"));
        assertThat(sqls.get(1), equalTo("select count(id) from UserInfo where id in (?,?,?,?,?,?,?,?,?,?,?)  and name = ? "));
    }

    @Test
    public void findPageByIn3()
    {
        Class<UserInfo> entity = UserInfo.class;
        String fieldName = "id";
        List<Long> fieldValues = Stream.of(1L,2L,3L).collect(Collectors.toList());
        UserInfo equals = new UserInfo();
        equals.setName("Jsxxv");
        boolean notCount = false;
        int pageIndex = 1;
        int pageSize = 3;
        boolean contain = true;
        String[] fields = {"id","name","age"};

        userInfoDBService.findPageByIn(entity, fieldName, fieldValues, equals, notCount, pageIndex, pageSize, contain, fields);
        assertThat(fieldValues.size(),is(3));
        userInfoDBService.findPageByIn(entity, fieldName, fieldValues, equals, notCount, pageIndex, pageSize, contain, fields);
        assertThat(fieldValues.size(),is(3));
        userInfoDBService.findPageByIn(entity, fieldName, fieldValues, equals, notCount, pageIndex, pageSize, contain, fields);
        assertThat(fieldValues.size(),is(3));
    }


}
