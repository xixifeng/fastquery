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

import org.fastquery.core.RepositoryException;
import org.fastquery.dao.SetDBService;
import org.fastquery.service.FQuery;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

/**
 * @author mei.sir@aliyun.cn
 */
@RunWith(Theories.class)
public class SetDBServiceTest2 extends FastQueryTest
{

    private final SetDBService db = FQuery.getRepository(SetDBService.class);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final String no = "c03";

    @Test
    public void testUpdateCourse1()
    {

        // 断言会抛出 RepositoryException
        thrown.expect(RepositoryException.class);
        thrown.expectMessage(containsString("@Set 修改选项全部被忽略了"));

        int effect = db.updateCourse(null, null, null, null, no);
        assertThat(effect, is(1));
    }

    @Test
    public void testUpdateCourse2()
    {

        // 断言会抛出 RepositoryException
        thrown.expect(RepositoryException.class);
        thrown.expectMessage(containsString("@Set 修改选项全部被忽略了"));

        int effect = db.updateCourse("", null, null, null, no);
        assertThat(effect, is(1));
    }

}
