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

package org.fastquery.mapper;

import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.Set;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.fastquery.core.Resource;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class QueryPoolTest {
	
	static Resource resource;
	
	@BeforeClass
	public static void beforeClass(){
		resource = new Resource() {
			@Override
			public InputStream getResourceAsStream(String name) {
				return QueryPoolTest.class.getClassLoader().getResourceAsStream(name);
			}
			@Override
			public boolean exist(String name) {
				URL url = QueryPoolTest.class.getClassLoader().getResource(name);
				if (url == null) {
					return false;
				}
				return true;
			}
		};
	}
	
	@Before
	public void before(){

	}
	
	@Test
	public void testXml2QueryMapper() {
		Set<QueryMapper> queryMappers = QueryPool.xml2QueryMapper("org.fastquery.dao.QueryByNamedDBExample",resource);
		
		JSONArray jsonArray = new JSONArray();
		for (QueryMapper queryMapper : queryMappers) {
			//System.out.println(String.format("{\n\t\"id\" : \"%s\",\n\t\"template\" : \"%s\"\n}", queryMapper.getId(),queryMapper.getTemplate()));
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("id", queryMapper.getId());
			jsonObject.put("template", queryMapper.getTemplate());
			jsonArray.add(jsonObject);
		}
		
		System.out.println(JSON.toJSONString(jsonArray, true));
		
		VelocityContext context = new VelocityContext();
		// 把数据填入上下文
		context.put("name","张三");
		context.put("age", 23);
		  // 输出流
		StringWriter writer = new StringWriter();
		 // 转换输出
		Velocity.evaluate(context, writer, "", jsonArray.getJSONObject(2).getString("template")); // 关键方法
		System.out.println(writer.toString());
	}
	
	@Test
	public void put(){
		String str = QueryPool.render("org.fastquery.dao.UserInfoDBService", "findUserInfoAll",null);
		System.out.println("str:" + str);
	}

	@Test
	public void reset(){
		QueryPool.reset("org.fastquery.dao.UserInfoDBService");
		String str = QueryPool.render("org.fastquery.dao.UserInfoDBService", "findUserInfoAll",null);
		System.out.println("str:" + str);
	}
}
