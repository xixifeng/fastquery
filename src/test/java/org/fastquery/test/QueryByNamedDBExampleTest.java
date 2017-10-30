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

import org.apache.log4j.Logger;
import org.fastquery.bean.Student;
import org.fastquery.bean.UserInfo;
import org.fastquery.core.RepositoryException;
import org.fastquery.dao.QueryByNamedDBExample;
import org.fastquery.dao.UserInfoDBService;
import org.fastquery.page.Page;
import org.fastquery.page.PageableImpl;
import org.fastquery.service.FQuery;
import org.fastquery.struct.SQLValue;
import org.junit.Rule;
import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static org.hamcrest.Matchers.*;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class QueryByNamedDBExampleTest {

	private static final Logger LOG = Logger.getLogger(QueryByNamedDBExampleTest.class);

	@Rule
	public FastQueryTestRule rule = new FastQueryTestRule();

	private QueryByNamedDBExample db = FQuery.getRepository(QueryByNamedDBExample.class);
	private UserInfoDBService userInfoDBService = FQuery.getRepository(UserInfoDBService.class);

	@Test
	public void findUserInfoAll() {
		JSONArray jsonArray = db.findUserInfoAll();
		assertThat(jsonArray.isEmpty(), is(false));
	}

	@Test
	public void findUserInfoOne() {
		UserInfo userInfo = db.findUserInfoOne(1);
		assertThat(userInfo.getId().intValue(), is(1));
	}

	@Test
	public void findUserInfoByNameAndAge1() {
		String name = "张三";
		Integer age = null;
		JSONArray jsonArray = db.findUserInfoByNameAndAge(name, age);
		for (Object object : jsonArray) {
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>) object;
			Set<String> keys = map.keySet();
			for (String key : keys) {
				if ("name".equals(key)) {
					assertThat(map.get(key).toString().equals(name), is(true));
				}
			}
		}
	}

	@Test
	public void findUserInfoByNameAndAge2() {
		String name = null;
		Integer age = 8;
		JSONArray jsonArray = db.findUserInfoByNameAndAge(name, age);
		for (Object object : jsonArray) {
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>) object;
			Set<String> keys = map.keySet();
			for (String key : keys) {
				if ("age".equals(key)) {
					assertThat(map.get(key).toString().equals(age.toString()), is(true));
				}
			}
		}
	}

	@Test
	public void findUserInfoByNameAndAge3() {
		UserInfo userInfo = db.findUserInfoOne(1);
		String name = userInfo.getName();
		Integer age = userInfo.getAge();
		JSONArray jsonArray = db.findUserInfoByNameAndAge(name, age);
		for (Object object : jsonArray) {
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>) object;
			assertThat(map, notNullValue());
			assertThat(map.isEmpty(), is(false));
			Set<String> keys = map.keySet();
			for (String key : keys) {
				if ("age".equals(key)) {
					assertThat(map.get(key).toString().equals(age.toString()), is(true));
				}
				if ("name".equals(key)) {
					assertThat(map.get(key).toString().equals(name.toString()), is(true));
				}
			}
		}
	}

	@Test
	public void findPage() {

		int page = 1; // 第3页
		int size = 5; // 指定每页显示5条数据

		String no = "95";
		String name = null;
		Integer age = 8;

		Page<Student> pageObj = db.findPage(new PageableImpl(page, size), no, name, age);
		assertThat(pageObj.getContent().size(), lessThanOrEqualTo(size));
		if (pageObj.getContent().size() > 1) { // 如果当前页有数据,那么总页数肯定不为0l
			assertThat(pageObj.getTotalElements(), not(0l)); // 总记录数不为0l
																// 注意TotalElements是long类型,比较是最好保持类型一致!
		}
		LOG.debug(JSON.toJSONString(JSON.toJSON(pageObj), true));

		if (rule.isDebug()) {
			List<SQLValue> sqlValues = rule.getListSQLValue();
			assertThat(sqlValues.size(), is(2));
			// 分页主体语句
			SQLValue sv1 = sqlValues.get(0);

			assertThat(sv1.getSql(), equalToIgnoringWhiteSpace(
					"select no, name, sex from Student where no like ? or age > ? order by age desc limit 0,5"));
			assertThat(sv1.getSql(), equalTo(
					"select no, name, sex from Student where no like ? or age > ? order by age desc limit 0,5"));
			List<Object> ps1 = sv1.getValues();
			assertThat(ps1.size(), is(2));
			assertThat(ps1.get(0), instanceOf(String.class));
			assertThat(ps1.get(1), instanceOf(Integer.class));
			assertThat(ps1.get(0), is(no));
			assertThat(ps1.get(1), is(age));

			// 分页求和语句
			SQLValue sv2 = sqlValues.get(1);
			assertThat(sv2.getSql(),
					equalToIgnoringWhiteSpace("select count(no) from Student where no like ? or age > ?"));
			assertThat(sv2.getSql(), equalTo("select count(no) from Student where no like ? or age > ?"));
			List<Object> ps2 = sv1.getValues();
			assertThat(ps2.size(), is(2));
			assertThat(ps2.get(0), instanceOf(String.class));
			assertThat(ps2.get(1), instanceOf(Integer.class));
			assertThat(ps2.get(0), is(no));
			assertThat(ps2.get(1), is(age));
		}
	}
	
	
	@Test
	public void findPage2() {

		int page = 1; // 第3页
		int size = 5; // 指定每页显示5条数据

		String no = "95";
		String name = null;
		Integer age = 8;

		Page<Student> pageObj = db.findPage2(new PageableImpl(page, size), no, name, age);
		int len = pageObj.getContent().size();
		assertThat(len, lessThanOrEqualTo(size));
		if (len > 1) { // 如果当前页有数据,那么总页数肯定不为0l
			assertThat(pageObj.getTotalElements(), not(0l)); 
		}
		assertThat(pageObj.getTotalElements(), is(-1L));
		assertThat(pageObj.getTotalPages(), is(-1));
	}

	@Test
	public void updateUserInfoById() {
		int id = Math.abs(new Random().nextInt() % 3) + 1; // [1,3] 范围的随机数
		LOG.debug("正在修改主键为" + id + "的UserInfo.");
		String name = "清风习习" + new Random().nextInt(8);
		int age = new Random().nextInt(99);
		int effect = db.updateUserInfoById(id, name, age);
		assertThat(effect, is(1));

		// 把改后的数据查询出来进行断言是否改正确
		UserInfo userInfo = userInfoDBService.findById(id);
		assertThat(userInfo.getName(), equalTo(name));
		assertThat(userInfo.getAge().intValue(), is(age));

	}

	@Test
	public void findUserInfoByFuzzyName() {
		String name = "三";
		List<UserInfo> uis = db.findUserInfoByFuzzyName(name);
		assertThat(uis.size(), greaterThanOrEqualTo(2));
		uis.forEach(u -> assertThat(u.getName(), containsString(name)));

		if (rule.isDebug()) {
			SQLValue sqlValue = rule.getSQLValue();
			assertThat(sqlValue.getSql(), notNullValue());
			assertThat(sqlValue.getSql(), equalToIgnoringWhiteSpace("select * from UserInfo where name like ?"));
			assertThat(sqlValue.getSql().trim(), equalTo("select * from UserInfo where name like ?"));

			List<Object> values = sqlValue.getValues();
			assertThat(values, notNullValue());
			assertThat(values.get(0), equalTo("%" + name + "%"));
		}
	}
	
	@Test(expected=RepositoryException.class)
	public void findUserInfoByFuzzyName2() {
		db.findUserInfoByFuzzyName(null);
	}
	
	@Test(expected=RepositoryException.class)
	public void findUserInfoByFuzzyName3() {
		db.findUserInfoByFuzzyName("%");
	}
	
	@Test(expected=RepositoryException.class)
	public void findUserInfoByFuzzyName4() {
		db.findUserInfoByFuzzyName("");
	}
	
	@Test(expected=RepositoryException.class)
	public void findUserInfoByFuzzyName5() {
		db.findUserInfoByFuzzyName("%%");
	}
	
	@Test
	public void findUserInfo() {
		Integer id = null;
		String name = "J";
		Integer age = null;
		db.findUserInfo(id, name, age);
		if(rule.isDebug()) {
			SQLValue sqlValue = rule.getSQLValue();
			assertThat(sqlValue.getSql(), equalTo("select * from UserInfo where id > ? and age > 18 or name like ?"));
			List<Object> vals = sqlValue.getValues();
			assertThat(vals.size(), is(2));
			assertThat(vals.get(0), nullValue());
			assertThat(vals.get(1), equalTo("%"+name+"%"));
		}
	}
}
