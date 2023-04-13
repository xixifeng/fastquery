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
import org.fastquery.bean.Department;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.fastquery.bean.UserInfo;
import org.fastquery.bean.UserInformation;
import org.fastquery.core.ConditionList;
import org.fastquery.core.QueryBuilder;
import org.fastquery.core.RepositoryException;
import org.fastquery.dao.UserInfoDBService;
import org.fastquery.page.Page;
import org.fastquery.page.PageableImpl;
import org.fastquery.page.Slice;
import org.fastquery.service.FQuery;
import org.fastquery.struct.SQLValue;
import org.junit.Rule;
import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import static org.hamcrest.MatcherAssert.assertThat;

import java.util.*;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.*;

/**
 * @author xixifeng (fastquery@126.com)
 */
@Slf4j
public class UserInfoDBServiceTest extends TestFastQuery
{
    private final UserInfoDBService db = FQuery.getRepository(UserInfoDBService.class);

    @Rule
    public FastQueryTestRule rule = new FastQueryTestRule();

    @Test
    public void findId()
    {
        try
        {
            Object id = db.findId(1);
            log.info("id:{}", id);
        }
        catch (Exception e)
        {
            String str = ExceptionUtils.getStackTrace(e);
            assertThat(str, containsString("类型转换失败,发生方法:org.fastquery.core.MethodInfo.findId, 建议将这个方法的返回类型由class java.lang.Character 修改成 java.lang.Integer"));
        }
    }

    @Test
    public void findById()
    {
        Integer id = 1;
        String sql = "select id,name,age from UserInfo where id = ?2";
        UserInfo userInfo = db.findById(sql, id);
        assertThat(userInfo.getId(), is(id));

        SQLValue sqlValue = rule.getSQLValue();
        assertThat(sqlValue.getSql(), equalTo("select id,name,age from UserInfo where id = ?"));
        List<Object> values = sqlValue.getValues();
        assertThat(values.size(), is(1));
        Object arg = values.get(0);
        assertThat((arg instanceof Integer), is(true));
        assertThat(arg, is(id));

        sql = "select id,name,age from UserInfo where id = :id";
        userInfo = db.findById(sql, id);
        assertThat(userInfo.getId(), is(id));
        sqlValue = rule.getSQLValue();
        assertThat(sqlValue.getSql(), equalTo("select id,name,age from UserInfo where id = ?"));
        values = sqlValue.getValues();
        assertThat(values.size(), is(1));
        arg = values.get(0);
        assertThat((arg instanceof Integer), is(true));
        assertThat(arg, is(id));
    }

    @Test
    public void findById2()
    {
        int id = 35;
        UserInfo userInfo = db.findById(id); // 没有找到
        // userInfo会返回null
        if (userInfo != null)
        {
            assertThat(userInfo.getAge(), nullValue());
        }

        SQLValue sqlValue = rule.getSQLValue();
        assertThat(sqlValue.getSql(), equalTo("select id,name,age from UserInfo where id = ?"));
        List<Object> values = sqlValue.getValues();
        assertThat(values.size(), is(1));
        Object arg = values.get(0);
        assertThat((arg instanceof Integer), is(true));
        assertThat(arg, is(id));
    }

    @Test
    public void findUserInfoByNameOrAge()
    {
        String name = "王五";
        int age = 8;
        UserInfo[] userInfos = db.findUserInfoByNameOrAge(name, age);
        for (UserInfo userInfo : userInfos)
        {
            assertThat((userInfo.getName().equals(name) || userInfo.getAge() == age), is(true));
        }
        SQLValue sqlValue = rule.getSQLValue();
        assertThat(sqlValue.getSql(), equalTo("select name,age from UserInfo u where u.name=? or u.age=?"));
        List<Object> values = sqlValue.getValues();
        assertThat(values.size(), is(2));
        Object arg = values.get(0);
        assertThat((arg instanceof String), is(true));
        assertThat(arg, is(name));
        arg = values.get(1);
        assertThat((arg instanceof Integer), is(true));
        assertThat(arg, is(age));
    }

    @Test
    public void findUserInfoByIds()
    {
        JSONArray json = db.findUserInfoByIds("2,3,4");
        assertThat(JSONObject.parseObject(JSON.toJSONString(json.get(0))).getIntValue("id"), equalTo(2));
        assertThat(JSONObject.parseObject(JSON.toJSONString(json.get(1))).getIntValue("id"), equalTo(3));
        assertThat(JSONObject.parseObject(JSON.toJSONString(json.get(2))).getIntValue("id"), equalTo(4));
    }

