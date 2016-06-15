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

import org.fastquery.dao2.UserInfoDBService3;
import org.fastquery.service.FQuery;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertThat;

import static org.hamcrest.Matchers.*;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class UserInfoDBServiceTest3 {
	
	private UserInfoDBService3 userInfoDBService;
	
	@Before
	public void before() throws ClassNotFoundException{
		userInfoDBService = FQuery.getRepository(UserInfoDBService3.class);
	}
	
	@Test
	public void testUpdateBatch() {
		
		// 修改 xk3 里的数据 (xk3是用jdbc连接的,未使用到连接池)
		int effect = userInfoDBService.updateBatch("大张张", 66, 1,"xk3");
		assertThat("断言该行修改操作一共影响了3行",effect, equalTo(3));
	}
	
	
}












