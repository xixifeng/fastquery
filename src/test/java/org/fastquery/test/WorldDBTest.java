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

import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;

import org.fastquery.bean.City;
import org.fastquery.page.Page;
import org.fastquery.page.PageableImpl;
import org.fastquery.service.FQuery;
import org.fastquery.sqlserver.dao.WorldDB;
import org.junit.Test;


/**
 * 
 * @author mei.sir@aliyun.cn
 */
public class WorldDBTest extends FastQueryTest  {

	private WorldDB db = FQuery.getRepository(WorldDB.class);
	
	@Test
	public void findCityById() {
		 City city = db.find(City.class, 1);
		 assertThat(city.getId(), is(1));
		 assertThat(city.getCode(), is(110100));
		 assertThat(city.getCityAbb(), is("京A"));
		 assertThat(city.getCityName(), is("北京市"));
	}

	@Test
	public void findPage1() {
		int size = 2;
		Page<Map<String, Object>> page = db.findPage(new PageableImpl(1, size));
		List<Map<String, Object>> content = page.getContent();
		assertThat(content.get(0).get("id"), is(1));
		assertThat(content.get(1).get("id"), is(2));
		
		page = db.findPage(new PageableImpl(2, size));
		content = page.getContent();
		assertThat(content.get(0).get("id"), is(3));
		assertThat(content.get(1).get("id"), is(4));
		
		page = db.findPage(new PageableImpl(3, size));
		content = page.getContent();
		assertThat(content.get(0).get("id"), is(5));
		assertThat(content.get(1).get("id"), is(6));
		
		page = db.findPage(new PageableImpl(0, size));
		content = page.getContent();
		assertThat(content.get(0).get("id"), is(1));
		assertThat(content.get(1).get("id"), is(2));
		
		page = db.findPage(new PageableImpl(194, size));
		content = page.getContent();
		assertThat(content.get(0).get("id"), is(387));
		assertThat(content.get(1).get("id"), is(388));
		assertThat(page.isLast(), is(true));
		
		
		page = db.findPage(new PageableImpl(195, size));
		content = page.getContent();
		assertThat(content.isEmpty(), is(true));
		
		assertThat(page.isHasContent(), is(false));
		assertThat(page.isHasNext(), is(false));
		assertThat(page.isFirst(), is(false)); // 不是地一页
		assertThat(page.isLast(), is(false)); // 不是最后一页

	}
	
	@Test
	public void findPage2() {
		Integer id = 60;
		String cityAbb = "%B";
		int pageSize = 8;
		
		// 分别是当前页的第1条,第3条,第5条,第7条,
		City[] group1 = {new City(69,220200,"吉B","吉林市"),new City(93,310101,"沪B","上海市"),new City(110,330200,"浙B","宁波市"),new City(140,350300,"闽B","莆田市")};
		Page<City> page = db.findPage(id, cityAbb, new PageableImpl(1,pageSize));
		assertThat(page.getTotalElements(), is(24L));
		assertThat(page.getTotalPages(), is(3));
		checkPage(page, group1);
		
		City[] group2 = {new City(285,510700,"川B","绵阳市"),new City(329,542100,"藏B","昌都地区")};
		page = db.findPage(id, cityAbb, new PageableImpl(3,pageSize));
		assertThat(page.getTotalElements(), is(24L));
		assertThat(page.getTotalPages(), is(3));
		checkPage(page, group2);
		
	}
	
	@Test
	public void findPageWithWhere() {
	
		Integer id = 19;
		String cityAbb = "%A";
		int pageSize = 5;
		
		// 分别是当前页的第1条,第3条,第5条
		City[] group1 = {new City(30,140100,"晋A","太原市"),new City(54,210100,"辽A","沈阳市"),new City(77,230100,"黑A","哈尔滨市")};
		checkPage(db.findPageWithWhere(id, cityAbb, 1,pageSize), group1);
		checkPage(db.findPageWithWhere(id, cityAbb, new PageableImpl(1,pageSize)), group1);
		
		City[] group2 = {new City(147, 360100, "赣A", "南昌市"),new City(179, 410100, "豫A", "郑州市"),new City(214, 430100, "湘A", "长沙市")};
		checkPage(db.findPageWithWhere(id, cityAbb, 3,pageSize), group2);
		checkPage(db.findPageWithWhere(id, cityAbb, new PageableImpl(3,pageSize)), group2);

		City[] group3 = {new City(301, 520100, "贵A", "贵阳市"),new City(328, 540100, "藏A", "拉萨市"),new City(346, 620100, "甘A", "兰州市")};
		checkPage(db.findPageWithWhere(id, cityAbb, 5,pageSize), group3);
		checkPage(db.findPageWithWhere(id, cityAbb, new PageableImpl(5,pageSize)), group3);

		// 分别是当前页的第1条,第3条
		City[] group4 = {new City(360, 630100, "青A", "西宁市"),new City(373, 650100, "新A", "乌鲁木齐市")};
		checkPage(db.findPageWithWhere(id, cityAbb, 6,pageSize), group4);
		checkPage(db.findPageWithWhere(id, cityAbb, new PageableImpl(6,pageSize)), group4);

		assertThat(db.findPageWithWhere(id, cityAbb, 7,pageSize).isHasContent(), is(false));
		
	}

	private void checkPage(Page<City> page, City...cities) {
		int len = cities.length;
		List<City> content = page.getContent();
		for (int i = 0; i < len; i++) {
			City row = content.get(i*2); // i=0>0(第1条) , i=1>2(第3条) , i=2>4(第5条) , i=3>6(第7条)
			City city = cities[i];
			assertThat(row.getId(), is(city.getId()));
			assertThat(row.getCode(), is(city.getCode()));
			assertThat(row.getCityAbb(), is(city.getCityAbb()));
			assertThat(row.getCityName(), is(city.getCityName()));
		}
	}	
}