    @Test
    public void findUserInfo()
    {
        String orderby = "order by age desc";
        int i = 1;
        db.findUserInfo(orderby, i);
        assertThat(rule.getSQLValue().getSql(), equalTo("select * from `userinfo` where 1 order by age desc"));
    }

    @Test
    public void findUserInfoById()
    {
        UserInformation userInformation = db.findUserInfoById(1);
        assertThat(userInformation, notNullValue());
        log.debug(userInformation.toString());
    }

    @Test
    public void testFindUserInfoByAge()
    {
        int age = 20;
        JSONArray jsonArray = db.findUserInfoByAge(age);
        for (int i = 0; i < jsonArray.size(); i++)
        {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            assertThat(jsonObject.getInteger("age"), greaterThan(age));
        }
    }

    @Test
    public void findSome()
    {
        List<UserInfo> userInfos = db.findSome(30);
        userInfos.forEach(userInfo -> assertThat(userInfo.getId(), greaterThan(30)));
    }

    @Test
    public void testFindUserInfoByAge2()
    {
        int age = 1000;
        // 数据库中age没有大于1千的记录
        // 断言: 查询返回的值应该是一个空对象,不是null.
        JSONArray jsonArray = db.findUserInfoByAge(age);
        assertThat(jsonArray, notNullValue());
        assertThat(jsonArray.isEmpty(), is(true));
    }

    @Test
    public void findOne()
    {
        int age = 1000;
        // 数据库中age没有大于1千的记录
        // 断言: 查询返回的值应该是一个空对象,不是null.
        Map<String, Object> map = db.findOne(age, "xk-c3p0");
        assertThat(map, notNullValue());
        assertThat(map.isEmpty(), is(true));
    }

    @Test
    public void testUpdateBatch()
    {
        int effect = db.updateBatch("小张张", 26, 1);
        assertThat("断言该行修改操作一共影响了3行", effect, equalTo(3));
    }

    @Test
    public void update1()
    {
        boolean b = db.update(1);
        assertThat(b, is(true));

        b = db.update(-10);
        assertThat(b, is(true));
    }

    @Test
    public void update2()
    {
        boolean b = db.update2(1);
        assertThat(b, is(true));
    }

    // 断言: 它会抛出RepositoryException异常
    @Test(expected = RepositoryException.class)
    public void testUpdateBatch2_a()
    {
        int effect = db.updateBatch2("小不点", 6, 2);
        // updateBatch2 中途会报错,因此修改影响的行数为0
        assertThat(effect, equalTo(0));
    }

    @Test(expected = RepositoryException.class)
    public void testUpdateBatch2_b()
    {
        int effect = db.updateBatch2("小不点", 6, 2);
        assertThat(effect, equalTo(0));
    }

    @Test
    public void testUpdateBatch3()
    {
        int[] effects = db.updateBatch3("清风习习", 23, 3);
        assertThat(effects.length, is(2));
        assertThat(effects[0], is(1));
        assertThat(effects[1], is(1));
    }

    @Test
    public void findAll()
    {

        int p = 1;
        int size = 6;
        Page<Map<String, Object>> page = db.findAll(new PageableImpl(p, size));
        assertThat(String.format("断言: 当前是第%s页", p), page.getNumber(), is(p));
        assertThat(page.getNumberOfElements(), lessThanOrEqualTo(size));

        // 打印出来看看
        String str = JSON.toJSONString(page, SerializerFeature.PrettyFormat);
        log.debug(str);

    }

    @Test
    public void find()
    {
        Page<UserInfo> page = db.find(100, 50, new PageableImpl(1, 3));
        List<UserInfo> userInfos = page.getContent();
        if (page.isHasContent())
        {
            userInfos.forEach(u -> log.debug(u.toString()));
        }
        assertThat(page.isFirst(), is(true));

        String str = JSON.toJSONString(page, SerializerFeature.PrettyFormat);

        log.debug(str);
    }

    @Test
    public void findSome1()
    {
        Page<UserInfo> page = db.findSome1(1, 100, new PageableImpl(1, 15));
        assertThat(page, notNullValue());
    }

