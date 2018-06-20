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
import org.fastquery.bean.UserInfo;
import org.fastquery.bean.UserInformation;
import org.fastquery.core.RepositoryException;
import org.fastquery.dao.UserInfoDBService;
import org.fastquery.filter.SkipFilter;
import org.fastquery.page.Page;
import org.fastquery.page.PageableImpl;
import org.fastquery.page.Slice;
import org.fastquery.service.FQuery;
import org.fastquery.struct.SQLValue;
import org.junit.Rule;
import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.*;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class UserInfoDBServiceTest {

	private static final Logger LOG = LoggerFactory.getLogger(UserInfoDBServiceTest.class);

	private UserInfoDBService userInfoDBService = FQuery.getRepository(UserInfoDBService.class);

	@Rule
	public FastQueryTestRule rule = new FastQueryTestRule();

	@Test
	public void findById() {
		Integer id = 1;
		String sql = "select id,name,age from UserInfo where id = ?2";
		UserInfo userInfo = userInfoDBService.findById(sql, id);
		assertThat(userInfo.getId(), is(id));

		SQLValue sqlValue = rule.getSQLValue();
		assertThat(sqlValue.getSql(), equalTo("select id,name,age from UserInfo where id = ?"));
		List<Object> values = sqlValue.getValues();
		assertThat(values.size(), is(1));
		Object arg = values.get(0);
		assertThat((arg instanceof Integer), is(true));
		assertThat(arg, is(id));

		sql = "select id,name,age from UserInfo where id = :id";
		userInfo = userInfoDBService.findById(sql, id);
		assertThat(userInfo.getId(), is(id));
		sqlValue = rule.getSQLValue();
		assertThat(sqlValue.getSql(), equalTo("select id,name,age from UserInfo where id = ?"));
		values = sqlValue.getValues();
		assertThat(values.size(), is(1));
		arg = values.get(0);
		assertThat((arg instanceof Integer), is(true));
		assertThat(arg, is(id));
	}

	@Test
	public void findById2() {
		int id = 35;
		UserInfo userInfo = userInfoDBService.findById(id); // 没有找到
															// userInfo会返回null
		if (userInfo != null) {
			assertThat(userInfo.getAge(), nullValue());
		}

		SQLValue sqlValue = rule.getSQLValue();
		assertThat(sqlValue.getSql(), equalTo("select id,name,age from UserInfo where id = ?"));
		List<Object> values = sqlValue.getValues();
		assertThat(values.size(), is(1));
		Object arg = values.get(0);
		assertThat((arg instanceof Integer), is(true));
		assertThat(arg, is(id));
	}

	@Test
	public void findUserInfoByNameOrAge() {
		String name = "王五";
		Integer age = 8;
		UserInfo[] userInfos = userInfoDBService.findUserInfoByNameOrAge(name, age);
		for (UserInfo userInfo : userInfos) {
			assertThat((userInfo.getName().equals(name) || userInfo.getAge().intValue() == age), is(true));
		}
		SQLValue sqlValue = rule.getSQLValue();
		assertThat(sqlValue.getSql(), equalTo("select name,age from UserInfo u where u.name=? or u.age=?"));
		List<Object> values = sqlValue.getValues();
		assertThat(values.size(), is(2));
		Object arg = values.get(0);
		assertThat((arg instanceof String), is(true));
		assertThat(arg, is(name));
		arg = values.get(1);
		assertThat((arg instanceof Integer), is(true));
		assertThat(arg, is(age));
	}

	@Test
	public void findUserInfoByIds() {
		JSONArray json = userInfoDBService.findUserInfoByIds("2,3,4");
		assertThat(JSONObject.parseObject(JSON.toJSONString(json.get(0))).getIntValue("id"), equalTo(2));
		assertThat(JSONObject.parseObject(JSON.toJSONString(json.get(1))).getIntValue("id"), equalTo(3));
		assertThat(JSONObject.parseObject(JSON.toJSONString(json.get(2))).getIntValue("id"), equalTo(4));
	}

	@Test
	public void findUserInfo() {
		String orderby = "order by age desc";
		int i = 1;
		userInfoDBService.findUserInfo(orderby, i);
	}

	@Test
	public void findUserInfoById() {
		UserInformation userInformation = userInfoDBService.findUserInfoById(1);
		LOG.debug(userInformation.toString());
	}

	@Test
	public void testFindUserInfoByAge() {
		int age = 20;
		JSONArray jsonArray = userInfoDBService.findUserInfoByAge(age);
		for (int i = 0; i < jsonArray.size(); i++) {
			JSONObject jsonObject = jsonArray.getJSONObject(i);
			assertThat(jsonObject.getInteger("age"), greaterThan(age));
		}
	}

	@Test
	public void findSome() {
		List<UserInfo> userInfos = userInfoDBService.findSome(30);
		userInfos.forEach(userInfo -> {
			assertThat(userInfo.getId(), greaterThan(30));
		});
	}

	@Test
	public void testFindUserInfoByAge2() {
		int age = 1000;
		// 数据库中age没有大于1千的记录
		// 断言: 查询返回的值应该是一个空对象,不是null.
		JSONArray jsonArray = userInfoDBService.findUserInfoByAge(age);
		assertThat(jsonArray, notNullValue());
		assertThat(jsonArray.isEmpty(), is(true));
	}

	@Test
	public void findOne() {
		int age = 1000;
		// 数据库中age没有大于1千的记录
		// 断言: 查询返回的值应该是一个空对象,不是null.
		Map<String, Object> map = userInfoDBService.findOne(age, "xk-c3p0");
		assertThat(map, notNullValue());
		assertThat(map.isEmpty(), is(true));
	}

	@Test
	public void testUpdateBatch() {
		int effect = userInfoDBService.updateBatch("小张张", 26, 1);
		assertThat("断言该行修改操作一共影响了3行", effect, equalTo(3));
	}

	@Test
	public void update1() {
		boolean b = userInfoDBService.update(1);
		assertThat(b, is(true));

		b = userInfoDBService.update(-10);
		assertThat(b, is(true));
	}

	@Test
	public void update2() {
		boolean b = userInfoDBService.update2(1);
		assertThat(b, is(true));
	}

	// 断言: 它会抛出RepositoryException异常
	@Test(expected = RepositoryException.class)
	public void testUpdateBatch2_a() {
		int effect = userInfoDBService.updateBatch2("小不点", 6, 2);
		// updateBatch2 中途会报错,因此修改影响的行数为0
		assertThat(effect, equalTo(0));
	}

	@Test(expected = RepositoryException.class)
	public void testUpdateBatch2_b() {
		int effect = userInfoDBService.updateBatch2("小不点", 6, 2);
		assertThat(effect, equalTo(0));
	}

	@Test
	public void testUpdateBatch3() {
		int[] effects = userInfoDBService.updateBatch3("清风习习", 23, 3);
		assertThat(effects.length, is(2));
		assertThat(effects[0], is(1));
		assertThat(effects[1], is(1));
	}

	@Test
	public void findAll() {

		int p = 1;
		int size = 6;
		Page<Map<String, Object>> page = userInfoDBService.findAll(new PageableImpl(p, size));
		assertThat(String.format("断言: 当前是第%s页", p), page.getNumber(), is(p));
		assertThat(page.getNumberOfElements(), lessThanOrEqualTo(size));

		// 打印出来看看
		String str = JSON.toJSONString(page, true);
		LOG.debug(str);

	}

	@Test
	@SkipFilter
	public void find() {
		Page<UserInfo> page = userInfoDBService.find(100, 50, new PageableImpl(1, 3));
		List<UserInfo> userInfos = page.getContent();
		if (page.isHasContent()) {
			userInfos.forEach(u -> LOG.debug(u.toString()));
		}
		assertThat(page.isFirst(), is(true));

		String str = JSON.toJSONString(page, true);

		LOG.debug(str);
	}

	@Test
	public void findSome1() {
		Page<UserInfo> page = userInfoDBService.findSome1(1, 100, new PageableImpl(1, 15));
		assertThat(page, notNullValue());
	}

	@Test
	public void findSome2() {

		// pageIndex:5 pageSize:24
		// pageIndex:1 pageSize:99 totalPages:1
		// pageIndex:3 pageSize:49 totalPages:3
		// pageIndex:4 pageSize:49 totalPages:3

		int pageIndex = 3;
		int pageSize = 5;

		int age = 1;
		int id = 100;
		// 总行数
		long totalElements = userInfoDBService.count(age, id);
		for (int i = 1; i <= totalElements; i++) {
			pageIndex = i % 10;
			pageSize = new Random().nextInt(1000) % (int) 10;

			if (pageSize * pageIndex > totalElements) { // 显然是错的
				pageSize = 1;
			}

			if (pageSize == 0) {
				pageSize += 1;
			}

			int totalPages; // 总页码
			totalPages = (int) totalElements / pageSize;
			if (totalElements % pageSize != 0) {
				totalPages += 1;
			}
			if (pageIndex > totalPages) {
				pageIndex = new Random().nextInt(1000) % totalPages;
			}
			if (pageIndex == 0) {
				pageIndex += 1;
			}
			LOG.debug("totalElements:" + totalElements + " pageIndex:" + pageIndex + "  pageSize:" + pageSize + "  totalPages:" + totalPages);

			Page<Map<String, Object>> page = userInfoDBService.findSome2(age, id, pageIndex, pageSize);
			assertThat(page, notNullValue());
			assertThat(page.getNumber(), equalTo(pageIndex));
			assertThat(page.getSize(), equalTo(pageSize));
			assertThat(page.getTotalElements(), equalTo(-1L));
			assertThat(page.getTotalPages(), equalTo(-1));
			assertThat(page.getNumberOfElements(), lessThanOrEqualTo(pageSize));
			// LOG.debug(JSON.toJSONString(page, true));

			boolean hasContent = pageIndex <= totalPages; // 是否有结果集
			boolean isFirst = pageIndex == 1; // 是否是第一页
			boolean isLast = pageIndex == totalPages; // 是否是最后一页
			boolean hasNext = pageIndex < totalPages; // 是否有下一页
			boolean hasPrevious = (pageIndex > 1) && hasContent; // 是否有上一页
			Slice previousPageable = new Slice((!isFirst) ? (pageIndex - 1) : pageIndex, pageSize); // 上一页的Pageable对象
			Slice nextPageable = new Slice((!isLast) ? (pageIndex + 1) : pageIndex, pageSize); // 下一页的Pageable对象

			assertThat(page.isHasContent(), is(hasContent));
			assertThat(page.isFirst(), is(isFirst));
			assertThat(page.isLast(), is(isLast));
			assertThat(page.isHasNext(), is(hasNext));
			assertThat(page.isHasPrevious(), is(hasPrevious));
			assertThat(page.getPreviousPageable().getNumber(), is(previousPageable.getNumber()));
			assertThat(page.getPreviousPageable().getSize(), is(previousPageable.getSize()));
			assertThat(page.getNextPageable().getNumber(), is(nextPageable.getNumber()));
			assertThat(page.getNextPageable().getSize(), is(nextPageable.getSize()));

		}
	}

	@Test
	public void countDouble() {
		Double d = userInfoDBService.countDouble(2100, 2308);
		if (d != null) {
			LOG.debug(d.toString());
		}
	}

	@Test
	public void findByIds() {
		int[] ids = new int[] { 1, 2, 3 };
		UserInfo[] userInfos = userInfoDBService.findByIds(ids);
		assertThat(userInfos[0].getId(), equalTo(1));
		assertThat(userInfos[1].getId(), equalTo(2));
		assertThat(userInfos[2].getId(), equalTo(3));
	}

	@Test
	public void insert() {
		Integer id = 1950;
		String name = "香月儿";
		Integer age = 23;

		while (userInfoDBService.findById(id) != null) { // 该主键已经存在,直到该主键不存在时,才会结束循环
			id += 1;
		}
		UserInfo u = userInfoDBService.insert(id, name, age);
		assertThat(u.getId(), equalTo(id));
		assertThat(u.getName(), equalTo(name));
		assertThat(u.getAge(), equalTo(age));

	}

	@Test
	public void updateNameById() {
		int id = 2;
		int i = userInfoDBService.updateNameById("'戚继光'", 2);
		assertThat(i, is(1));
		UserInfo u = userInfoDBService.findById(id);
		assertThat(u.getId(), equalTo(id));
		assertThat(u.getName(), equalTo("戚继光"));
	}

	@Test
	public void updateAgeById() {
		UserInfo userInfo = userInfoDBService.updateAgeById(null, 3);
		assertThat(userInfo.getId(), is(3));
		assertThat(userInfo.getAge(), nullValue());
		userInfo = userInfoDBService.updateAgeById(21, 3);
		assertThat(userInfo.getId(), is(3));
		assertThat(userInfo.getAge(), is(21));
	}

	@Test
	public void updateAge() {
		JSONObject userInfo = userInfoDBService.updateAge(null, 3);
		assertThat(userInfo.getIntValue("id"), is(3));
		// 断言:包含有age属性 (age即使是null)
		assertThat(JSON.toJSONString(userInfo, SerializerFeature.WriteMapNullValue).indexOf("\"age\"") != -1, is(true));
	}

	@Test
	public void findAge() {
		Integer age = userInfoDBService.findAge(35);
		assertThat(age, nullValue());
	}

	@Test
	public void findUserInfoByNullAge() {
		// 查询age为null的UserInfo
		List<UserInfo> us = userInfoDBService.findUserInfoByNullAge(null);
		assertThat(us.size(), greaterThanOrEqualTo(1));
		us.forEach(u -> assertThat(u.getAge(), nullValue()));
	}

	@Test
	public void findNames() {
		String[] names = userInfoDBService.findNames();
		assertThat(names.length, is(3));
		for (String name : names) {
			assertThat(Pattern.matches("\\{\".+\":\".+\"\\}", name), is(false));
			LOG.debug(name);
		}
	}

	@Test
	public void findAges1() {
		String[] ages = userInfoDBService.findAges();
		assertThat(ages.length, is(3));
		for (String age : ages) {
			if (age != null) {
				assertThat(Pattern.matches("\\{\".+\":\".+\"\\}", age), is(false));
			}
		}
	}

	@Test
	public void findAges2() {
		Integer[] ages = userInfoDBService.findAges2();
		assertThat(ages.length, is(3));
		for (Integer age : ages) {
			assertThat(age, notNullValue());
		}
	}

	@Test
	public void findUserSome2() {
		String name = null;
		Integer age = null;
		List<Map<String, Object>> maps = userInfoDBService.findUserSome2(age, name);
		maps.forEach(m -> {
			m.forEach((k, v) -> {
				if ("age".equals(k))
					assertThat(v, nullValue());
			});
		});

		SQLValue sqlValue = rule.getSQLValue();
		assertThat(sqlValue.getSql(), equalTo("select id,name,age from UserInfo where age is null"));
		List<Object> objects = sqlValue.getValues();
		assertThat(objects.isEmpty(), is(true));
	}

}
