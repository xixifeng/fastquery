/*
 * Copyright (c) 2016-2017, fastquery.org and/or its affiliates. All rights reserved.
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

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import org.fastquery.bean.UserInfo;
import org.fastquery.dao.UserInfoDBService;
import org.fastquery.page.Page;
import org.fastquery.page.PageableImpl;
import org.fastquery.service.FQuery;
import org.junit.Test;

import static org.hamcrest.Matchers.*;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class PageTest {

	private UserInfoDBService userInfoDBService = FQuery.getRepository(UserInfoDBService.class);

	@Test
	public void findSome1() {

		int pageIndex = 0;
		int size = 0;
		Page<UserInfo> page = userInfoDBService.findSome1(1, 100, new PageableImpl(pageIndex, size));
		assertThat(page, notNullValue());
		assertThat(page.getNumber(), equalTo(1));
		assertThat(page.getSize(), equalTo(1));

	}
}
