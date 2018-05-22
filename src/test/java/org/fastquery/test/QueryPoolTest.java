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

import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.fastquery.core.Resource;
import org.fastquery.dao.UserInfoDBService;
import org.fastquery.mapper.QueryMapper;
import org.fastquery.mapper.QueryPool;
import org.fastquery.service.FQuery;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class QueryPoolTest {

	static Resource resource;

	public UserInfoDBService userInfoDBService = FQuery.getRepository(UserInfoDBService.class);
	
	@BeforeClass
	public static void beforeClass() throws Exception {
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

	@SuppressWarnings("unchecked")
	private Set<QueryMapper> xml2QueryMapper(String className, Resource resource) throws Exception {
		Method method = QueryPool.class.getDeclaredMethod("xml2QueryMapper", String.class, Resource.class);
		method.setAccessible(true);
		return (Set<QueryMapper>) method.invoke(null, className, resource);
	}

	private String render(String tpl, String logTag, Map<String, Object> map) throws Exception {
		Method method = QueryPool.class.getDeclaredMethod("render", String.class, String.class, Map.class);
		method.setAccessible(true);
		return method.invoke(null, tpl, logTag, map).toString();
	}

	@Test
	public void testXml2QueryMapper() throws Exception {
		Set<QueryMapper> queryMappers = xml2QueryMapper("org.fastquery.dao.QueryByNamedDBExample", resource);
		queryMappers.forEach(queryMapper -> {
			String id = queryMapper.getId();
			String template = queryMapper.getTemplate();
			if ("findUAll".equals(id)) {
				assertThat(template, equalToIgnoringWhiteSpace("select id,name,age from UserInfo limit 3"));
			} else if ("findUserAll".equals(id)) {
				assertThat(template, equalToIgnoringWhiteSpace("select name from UserInfo limit 3"));
			} else if ("findUserInfo".equals(id)) {
				assertThat(template, equalToIgnoringWhiteSpace(
						"select * from UserInfo where id > :id and age > 18 or name like `-'%:name%'-`"));
			}

		});
	}

	@Test
	public void render() throws Exception {
		String template = "$abc${abc}";
		Map<String, Object> map = new HashMap<>();
		map.put("abc", "hi");
		String str = render(template, "render", map);
		assertThat(str, equalTo("hihi"));
	}
}
