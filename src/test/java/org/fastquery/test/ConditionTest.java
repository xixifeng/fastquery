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

import org.fastquery.dao.ConditionDBService;
import org.fastquery.service.FQuery;
import org.fastquery.struct.SQLValue;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * 
 * @author mei.sir@aliyun.cn
 */
public class ConditionTest {

	private ConditionDBService db = FQuery.getRepository(ConditionDBService.class);

	@Rule
	public FastQueryTestRule rule = new FastQueryTestRule();
	
	@BeforeClass
	public static void beforeClass() {
		System.setProperty("fastquery.config.dir", "/mywork/workspace-2018/fastquery/conf");
	}

	@Test
	public void findUserInfo() {
		String tname = "from UserInfo";
		String w1 = "name like ?1";
		String w2 = "and age > ?2";
		db.findUserInfo(w1, w2, tname);
		SQLValue sqlValue = rule.getSQLValue();
		String sql = sqlValue.getSql();
		assertThat(sql, equalTo("select * from UserInfo where  name like ?  and age > ?"));
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

}
