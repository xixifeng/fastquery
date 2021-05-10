/*
 * Copyright (c) 2016-2017, fastquery.org and/or its affiliates. All rights reserved.
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

import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.UUID;
import static org.hamcrest.Matchers.*;
import lombok.extern.slf4j.Slf4j;
import org.fastquery.bean.UserInfo;
import org.fastquery.core.RepositoryException;
import org.fastquery.dao2.DefaultDBService;
import org.fastquery.service.FQuery;
import org.junit.Test;

/**
 * 测试QueryRepository中的默认方法
 *
 * @author mei.sir@aliyun.cn
 */
@Slf4j
public class DefaultMethodTest extends FastQueryTest
{

    private final DefaultDBService db = FQuery.getRepository(DefaultDBService.class);

    @Test
    public void dbNoneNull()
    {
        assertThat(db, notNullValue());
    }

    @Test
    public void saveReturnEntity()
    {
        UserInfo u1 = new UserInfo("婤姶", 2558);
        UserInfo u2 = db.save(u1);
        log.debug("{}", u2);
        long id = u2.getId();
        int effect = db.delete("UserInfo", "id", id);
        assertThat(effect, is(1));
    }

    @Test
    public void updateReturnEntity()
    {
        String name = "海猫" + UUID.randomUUID().toString().substring(0, 3);
        Integer age = Math.abs(new Random().nextInt(30));
        UserInfo u1 = new UserInfo(1, name, age);
        UserInfo u2 = db.update(u1);
        assertThat(u2.getId(), is(u1.getId()));
        assertThat(u2.getName(), is(name));
        assertThat(u2.getAge(), is(age));
    }

    @Test
    public void saveOrUpdateReturnEntity1()
    {
        String name = "海猫" + UUID.randomUUID().toString().substring(0, 3);
        Integer age = Math.abs(new Random().nextInt(30));
        UserInfo u1 = new UserInfo(1, name, age);
        UserInfo u2 = db.saveOrUpdate(u1);
        assertThat(u2.getId(), is(u1.getId()));
        assertThat(u2.getName(), is(name));
        assertThat(u2.getAge(), is(age));
    }

    @Test
    public void saveOrUpdateReturnEntity2()
    {
        Integer id = Math.abs(new Random().nextInt(30)) + 1000;
        String name = "海猫" + UUID.randomUUID().toString().substring(0, 3);
        Integer age = Math.abs(new Random().nextInt(30));
        UserInfo u1 = new UserInfo(id, name, age);
        UserInfo u2 = db.saveOrUpdate(u1);
        assertThat(u2.getId(), is(id));
        assertThat(u2.getName(), is(name));
        assertThat(u2.getAge(), is(age));

        int effect = db.delete("UserInfo", "id", id);
        assertThat(effect, is(1));
    }

    @Test
    public void sqlFun()
    {
        String str = db.sqlFun("select hex(255)");
        assertThat(str, equalTo("FF"));

        str = db.sqlFun("select concat('A1','B2','C3')");
        assertThat(str, equalTo("A1B2C3"));

        str = db.sqlFun("select concat_ws('-','A1','B2','C3')");
        assertThat(str, equalTo("A1-B2-C3"));
    }

    @Test
    public void count()
    {
        UserInfo u = new UserInfo(16, "986545457", 32);
        long count = db.count(u);
        assertThat(count, is(0L));

        u = new UserInfo(16, null, 32);
        count = db.count(u);
        assertThat(count, is(0L));

        u = new UserInfo(null, null, 36);
        count = db.count(u);
        assertThat(count, is(1L));

        u = new UserInfo(null, null, 82);
        count = db.count(u);
        assertThat(count, is(1L));
    }

    @Test
    public void findOne()
    {
        UserInfo userInfo = new UserInfo();
        UserInfo u = db.findOne(userInfo, true, "id", "name");
        assertThat(u, nullValue());

        String name = "王五";
        userInfo.setName(name);
        u = db.findOne(userInfo, true, "id", "name");
        log.info("u:{}", u);
        assertThat(u.getId(), notNullValue());
        assertThat(u.getName(), equalTo(name));
        assertThat(u.getAge(), nullValue());

    }

    @Test
    public void exists()
    { // xk3
        UserInfo userInfo = new UserInfo();
        userInfo.setName(null);
        boolean b = db.exists(userInfo, true);
        assertThat(b, is(true));

        userInfo.setName("婤姶");
        b = db.exists(userInfo, true);
        assertThat(b, is(true));

        userInfo.setName("凤雏");
        b = db.exists(userInfo, true);
        assertThat(b, is(false));

        userInfo.setName("王五");
        userInfo.setAge(36);
        b = db.exists(userInfo, true);
        assertThat(b, is(true));

        userInfo.setId(2);
        b = db.exists(userInfo, true);
        assertThat(b, is(true));
    }

    @Test
    public void exists1()
    { // xk3
        UserInfo userInfo = new UserInfo();
        userInfo.setName(null);
        boolean b = db.exists(userInfo, false);
        assertThat(b, is(true));

        userInfo.setName("婤姶");
        b = db.exists(userInfo, false);
        assertThat(b, is(true));

        userInfo.setName("凤雏");
        b = db.exists(userInfo, false);
        assertThat(b, is(false));

        userInfo.setName("王五");
        userInfo.setAge(36);
        b = db.exists(userInfo, false);
        assertThat(b, is(true));

        userInfo.setId(2);
        b = db.exists(userInfo, false);
        assertThat(b, is(true));

        userInfo.setName("瘌蛤蟆");
        b = db.exists(userInfo, false);
        assertThat(b, is(false));
    }

    @Test
    public void existsEachOn()
    {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(56);
        userInfo.setName("李四");
        userInfo.setAge(79);
        String str = db.existsEachOn(userInfo);
        assertThat(str, equalTo("name"));

        userInfo.setId(56);
        userInfo.setName("李逵");
        userInfo.setAge(79);
        str = db.existsEachOn(userInfo);
        assertThat(str, nullValue());
    }

    @Test
    public void existsEachOn1()
    {
        UserInfo userInfo = new UserInfo();
        userInfo.setName(null);
        String str = db.existsEachOn(userInfo);
        assertThat(str, nullValue());
    }

    @Test
    public void exists2()
    {
        try
        {
            db.exists("cc", "bb");
        }
        catch (RepositoryException e)
        {
            assertThat(e.getMessage(), containsString("Unknown column 'cc' in 'where clause'"));
        }
    }

    @Test
    public void exists3()
    {
        boolean b = db.exists("name", "张三");
        assertThat(b, is(false));
    }

    @Test
    public void exists4()
    {
        boolean b = db.exists("name", "王五");
        assertThat(b, is(true));
    }

    @Test
    public void exists5()
    {
        try
        {
            db.exists("id and or", "200");
        }
        catch (RepositoryException e)
        {
            assertThat(e.getMessage(), containsString("有注入风险"));
        }
    }

    @Test
    public void exists6()
    {
        boolean b = db.exists("id", "200");
        assertThat(b, is(false));
    }

    @Test
    public void update1()
    {
        Collection<?> entities = new HashSet<>();
        int effect = db.update(entities);
        assertThat(effect, is(0));
    }

    @Test
    public void update2()
    {
        int effect = db.update(null);
        assertThat(effect, is(0));
    }

}