    @Test
    public void findSome2()
    {

        // pageIndex:5 pageSize:24
        // pageIndex:1 pageSize:99 totalPages:1
        // pageIndex:3 pageSize:49 totalPages:3
        // pageIndex:4 pageSize:49 totalPages:3

        int pageIndex;
        int pageSize;

        int age = 1;
        int id = 100;
        // 总行数
        long totalElements = db.count(age, id);
        for (int i = 1; i <= totalElements; i++)
        {
            pageIndex = i % 10;
            pageSize = new Random().nextInt(1000) % 10;

            if (pageSize * pageIndex > totalElements)
            { // 显然是错的
                pageSize = 1;
            }

            if (pageSize == 0)
            {
                pageSize += 1;
            }

            int totalPages; // 总页码
            totalPages = (int) totalElements / pageSize;
            if (totalElements % pageSize != 0)
            {
                totalPages += 1;
            }
            if (pageIndex > totalPages)
            {
                pageIndex = new Random().nextInt(1000) % totalPages;
            }
            if (pageIndex == 0)
            {
                pageIndex += 1;
            }
            log.debug("totalElements:" + totalElements + " pageIndex:" + pageIndex + "  pageSize:" + pageSize + "  totalPages:" + totalPages);

            Page<Map<String, Object>> page = db.findSome2(age, id, pageIndex, pageSize);
            extracted(pageIndex, pageSize, totalPages, page);

            page = db.findSome2(age, id, pageIndex, pageSize,true);
            extracted(pageIndex, pageSize, totalPages, page);

            page = db.findSome2(age, id, pageIndex, pageSize,false);
            assertThat(page.getTotalElements(), not(is(-1L)));
            assertThat(page.getTotalPages(), not(is(-1L)));

        }
    }

    private void extracted(int pageIndex, int pageSize, int totalPages, Page<Map<String, Object>> page)
    {
        assertThat(page, notNullValue());
        assertThat(page.getNumber(), equalTo(pageIndex));
        assertThat(page.getSize(), equalTo(pageSize));
        assertThat(page.getTotalElements(), equalTo(-1L));
        assertThat(page.getTotalPages(), equalTo(-1));
        assertThat(page.getNumberOfElements(), lessThanOrEqualTo(pageSize));
        // log.debug(JSON.toJSONString(page, true));

        boolean hasContent = pageIndex <= totalPages; // 是否有结果集
        boolean isFirst = pageIndex == 1; // 是否是第一页
        boolean isLast = pageIndex == totalPages; // 是否是最后一页
        boolean hasNext = pageIndex < totalPages; // 是否有下一页
        boolean hasPrevious = (pageIndex > 1) && hasContent; // 是否有上一页
        Slice previousPageable = new Slice((!isFirst) ? (pageIndex - 1) : pageIndex, pageSize); // 上一页的Pageable对象
        Slice nextPageable = new Slice((!isLast) ? (pageIndex + 1) : pageIndex, pageSize); // 下一页的Pageable对象

        assertThat(page.isHasContent(), is(hasContent));
        assertThat(page.isFirst(), is(isFirst));
        assertThat(page.isLast(), is(isLast));
        assertThat(page.isHasNext(), is(hasNext));
        assertThat(page.isHasPrevious(), is(hasPrevious));
        assertThat(page.getPreviousPageable().getNumber(), is(previousPageable.getNumber()));
        assertThat(page.getPreviousPageable().getSize(), is(previousPageable.getSize()));
        assertThat(page.getNextPageable().getNumber(), is(nextPageable.getNumber()));
        assertThat(page.getNextPageable().getSize(), is(nextPageable.getSize()));
    }

    @Test
    public void countDouble()
    {
        Double d = db.countDouble(2100, 2308);
        assertThat(d, nullValue());
    }

    @Test
    public void findByIds()
    {
        int[] ids = {1, 2, 3};
        UserInfo[] userInfos = db.findByIds(ids);
        assertThat(userInfos[0].getId(), equalTo(1));
        assertThat(userInfos[1].getId(), equalTo(2));
        assertThat(userInfos[2].getId(), equalTo(3));
    }

    @Test
    public void insert()
    {
        int id;
        String name = "香月儿";
        Integer age = 23;

        id = db.findByMaxId() + 1;
        UserInfo u = db.insert(id, name, age);
        assertThat(u.getId(), equalTo(id));
        assertThat(u.getName(), equalTo(name));
        assertThat(u.getAge(), equalTo(age));

    }

    @Test
    public void insertByQueryBuilder()
    {
        int id;
        String name = "香月儿";
        Integer age = 23;

        id = db.findByMaxId() + 1;

        String query = "insert into UserInfo(id,name,age) values(:id,:name,:age)";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("id", id);
        parameters.put("name", name);
        parameters.put("age", age);
        QueryBuilder queryBuilder = new QueryBuilder(query, parameters);

        UserInfo u = db.insert(queryBuilder);
        assertThat(u.getId(), equalTo(id));
        assertThat(u.getName(), equalTo(name));
        assertThat(u.getAge(), equalTo(age));

    }

