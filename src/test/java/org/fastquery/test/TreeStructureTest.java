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
import static org.hamcrest.Matchers.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 
 * @author mei.sir@aliyun.cn
 */
public class TreeStructureTest {
	
	private static List<Map<String, Object>> listMap = new ArrayList<>();
	
	static {
		Map<String, Object> map1 = new HashMap<>();
		map1.put("id", 1);
		map1.put("parentID", 0);
		map1.put("name", "顶级1");		
		Map<String, Object> map2 = new HashMap<>();
		map2.put("id", 2);
		map2.put("parentID", 1);
		map2.put("name", "顶级1>1");	
		Map<String, Object> map3 = new HashMap<>();
		map3.put("id", 3);
		map3.put("parentID", 2);
		map3.put("name", "顶级1>1>1");	
		Map<String, Object> map4 = new HashMap<>();
		map4.put("id", 4);
		map4.put("parentID", 3);
		map4.put("name", "顶级1>1>1>1");	
		Map<String, Object> map5 = new HashMap<>();
		map5.put("id", 5);
		map5.put("parentID", 0);
		map5.put("name","顶级2");	
		Map<String, Object> map6 = new HashMap<>();
		map6.put("id", 6);
		map6.put("parentID", 5);
		map6.put("name", "顶级2>1");	
		Map<String, Object> map7 = new HashMap<>();
		map7.put("id", 7);
		map7.put("parentID", 5);
		map7.put("name", "顶级2>2");	
		Map<String, Object> map8 = new HashMap<>();
		map8.put("id", 8);
		map8.put("parentID", 5);
		map8.put("name", "顶级2>3");	
		Map<String, Object> map9 = new HashMap<>();
		map9.put("id", 9);
		map9.put("parentID", 7);
		map9.put("name", "顶级2>2>1");	
		Map<String, Object> map10 = new HashMap<>();
		map10.put("id", 10);
		map10.put("parentID", 7);
		map10.put("name", "顶级2>2>2");	
		Map<String, Object> map11 = new HashMap<>();
		map11.put("id", 11);
		map11.put("parentID", 7);
		map11.put("name", "顶级2>2>3");	
		listMap.add(map1);
		listMap.add(map2);
		listMap.add(map3);
		listMap.add(map4);
		listMap.add(map5);
		listMap.add(map6);
		listMap.add(map7);
		listMap.add(map8);
		listMap.add(map9);
		listMap.add(map10);
		listMap.add(map11);
	}
	
	/**
	 
	模拟一些数据
	 
	 id, parentID, name
	 
	 1,  0  , "顶级1"
	 2,  1  , "顶级1>1"
	 3,  2  , "顶级1>1>1"
	 4,  3  , "顶级1>1>1>1"
	 
	 5,  0	, "顶级2"
	 6,  5 	, "顶级2>1"
	 7,  5 	, "顶级2>2"
	 8,  5 	, "顶级2>3"
	 
	 9,  7	, "顶级2>2>1"
	 10, 7	, "顶级2>2>2"
	 11, 7	, "顶级2>2>3"
	 
	 */
	
	// 测试 list map 生成树
	@Test
	public void toListTree1() {
		List<Map<String, Object>> lispTree = TreeStructure.toListTree(
				listMap,   // 需要生成树的集合
				map -> Integer.parseInt(map.get("id").toString()),  // 获取 id 的方式
				map -> Integer.parseInt(map.get("parentID").toString()), // 获取 parentID 的方式
				(map,children) -> map.put("children", children)); // 存储 children 的方式
		
		assertItems(lispTree);
	}
	
	@Test
	public void interateTree() {
		List<Map<String, Object>> lispTree = TreeStructure.toListTree(
				listMap,   // 需要生成树的集合
				map -> Integer.parseInt(map.get("id").toString()),  // 获取 id 的方式
				map -> Integer.parseInt(map.get("parentID").toString()), // 获取 parentID 的方式
				(map,children) -> map.put("children", children)); // 存储 children 的方式
		
		List<String> items = new ArrayList<>();
		

		
		@SuppressWarnings("unchecked")
		Function<Map<String, Object>, List<Map<String, Object>>> getChildrenFun = map -> (List<Map<String,Object>>)  map.get("children");
		
		Consumer<Map<String,Object>> consumerItem = map -> items.add(map.get("id")+", " + map.get("parentID") + ", " + map.get("name"));
		
		TreeStructure.interateTree(lispTree, getChildrenFun, consumerItem);
		assertThat(items.get(0), equalTo("1, 0, 顶级1"));
		assertThat(items.get(1), equalTo("2, 1, 顶级1>1"));
		assertThat(items.get(2), equalTo("3, 2, 顶级1>1>1"));
		assertThat(items.get(3), equalTo("4, 3, 顶级1>1>1>1"));
		assertThat(items.get(4), equalTo("5, 0, 顶级2"));
		assertThat(items.get(5), equalTo("6, 5, 顶级2>1"));
		assertThat(items.get(6), equalTo("7, 5, 顶级2>2"));
		assertThat(items.get(7), equalTo("9, 7, 顶级2>2>1"));
		assertThat(items.get(8), equalTo("10, 7, 顶级2>2>2"));
		assertThat(items.get(9), equalTo("11, 7, 顶级2>2>3"));
		assertThat(items.get(10), equalTo("8, 5, 顶级2>3"));
	}

	
	private <ITEM> void assertItems(Collection<ITEM> items) {
		assertThat(items.size(), is(2));
		JSONArray jsonArray = (JSONArray) JSON.toJSON(items);
		JSONObject line1, line2, line3, line4, line5, line6, line7, line8, line9, line10, line11;
		line1 = jsonArray.getJSONObject(0);
		line2 = line1.getJSONArray("children").getJSONObject(0);
		line3 = line2.getJSONArray("children").getJSONObject(0);
		line4 = line3.getJSONArray("children").getJSONObject(0);
		line5 = jsonArray.getJSONObject(1);
		line6 = line5.getJSONArray("children").getJSONObject(0);
		line7 = line5.getJSONArray("children").getJSONObject(1);
		line8 = line5.getJSONArray("children").getJSONObject(2);
		line9 = line7.getJSONArray("children").getJSONObject(0);
		line10 = line7.getJSONArray("children").getJSONObject(1);
		line11 = line7.getJSONArray("children").getJSONObject(2);

		assertThat(line4.getJSONArray("children").size(), is(0));
		assertThat(line6.getJSONArray("children").size(), is(0));
		assertThat(line8.getJSONArray("children").size(), is(0));
		assertThat(line9.getJSONArray("children").size(), is(0));
		assertThat(line10.getJSONArray("children").size(), is(0));
		assertThat(line11.getJSONArray("children").size(), is(0));

		checkLine(1, 0, "顶级1", line1);
		checkLine(2, 1, "顶级1>1", line2);
		checkLine(3, 2, "顶级1>1>1", line3);
		checkLine(4, 3, "顶级1>1>1>1", line4);
		checkLine(5, 0, "顶级2", line5);
		checkLine(6, 5, "顶级2>1", line6);
		checkLine(7, 5, "顶级2>2", line7);
		checkLine(8, 5, "顶级2>3", line8);
		checkLine(9, 7, "顶级2>2>1", line9);
		checkLine(10, 7, "顶级2>2>2", line10);
		checkLine(11, 7, "顶级2>2>3", line11);
	}
	
	private void checkLine(Integer id, Integer parentID, String name, JSONObject jsonObject) {
		assertThat(jsonObject.get("id"), is(id));
		assertThat(jsonObject.get("parentID"), is(parentID));
		assertThat(jsonObject.get("name"), is(name));
	}

}








