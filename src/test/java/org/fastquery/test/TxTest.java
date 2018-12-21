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
import static org.junit.Assert.assertThat;

import org.fastquery.bean.UserInfo;
import org.fastquery.core.RepositoryException;
import org.fastquery.dao.UserInfoDBService;
import org.fastquery.example.StudentDBService;
import org.fastquery.service.FQuery;
import org.junit.Test;

/**
 * 
 * @author mei.sir@aliyun.cn
 */
public class TxTest extends FastQueryTest {
	
	private StudentDBService studentDBService = FQuery.getRepository(StudentDBService.class);
	private UserInfoDBService userInfoDBService = FQuery.getRepository(UserInfoDBService.class);
	
	@Test(expected = RepositoryException.class)
	public void updateTx1() {
		int effect = userInfoDBService.tx(()-> userInfoDBService.updateBatch2("小不点", 6, 2));
		assertThat(effect, equalTo(0));
	}
	
	private int u1(Integer id, String name, Integer age) {
		UserInfo userInfo = new UserInfo(id, name, age);
		int effect = studentDBService.executeSaveOrUpdate(userInfo);
		assertThat(effect, is(1));
		UserInfo u1 = userInfoDBService.findById(id);
		assertThat(u1.getId(),equalTo(id));
		assertThat(u1.getName(),equalTo(name));
		assertThat(u1.getAge(),equalTo(age));
		return effect;
	}
	
	@Test(expected = RepositoryException.class)
	public void updateTx2() {
		int id = 100;
		String name = "hikey";
		int age = 13;
		
		try {
			userInfoDBService.tx(()-> {
				u1(id, name, age);
				u1(id, name, age);
				u1(id, name, age);
				throw new RepositoryException("Do...Do...");
			});
		} finally {
			UserInfo u1 = userInfoDBService.findById(id);
			assertThat(u1.getId(),equalTo(id));
			assertThat(u1.getName(),not(equalTo(name)));
			assertThat(u1.getAge(),not(equalTo(age)));
		}
	}
	
}
