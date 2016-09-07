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

import org.fastquery.bean.UserInfo;
import org.fastquery.bean.UserInformation;
import org.fastquery.core.RepositoryException;
import org.fastquery.dao.UserInfoDBService;
import org.fastquery.page.Page;
import org.fastquery.page.PageableImpl;
import org.fastquery.page.Slice;
import org.fastquery.service.FQuery;
import org.junit.Before;
import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.hamcrest.Matchers.*;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class UserInfoDBServiceTest {
	
	UserInfoDBService userInfoDBService;
	
	@Before
	public void before(){
		userInfoDBService = FQuery.getRepository(UserInfoDBService.class);
	}
	
	@Test
	public void findUserInfo(){		
		String orderby = "order by age desc";
		int i = 1;
		JSONArray jsonArray = userInfoDBService.findUserInfo(orderby,i);
	}
	
	@Test
	public void findUserInfoById(){
		UserInformation userInformation = userInfoDBService.findUserInfoById(1);
		System.out.println(userInformation);
	}
	
	@Test
	public void testFindUserInfoByAge(){
		int age = 20;
		JSONArray jsonArray = userInfoDBService.findUserInfoByAge(age);
		for (int i = 0; i < jsonArray.size(); i++) {
			JSONObject jsonObject = jsonArray.getJSONObject(i);
			assertThat(jsonObject.getInteger("age"),greaterThan(age));
		}
	}
	
	@Test
	public void findSome(){
		List<UserInfo> userInfos = userInfoDBService.findSome(30);
		userInfos.forEach( userInfo -> {
			assertThat(userInfo.getId(), greaterThan(30));
		});
	}
	
	@Test
	public void testFindUserInfoByAge2(){
		int age = 1000;
		// 数据库中age没有大于1千的记录
		// 断言: 查询返回的值应该是一个空对象,不是null.
		JSONArray jsonArray = userInfoDBService.findUserInfoByAge(age);
		assertThat(jsonArray, notNullValue());
		assertThat(jsonArray.isEmpty(),is(true));
	}
	
	
	@Test
	public void findOne(){
		int age = 1000;
		// 数据库中age没有大于1千的记录
		// 断言: 查询返回的值应该是一个空对象,不是null.
		Map<String, Object> map = userInfoDBService.findOne(age,"xk-c3p0");
		assertThat(map, notNullValue());
		assertThat(map.isEmpty(), is(true));
	}
	
	@Test
	public void testUpdateBatch() {
		int effect = userInfoDBService.updateBatch("小张张", 26, 1);
		assertThat("断言该行修改操作一共影响了3行",effect, equalTo(3));
	}
	
	// 断言: 它会抛出RepositoryException异常
	@Test(expected=RepositoryException.class)
	public void testUpdateBatch2_a() {
		int effect = userInfoDBService.updateBatch2("小不点", 6, 2);
		// updateBatch2 中途会报错,因此修改影响的行数为0
		assertThat(effect, equalTo(0));
	}
	
	@Test
	public void testUpdateBatch2_b() {
		try {
			int effect = userInfoDBService.updateBatch2("小不点", 6, 2);
			assertThat(effect, equalTo(0));	
		} catch (RepositoryException e) {
			//  Handle exceptional condition
			// TODO ...
		}
	}
	
	@Test
	public void findAll() {
		
		int p = 1;
		int size = 6;
		Page<Map<String, Object>> page = userInfoDBService.findAll(new PageableImpl(p, size));
		assertThat(String.format("断言: 当前是第%s页", p), page.getNumber(), is(p));
		assertThat(page.getNumberOfElements(), lessThanOrEqualTo(size));
		
		
		// 打印出来看看
		String str = JSON.toJSONString(page,true);
		System.out.println(str);
		
	}
	
	@Test
	public void find(){
		Page<UserInfo> page = userInfoDBService.find(100, 50,new PageableImpl(1, 3));
		List<UserInfo> userInfos = page.getContent();
		if(page.isHasContent()){
			userInfos.forEach(System.out::println);
		}
		assertThat(page.isFirst(), is(true));
		
		String str = JSON.toJSONString(page,true);
		
		System.out.println(str);
	}
	
	@Test
	public void findSome1(){
		Page<UserInfo> page = userInfoDBService.findSome1(1, 100, new PageableImpl(1, 15));
		assertThat(page, notNullValue());
	}
	
	@Test
	public void findSome2(){
		
		// pageIndex:5  pageSize:24
		// pageIndex:1  pageSize:99  totalPages:1
		// pageIndex:3  pageSize:49  totalPages:3
		// pageIndex:4  pageSize:49  totalPages:3
		
		int pageIndex = 3;
		int pageSize = 5;
		
		int age = 1;
		int id = 100;
		// 总行数
		long totalElements = userInfoDBService.count(age, id);
		for (int i = 1; i <= totalElements; i++) {
			pageIndex = i%10;
			pageSize = new Random().nextInt(1000)%(int)10;
			
			if(pageSize*pageIndex>totalElements) { // 显然是错的
				pageSize = 1;
			}
			
			if(pageSize==0){
				pageSize +=1;
			}
	
			int totalPages;       // 总页码
			totalPages = (int)totalElements/pageSize;
			if(totalElements%pageSize!=0){
				totalPages += 1;
			}
			if(pageIndex>totalPages) {
				pageIndex = new Random().nextInt(1000)%totalPages;
			}
			if(pageIndex==0) {
				pageIndex += 1; 
			}
			System.out.println("totalElements:"+totalElements + " pageIndex:" + pageIndex + "  pageSize:" + pageSize + "  totalPages:" + totalPages);
			
			Page<Map<String, Object>> page = userInfoDBService.findSome2(age, id, pageIndex, pageSize);
			assertThat(page, notNullValue());
			assertThat(page.getNumber(), equalTo(pageIndex));
			assertThat(page.getSize(), equalTo(pageSize));
			assertThat(page.getTotalElements(), equalTo(-1L));
			assertThat(page.getTotalPages(), equalTo(-1));
			assertThat(page.getNumberOfElements(), lessThanOrEqualTo(pageSize));
			//System.out.println(JSON.toJSONString(page, true));
			
			boolean hasContent=pageIndex<=totalPages;   // 是否有结果集
			boolean isFirst=pageIndex==1;      // 是否是第一页
			boolean isLast=pageIndex == totalPages;       // 是否是最后一页
			boolean hasNext=pageIndex<totalPages;      // 是否有下一页
			boolean hasPrevious=(pageIndex > 1) && hasContent;  // 是否有上一页
			Slice previousPageable = new Slice((!isFirst) ? (pageIndex - 1) : pageIndex, pageSize);  // 上一页的Pageable对象
			Slice nextPageable = new Slice((!isLast) ? (pageIndex + 1) : pageIndex, pageSize);      // 下一页的Pageable对象

			
			assertThat(page.isHasContent(),is(hasContent));
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
}












