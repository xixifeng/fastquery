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

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.fastquery.core.DB;
import org.fastquery.example.StudentDBService;
import org.fastquery.service.FQuery;
import org.fastquery.struct.RespUpdate;
import org.fastquery.struct.SQLValue;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 
 * @author mei.sir@aliyun.cn
 */
public class DBTest {

	private static final Logger LOG = LoggerFactory.getLogger(DBTest.class);

	private StudentDBService db = FQuery.getRepository(StudentDBService.class);

	@Rule
	public FastQueryTestRule rule = new FastQueryTestRule();

	public List<RespUpdate> delete(Object... obs) {
		List<Object> objs = Arrays.asList(obs);

		List<SQLValue> sqlValues = new ArrayList<>();

		sqlValues.add(new SQLValue("DELETE FROM `userinfo` WHERE id = ?", objs));

		return DB.modify(sqlValues, true, true);
	}

	public void update() {
		List<SQLValue> sqlValues = new ArrayList<>();
		sqlValues.add(new SQLValue("INSERT INTO `userinfo`(`name`, `age`) VALUES (?,?)", Arrays.asList("回家孩子1", 21)));
		sqlValues.add(new SQLValue("INSERT INTO `userinfo`(`name`, `age`) VALUES (?,?)", Arrays.asList("回家孩子2", 22)));
		sqlValues.add(new SQLValue("INSERT INTO `userinfo`(`name`, `age`) VALUES (?,?)", Arrays.asList("回家孩子3", 23)));
		sqlValues.add(new SQLValue("INSERT INTO `userinfo`(`name`, `age`) VALUES (?,?)", Arrays.asList("回家孩子4", 23)));
		sqlValues.add(new SQLValue("INSERT INTO `userinfo`(`name`, `age`) VALUES (?,?)", Arrays.asList("回家孩子5", 24)));
		sqlValues.add(new SQLValue("INSERT INTO `userinfo`(`name`, `age`) VALUES (?,?)", Arrays.asList("回家孩子6", 26)));
		sqlValues.add(new SQLValue("INSERT INTO `userinfo`(`name`, `age`) VALUES (?,?)", Arrays.asList("回家孩子7", 27)));
		sqlValues.add(new SQLValue("INSERT INTO `userinfo`(`name`, `age`) VALUES (?,?)", Arrays.asList("回家孩子8", 28)));
		sqlValues.add(new SQLValue("INSERT INTO `userinfo`(`name`, `age`) VALUES (?,?)", Arrays.asList("回家孩子9", 29)));
		sqlValues.add(new SQLValue("INSERT INTO `userinfo`(`name`, `age`) VALUES (?,?)", Arrays.asList("回家孩子10", 30)));
		sqlValues.add(new SQLValue("INSERT INTO `userinfo`(`name`, `age`) VALUES (?,?)", Arrays.asList("回家孩子11", 31)));

		List<RespUpdate> rus = DB.modify(sqlValues, true, true);
		assertThat(rus.size(), is(11));
		rus.forEach(ru -> {
			assertThat(ru.getEffect(), greaterThanOrEqualTo(1));
			assertThat(ru.getPk(), greaterThanOrEqualTo(1L));
			LOG.debug("正在删除:" + ru.getPk());
			List<RespUpdate> rusx = delete(ru.getPk());
			rusx.forEach(r -> {
				assertThat(r.getEffect(), greaterThanOrEqualTo(1));
				assertThat(r.getPk(), nullValue());
			});
			assertThat(rusx.size(), is(1));
		});
	}

	@Test
	public void db() {
		db.db();
		if (rule.isDebug()) {
			assertThat(QueryContextHelper.getQueryContext(), notNullValue());
			update();
			assertThat(rule.getSQLValue(), nullValue());
			assertThat(rule.getListSQLValue(), nullValue());
		} else {
			assertThat(QueryContextHelper.getQueryContext(), nullValue());
		}
	}
}
