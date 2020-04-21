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

import java.util.Random;
import java.util.UUID;

import static org.hamcrest.Matchers.*;

import org.fastquery.bean.UserInfo;
import org.fastquery.dao2.DefaultDBService;
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

	private final DefaultDBService db = FQuery.getRepository(DefaultDBService.class);

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
	public void sqlFun() {
		String str = db.sqlFun("select hex(255)");
		assertThat(str, equalTo("FF"));
		
		str = db.sqlFun("select concat('A1','B2','C3')");
		assertThat(str, equalTo("A1B2C3"));
		
		str = db.sqlFun("select concat_ws('-','A1','B2','C3')");
		assertThat(str, equalTo("A1-B2-C3"));
	}

	@Test
	public void count(){
		UserInfo u = new UserInfo(16,"986545457",32);
		long count = db.count(u);
		assertThat(count,is(0L));

		u = new UserInfo(16,null,32);
		count = db.count(u);
		assertThat(count,is(0L));

		u = new UserInfo(null,null,36);
		count = db.count(u);
		assertThat(count,is(1L));

		u = new UserInfo(null,null,82);
		count = db.count(u);
		assertThat(count,is(1L));
	}
}
