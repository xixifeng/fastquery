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

import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static org.hamcrest.Matchers.*;

import org.fastquery.bean.UserInfo;
import org.fastquery.dao2.DefaultDBService;
import org.fastquery.page.Page;
import org.fastquery.page.Pageable;
import org.fastquery.page.PageableImpl;
import org.fastquery.service.FQuery;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 测试QueryRepository中的默认方法
 * 
 * @author mei.sir@aliyun.cn
 */
public class DefaultMethodTest extends FastQueryTest {

	private DefaultDBService db = FQuery.getRepository(DefaultDBService.class);

	private static final Logger LOG = LoggerFactory.getLogger(DefaultMethodTest.class);

	@Test
	public void dbNoneNull() {
		assertThat(db, notNullValue());
	}

	@Test
	public void saveReturnEntity() {
		UserInfo u1 = new UserInfo("婤姶", 2558);
		UserInfo u2 = db.save(u1);
		LOG.debug("{}", u2);
		long id = u2.getId();
		int effect = db.delete("UserInfo", "id", id);
		assertThat(effect, is(1));
	}

	@Test
	public void updateReturnEntity() {
		String name = "海猫" + UUID.randomUUID().toString().substring(0, 3);
		Integer age = Math.abs(new Random().nextInt(30));
		UserInfo u1 = new UserInfo(1, name, age);
		UserInfo u2 = db.update(u1);
		assertThat(u2.getId(), is(u1.getId()));
		assertThat(u2.getName(), is(name));
		assertThat(u2.getAge(), is(age));
	}

	@Test
	public void saveOrUpdateReturnEntity1() {
		String name = "海猫" + UUID.randomUUID().toString().substring(0, 3);
		Integer age = Math.abs(new Random().nextInt(30));
		UserInfo u1 = new UserInfo(1, name, age);
		UserInfo u2 = db.saveOrUpdate(u1);
		assertThat(u2.getId(), is(u1.getId()));
		assertThat(u2.getName(), is(name));
		assertThat(u2.getAge(), is(age));
	}

	@Test
	public void saveOrUpdateReturnEntity2() {
		Integer id = Math.abs(new Random().nextInt(30)) + 1000;
		String name = "海猫" + UUID.randomUUID().toString().substring(0, 3);
		Integer age = Math.abs(new Random().nextInt(30));
		UserInfo u1 = new UserInfo(id, name, age);
		UserInfo u2 = db.saveOrUpdate(u1);
		assertThat(u2.getId(), is(id));
		assertThat(u2.getName(), is(name));
		assertThat(u2.getAge(), is(age));

		int effect = db.delete("UserInfo", "id", id);
		assertThat(effect, is(1));
	}

	@Test
	public void findPage1() {
		Pageable pageable = new PageableImpl(1, 3);
		Integer id = 500;
		Integer age = 18;
		Page<Map<String, Object>> page = db.findPage(id, age, pageable, m -> {
			m.setQuery("select id,name,age from `userinfo`");
			m.setWhere("where id < ?1 and age > :age");// 引用问号表达式(?expression) , 冒号表达式(:expression)
			m.setCountQuery("select count(`id`) from `userinfo`");
		});

		assertThat(page.isFirst(), is(true));
		assertThat(page.getSize(), is(3));

		List<Map<String, Object>> list = page.getContent();
		list.forEach(m -> m.forEach((k, v) -> {
			if ("id".equals(k)) {
				assertThat((Integer) v, lessThan(id));
			} else if ("age".equals(k)) {
				assertThat((Integer) v, greaterThan(age));
			}
		}));
	}

	// 测试 函数式什么都不干
	@Test
	public void findPage2() {
		Pageable pageable = new PageableImpl(1, 3);
		Integer id = 500;
		Integer age = 18;
		try {
			db.findPage(id, age, pageable, m -> {
			});
		} catch (Exception e) {
			String msg = e.getMessage();
			assertThat(msg, containsString("query 语句必须设置正确"));
		}
	}

	// 测试给 query 设置 null
	@Test
	public void findPage3() {
		Pageable pageable = new PageableImpl(1, 3);
		Integer id = 500;
		Integer age = 18;
		try {
			db.findPage(id, age, pageable, m -> m.setQuery(null));
		} catch (Exception e) {
			String msg = e.getMessage();
			assertThat(msg, containsString("query 语句必须设置正确"));
		}
	}

	@Test
	public void findPage4() {
		Pageable pageable = new PageableImpl(1, 3);
		Integer id = 500;
		Integer age = 18;
		try {
			db.findPage(id, age, pageable, m -> {
				m.setQuery("select id,name,age from `userinfo`");
				m.setCountQuery("");
			});
		} catch (Exception e) {
			String msg = e.getMessage();
			assertThat(msg, containsString("query 语句必须设置正确"));
		}
	}
}
