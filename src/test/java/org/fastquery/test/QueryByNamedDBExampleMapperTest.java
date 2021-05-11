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

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.fastquery.dao.QueryByNamedDBExample;
import org.fastquery.mapper.QueryPool;
import org.fastquery.service.FQuery;
import org.fastquery.util.TypeUtil;
import org.junit.BeforeClass;

import static org.hamcrest.Matchers.*;

/**
 * 测试 : org.fastquery.dao.QueryByNamedDBExample.queries.xml
 *
 * @author xixifeng (fastquery@126.com)
 */
public class QueryByNamedDBExampleMapperTest extends TestFastQuery
{

    private final String className = "org.fastquery.dao.QueryByNamedDBExample";

    private final String logTag = "QueryByNamedDBExampleMapperTest";

    @BeforeClass
    public static void beforeClass()
    {
        FQuery.getRepository(QueryByNamedDBExample.class);
    }

    public String getTemplate(String className, String id) throws Exception
    {
        Method method = QueryPool.class.getDeclaredMethod("getTemplate", String.class, String.class);
        method.setAccessible(true);
        return method.invoke(null, className, id).toString();
    }

    public String render(String tpl, String logTag, Map<String, Object> map) throws Exception
    {
        Method method = QueryPool.class.getDeclaredMethod("render", String.class, String.class, Map.class);
        method.setAccessible(true);
        return method.invoke(null, tpl, logTag, map).toString();
    }

    @Test
    public void findUserInfoAll() throws Exception
    {
        String tpl = getTemplate(className, "findUserInfoAll");
        assertThat(tpl, equalToIgnoringWhiteSpace("select id,name,age from UserInfo"));
    }

    @Test
    public void findUserInfoOne() throws Exception
    {
        String tpl = getTemplate(className, "findUserInfoOne");
        Map<String, Object> map = new HashMap<>();
        map.put("id", "1");
        tpl = render(tpl, logTag, map);
        assertThat(tpl, equalToIgnoringWhiteSpace("select id,name,age from UserInfo where id = :id"));
    }

    @Test
    public void findUserInfoByNameAndAge() throws Exception
    {
        String tpl = getTemplate(className, "findUserInfoByNameAndAge");
        Map<String, Object> map;
        String str;

        map = new HashMap<>();
        map.put("name", null);
        map.put("age", null);
        str = render(tpl, logTag, map);
        assertThat(str, equalToIgnoringWhiteSpace("select id,name,age from UserInfo where 1"));

        map = new HashMap<>();
        map.put("name", "zhangsan");
        map.put("age", 18);
        str = render(tpl, logTag, map);
        assertThat(str, equalToIgnoringWhiteSpace("select id,name,age from UserInfo where 1 and name = :name and age = :age"));

        map = new HashMap<>();
        map.put("age", 18);
        str = render(tpl, logTag, map);
        assertThat(str, equalToIgnoringWhiteSpace("select id,name,age from UserInfo where 1 and age = :age"));

        map = new HashMap<>();
        map.put("name", "zhangsan");
        str = render(tpl, logTag, map);
        assertThat(str, equalToIgnoringWhiteSpace("select id,name,age from UserInfo where 1 and name = :name"));

    }

    @Test
    public void findPage() throws Exception
    {
        String tpl = getTemplate(className, "findPage");
        Map<String, Object> map;
        String str;

        map = new HashMap<>();
        str = render(tpl, logTag, map);
        str = TypeUtil.parWhere(str);
        assertThat(str, equalToIgnoringWhiteSpace("select no, name, sex from Student order by age desc"));

        map = new HashMap<>();
        map.put("name", "zhangsan");
        str = render(tpl, logTag, map);
        str = TypeUtil.parWhere(str);
        assertThat(str, equalToIgnoringWhiteSpace("select no, name, sex from Student where name like :name order by age desc"));
    }

    @Test
    public void updateUserInfoById() throws Exception
    {
        String tpl = getTemplate(className, "updateUserInfoById");
        String str;
        str = render(tpl, logTag, new HashMap<>());
        assertThat(str, equalToIgnoringWhiteSpace("update UserInfo set name = :name,age = :age where id= :id"));
    }

    @Test
    public void findUAll() throws Exception
    {
        String tpl = getTemplate(className, "findUAll");
        assertThat(tpl, equalToIgnoringWhiteSpace("select id,name,age from UserInfo limit 3"));
    }

    @Test
    public void findUserAll() throws Exception
    {
        String tpl = getTemplate(className, "findUserAll");
        assertThat(tpl, equalToIgnoringWhiteSpace("select name from UserInfo limit 3"));
    }

}
