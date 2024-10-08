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

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.fastquery.bean.*;
import org.fastquery.core.*;
import org.fastquery.dao.UserInfoDBService;
import org.fastquery.example.StudentDBService;
import org.fastquery.service.FQuery;
import org.fastquery.struct.SQLOperator;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author xixifeng (fastquery@126.com)
 */
@RunWith(Theories.class)
public class MethodQueryTest extends TestFastQuery
{

    @Rule
    public FastQueryTestRule rule = new FastQueryTestRule();

    private static StudentDBService studentDBService = FQuery.getRepository(StudentDBService.class);
    private static UserInfoDBService userInfoDBService = FQuery.getRepository(UserInfoDBService.class);

    @Test
    public void db() throws NoSuchMethodException, SecurityException
    {
        assertThat(QueryRepository.class.isAssignableFrom(studentDBService.getClass()), is(true));
        // 断言是否继承了QueryRepository中的方法
        assertThat(studentDBService.getClass().getMethod("executeBatch", String.class), notNullValue());
        assertThat(QueryRepository.class.isAssignableFrom(userInfoDBService.getClass()), is(true));
        assertThat(userInfoDBService.getClass().getMethod("executeBatch", String.class), notNullValue());
    }

    @Test
    public void testSave()
    {

        int id = 36;
        String name = "Jsxxv";
        Integer age = 23;
        UserInfo u = new UserInfo(id, name, age);

        while (userInfoDBService.findById(id) != null)
        { // 该主键已经存在,直到该主键不存在时,才会结束循环
            id += 1;
        }

        u.setId(id);
        int effect = studentDBService.insert(u);
        assertThat(effect, is(1));
    }

    @Test
    public void save3()
    {
        UserInfo u1 = new UserInfo(1, "equinox", 10);
        UserInfo u2 = new UserInfo(2, "Eclipse", 3);
        UserInfo u3 = new UserInfo(3, "ement", 2);
        int effect = studentDBService.saveArray(true, u1, u2, u3);
        assertThat(effect, is(0));
    }

    @Test(expected = RepositoryException.class)
    public void save4()
    {
        UserInfo u1 = new UserInfo(1, "equ", 10);
        UserInfo u2 = new UserInfo(2, "Ecl", 3);
        UserInfo u3 = new UserInfo(3, "ement", 2);
        int effect = studentDBService.saveArray(false, u1, u2, u3);
        assertThat(effect, is(0));
    }

    @Test
    public void save5()
    {
        UserInfo u1 = new UserInfo("equ", 10);
        UserInfo u2 = new UserInfo("Ecl", 3);
        UserInfo u3 = new UserInfo("ement", 2);
        int effect = studentDBService.saveArray(false, u1, u2, u3);
        assertThat(effect, is(3));

        effect = studentDBService.saveArray(false);
        assertThat(effect, is(0));

        effect = studentDBService.saveArray(false,null);
        assertThat(effect, is(0));
    }

    @Test
    public void save6()
    {
        UserInfo u1 = new UserInfo("安小惠", 10);
        UserInfo u2 = new UserInfo("袁承志", 3);
        UserInfo u3 = new UserInfo("袁崇焕", 2);
        Collection<UserInfo> userInfos = new ArrayList<>();
        userInfos.add(u1);
        userInfos.add(u2);
        userInfos.add(u3);
        userInfos.add(new UserInfo("我是谁", null));
        int effect = studentDBService.save(false, userInfos);
        assertThat(effect, is(4));
    }

    @Test
    public void executeBatch1()
    {
        int[] ints = studentDBService.executeBatch("update.sql");
        assertThat(ints.length, is(10));
    }

    @Test
    public void executeBatch2()
    {
        int[] ints = studentDBService.executeBatch(System.getProperty("user.dir") + "/src/test/resources/testFiles" + "/update.sql");
        assertThat(ints.length, is(10));
    }

    @Test
    public void executeBatch3()
    {
        int[] ints = studentDBService.executeBatch(null);
        assertThat(ints, notNullValue());
        assertThat(ints,is(ArrayUtils.EMPTY_INT_ARRAY));

        ints = studentDBService.executeBatch(null,null);
        assertThat(ints, notNullValue());
        assertThat(ints,is(ArrayUtils.EMPTY_INT_ARRAY));
    }

    @Test
    public void executeBatch4()
    {
        int[] ints = studentDBService.executeBatch("create.sql");
        assertThat(ints.length, is(3));

        ints = studentDBService.executeBatch("create.sql",null);
        assertThat(ints.length, is(3));

        ints = studentDBService.executeBatch("create.sql",ArrayUtils.EMPTY_STRING_ARRAY);
        assertThat(ints.length, is(3));
    }

