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

package org.fastquery.dao;

import java.util.List;
import java.util.Map;
import org.fastquery.bean.Department;
import org.fastquery.bean.UserInfo;
import org.fastquery.bean.UserInformation;
import org.fastquery.core.Source;
import org.fastquery.core.Id;
import org.fastquery.core.Modifying;
import org.fastquery.core.Param;
import org.fastquery.core.Query;
import org.fastquery.core.QueryBuilder;
import org.fastquery.core.QueryByNamed;
import org.fastquery.core.QueryRepository;
import org.fastquery.core.Transactional;
import org.fastquery.page.NotCount;
import org.fastquery.page.Page;
import org.fastquery.page.PageIndex;
import org.fastquery.page.PageSize;
import org.fastquery.page.Pageable;
import org.fastquery.where.Condition;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * @author xixifeng (fastquery@126.com)
 */
public interface UserInfoDBService extends QueryRepository
{

    @Query("select id from UserInfo where id = :id")
    Character findId(@Param("id") Integer id);

    @Query("select id,name,age from UserInfo where id = :id")
    UserInfo findById(@Param("id") Integer id);

    @Query("select id from xk.userinfo where id = (select max(id) from xk.userinfo);")
    Integer findByMaxId();

    @Query("${sql}")
    UserInfo findById(@Param("sql") String sql, @Param("id") Integer id);

    @Query("select name,age from UserInfo u where u.name=:name or u.age=:age")
    UserInfo[] findUserInfoByNameOrAge(@Param("name") String name, @Param("age") Integer age);

    @Query("select * from `userinfo` where id in (${ids})")
    JSONArray findUserInfoByIds(@Param("ids") String ids);

    @Query("select * from `userinfo` where ${one} ${orderby}")
    JSONArray findUserInfo(@Param("orderby") String orderby, @Param("one") int i);

    @Query("select * from `userinfo` where ${one} ${orderby}")
    JSONArray findUserInfo(@Param("orderby") String orderby, @Param("one") int i,  @Param("one") int y);

    // 通过defaultVal属性指定:若参数接受到null值,应该采用的默认值(该属性不是必须的,默认为"").
    @Query("select * from `userinfo` ${orderby}")
    // orderby 若为null, 那么 {orderby}的值,就取defaultVal的值
    JSONArray findUserInfo(@Param(value = "orderby", defaultVal = "order by age desc") String orderby);

    // 类属性名称与表字段不一致时，如何映射？
    @Query("select id as uid,name as myname,age as myage from UserInfo u where u.id = ?1")
    UserInformation findUserInfoById(Integer id);

    @Query("select id,name,age from `userinfo` as u where u.age>?1")
    JSONArray findUserInfoByAge(Integer age);

    @Query("select id,name,age from `userinfo` as u where u.age>?1")
    Map<String, Object> findOne(Integer age, @Source String dataSource);

    @Query("select id,name,age from `userinfo` as u where u.id>?1")
    List<UserInfo> findSome(Integer id);

    @Transactional
    @Modifying
    @Query("update `userinfo` set `name`=?1 where id=?3")
    @Query("update `userinfo` set `age`=?2 where id=?3")
    @Query("update `userinfo` set `name`=?1,`age`=?2 where id=?3")
    int updateBatch(String name, Integer age, Integer id);

    @Transactional
    @Modifying
    @Query("update `userinfo` set `age` = age + 1 where id=?1")
    @Query("update `userinfo` set `age` = age - 1 where id=?1")
    boolean update(int id);

    @Transactional
    @Modifying
    @Query("update `userinfo` set `age` = age where id=?1")
    @Query("update `userinfo` set `age` = age where id=?1")
    boolean update2(int id);

    // 将三条改操作纳入到一个事务中.
    @Transactional
    @Modifying
    @Query("update `userinfo` set `name`=?1 where id=?3")
    @Query("update `userinfo` set `age`=?2 where id=?3")
    @Query("update `userinfo` set `id`=1 where `id`=?3")
    // 把主键id修改为1,目前主键id=1是存在的.这行会报错.那么前两行所做的操作全部失效.
    int updateBatch2(String name, Integer age, Integer id);

    // 返回值如果是int[]类型,表示这个事务成功提交后,每个最小修改单元所影响行数的集合.
    // 举例说明: 若有个事务T,它里面有3条改操作,分别叫U1,U2,U3. T成功提交后,U1,U2,U3所影响的数据行数分别为N1,N2,N3.
    // 则: 返回值为: new int[]{N1,N2,N3}
    @Transactional
    @Modifying
    @Query("update `userinfo` set `name`=?1 where id=?3")
    @Query("update `userinfo` set `age`=?2 where id=?3")
    int[] updateBatch3(String name, Integer age, Integer id);

    @Transactional
    @Modifying
    @Query("delete from ${db}.PMSchedule where id in (${ids})")
    @Query("delete from ${db}.PMSchedule_User where scheduleId in (${ids})")
    int[] deleteByIds(@Source String dsName, @Param("db") String db, @Param("ids") String ids);

    // countField : 明确指定求和字段count(countField),默认值是"id"
    @Query(value = "select id,name,age from `userinfo` where 1")
    Page<Map<String, Object>> findAll(Pageable pageable);

    // 如果没有指定求和语句,那么`fastquery`自动为分析出最优的求和语句.
    @Query("select id,name,age from `userinfo` #{#where}")
    @Condition("age > ?1")
    @Condition("and id < ?2")
    Page<UserInfo> find(Integer age, Integer id, Pageable pageable);

