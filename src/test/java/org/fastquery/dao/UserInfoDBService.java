/*
 * Copyright (c) 2016-2016, fastquery.org and/or its affiliates. All rights reserved.
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

import org.fastquery.bean.UserInfo;
import org.fastquery.bean.UserInformation;
import org.fastquery.core.Source;
import org.fastquery.core.Modifying;
import org.fastquery.core.Param;
import org.fastquery.core.Query;
import org.fastquery.core.QueryRepository;
import org.fastquery.core.Transactional;
import org.fastquery.page.NotCount;
import org.fastquery.page.Page;
import org.fastquery.page.PageIndex;
import org.fastquery.page.PageSize;
import org.fastquery.page.Pageable;
import org.fastquery.where.COperator;
import org.fastquery.where.Condition;
import org.fastquery.where.Operator;

import com.alibaba.fastjson.JSONArray;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public interface UserInfoDBService extends QueryRepository {

	// 
	@Query("select * from `userinfo` where {one} {orderby}")
	JSONArray findUserInfo(@Param("orderby") String orderby, @Param("one") int i);
	
	// 类属性名称与表字段不一致时，如何映射？
	@Query("select id as uid,name as myname,age as myage from UserInfo u where u.id = ?1")
	UserInformation findUserInfoById(Integer id);
	
	@Query("select id,name,age from `userinfo` as u where u.age>?1")
	JSONArray findUserInfoByAge(Integer age);
	
	@Query("select id,name,age from `userinfo` as u where u.age>?1")
	Map<String, Object> findOne(Integer age,@Source String dataSource);
	
	@Query("select id,name,age from `userinfo` as u where u.id>?1")
	List<UserInfo> findSome(Integer id);
	
	@Transactional
	@Modifying
	@Query("update `userinfo` set `name`=?1 where id=?3")
	@Query("update `userinfo` set `age`=?2 where id=?3")
	@Query("update `userinfo` set `name`=?1,`age`=?2 where id=?3")
	int updateBatch(String name,Integer age,Integer id);
	
	
    // 将三条改操作纳入到一个事务中.
	@Transactional
	@Modifying
	@Query("update `userinfo` set `name`=?1 where id=?3")
	@Query("update `userinfo` set `age`=?2 where id=?3")
	@Query("update `userinfo` set `id`=1 where `id`=?3") // 把主键id修改为1,目前主键id=1是存在的.这行会报错.那么前两行所做的操作全部失效.
	int updateBatch2(String name,Integer age,Integer id);
	
	
	// 返回值如果是int[]类型,表示这个事务成功提交后,每个最小修改单元所影响行数的集合.
    // 举例说明: 若有个事务T,它里面有3条改操作,分别叫U1,U2,U3. T成功提交后,U1,U2,U3所影响的数据行数分别为N1,N2,N3.
    // 则: 返回值为: new int[]{N1,N2,N3}
	@Transactional
	@Modifying
	@Query("update `userinfo` set `name`=?1 where id=?3")
	@Query("update `userinfo` set `age`=?2 where id=?3")
	@Query("update `userinfo` set `name`=?1,`age`=?2 where id=?3")
	@Query("update `userinfo` set `name`=?1 where age > ?2")
	int[] updateBatch3(String name,Integer age,Integer id);
	
	// countField : 明确指定求和字段count(countField),默认值是"id"
	@Query(value="select id,name,age from `userinfo` where 1",countField="id")
	Page<Map<String, Object>> findAll(Pageable pageable);
	
	// 如果没有指定求和语句,那么`fastquery`自动为分析出最优的求和语句.
	@Query("select id,name,age from `userinfo` #{#where}")
	@Condition(l="age",o=Operator.GT,r="?1")                // age > ?1
	@Condition(c=COperator.AND,l="id",o=Operator.LT,r="?2") // id < ?2
	Page<UserInfo> find(Integer age,Integer id,Pageable pageable);
	
	// countQuery : 指定自定义求和语句
	@Query(value = "select id,name,age from `userinfo` #{#where}", countQuery = "select count(name) from `userinfo` #{#where}")
	@Condition(l = "age", o = Operator.GT, r = "?1")        // age > ?1
	@Condition(c=COperator.AND,l="id",o=Operator.LT,r="?2") // id < ?2
	Page<UserInfo> findSome1(Integer age,Integer id,Pageable pageable);
	
	@NotCount // 标识分页不统计总行数. 从上百万的数据里求和很消耗性能.
	@Query(value = "select id,name,age from `userinfo` #{#where}")
	@Condition(l = "age", o = Operator.GT, r = "?1")        // age > ?1
	@Condition(c=COperator.AND,l="id",o=Operator.LT,r="?2") // id < ?2
	Page<Map<String,Object>> findSome2(Integer age, Integer id,@PageIndex int pageIndex, @PageSize int pageSize);
	
	@Query("select count(id) from `userinfo` where age > ?1 AND id < ?2")
	long count(Integer age, Integer id);
}


