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

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.fastquery.bean.Fish;
import org.fastquery.bean.Student;
import org.fastquery.bean.UserInfo;
import org.fastquery.core.RepositoryException;
import org.fastquery.dao.UserInfoDBService;
import org.fastquery.db2.AA;
import org.fastquery.db2.BB;
import org.fastquery.db2.CC;
import org.fastquery.example.StudentDBService;
import org.fastquery.service.FQuery;
import org.junit.Test;

/**
 * @author mei.sir@aliyun.cn
 */
public class TxTest extends TestFastQuery
{

    private final StudentDBService studentDBService = FQuery.getRepository(StudentDBService.class);
    private final UserInfoDBService userInfoDBService = FQuery.getRepository(UserInfoDBService.class);

    @Test
    public void updateTx1()
    {
        try
        {
            userInfoDBService.tx(() -> userInfoDBService.updateBatch2("小不点", 6, 2));
        } catch (Exception e)
        {
            String[] rootCauseStackTrace = ExceptionUtils.getRootCauseStackTrace(e);
            assertThat(rootCauseStackTrace[0], equalTo("java.sql.SQLIntegrityConstraintViolationException: Duplicate entry '1' for key 'PRIMARY'"));
        }
    }

    private void u1(Integer id, String name, Integer age)
    {
        UserInfo userInfo = new UserInfo(id, name, age);
        int effect = studentDBService.executeSaveOrUpdate(userInfo);
        assertThat(effect, is(1));
        UserInfo u1 = userInfoDBService.findById(id);
        assertThat(u1.getId(), equalTo(id));
        assertThat(u1.getName(), equalTo(name));
        assertThat(u1.getAge(), equalTo(age));
    }

    @Test
    public void updateTx2()
    {
        int id = 100;
        String name = "hikey";
        int age = 13;

        try
        {
            userInfoDBService.tx(() -> {
                u1(id, name, age);
                u1(id, name, age);
                u1(id, name, age);

                UserInfo u1 = userInfoDBService.findById(id);
                assertThat(u1.getId(), equalTo(id));
                assertThat(u1.getName(), equalTo(name));
                assertThat(u1.getAge(), equalTo(age));

                Student student = new Student();
                student.setNo("0018480");
                student.setName("迈克狐");
                student.setAge(19);

                int effect = studentDBService.update(student.getNo(),student.getName(),student.getAge().intValue());
                assertThat(effect, equalTo(1));
                Student student2 = studentDBService.findByNo(student.getNo());
                assertThat(student2.getNo(), equalTo(student.getNo()));
                assertThat(student2.getName(), equalTo(student.getName()));
                assertThat(student2.getAge(), equalTo(student.getAge()));

                throw new RepositoryException("Do...Do...");
            });
        }
        catch (RepositoryException e) {
            assertThat(e.getCause().getCause().getMessage(), equalTo("Do...Do..."));
        }
        finally
        {
            UserInfo u1 = userInfoDBService.findById(id);
            assertThat(u1.getId(), equalTo(id));
            assertThat(u1.getName(), not(equalTo(name)));
            assertThat(u1.getAge(), not(equalTo(age)));

            Student student2 = studentDBService.findByNo("0018480");
            assertThat(student2.getNo(), equalTo("0018480"));
            assertThat(student2.getName(), not(equalTo("迈克狐")));
            assertThat(student2.getAge(), not(equalTo(19)));
        }
    }

    private final AA aa = FQuery.getRepository(AA.class);
    private final BB bb = FQuery.getRepository(BB.class);
    private final CC cc = FQuery.getRepository(CC.class);

    @Test
    public void save()
    {
        Integer id = 4;
        String name = "泥鳅";
        Integer num = 800;

        assertThat(aa.findDatabaseName(), equalTo("aa"));
        assertThat(aa.findDatabasePort().get("Value"), equalTo("3306"));
        aa.delete("Fish", "id", 4);
        Fish fish = aa.save(new Fish(id, name, num));
        part2(id, name, num, fish);
        bb.delete("Fish", "id", 4);
        fish = bb.save(new Fish(id, name, num));
        assertThat(fish.getId(), equalTo(id));
        assertThat(fish.getName(), equalTo(name));
        assertThat(fish.getNum(), equalTo(num));

        assertThat(cc.findDatabaseName(), equalTo("cc"));
        assertThat(cc.findDatabasePort().get("Value"), equalTo("3306"));
        cc.delete("Fish", "id", 4);
        fish = cc.save(new Fish(id, name, num));
        assertThat(fish.getId(), equalTo(id));
        assertThat(fish.getName(), equalTo(name));
        assertThat(fish.getNum(), equalTo(num));
    }

    @Test
    public void tx1()
    {
        assertThat(aa.tx(() -> 1), is(1L));
        assertThat(bb.tx(() -> 2), is(2L));
        assertThat(cc.tx(() -> 3), is(3L));
    }

    @Test
    public void tx2()
    {
        Integer id = 1;
        String name = "乌龟";
        Integer num = 300;
        try
        {
                aa.tx(() -> {

                Fish fish = aa.update(new Fish(id, name, num));
                part1(id, name, num, fish);

                fish = bb.update(new Fish(id, name, num));
                part2(id, name, num, fish);

                aa.save(new Fish(name, num));
                bb.save(new Fish(name, num));
                cc.save(new Fish(name, num));

                fish = cc.update(new Fish(id, name, num));
                part3(id, name, num, fish);

                int i = aa.delete("Fish", "id", 1);
                assertThat(i, is(1));
                i = bb.delete("Fish", "id", 1);
                assertThat(i, is(1));
                i = cc.delete("Fish", "id", 1);
                assertThat(i, is(1));

                throw new RepositoryException("模拟异常");
            });
        } catch (Exception e)   {
            assertThat(e.getCause().getCause().getCause().getMessage(), equalTo("tx 不支持多数据源"));
        }
    }

    public void part3(Integer id, String name, Integer num, Fish fish)
    {
        assertThat(fish.getId(), equalTo(id));
        assertThat(fish.getName(), equalTo(name));
        assertThat(fish.getNum(), equalTo(num));
        assertThat(cc.findDatabaseName(), equalTo("cc"));
        assertThat(cc.findDatabasePort().get("Value"), equalTo("3307"));
    }

    public void part2(Integer id, String name, Integer num, Fish fish)
    {
        assertThat(fish.getId(), equalTo(id));
        assertThat(fish.getName(), equalTo(name));
        assertThat(fish.getNum(), equalTo(num));
        assertThat(bb.findDatabaseName(), equalTo("bb"));
        assertThat(bb.findDatabasePort().get("Value"), equalTo("3306"));
    }

    public void part1(Integer id, String name, Integer num, Fish fish)
    {
        assertThat(fish.getId(), equalTo(id));
        assertThat(fish.getName(), equalTo(name));
        assertThat(fish.getNum(), equalTo(num));
        assertThat(aa.findDatabaseName(), equalTo("aa"));
        assertThat(aa.findDatabasePort().get("Value"), equalTo("3306"));
    }


}