    // countQuery : 指定自定义求和语句
    @Query(value = "select id,name,age from `userinfo` #{#where}", countQuery = "select count(name) from `userinfo` #{#where}")
    @Condition("age > :age")
    @Condition("and id < :id")
    Page<UserInfo> findSome1(@Param("age") Integer age, @Param("id") Integer id, Pageable pageable);

    @Query(value = "select count(name) from `userinfo` #{#where}")
    @Condition("age > :age")
    @Condition("and id < :id")
    long countByAgeAndId(@Param("age") Integer age, @Param("id") Integer id);

    @NotCount // 标识分页不统计总行数. 从上百万的数据里求和很消耗性能.
    @Query(value = "select id,name,age from `userinfo` #{#where}")
    @Condition("age > ?1")
    @Condition("and id < ?2")
    Page<Map<String, Object>> findSome2(Integer age, Integer id, @PageIndex int pageIndex, @PageSize int pageSize);

    @Query(value = "select id,name,age from `userinfo` #{#where}")
    @Condition("age > ?1")
    @Condition("and id < ?2")
    Page<Map<String, Object>> findSome2(Integer age, Integer id, @PageIndex int pageIndex, @PageSize int pageSize, @NotCount boolean notCount);

    @Query("select count(id) from `userinfo` where age > ?1 AND id < ?2")
    long count(Integer age, Integer id);

    @Query("select sum(id) from `userinfo` where age > ?1 AND id < ?2")
    Double countDouble(Integer age, Integer id);

    @Query("select id,name,age from UserInfo where id in (${ids})")
    UserInfo[] findByIds(@Param("ids") int[] ids);

    @Modifying(table = "UserInfo")
    @Query("insert into UserInfo(id,name,age) values(:id,:name,:age)")
    UserInfo insert(@Param("id") Integer id, @Param("name") String name, @Param("age") Integer age);

    @Modifying(table = "UserInfo")
    UserInfo insert(QueryBuilder queryBuilder);

    // 这行SQL参数完全可以用?或:name表达式,在此仅用来测试语法特性
    @Modifying
    @Query("update UserInfo set name = ${name} where id = ${id}")
    int updateNameById(@Param("name") String name, @Param("id") int id);

    @Modifying(table = "UserInfo")
    @Query("update UserInfo set age = ${age} where id = ${id}")
    UserInfo updateAgeById(@Param(value = "age", defaultVal = "null") Integer age, @Id @Param("id") int id);

    @Modifying(table = "UserInfo")
    @Query("update UserInfo set age = ${age} where id = ${id}")
    JSONObject updateAge(@Param(value = "age", defaultVal = "null") Integer age, @Id @Param("id") int id);

    @QueryByNamed("findLimit")
    UserInfo findLimit();

    @Query("select age from UserInfo where id = ?1")
    Integer findAge(Integer id);

    @Query("select * from UserInfo #{#where}")
    @Condition(value = "age = ?1", ignoreNull = false)
    List<UserInfo> findUserInfoByNullAge(Integer age);

    @Query("select name from UserInfo limit 3")
    String[] findNames();

    @Query("select age from UserInfo limit 3")
    String[] findAges();

    @Query("select age from UserInfo where age is not null limit 3")
    Integer[] findAges2();

    @Query("select id,name,age from UserInfo #{#where}")
    @Condition(value = "age = ?1", ignoreNull = false)
    @Condition(value = " and name like ?2")
    List<Map<String, Object>> findUserSome2(Integer age, String name);

    @Query("select name from UserInfo where name = `- %:name% -` limit 1")
    String[] findNamesToSmile(@Param("name") String name);

    @Query("select name from `userinfo` where name like '%::x%' limit 1")
    String findContainColon();

    @Query("select t.A from (select 11 as A,22 as B,33 as C) as T where if(?1 > 10,t.B>10,t.C>100)")
    List<Map<String, Object>> findLogic(Integer i);

    @Query("select if(?1 > 10,'大于10','不大于10') as msg")
    String findLogic2(Integer i);

    @Query("select t.A from (select 11 as A,22 as B,33 as C) as T where if(:number > 10,t.B>10,t.C>100)")
    List<Map<String, Object>> findLogic3(@Param("number") Integer number);

    @Query("SELECT if(:number > 10,'大于10','不大于10') as msg")
    String findLogic4(@Param("number") Integer number);

    @Query("select name from UserInfo where if(:predicate,name like '%三',1)")
    List<String> findLogic5(@Param("predicate") boolean predicate);

    @Query
    Page<Map<String, Object>> pageByQueryBuilder(QueryBuilder queryBuilder, Pageable pageable);

    @Query
    @NotCount
    Page<Map<String, Object>> pageByQueryBuilderNotCount(QueryBuilder queryBuilder, Pageable pageable);

    @Query
    UserInfo findByIdWithQueryBuilder(QueryBuilder queryBuilder);

    @Query("select d.id as departmentId, d.name as departmentName from `department` d left join employee e on d.id = e.departmentId")
    List<Map<String, Object>> findEmpl();

    @Query("select d.id as departmentId, d.name as departmentName from `department` d left join employee e on d.id = e.departmentId")
    List<Department> findDepartments();

    @Query(value = "select d.id as departmentId, d.name as departmentName from `department` d left join employee e on d.id = e.departmentId", countField = "d.id")
    Page<Department> findDepPage(Pageable pageable);
}
