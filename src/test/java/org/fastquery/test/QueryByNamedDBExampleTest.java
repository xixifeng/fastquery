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

import org.apache.log4j.Logger;
import org.fastquery.bean.Student;
import org.fastquery.bean.UserInfo;
import org.fastquery.dao.QueryByNamedDBExample;
import org.fastquery.dao.UserInfoDBService;
import org.fastquery.page.Page;
import org.fastquery.page.PageableImpl;
import org.fastquery.service.FQuery;
import org.junit.Before;
import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

import static org.junit.Assert.assertThat;

import java.util.Map;
import java.util.Random;
import java.util.Set;

import static org.hamcrest.Matchers.*;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class QueryByNamedDBExampleTest {
	
	private static  final Logger LOG = Logger.getLogger(QueryByNamedDBExampleTest.class);
	
	QueryByNamedDBExample queryByNamedDBExample;
	UserInfoDBService userInfoDBService;
	@Before
	public void before(){
		queryByNamedDBExample = FQuery.getRepository(QueryByNamedDBExample.class);
		userInfoDBService = FQuery.getRepository(UserInfoDBService.class);
	}
	
	
	@Test
	public void findUserInfoAll(){
		JSONArray jsonArray = queryByNamedDBExample.findUserInfoAll();
		assertThat(jsonArray.isEmpty(), is(false));
	}
	
	@Test
	public void findUserInfoOne(){
		UserInfo userInfo = queryByNamedDBExample.findUserInfoOne(1);
		assertThat(userInfo.getId().intValue(), is(1));
	}
	
	@Test
	public void findUserInfoByNameAndAge1(){
		String name = "张三";
		Integer age = null;
		JSONArray jsonArray = queryByNamedDBExample.findUserInfoByNameAndAge(name, age);
		for (Object object : jsonArray) {
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>) object;
			Set<String> keys = map.keySet();
			for (String key : keys) {
				if("name".equals(key)){
					assertThat(map.get(key).toString().equals(name), is(true));
				}
			}
		}
	}
	
	@Test
	public void findUserInfoByNameAndAge2(){
		String name = null;
		Integer age = 8;
		JSONArray jsonArray = queryByNamedDBExample.findUserInfoByNameAndAge(name, age);
		for (Object object : jsonArray) {
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>) object;
			Set<String> keys = map.keySet();
			for (String key : keys) {
				if("age".equals(key)){
					assertThat(map.get(key).toString().equals(age.toString()), is(true));
				}
			}
		}
	}
	
	@Test
	public void findUserInfoByNameAndAge3(){
		UserInfo userInfo = queryByNamedDBExample.findUserInfoOne(1);
		String name = userInfo.getName();
		Integer age = userInfo.getAge();
		JSONArray jsonArray = queryByNamedDBExample.findUserInfoByNameAndAge(name, age);
		for (Object object : jsonArray) {
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>) object;
			assertThat(map, notNullValue());
			assertThat(map.isEmpty(),is(false));
			Set<String> keys = map.keySet();
			for (String key : keys) {
				if("age".equals(key)){
					assertThat(map.get(key).toString().equals(age.toString()), is(true));
				}
				if("name".equals(key)){
					assertThat(map.get(key).toString().equals(name.toString()), is(true));
				}
			}
		}
	}
	
	
	@Test
	public void findPage(){
		
		int page = 1;  // 第3页
		int size = 5;  // 指定每页显示5条数据
		
		String no = "95";    
		String name = null;  
		Integer age = 8;
		
		Page<Student> pageObj = queryByNamedDBExample.findPage(new PageableImpl(page, size),no,name,age);
		assertThat(pageObj.getContent().size(), lessThanOrEqualTo(size));
		if(pageObj.getContent().size()>1){ // 如果当前页有数据,那么总页数肯定不为0l
			assertThat(pageObj.getTotalElements(), not(0l)); // 总记录数不为0l	 注意TotalElements是long类型,比较是最好保持类型一致!
		}
		System.out.println(JSON.toJSONString(JSON.toJSON(pageObj), true));
	}
	
	@Test
	public void updateUserInfoById(){
		int id = Math.abs(new Random().nextInt()%3) + 1; //[1,3] 范围的随机数
		LOG.debug("正在修改主键为"+id+"的UserInfo.");
		String name = "清风习习"+new Random().nextInt(8);
		int age = new Random().nextInt(99);
		int effect = queryByNamedDBExample.updateUserInfoById(id, name,age);
		assertThat(effect, is(1));
		
		// 把改后的数据查询出来进行断言是否改正确
		UserInfo userInfo = userInfoDBService.findById(id);
		assertThat(userInfo.getName(), equalTo(name));
		assertThat(userInfo.getAge().intValue(), is(age));
		
	}
}