    @Test
    public void executeBatch5()
    {
        int[] ints = studentDBService.executeBatch("create2.sql", new String[]{null, "table", "demo_table", "table", "auto_increment", StringUtils.EMPTY});
        assertThat(ints.length, is(3));
    }

    @Test
    public void update2()
    {
        Integer id = 3;
        UserInfo userInfo = userInfoDBService.findById(id);
        assertThat(userInfo.getId(), equalTo(id));
        UserInfo entity = new UserInfo(userInfo.getId(), userInfo.getName(), userInfo.getAge());
        int i = userInfoDBService.executeUpdate(entity);
        assertThat(i, is(1));
    }

    @Test
    public void saveOrUpdate1()
    {
        Integer id = 100;
        UserInfo userInfo = new UserInfo(id, "小蜜蜂", 5);

        int effect = studentDBService.executeSaveOrUpdate(userInfo);
        assertThat(effect, either(is(0)).or(is(1)));
        UserInfo u1 = userInfoDBService.findById(id);
        Integer id1 = u1.getId();
        assertThat(id1, notNullValue());
        assertThat(u1.getName(), equalTo("小蜜蜂"));
        assertThat(u1.getAge(), equalTo(5));

        studentDBService.executeSaveOrUpdate(u1);
        UserInfo u2 = userInfoDBService.findById(id1);
        Integer id2 = u2.getId();
        assertThat(id2, equalTo(id1));
        assertThat(u2.getName(), equalTo("小蜜蜂"));
        assertThat(u2.getAge(), equalTo(5));

    }

    @Test
    public void saveOrUpdate2()
    {
        UserInfo userInfo = new UserInfo(null, "小蜜蜂", 5);
        int effect = userInfoDBService.executeSaveOrUpdate(userInfo);
        assertThat(effect, is(1));

        effect = userInfoDBService.executeSaveOrUpdate(null);
        assertThat(effect, is(0));
    }

    // 使用update时,同时自定义条件的例子
    @Test
    public void update4()
    {
        Integer id = 1;
        String name = "框架测试!";
        Integer age = 3;
        UserInfo userInfo = new UserInfo(id, name, age);

        int e = studentDBService.executeUpdate(userInfo);
        assertThat(e, is(1));

        // 会解析成:update `UserInfo` set `id`=?, `age`=? where name = ?
        userInfo.set(userInfo::id, userInfo.getId());
        userInfo.set(userInfo::age, userInfo.getAge());
        userInfo.and(userInfo::name, SQLOperator.EQ, userInfo.getName());
        userInfo.finish();
        int effect = studentDBService.executeUpdate(userInfo);
        // 断言: 影响的行数大于0行
        assertThat(effect, greaterThan(0));
    }

    @Test
    public void update5()
    {
        Integer id = 1;
        String name = "框架测试!";
        Integer age = 23;
        UserInfo userInfo = new UserInfo(id, name, age);

        userInfo.set(userInfo::name, userInfo.getName());
        userInfo.set(userInfo::age, userInfo.getAge());
        userInfo.and(userInfo::id, SQLOperator.EQ, userInfo.getId());
        userInfo.finish();

        int i = userInfoDBService.executeUpdate(userInfo);
        assertThat(i, equalTo(1));
        UserInfo u = userInfoDBService.findById(userInfo.getId());
        assertThat(u.getId(), equalTo(id));
        assertThat(u.getName(), equalTo(name));
        assertThat(u.getAge(), equalTo(age));

        i = userInfoDBService.executeUpdate(null);
        assertThat(i,equalTo(0));
    }

    // 测试批量更新集合
    @Test
    public void updateCollection1()
    {
        userInfoDBService.executeSaveOrUpdate(new UserInfo(77, "河虾", 2));
        userInfoDBService.executeSaveOrUpdate(new UserInfo(88, "番茄", 5));
        userInfoDBService.executeSaveOrUpdate(new UserInfo(99, "酸奶", 2));

        List<UserInfo> userInfos = new ArrayList<>();
        userInfos.add(new UserInfo(77, "茝'若", 18));
        userInfos.add(new UserInfo(88, "芸兮", null));
        userInfos.add(new UserInfo(99, "梓", 16));

        int effect = userInfoDBService.update(userInfos);
        assertThat(effect, is(3));
    }

