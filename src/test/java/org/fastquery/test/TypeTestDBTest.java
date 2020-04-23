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

import org.fastquery.bean.TypeTest;
import org.fastquery.example.TypeTestDB;
import org.fastquery.service.FQuery;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author xixifeng (fastquery@126.com)
 */
public class TypeTestDBTest extends FastQueryTest {

    private static final Logger log = LoggerFactory.getLogger(TypeTestDBTest.class);

    private TypeTestDB db = FQuery.getRepository(TypeTestDB.class);

    @Rule
    public FastQueryTestRule rule = new FastQueryTestRule();

    @Test
    public void save() {
        boolean deleted = true;
        boolean activated = false;
        String gender = "男";
        TypeTest tt = new TypeTest(deleted,activated,gender);
        TypeTest t = db.save(tt);
        assertThat(t.getDeleted().booleanValue(),is(deleted));
        assertThat(t.getActivated().booleanValue(),is(activated));
        assertThat(t.getGender(),equalTo(gender));

        deleted = false;
        activated = true;
        gender = "女";
        tt = new TypeTest(deleted,activated,gender);
        t = db.save(tt);
        assertThat(t.getDeleted().booleanValue(),is(deleted));
        assertThat(t.getActivated().booleanValue(),is(activated));
        assertThat(t.getGender(),equalTo(gender));
    }

}