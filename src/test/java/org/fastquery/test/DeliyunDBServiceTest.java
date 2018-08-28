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

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import java.util.Map;

import org.fastquery.core.BuilderQuery;
import org.fastquery.example.DeliyunDBService;
import org.fastquery.page.Page;
import org.fastquery.page.PageableImpl;
import org.fastquery.service.FQuery;
import org.junit.Test;

/**
 * 
 * @author mei.sir@aliyun.cn
 */
public class DeliyunDBServiceTest {

	private DeliyunDBService db = FQuery.getRepository(DeliyunDBService.class);
	
	@Test
	public void testFindPunitFeeStateByFeetermPage1() {
		Page<Map<String, Object>> page = db.findPunitFeeStateByFeetermPage(222,new PageableImpl(1, 3));
		assertThat(page, notNullValue());
	}
	
	@Test
	public void testFindPunitFeeStateByFeetermPage2() {
		
		BuilderQuery builderQuery =  metaData -> {
			
			String where ="where state = 1 and detail.punitId=:punitId";
			
			String query="select detail.roomId,detail.period,termId,sum(payOfMoney + breachMoney + reliefMoney + offsetMoney) money,beginDate,endDate,b.buName";
			query+=" from deliyunservice.PunitFeeBillDetail detail ";
			query+=" join (select roomId, period from deliyunservice.PunitFeeBillDetail detail " + where + " group by roomId , period #{#limit}) tb on tb.roomId=detail.roomId and tb.period=detail.period";
			query+=" left join deliyunservice.BUnit b on detail.bunitId=b.id";
			query+=" " + where;
			query+=" group by roomId , period , termId";
			query+=" order by detail.period desc,detail.roomId desc";
			
			String countQuery="select count(id) from (select id from deliyunservice.PunitFeeBillDetail detail " + where + " group by roomId,period) tb";
			
			metaData.setQuery(query);
			metaData.setCountQuery(countQuery);
		};
		
		Page<Map<String, Object>> page = db.findPunitFeeStateByFeetermPage(222,new PageableImpl(1, 3),builderQuery);
		assertThat(page, notNullValue());
	}

}
