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
		assertThat(u2, equalTo(u));
		assertThat(u2.getId(), equalTo(u.getId()));
		assertThat(u2.getName(), equalTo(u.getName()));
		assertThat(u2.getAge(), equalTo(u.getAge()));
		
		
	}
	
	@Test
	public void executeBatch() {
		// 参考: http://mxm910821.iteye.com/blog/1701822
		studentDBService.executeBatch("update.sql", "sqlout.log");
	}
	
}
