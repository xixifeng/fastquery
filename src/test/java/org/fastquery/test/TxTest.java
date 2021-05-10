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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import javax.sql.DataSource;

import org.fastquery.bean.Fish;
import org.fastquery.bean.UserInfo;
import org.fastquery.core.RepositoryException;
import org.fastquery.dao.UserInfoDBService;
import org.fastquery.db2.AA;
import org.fastquery.db2.BB;
import org.fastquery.db2.CC;
import org.fastquery.example.StudentDBService;
import org.fastquery.service.FQuery;
import org.fastquery.struct.DC;
import org.junit.Test;

import com.alibaba.druid.pool.DruidDataSource;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mysql.cj.jdbc.MysqlDataSource;

/**
 * @author mei.sir@aliyun.cn
 */
public class TxTest extends FastQueryTest
{

    private final StudentDBService studentDBService = FQuery.getRepository(StudentDBService.class);
    private final UserInfoDBService userInfoDBService = FQuery.getRepository(UserInfoDBService.class);

    @Test
    public void updateTx1()
    {
        int effect = userInfoDBService.tx(() -> userInfoDBService.updateBatch2("小不点", 6, 2));
        assertThat(effect, equalTo(-1));
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
                throw new RepositoryException("Do...Do...");
            });
        }
        finally
        {
            UserInfo u1 = userInfoDBService.findById(id);
            assertThat(u1.getId(), equalTo(id));
            assertThat(u1.getName(), not(equalTo(name)));
            assertThat(u1.getAge(), not(equalTo(age)));
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
        assertThat(fish.getId(), equalTo(id));
        assertThat(fish.getName(), equalTo(name));
        assertThat(fish.getNum(), equalTo(num));

        assertThat(bb.findDatabaseName(), equalTo("bb"));
        assertThat(bb.findDatabasePort().get("Value"), equalTo("3306"));
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
        assertThat(aa.tx(() -> 1), is(1));
        assertThat(bb.tx(() -> 2), is(2));
        assertThat(cc.tx(() -> 3), is(3));
    }


    @SuppressWarnings("unchecked")
    private static List<DC> getDclist()
    {
        try
        {
            Class<?> tc = Class.forName("org.fastquery.core.TxContext");
            Method ldc = tc.getDeclaredMethod("getTxContext");
            ldc.setAccessible(true);
            Object obj = ldc.invoke(null);
            Field field = tc.getDeclaredField("dclist");
            field.setAccessible(true);
            return (List<DC>) field.get(obj);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends DataSource> T getDataSource(Class<T> cls)
    {
        int len = getDclist().size();
        for (int i = 0; i < len; i++)
        {
            DataSource ds = getDclist().get(i).getDs();
            if (ds.getClass() == cls)
            {
                return (T) ds;
            }
        }
        return null;
    }

    @Test
    public void tx2()
    {
		
		/*
		 
		 Fish fish = aa.find(Fish.class, id);
				assertThat(fish.getId(), equalTo(id));
				assertThat(fish.getName(), not(equalTo(name)));
				assertThat(fish.getNum(), not(equalTo(num)));
		 */


        Integer id = 1;
        String name = "乌龟";
        Integer num = 300;
        int effect = aa.tx(() -> {

            Fish fish = aa.update(new Fish(id, name, num));
            assertThat(fish.getId(), equalTo(id));
            assertThat(fish.getName(), equalTo(name));
            assertThat(fish.getNum(), equalTo(num));
            assertThat(aa.findDatabaseName(), equalTo("aa"));
            assertThat(aa.findDatabasePort().get("Value"), equalTo("3306"));

            fish = bb.update(new Fish(id, name, num));
            assertThat(fish.getId(), equalTo(id));
            assertThat(fish.getName(), equalTo(name));
            assertThat(fish.getNum(), equalTo(num));
            assertThat(bb.findDatabaseName(), equalTo("bb"));
            assertThat(bb.findDatabasePort().get("Value"), equalTo("3306"));

            aa.save(new Fish(name, num));
            bb.save(new Fish(name, num));
            cc.save(new Fish(name, num));

            fish = cc.update(new Fish(id, name, num));
            assertThat(fish.getId(), equalTo(id));
            assertThat(fish.getName(), equalTo(name));
            assertThat(fish.getNum(), equalTo(num));
            assertThat(cc.findDatabaseName(), equalTo("cc"));
            assertThat(cc.findDatabasePort().get("Value"), equalTo("3307"));

            int i = aa.delete("Fish", "id", 1);
            assertThat(i, is(1));
            i = bb.delete("Fish", "id", 1);
            assertThat(i, is(1));
            i = cc.delete("Fish", "id", 1);
            assertThat(i, is(1));

            // 断言目前的连接数
            assertThat(getDclist().size(), is(3));

            // 断言数据源信息
            DruidDataSource dds = getDataSource(DruidDataSource.class);
            assertThat(dds, notNullValue());
            assertThat(dds.getUsername(), equalTo("aa"));
            assertThat(dds.getPassword(), equalTo("aa123456"));

            ComboPooledDataSource cpds = getDataSource(ComboPooledDataSource.class);
            assertThat(cpds, notNullValue());
            assertThat(cpds.getDataSourceName(), equalTo("bb"));
            assertThat(cpds.getUser(), equalTo("bb"));
            assertThat(cpds.getPassword(), equalTo("bb123456"));


            MysqlDataSource mds = getDataSource(MysqlDataSource.class);
            assertThat(mds, notNullValue());
            assertThat(mds.getUser(), equalTo("cc"));
            assertThat(mds.getDatabaseName(), equalTo("cc"));
            assertThat(mds.getPassword(), equalTo("cc123456"));

            throw new RepositoryException("模拟异常");
        });

        assertThat(effect, is(-1));
    }


}
