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

import static org.junit.Assert.assertThat;

import java.math.BigInteger;

import static org.hamcrest.Matchers.*;

import org.fastquery.bean.sunny.Card;
import org.fastquery.bean.sunny.Tenant;
import org.fastquery.dao.SunnyDBService;
import org.fastquery.service.FQuery;
import org.junit.Test;


/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class SunnyDBServiceTest {

	private SunnyDBService sunnyDBService = FQuery.getRepository(SunnyDBService.class);
	
	@Test
	public void save() {
		Card card = new Card();
		card.setNumber("852000XXXXX");
		Card c = sunnyDBService.save(card);
		assertThat(c.getNumber(), equalTo(card.getNumber()));
		Integer id = c.getId();
		int effect = sunnyDBService.delete(id);
		assertThat(effect, is(1));
		assertThat(sunnyDBService.exists(id), is(false));
	}
	
	@Test
	public void save2() {
		Card card = new Card(-1,"32ccczuidazhi");
		Card c = sunnyDBService.save(card);
		assertThat(c,nullValue());
		int effect = sunnyDBService.delete(-1);
		assertThat(effect, is(1));
		assertThat(sunnyDBService.exists(-1), is(false));
	}
	
	@Test
	public void save3() {
		Card card = new Card("32ccczuidazhi");
		BigInteger bigInteger = sunnyDBService.saveToId(card);
		int effect = sunnyDBService.delete(bigInteger.intValue());
		assertThat(effect, is(1));
	}
	
	@Test
	public void saveTenant() {
		System.out.println(Integer.MAX_VALUE);
		long id = new Long(Integer.MAX_VALUE) + 10;
		while(sunnyDBService.existsTenant(id)){
			id = id + 1;
		}
		Tenant tenant = new Tenant(id, "测试用户"+id);
		BigInteger bigInteger = sunnyDBService.saveToId(tenant);
		assertThat(bigInteger.longValue(), is(id));
	}

	@Test
	public void delete() {
		int effect = sunnyDBService.delete(-1);
		assertThat(effect, is(0));
		effect = sunnyDBService.delete(0);
		assertThat(effect, is(0));
	}
	
	@Test
	public void deleteById(){
		boolean b = sunnyDBService.deleteById(-1);
		assertThat(b, is(true));
	}
}
