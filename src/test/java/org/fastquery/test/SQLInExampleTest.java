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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.fastquery.bean.Student;
import org.fastquery.bean.UserInfo;
import org.fastquery.dao.SQLInExample;
import org.fastquery.service.FQuery;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class SQLInExampleTest {

	private SQLInExample sqlInExample = FQuery.getRepository(SQLInExample.class);
	@Test
	public void testFindByNameIn1() {
		String name = "袁承志";
		List<UserInfo> userinfos = sqlInExample.findByNameIn(name);
		if(userinfos.isEmpty()) 
			return;
		for (UserInfo u : userinfos) {
			assertThat(u.getName(), equalTo(name));
		}
	}
	
	@Test
	public void testFindByNameIn2() {
		String name1 = "袁承志";
		String name2 = "安小惠";
		List<UserInfo> userinfos = sqlInExample.findByNameIn(name1,name2);
		if(userinfos.isEmpty()) 
			return;
		for (UserInfo u : userinfos) {
			assertThat(u.getName().equals(name1) || u.getName().equals(name2), is(true));
		}
	}
	
	@Test
	public void findByNameListIn(){
		String name1 = "袁承志";
		String name2 = "安小惠";
		String name3 = "小青小青86545lk";
		Integer id = 2398;
		List<String> names = new ArrayList<>();
		names.add(name1);
		names.add(name2);
		names.add(name3);
		List<UserInfo> userinfos = sqlInExample.findByNameListIn(names,id);
		userinfos.forEach(u -> {
			assertThat(u.getName().equals(name1) || u.getName().equals(name2), is(true));
			assertThat(u.getName(),not(is(name3)));
			assertThat(u.getId().intValue(), greaterThan(id));
		});
	}
	
	@Test
	public void findByIn(){
		String sex = "女";
		Integer age = 10;
		String name1 = "小蚂蚁";
		String name2 = "蜘蛛";
		String name3 = "钱大平";
		Set<String> names = new HashSet<>();
		names.add(name1);
		names.add(name2);
		names.add(name3);
		
		List<Student> students = sqlInExample.findByIn(sex, age, names);
		assertThat(students.isEmpty(), is(false));
		students.forEach(s -> {
			assertThat(s.getName().equals(name1) || s.getName().equals(name2) || s.getName().equals(name3), is(true));
			assertThat(s.getAge(), greaterThan(10));
			assertThat(s.getSex(), is(sex));
		});
	}

}