    @Test
    public void updateCollection2()
    {
        userInfoDBService.executeSaveOrUpdate(new UserInfo(77, "河虾", 2));
        userInfoDBService.executeSaveOrUpdate(new UserInfo(88, "番茄", 5));
        userInfoDBService.executeSaveOrUpdate(new UserInfo(99, "酸奶", 2));

        List<UserInfo> userInfos = new ArrayList<>();
        userInfos.add(new UserInfo(77, "茝若", 18));
        userInfos.add(new UserInfo(88, "芸兮", null));
        userInfos.add(new UserInfo(99, null, 16));

        int effect = userInfoDBService.update(userInfos);
        assertThat(effect, is(3));
    }

    @Test
    public void updateCollection3()
    {
        userInfoDBService.executeSaveOrUpdate(new UserInfo(77, "河虾", 2));
        userInfoDBService.executeSaveOrUpdate(new UserInfo(88, "番茄", 5));
        userInfoDBService.executeSaveOrUpdate(new UserInfo(99, "酸奶", 2));

        List<UserInfo> userInfos = new ArrayList<>();
        userInfos.add(new UserInfo(77, null, null));
        userInfos.add(new UserInfo(88, null, null));
        userInfos.add(new UserInfo(99, null, null));

        int effect = userInfoDBService.update(userInfos);
        assertThat(effect, is(3));

        UserInfo[] uis = userInfoDBService.findByIds(new int[]{77, 88, 99});
        for (UserInfo userInfo : uis)
        {
            if (userInfo.getId().equals(77))
            {
                assertThat(userInfo.getName(), equalTo("河虾"));
                assertThat(userInfo.getAge(), equalTo(2));
            }
            else if (userInfo.getId().equals(88))
            {
                assertThat(userInfo.getName(), equalTo("番茄"));
                assertThat(userInfo.getAge(), equalTo(5));
            }
            else if (userInfo.getId().equals(99))
            {
                assertThat(userInfo.getName(), equalTo("酸奶"));
                assertThat(userInfo.getAge(), equalTo(2));
            }
        }
    }
    // 测试批量更新集合 End

    @Test
    public void find()
    {
        assertThat(userInfoDBService.find(UserInfo.class, 3).getId(), is(3));
    }

    @Test
    public void find2()
    {
        UserInfo u = userInfoDBService.find(UserInfo.class, 3, false, "name");
        assertThat(u.getName(), equalTo(StringUtils.EMPTY));
        assertThat(u.getId(), notNullValue());
        assertThat(u.getAge(), nullValue());

        u = userInfoDBService.find(UserInfo.class, 3, false, "name", "age");
        assertThat(u.getName(), equalTo(StringUtils.EMPTY));
        assertThat(u.getId(), notNullValue());
        assertThat(u.getAge(), nullValue());

        u = userInfoDBService.find(UserInfo.class, 3, false, "name", "age", "id");
        assertThat(u.getName(), equalTo(StringUtils.EMPTY));
        assertThat(u.getId(), nullValue());
        assertThat(u.getAge(), nullValue());

    }

    @DataPoints("contains")
    public static boolean[] contains = {true,false};

    @DataPoints("fields")
    public static String[][] fields = {null, ArrayUtils.EMPTY_STRING_ARRAY};

    @Theory
    public void find3(@FromDataPoints("contains") boolean contain)
    {
        UserInfo u = userInfoDBService.find(UserInfo.class, 2, contain);
        assertThat(u.getId(), notNullValue());
        assertThat(u.getName(), notNullValue());
        assertThat(u.getAge(), notNullValue());
    }

    @Theory
    public void find4(@FromDataPoints("contains") boolean contain, @FromDataPoints("fields")  String[] fields)
    {
        UserInfo u = userInfoDBService.find(UserInfo.class, 2, contain, fields);
        assertThat(u.getId(), notNullValue());
        assertThat(u.getName(), notNullValue());
        assertThat(u.getAge(), notNullValue());
    }

    @Test
    public void delete1()
    {
        int id = 8999;
        int effect = userInfoDBService.insert(new UserInfo(id, "植物", 17));
        assertThat(effect, is(1));
        effect = userInfoDBService.delete("UserInfo", "id", id);
        assertThat(effect, is(1));
    }


    @DataPoints("emptyNull")
    public static String[] emptyNull = {StringUtils.EMPTY, null};

    @Theory
    public void delete2(@FromDataPoints("emptyNull") String tableName, @FromDataPoints("emptyNull") String primaryKeyName)
    {
        int effect = userInfoDBService.delete(tableName, primaryKeyName, 1);
        assertThat(effect, is(0));
    }
}






















