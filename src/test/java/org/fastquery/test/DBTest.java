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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 
 * @author mei.sir@aliyun.cn
 */
public class DBTest extends FastQueryTest {

	private static final Logger LOG = LoggerFactory.getLogger(DBTest.class);

	private final StudentDBService db = FQuery.getRepository(StudentDBService.class);

	// 调用: DB.modify
	@SuppressWarnings("unchecked")
	private static List<RespUpdate> modify(List<SQLValue> sqlValues, boolean hasPK) throws Exception {
		Method method = DB.class.getDeclaredMethod("modify", List.class,boolean.class);
		method.setAccessible(true);
		return (List<RespUpdate>) method.invoke(null, sqlValues,hasPK);
	}
	
	@Rule
	public FastQueryTestRule rule = new FastQueryTestRule();

	public List<RespUpdate> delete(Object... obs) throws Exception {
		List<Object> objs = Arrays.asList(obs);

		List<SQLValue> sqlValues = new ArrayList<>();

		sqlValues.add(new SQLValue("DELETE FROM `userinfo` WHERE id = ?", objs));

		return modify(sqlValues, true);
	}

	public void update() throws Exception {
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

		List<RespUpdate> rus = modify(sqlValues, true);
		assertThat(rus.size(), is(11));
		rus.forEach(ru -> {
			assertThat(ru.getEffect(), greaterThanOrEqualTo(1));
			assertThat(ru.getPk(), greaterThanOrEqualTo(1L));
			LOG.debug("正在删除:" + ru.getPk());
			List<RespUpdate> rusx;
			try {
				rusx = delete(ru.getPk());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			rusx.forEach(r -> {
				assertThat(r.getEffect(), greaterThanOrEqualTo(1));
				assertThat(r.getPk(), nullValue());
			});
			assertThat(rusx.size(), is(1));
		});
	}

	@Test
	public void db() throws Exception {
		db.db();
		assertThat(QueryContextHelper.getQueryContext(), notNullValue());
		update();
		assertThat(rule.getListSQLValue(), nullValue());
	}

	// 测试 db 中的parserSQLFile 方法
	@SuppressWarnings("unchecked")
	private static Stream<String> parserSQLFile(String name) throws Exception {
		Class<DB> clazz = DB.class;
		Method method = clazz.getDeclaredMethod("parserSQLFile", String.class);
		method.setAccessible(true);
		return (Stream<String>) method.invoke(null, name);
	}

	@Test
	public void parserSQLFile() throws Exception {
		String name = System.getProperty("user.dir") + "/src/test/resources/testFiles/update.sql";
		Stream<String> stream = parserSQLFile(name);
		List<String> list = stream.collect(Collectors.toList());
		assertThat(list.get(0), equalTo("DELETE FROM `product` WHERE `pid` = 1 and `lid` = 2"));
		assertThat(list.get(1), equalTo("DELETE FROM `product` WHERE `pid` = 2 and `lid` = 1"));
		assertThat(list.get(2), equalTo("DELETE FROM `product` WHERE `pid` = 1 and `lid` = 3"));
		assertThat(list.get(3), equalTo("INSERT INTO `product`(`pid`, `lid`, `pname`, `description`) VALUES (1,2,'電風扇','效果很好')"));
		assertThat(list.get(4), equalTo("INSERT INTO `product`(`pid`, `lid`, `pname`, `description`) VALUES (2,1,'杯子','喝茶使用')"));
		assertThat(list.get(5), equalTo("INSERT INTO `product`(`pid`, `lid`, `pname`, `description`) VALUES (1,3,'電腦','寫代碼')"));
		assertThat(list.get(6), equalTo("DELETE FROM `product` WHERE `pid` = 1 and `lid` = 2"));
		assertThat(list.get(7), equalTo("DELETE FROM `product` WHERE `pid` = 2 and `lid` = 1"));
		assertThat(list.get(8), equalTo("DELETE FROM `product` WHERE `pid` = 1 and `lid` = 3"));
		assertThat(list.get(9), equalTo("update `UserInfo` set `name` = case `id` when 77 then '茝若' when 88 then '芸兮' when 99 then '梓' else `name` end, `age` = case `id` when 77 then '18' when 99 then '16' else `age` end where `id` in(77,88,99,66)"));
		
		name = System.getProperty("user.dir") + "/src/test/resources/testFiles/create.sql";
		stream = parserSQLFile(name);
		list = stream.collect(Collectors.toList());
		assertThat(list.size(), is(3));
		assertThat(list.get(0), equalTo("drop table if exists demo_table"));
		assertThat(list.get(1), equalTo("CREATE TABLE demo_table ( id int primary key auto_increment NOT NULL )"));
		assertThat(list.get(2), equalTo("DROP TABLE demo_table"));
	}
}
