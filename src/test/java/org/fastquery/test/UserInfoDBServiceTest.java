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

import org.fastquery.example.UserInfoDBService;
import org.fastquery.service.FQuery;
import org.junit.Before;
import org.junit.Test;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import static org.junit.Assert.assertThat;

import static org.hamcrest.Matchers.*;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class UserInfoDBServiceTest {

	private UserInfoDBService userInfoDBService;
	
	@Before
	public void before(){
		userInfoDBService = FQuery.getRepository(UserInfoDBService.class);
	}
	
	@Test
	public void testFindUserInfoByAge(){
		int age = 20;
		JSONArray jsonArray = userInfoDBService.findUserInfoByAge(age);
		for (int i = 0; i < jsonArray.size(); i++) {
			JSONObject jsonObject = jsonArray.getJSONObject(i);
			assertThat(jsonObject.getInteger("age"),greaterThan(age));
		}
	}
	
	@Test
	public void testUpdateBatch() {
		int effect = userInfoDBService.updateBatch("小张张", 26, 1);
		assertThat("断言该行修改操作一共影响了3行",effect, equalTo(3));
	}
	
	@Test
	public void testUpdateBatch2() {
		int effect = userInfoDBService.updateBatch2("小不点", 6, 2);
		// updateBatch2 中途会报错,因此修改影响的行数为0
		assertThat(effect, equalTo(0)); // 在不支持事务的前提下 
	}
	
	@Test
	public void testUpdateBatch3() {
		int[] effects = userInfoDBService.updateBatch3("白云", 66, 2);
		// updateBatch2 中途会报错,因此修改影响的行数为0
		for (int effect : effects) {
			assertThat(effect, greaterThanOrEqualTo(1));
		}
	}

	
}
