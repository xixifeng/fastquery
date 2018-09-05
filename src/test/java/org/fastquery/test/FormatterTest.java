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

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;

import org.fastquery.dao2.UserInfoDBService3;
import org.fastquery.service.FQuery;
import org.fastquery.struct.SQLValue;
import org.junit.Rule;
import org.junit.Test;

/**
 * 
 * @author mei.sir@aliyun.cn
 */
public class FormatterTest extends FastQueryTest {
	
	private UserInfoDBService3 db = FQuery.getRepository(UserInfoDBService3.class);
	
	@Rule
	public FastQueryTestRule rule = new FastQueryTestRule();
	
	@Test
	public void findByName1() {
		String name = "张三";
		db.findByName1(name);
		testSQL(name);
	}
	
	@Test
	public void findByName2() {
		String name = "张三";
		db.findByName2(name);
		testSQL(name);
	}
	
	@Test
	public void findByName3() {
		String name = "张三";
		db.findByName3(name);
		SQLValue sv = rule.getSQLValue();
		assertThat(sv.getSql(), equalTo("select name from UserInfo where name like ? limit 1"));
		assertThat(sv.getValues().get(0), equalTo("%:name%"));
	}
	
	@Test
	public void findByName4() {
		String name = "张三";
		db.findByName4(name);
		SQLValue sv = rule.getSQLValue();
		assertThat(sv.getSql(), equalTo("select name from UserInfo where name like ? limit 1"));
		assertThat(sv.getValues().get(0), equalTo(":name"));
	}
	
	@Test
	public void findByName5() {
		String name = null;
		db.findByName5(name);
		SQLValue sv = rule.getSQLValue();
		assertThat(sv.getSql(), equalTo("select name from UserInfo where name like ? limit 1"));
		assertThat(sv.getValues().get(0), equalTo("%谷子%"));
	}

	private void testSQL(String name) {
		SQLValue sv = rule.getSQLValue();
		assertThat(sv.getSql(), equalTo("select name from UserInfo where name like ? limit 1"));
		assertThat(sv.getValues().get(0), equalTo("%"+name+"%"));
	}
}
