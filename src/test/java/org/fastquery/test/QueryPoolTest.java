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

import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.fastquery.core.Resource;
import org.fastquery.dao.UserInfoDBService;
import org.fastquery.mapper.QueryPool;
import org.fastquery.service.FQuery;
import org.fastquery.util.QueryContextUtil;
import org.fastquery.util.XMLParse;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author xixifeng (fastquery@126.com)
 */
public class QueryPoolTest extends TestFastQuery
{

    static Resource resource;

    public UserInfoDBService userInfoDBService = FQuery.getRepository(UserInfoDBService.class);

    @BeforeClass
    public static void beforeClass() throws Exception
    {

        QueryContextUtil.startQueryContext();

        resource = new Resource()
        {
            @Override
            public InputStream getResourceAsStream(String name)
            {
                return QueryPoolTest.class.getClassLoader().getResourceAsStream(name);
            }

            @Override
            public boolean exist(String name)
            {
                URL url = QueryPoolTest.class.getClassLoader().getResource(name);
                return url != null;
            }
        };
    }

    @AfterClass
    public static void afterClass() throws Exception
    {
        QueryContextUtil.clearQueryContext();
    }

    @SuppressWarnings("unchecked")
    private Set<Object> xml2QueryMapper(Resource resource) throws Exception
    {
        Method method = QueryPool.class.getDeclaredMethod("xml2QueryMapper", String.class, Resource.class);
        method.setAccessible(true);
        return (Set<Object>) method.invoke(null, "org.fastquery.dao.QueryByNamedDBExample", resource);
    }

    private String render(String tpl, Map<String, Object> map) throws Exception
    {
        Method method = QueryPool.class.getDeclaredMethod("render", String.class, String.class, Map.class);
        method.setAccessible(true);
        return method.invoke(null, tpl, "render", map).toString();
    }

    private String queryMapper$getId(Object queryMapper) throws Exception
    {
        Method method = queryMapper.getClass().getDeclaredMethod("getId");
        method.setAccessible(true);
        return (String) method.invoke(queryMapper);
    }

    private String queryMapper$getTemplate(Object queryMapper) throws Exception
    {
        Method method = queryMapper.getClass().getDeclaredMethod("getTemplate");
        method.setAccessible(true);
        return (String) method.invoke(queryMapper);
    }

    @Test
    public void testXml2QueryMapper() throws Exception
    {
        Set<Object> queryMappers = xml2QueryMapper(resource);
        for (Object queryMapper : queryMappers)
        {
            String id = queryMapper$getId(queryMapper);
            String template = queryMapper$getTemplate(queryMapper);
            if ("findUAll".equals(id))
            {
                assertThat(template, equalToCompressingWhiteSpace("select id,name,age from UserInfo limit 3"));
            }
            else if ("findUserAll".equals(id))
            {
                assertThat(template, equalToCompressingWhiteSpace("select name from UserInfo limit 3"));
            }
            else if ("findUserInfo".equals(id))
            {
                assertThat(template, equalToCompressingWhiteSpace("select * from UserInfo where id > :id and age > 18 or name like :name"));
            }
        }
    }

    @Test
    public void render1() throws Exception
    {
        String template = "$abc${abc}";
        Map<String, Object> map = new HashMap<>();
        map.put("abc", "hi");
        String str = render(template, map);
        assertThat(str, equalTo("hihi"));
    }

    @Test
    public void render2() throws Exception
    {
        String ok = "true";
        String err = "false";
        String template = "#if($state || $state == 0)"
                + ok
                + "#else "
                + err
                + " #end";
        Map<String, Object> map = new HashMap<>();
        map.put("state", (byte) 0);
        String str = render(template, map);
        assertThat(str, equalTo(ok));

        map.put("state", (byte) 1);
        str = render(template, map);
        assertThat(str, equalTo(ok));

        map.put("state", (byte) 1);
        str = render(template, map);
        assertThat(str, equalTo(ok));

        map.put("state", (byte) -1);
        str = render(template, map);
        assertThat(str, equalTo(ok));

        map.put("state", null);
        str = render(template, map);
        assertThat(str.trim(), equalTo(err));
    }

    @Test
    public void druidXml1()
    {

        Map<String, String> map = XMLParse.toMap(resource, "druid.xml", "xkdb1", "bean");
        Set<String> keys = map.keySet();
        int len = keys.size();
        assertThat(len, is(15));
        assertThat(map.get("url"), equalTo("jdbc:mysql://db.fastquery.org:3306/xk?serverTimezone=Asia/Shanghai"));
        assertThat(map.get("username"), equalTo("xk"));
        assertThat(map.get("password"), equalTo("abc123"));
        assertThat(map.get("filters"), equalTo("stat"));
        assertThat(map.get("maxActive"), equalTo("20"));
        assertThat(map.get("initialSize"), equalTo("1"));
        assertThat(map.get("maxWait"), equalTo("60000"));
        assertThat(map.get("minIdle"), equalTo("1"));
        assertThat(map.get("timeBetweenEvictionRunsMillis"), equalTo("60000"));
        assertThat(map.get("minEvictableIdleTimeMillis"), equalTo("300000"));
        assertThat(map.get("testWhileIdle"), equalTo("true"));
        assertThat(map.get("testOnBorrow"), equalTo("false"));
        assertThat(map.get("testOnReturn"), equalTo("false"));
        assertThat(map.get("poolPreparedStatements"), equalTo("true"));
        assertThat(map.get("maxOpenPreparedStatements"), equalTo("20"));
    }

    @Test
    public void druidXml2()
    {
        Map<String, String> map = XMLParse.toMap(resource, "druid.xml", "xkdb2", "bean");
        Set<String> keys = map.keySet();
        int len = keys.size();
        assertThat(len, is(3));
        assertThat(map.get("url"), equalTo("jdbc:mysql://db.fastquery.org:3306/xk?serverTimezone=Asia/Shanghai"));
        assertThat(map.get("username"), equalTo("xk"));
        assertThat(map.get("password"), equalTo("abc123"));
    }
}














