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

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.fastquery.dsm.FQueryProperties;
import org.junit.Test;
import com.mysql.cj.jdbc.MysqlDataSource;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author mei.sir@aliyun.cn
 */
@Slf4j
public class FQueryPropertiesTest extends FastQueryTest
{
    @SuppressWarnings("unchecked")
    public Map<String, String> getDataSourceIndexs()
    {
        try
        {
            Class<FQueryProperties> clazz = FQueryProperties.class;
            Field f = clazz.getDeclaredField("dataSourceIndexs");
            f.setAccessible(true);
            return (Map<String, String>) f.get(null);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public Map<String, DataSource> getDataSources()
    {
        try
        {
            Class<FQueryProperties> clazz = FQueryProperties.class;
            Field f = clazz.getDeclaredField("dataSources");
            f.setAccessible(true);
            return (Map<String, DataSource>) f.get(null);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    @Test
    public void testGetDataSourceIndexs()
    {
        Map<String, String> maps = getDataSourceIndexs();
        maps.forEach((k, v) -> log.debug(k + ":" + v));
        assertThat(maps.size(), is(20));
    }

    @Test
    public void testGetDataSources()
    {

        Map<String, DataSource> maps = getDataSources();
        maps.forEach((k, v) -> log.debug(k + ":" + v));
        assertThat(maps.size(), either(is(6)).or(is(10)).or(is(11)));
        Set<String> keys = maps.keySet();
        assertThat(keys, hasItems("sunnydb", "xkdb2", "xk3", "s1", "s2"));

        DataSource d1 = maps.get("xkdb2");
        DataSource d2 = maps.get("xkdb2");
        assertThat(d1, notNullValue());
        assertThat(d1 == d2, is(true));
    }

    @Test
    public void findDataSourceName()
    {
        String sourceName = FQueryProperties.findDataSourceName("org.fastquery.example");
        assertThat(sourceName, equalTo("xkdb2"));

        sourceName = FQueryProperties.findDataSourceName("org.fastquery.dao.UserInfoDBService");
        assertThat(sourceName, equalTo("xkdb2"));

        sourceName = FQueryProperties.findDataSourceName("org.fastquery.dao.SunnyDBService");
        assertThat(sourceName, equalTo("sunnydb"));

        sourceName = FQueryProperties.findDataSourceName("org.fastquery.dao2.UserInfoDB");
        assertThat(sourceName, nullValue());

        sourceName = FQueryProperties.findDataSourceName("org.fastquery.dao2.UserInfoDBRepository");
        assertThat(sourceName, equalTo("xk3"));

        sourceName = FQueryProperties.findDataSourceName("org.fastquery.db");
        assertThat(sourceName, equalTo("s1"));
        DataSource dataSource = FQueryProperties.findDataSource(sourceName);
        MysqlDataSource d = (MysqlDataSource) dataSource;
        assertThat(d.getDatabaseName(), equalTo(sourceName));

        sourceName = FQueryProperties.findDataSourceName("org.fastquery.dbn");
        assertThat(sourceName, nullValue());

        sourceName = FQueryProperties.findDataSourceName("org.fastquery.dbdb");
        assertThat(sourceName, nullValue());

        sourceName = FQueryProperties.findDataSourceName("org.fastquery.dbm");
        assertThat(sourceName, equalTo("s2"));
        dataSource = FQueryProperties.findDataSource(sourceName);
        d = (MysqlDataSource) dataSource;
        assertThat(d.getDatabaseName(), equalTo(sourceName));

        sourceName = FQueryProperties.findDataSourceName("org.fastquery.db.AccountDBService");
        assertThat(sourceName, equalTo("s1"));
        dataSource = FQueryProperties.findDataSource(sourceName);
        d = (MysqlDataSource) dataSource;
        assertThat(d.getDatabaseName(), equalTo(sourceName));

        sourceName = FQueryProperties.findDataSourceName("org.fastquery.dbm.AccountDBService");
        assertThat(sourceName, equalTo("s2"));
        dataSource = FQueryProperties.findDataSource(sourceName);
        d = (MysqlDataSource) dataSource;
        assertThat(d.getDatabaseName(), equalTo(sourceName));
    }

}
