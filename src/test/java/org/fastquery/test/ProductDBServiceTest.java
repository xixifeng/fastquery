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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.fastquery.bean.Product;
import org.fastquery.dao.ProductDBService;
import org.fastquery.service.FQuery;
import org.junit.Rule;
import org.junit.Test;

/**
 * 
 * @author mei.sir@aliyun.cn
 */
public class ProductDBServiceTest extends FastQueryTest  {

	private ProductDBService pdbs = FQuery.getRepository(ProductDBService.class);

	@Rule
	public FastQueryTestRule rule = new FastQueryTestRule();

	@Test
	public void testFindOne() {
		assertThat(pdbs.findOne().isEmpty(), is(false));
	}

	@Test
	public void inserts() {
		// 当数据库中 不存在 pid = 1 时, 第一个query执行后影响行数:0,第二个query影响行数:1,第二个query影响行数:1,第三个query影响行数:1  ->  3
		// 当数据库中 存在 pid = 1 时, 第一个query执行后影响行数:3,第二个query影响行数:1,第二个query影响行数:1,第三个query影响行数:1  ->  6
		int i = pdbs.inserts();
		assertThat("断言" + i + "要么是3,要么是6", i, either(is(3)).or(is(6)));
	}
	
	@Test
	public void save1() {
		assertThat(pdbs.save(true, null), is(0));
		List<Product> products = new ArrayList<>();
		assertThat(pdbs.save(false, products), is(0));
	}
	
	@Test
	public void save2() {
		List<Product> products = new ArrayList<>();
		
		Product p1 = new Product(6, 2, "几何");
		Product p2 = new Product(6, 2, "物理");
		
		products.add(p1);
		products.add(p2);
		
		assertThat(pdbs.save(true, products), is(1));
	}

}
