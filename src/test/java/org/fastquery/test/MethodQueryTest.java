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

package org.fastquery.test;

import org.fastquery.bean.UserInfo;
import org.fastquery.dao.UserInfoDBService;
import org.fastquery.example.StudentDBService;
import org.fastquery.service.FQuery;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class MethodQueryTest {

	private StudentDBService studentDBService;
	private UserInfoDBService userInfoDBService;
	
	@Before
	public void before(){
		studentDBService = FQuery.getRepository(StudentDBService.class);
		userInfoDBService = FQuery.getRepository(UserInfoDBService.class);
	}
	
	@Test
	public void testSave(){
		
		Integer id = 36;
		String name = "Jsxxv";
		Integer age = 23;
		UserInfo u = new UserInfo(id,name, age);
		
		while (userInfoDBService.findById(id)!=null) {  // 该主键已经存在,直到该主键不存在时,才会结束循环
			id += 1;
		}
		
		u.setId(id);
		UserInfo u2 = studentDBService.save(u);
		assertThat(u2.getId(), equalTo(u.getId()));
		assertThat(u2.getName(), equalTo(u.getName()));
		assertThat(u2.getAge(), equalTo(u.getAge()));
	}
	
	@Test
	public void testSave2(){
		Integer id = null;
		String name = "凤侯";
		Integer age = 32;
		UserInfo u = new UserInfo(id,name, age);
		UserInfo u2 = studentDBService.save("xk-c3p0", "xk", u);
		System.out.println("id:" + u2.getId());
		assertThat(u2.getId(), notNullValue());
		assertThat(u2.getName(), equalTo(u.getName()));
		assertThat(u2.getAge(), equalTo(u.getAge()));
	}
	
	@Test
	public void executeBatch() {
		// 参考: http://mxm910821.iteye.com/blog/1701822
		studentDBService.executeBatch("update.sql", "sqlout.log");
	}
	
	@Test
	public void update3(){
		String dataSourceName = "xk-c3p0"; 
		String dbName = "xk";
		UserInfo entity = new UserInfo(1,"好哇瓦",3);
		UserInfo u = studentDBService.update(dataSourceName, dbName, entity);
		assertThat(u.getId(), equalTo(1));
		assertThat(u.getName(), equalTo("好哇瓦"));
		assertThat(u.getAge(), equalTo(3));
	}
	
	@Test
	public void saveOrUpdate() {
		UserInfo userInfo = new UserInfo(100,"小蜜蜂", 5);
		UserInfo u1 = studentDBService.saveOrUpdate(userInfo);
		Integer id1 = u1.getId();
		assertThat(id1, notNullValue());
		assertThat(u1.getName(), equalTo("小蜜蜂"));
		assertThat(u1.getAge(), equalTo(5));
		
		UserInfo u2 = studentDBService.saveOrUpdate(u1);
		Integer id2 = u2.getId();
		assertThat(id2, equalTo(id1));
		assertThat(u2.getName(), equalTo("小蜜蜂"));
		assertThat(u2.getAge(), equalTo(5));
		
	}
	
	// 使用update时,同时自定义条件的例子
	@Test
	public void update4(){
		Integer id = 1;
		String name = "好哇瓦";
		Integer age = 3;
		UserInfo entity = new UserInfo(id,name,age);
		// 会解析成:update `UserInfo` set `id`=?, `age`=? where name = ?
		int effect = studentDBService.update(entity,"name = :name");
		// 断言: 影响的行数大于0行
		assertThat(effect, greaterThan(0));
		
		// 不想让id字段参与改运算
		entity.setId(null);
		// 会解析成:update `UserInfo` set `age`=? where name = ?
		effect = studentDBService.update(entity,"name = :name");
		assertThat(effect, greaterThan(0));
	}
}