    @Test
    public void updateNameById()
    {
        int id = 2;
        int i = db.updateNameById("'戚继光'", 2);
        assertThat(i, is(1));
        UserInfo u = db.findById(id);
        assertThat(u.getId(), equalTo(id));
        assertThat(u.getName(), equalTo("戚继光"));
    }

    @Test
    public void updateAgeById()
    {
        UserInfo userInfo = db.updateAgeById(null, 3);
        assertThat(userInfo.getId(), is(3));
        assertThat(userInfo.getAge(), nullValue());
        userInfo = db.updateAgeById(21, 3);
        assertThat(userInfo.getId(), is(3));
        assertThat(userInfo.getAge(), is(21));
    }

    @Test
    public void updateAge()
    {
        JSONObject userInfo = db.updateAge(null, 3);
        assertThat(userInfo.getIntValue("id"), is(3));
        // 断言:包含有age属性 (age即使是null)
        assertThat(JSON.toJSONString(userInfo, SerializerFeature.WriteMapNullValue).contains("\"age\""), is(true));
    }

    @Test
    public void findAge()
    {
        Integer age = db.findAge(35);
        assertThat(age, nullValue());
    }

    @Test
    public void findUserInfoByNullAge()
    {
        // 查询age为null的UserInfo
        List<UserInfo> us = db.findUserInfoByNullAge(null);
        assertThat(us.size(), greaterThanOrEqualTo(1));
        us.forEach(u -> assertThat(u.getAge(), nullValue()));
    }

    @Test
    public void findNames()
    {
        String[] names = db.findNames();
        assertThat(names.length, is(3));
        for (String name : names)
        {
            assertThat(Pattern.matches("\\{\".+\":\".+\"}", name), is(false));
            log.debug(name);
        }
    }

    @Test
    public void findAges1()
    {
        String[] ages = db.findAges();
        assertThat(ages.length, is(3));
        for (String age : ages)
        {
            log.info("age:{}",age);
            if (age != null)
            {
                assertThat(Pattern.matches("\\{\".+\":\".+\"}", age), is(false));
            }
        }
    }

    @Test
    public void findAges2()
    {
        Integer[] ages = db.findAges2();
        assertThat(ages.length, is(3));
        for (Integer age : ages)
        {
            assertThat(age, notNullValue());
        }
    }

    @Test
    public void findUserSome2()
    {
        Integer age = null;
        List<Map<String, Object>> maps = db.findUserSome2(age, null);
        maps.forEach(m -> m.forEach((k, v) -> {
            if ("age".equals(k))
                assertThat(v, nullValue());
        }));

        SQLValue sqlValue = rule.getSQLValue();
        assertThat(sqlValue.getSql(), equalTo("select id,name,age from UserInfo where age is null"));
        List<Object> objects = sqlValue.getValues();
        assertThat(objects.isEmpty(), is(true));
    }

    @Test
    public void findNamesToSmile()
    {
        String name = "Smile";
        db.findNamesToSmile(name);
        SQLValue sqlValue = rule.getSQLValue();
        assertThat(sqlValue.getSql(), equalTo("select name from UserInfo where name = ? limit 1"));
        List<Object> vals = sqlValue.getValues();
        assertThat(vals.size(), is(1));
        assertThat(vals.get(0).toString(), equalTo(" %Smile% "));
    }

    @Test
    public void findContainColon()
    {
        String name = db.findContainColon();
        assertThat(name, containsString(":x"));
    }

    @Test
    public void findLogic1_1()
    {
        List<Map<String, Object>> list = db.findLogic(11);
        assertThat(list.toString(), equalTo("[{A=11}]"));
    }

    @Test
    public void findLogic1_2()
    {
        List<Map<String, Object>> list = db.findLogic(10);
        assertThat(list.isEmpty(), is(true));
    }

    @Test
    public void findLogic2_1()
    {
        String str = db.findLogic2(10);
        assertThat(str, equalTo("不大于10"));
    }

    @Test
    public void findLogic2_2()
    {
        String str = db.findLogic2(11);
        assertThat(str, equalTo("大于10"));
    }

    @Test
    public void findLogic3_1()
    {
        List<Map<String, Object>> list = db.findLogic3(11);
        assertThat(list.toString(), equalTo("[{A=11}]"));
    }

    @Test
    public void findLogic3_2()
    {
        List<Map<String, Object>> list = db.findLogic3(10);
        assertThat(list.isEmpty(), is(true));
    }

