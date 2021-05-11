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

import org.fastquery.dao.UserInfoDBService;
import org.fastquery.service.FQuery;
import org.fastquery.util.FastQueryJSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;

import static org.hamcrest.Matchers.*;

/**
 * @author xixifeng (fastquery@126.com)
 */
public class FastQueryJSONObjectTest extends TestFastQuery
{

    @BeforeClass
    public static void before()
    {
        FQuery.getRepository(UserInfoDBService.class);
    }

    @Test
    public void testGetBasedir()
    {
        String basedir = FastQueryJSONObject.getBasedir();
        assertThat(basedir, equalTo(System.getProperty("user.dir").replaceAll(Matcher.quoteReplacement(String.valueOf(File.separatorChar)), "/") + "/src/test/resources/testFiles/"));
    }

    @Test
    public void testGetQueries()
    {
        List<String> queries = FastQueryJSONObject.getQueries();
        if (!queries.isEmpty())
        {
            assertThat(queries.contains("queries/"), is(true));
            assertThat(queries.contains("tpl/"), is(true));
        }
    }

    @Test
    public void getSlowQueryTime()
    {
        int time = FastQueryJSONObject.getSlowQueryTime();
        assertThat(time, is(50));
    }

}
