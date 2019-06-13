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

import org.fastquery.core.QueryRepository;
import org.fastquery.dao.ConditionDBService;
import org.fastquery.page.PageableImpl;
import org.fastquery.service.FQuery;
import org.fastquery.struct.SQLValue;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertThat;

import org.apache.commons.lang3.exception.ExceptionUtils;

import static org.hamcrest.Matchers.*;

/**
 * 
 * @author mei.sir@aliyun.cn
 */
public class ConditionTest extends FastQueryTest {

	private ConditionDBService db = FQuery.getRepository(ConditionDBService.class);

	@Rule
	public FastQueryTestRule rule = new FastQueryTestRule();
	
	@Test
	public void db() {
		assertThat(QueryRepository.class.isAssignableFrom(db.getClass()), is(false));
		// 断言是否继承了QueryRepository中的方法
		try {
			db.getClass().getMethod("executeBatch", String.class,String.class);
		} catch (Exception e) {
			String stackTrace = ExceptionUtils.getStackTrace(e);
			assertThat(stackTrace, containsString("java.lang.NoSuchMethodException:"));
			assertThat(stackTrace, containsString("executeBatch(java.lang.String, java.lang.String)"));
		}
	}
	
	@Test
	public void findUserInfo1() {
		String tname = "from UserInfo";
		String w1 = "name like ?1";
		String w2 = "and age > ?2";
		db.findUserInfo(w1, w2, tname);
		SQLValue sqlValue = rule.getSQLValue();
		String sql = sqlValue.getSql();
		assertThat(sql, equalTo("select * from UserInfo where  name like ?  and age > ? limit 3"));
	}

	@Test
	public void findUserInfo2() {
		String tname = "from UserInfo";
		String w1 = null;
		db.findUserInfo2(w1, "name like ?1", tname);
		SQLValue sqlValue = rule.getSQLValue();
		String sql = sqlValue.getSql();
		assertThat(sql, equalTo("select * from UserInfo where  name like ?"));
	}
	
	@Test
	public void findUserInfo3() {
		String tname = "from UserInfo";
		String w1 = null;
		String w2 = null;
		db.findUserInfo(w1, w2, tname);
		SQLValue sqlValue = rule.getSQLValue();
		String sql = sqlValue.getSql();
		assertThat(sql, equalTo("select * from UserInfo  limit 3"));
	}

	@Test
	public void find() {
		int age = 19;
		String name = "Rex-Boos";
		db.find(age, name, new PageableImpl(1, 5));
		String sql = rule.getSQLValue().getSql();
		assertThat(sql, equalTo("select id,name,age from `userinfo` where age > ? limit 0,5"));
	}
	
	@Test
	public void find2() {
		int age = 19;
		String name = "Rex-Boos";
		db.find2(age, name, new PageableImpl(1, 5));
		String sql = rule.getSQLValue().getSql();
		assertThat(sql, equalTo("select id,name,age from `userinfo` where age > ? limit 0,5"));
	}
	
	@Test
	public void find3$1() {
		int age = 19;
		String name = "Rex-Boos";
		db.find3(age, name, new PageableImpl(1, 5));
		String sql = rule.getSQLValue().getSql();
		assertThat(sql, equalTo("select id,name,age from `userinfo` where age > ? and name like ? limit 0,5"));
	}
	
	@Test
	public void find3$2() {
		int age = 19;
		String name = "Boos";
		db.find3(age, name, new PageableImpl(1, 5));
		String sql = rule.getSQLValue().getSql();
		assertThat(sql, equalTo("select id,name,age from `userinfo` where age > ? limit 0,5"));
	}
	
	@Test
	public void find4$1() {
		int age = 19;
		String name = "Rex-Boos";
		db.find4(age, name, new PageableImpl(1, 5));
		String sql = rule.getSQLValue().getSql();
		assertThat(sql, equalTo("select id,name,age from `userinfo` where age > ? and name like ? limit 0,5"));
	}
	
	@Test
	public void find4$2() {
		int age = 19;
		String name = "Boos";
		db.find4(age, name, new PageableImpl(1, 5));
		String sql = rule.getSQLValue().getSql();
		assertThat(sql, equalTo("select id,name,age from `userinfo` where age > ? and name = ? limit 0,5"));
	}
}