    @Test
    public void findLogic4_1()
    {
        String str = db.findLogic4(10);
        assertThat(str, equalTo("不大于10"));
    }

    @Test
    public void findLogic4_2()
    {
        String str = db.findLogic4(11);
        assertThat(str, equalTo("大于10"));
    }

    @Test
    public void findLogic5_1()
    {
        List<String> list = db.findLogic5(true);
        list.forEach(m -> assertThat(m, endsWith("三")));
    }

    @Test
    public void pageByQueryBuilder()
    {
        String query = "select id,name,age from userinfo #{#where}";
        String countQuery = "select count(name) from userinfo #{#where}";
        ConditionList conditions = ConditionList.of("age > :age", "and id < :id");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("age", 18);
        parameters.put("id", 50);

        QueryBuilder queryBuilder = new QueryBuilder(query, countQuery, conditions, parameters);
        Page<Map<String, Object>> page = db.pageByQueryBuilder(queryBuilder, new PageableImpl(1, 3));
        List<Map<String, Object>> content = page.getContent();
        content.forEach(map -> {
            Integer age = (Integer) map.get("age");
            Integer id = (Integer) map.get("id");
            assertThat(age, greaterThan(18));
            assertThat(id, lessThan(50));
        });

        List<String> executedSQLs = rule.getExecutedSQLs();
        assertThat("断言：执行过的sql有两条", executedSQLs.size(), is(2));
        assertThat(executedSQLs.get(0), equalTo("select id,name,age from userinfo where age > ? and id < ? limit 0,3"));
        assertThat(executedSQLs.get(1), equalTo("select count(name) from userinfo where age > ? and id < ?"));
    }

    @Test
    public void pageByQueryBuilderNotCount()
    {
        String query = "select id,name,age from userinfo #{#where}";
        String countQuery = "select count(name) from userinfo #{#where}";
        ConditionList conditions = ConditionList.of("age > :age", "and id < :id");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("age", 18);
        parameters.put("id", 50);

        QueryBuilder queryBuilder = new QueryBuilder(query, countQuery, conditions, parameters);
        Page<Map<String, Object>> page = db.pageByQueryBuilderNotCount(queryBuilder, new PageableImpl(1, 3));
        List<Map<String, Object>> content = page.getContent();
        content.forEach(map -> {
            Integer age = (Integer) map.get("age");
            Integer id = (Integer) map.get("id");
            assertThat(age, greaterThan(18));
            assertThat(id, lessThan(50));
        });
        assertThat(page.getTotalElements(), is(-1L));
        assertThat(page.getTotalPages(), is(-1));
        List<String> executedSQLs = rule.getExecutedSQLs();
        assertThat("断言：执行过的sql有两条", executedSQLs.size(), is(2));
        assertThat(executedSQLs.get(0), equalTo("select id,name,age from userinfo where age > ? and id < ? limit 0,3"));
        assertThat(executedSQLs.get(1), equalTo("select id,name,age from userinfo where age > ? and id < ? limit 3,1"));
    }

    @Test
    public void findByIdWithQueryBuilder()
    {
        String query = "select id,name,age from UserInfo where id = :id";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("id", 3);
        QueryBuilder queryBuilder = new QueryBuilder(query, parameters);
        UserInfo userInfo = db.findByIdWithQueryBuilder(queryBuilder);
        assertThat(userInfo.getId(), is(3));
    }

    @Test
    public void findEmpl()
    {
        List<Map<String, Object>> maps = db.findEmpl();
        assertThat(maps.size(), is(10));
        maps.forEach(map -> {
            Long departmentId = (Long) map.get("departmentId");
            if (departmentId == 1L)
            {
                assertThat(map.get("departmentName").toString(), equalTo("研发"));
            }
            else if (departmentId == 2L)
            {
                assertThat(map.get("departmentName").toString(), equalTo("人事"));
            }
            else if (departmentId == 3L)
            {
                assertThat(map.get("departmentName").toString(), equalTo("财务"));
            }
        });
    }

    @Test
    public void findDepartments()
    {
        List<Department> list = db.findDepartments();
        assertThat(list.size(), is(10));
    }

    @Test
    public void findDepPage()
    {
        Page<Department> page = db.findDepPage(new PageableImpl(1, 100));
        assertThat(page.getNumberOfElements(), is(10));
        List<Department> departments = page.getContent();
        assertThat(departments.size(), is(10));
    }

    @Test
    public void findAgesContainNull()
    {
        List<Integer> ages = db.findAgesContainNull();
        assertThat(ages.size(),is(3));
        ages.forEach( age -> assertThat(age, nullValue()));
    }
}
















