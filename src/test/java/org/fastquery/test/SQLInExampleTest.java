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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.fastquery.bean.Student;
import org.fastquery.bean.UserInfo;
import org.fastquery.dao.SQLInExample;
import org.fastquery.service.FQuery;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.Matchers.*;

/**
 * @author xixifeng (fastquery@126.com)
 */
@Slf4j
public class SQLInExampleTest extends FastQueryTest
{

    private final SQLInExample db = FQuery.getRepository(SQLInExample.class);

    @Rule
    public FastQueryTestRule rule = new FastQueryTestRule();

    @Test
    public void testFindByNameIn1()
    {
        String name = "袁承志";
        List<UserInfo> userinfos = db.findByNameIn(name);
        if (userinfos.isEmpty())
            return;
        for (UserInfo u : userinfos)
        {
            assertThat(u.getName(), equalTo(name));
        }
    }

    @Test
    public void testFindByNameIn2()
    {
        String name1 = "袁承志";
        String name2 = "安小惠";
        List<UserInfo> userinfos = db.findByNameIn(name1, name2);
        if (userinfos.isEmpty())
            return;
        for (UserInfo u : userinfos)
        {
            assertThat(u.getName().equals(name1) || u.getName().equals(name2), is(true));
        }
    }

    @Test
    public void findByNameListIn()
    {
        String name1 = "袁承志";
        String name2 = "安小惠";
        String name3 = "小青小青86545lk";
        Integer id = 2398;
        List<String> names = new ArrayList<>();
        names.add(name1);
        names.add(name2);
        names.add(name3);
        List<UserInfo> userinfos = db.findByNameListIn(names, id);
        userinfos.forEach(u -> {
            assertThat(u.getName().equals(name1) || u.getName().equals(name2), is(true));
            assertThat(u.getName(), not(is(name3)));
            assertThat(u.getId(), greaterThan(id));
        });
    }

    @Test
    public void findByIn1()
    {
        int[] ids = {1, 2, 3};
        UserInfo[] userInfos = db.findByIn(ids);
        for (UserInfo u : userInfos)
        {
            assertThat(u.getId(), either(is(1)).or(is(2)).or(is(3)));
        }
    }

    @Test
    public void findByIn2()
    {
        String sex = "女";
        Integer age = 10;
        String name1 = "小蚂蚁";
        String name2 = "蜘蛛";
        String name3 = "钱大平";
        Set<String> names = new HashSet<>();
        names.add(name1);
        names.add(name2);
        names.add(name3);

        List<Student> students = db.findByIn(sex, age, names);
        assertThat(students.isEmpty(), is(false));
        students.forEach(s -> {
            assertThat(s.getName().equals(name1) || s.getName().equals(name2) || s.getName().equals(name3), is(true));
            assertThat(s.getAge(), greaterThan(10));
            assertThat(s.getSex(), is(sex));
        });
    }

    @Test
    public void findByIn3()
    {
        UserInfo[] userInfos = db.findByIn((int[]) null);
        for (UserInfo u : userInfos)
        {
            assertThat(u.getId(), either(is(1)).or(is(2)).or(is(3)));
        }
    }

    @Test
    public void findByIn4()
    {
        int[] ids = {};
        UserInfo[] userInfos = db.findByIn(ids);
        for (UserInfo u : userInfos)
        {
            assertThat(u.getId(), either(is(1)).or(is(2)).or(is(3)));
        }
    }

    @Test
    public void findByIn5()
    {
        List<Integer> ids = new ArrayList<>();
        UserInfo[] userInfos = db.findByIn(ids);
        for (UserInfo u : userInfos)
        {
            assertThat(u.getId(), either(is(1)).or(is(2)).or(is(3)));
        }
    }

    @Test
    public void findByIn6()
    {
        List<Integer> ids = new ArrayList<>();
        UserInfo[] userInfos = db.findByObjIn(ids);
        for (UserInfo u : userInfos)
        {
            assertThat(u.getId(), either(is(1)).or(is(2)).or(is(3)));
        }
    }

    @Test
    public void findByIn7()
    {
        String[] ids = {};
        UserInfo[] userInfos = db.findByIn(ids);
        for (UserInfo u : userInfos)
        {
            assertThat(u.getId(), either(is(1)).or(is(2)).or(is(3)));
        }
    }
}
