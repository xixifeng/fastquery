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

import static org.hamcrest.Matchers.*;

import org.fastquery.dao.QueryByNamedDBExtend;
import org.fastquery.service.FQuery;
import org.fastquery.struct.SQLValue;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author mei.sir@aliyun.cn
 */
public class QueryByNamedDBExtendTest extends FastQueryTest
{

    private final QueryByNamedDBExtend db = FQuery.getRepository(QueryByNamedDBExtend.class);

    @Rule
    public FastQueryTestRule rule = new FastQueryTestRule();

    @Test
    public void findUAll()
    {
        assertThat(db.findUAll().size(), is(3));
    }

    @Test
    public void findLittle()
    {
        assertThat(db.findLittle().size(), is(3));
        SQLValue sqlValue = rule.getSQLValue();
        assertThat(sqlValue.getSql(), equalTo("select id,name,age from UserInfo limit 3"));
    }

    @Test
    public void findSome()
    {
        assertThat(db.findSome().size(), is(5));
        SQLValue sqlValue = rule.getSQLValue();
        assertThat(sqlValue.getSql(), equalTo("select `no`, `name` from Student limit 5"));
    }
}
