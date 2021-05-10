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

import lombok.extern.slf4j.Slf4j;
import org.fastquery.bean.TypeTest;
import org.fastquery.example.TypeTestDB;
import org.fastquery.service.FQuery;
import org.junit.Rule;
import org.junit.Test;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author xixifeng (fastquery@126.com)
 */
@Slf4j
public class TypeTestDBTest extends FastQueryTest
{

    private TypeTestDB db = FQuery.getRepository(TypeTestDB.class);

    @Rule
    public FastQueryTestRule rule = new FastQueryTestRule();

    @Test
    public void batchUpdate()
    {
        long id1 = 1L;
        long id3 = 3L;
        long id5 = 5L;
        TypeTest tt1 = new TypeTest(id1, true, false, true, "男");
        TypeTest tt2 = new TypeTest(id3, false, true, false, "女");
        TypeTest tt3 = new TypeTest(id5, true, false, true, "男");
        List<TypeTest> list = Stream.of(tt1, tt2, tt3).collect(Collectors.toList());
        int effect = db.update(list);
        assertThat(rule.getExecutedSQLs().get(0), equalTo("update TypeTest set deleted = case id when 1 then true when 3 then false when 5 then true else deleted end,activated = case id when 1 then false when 3 then true when 5 then false else activated end,auth = case id when 1 then true when 3 then false when 5 then true else auth end,gender = case id when 1 then '男' when 3 then '女' when 5 then '男' else gender end where id in(1,3,5)"));
        assertThat(effect, is(3));
        TypeTest t1 = db.find(TypeTest.class, id1);
        TypeTest t2 = db.find(TypeTest.class, id3);
        TypeTest t3 = db.find(TypeTest.class, id5);
        compare(t1, tt1);
        compare(t2, tt2);
        compare(t3, tt3);
    }

    public void compare(TypeTest t1, TypeTest t2)
    {
        assertThat(t1.getDeleted(), equalTo(t2.getDeleted()));
        assertThat(t1.getActivated(), equalTo(t2.getActivated()));
        assertThat(t1.getAuth(), equalTo(t2.getAuth()));
        assertThat(t1.getGender(), equalTo(t2.getGender()));
    }

    @Test
    public void save()
    {
        String gender = "男";
        TypeTest tt = new TypeTest(true, false, true, gender);
        TypeTest t = db.save(tt);
        assertThat(t.getDeleted(), is(true));
        assertThat(t.getActivated(), is(false));
        assertThat(t.getAuth(), is(true));
        assertThat(t.getGender(), equalTo(gender));

        gender = "女";
        tt = new TypeTest(false, true, false, gender);
        t = db.save(tt);
        assertThat(t.getDeleted(), is(false));
        assertThat(t.getActivated(), is(true));
        assertThat(t.getAuth(), is(false));
        assertThat(t.getGender(), equalTo(gender));
    }

}