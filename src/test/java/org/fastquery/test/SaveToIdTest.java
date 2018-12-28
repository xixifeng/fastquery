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

import java.math.BigInteger;

import org.fastquery.bean.Visitor;
import org.fastquery.example.VisitorDBServcie;
import org.fastquery.service.FQuery;
import org.junit.Rule;
import org.junit.Test;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * 
 * @author mei.sir@aliyun.cn
 * @date 2017年9月25日
 */
public class SaveToIdTest extends FastQueryTest  {

	private VisitorDBServcie vdbs = FQuery.getRepository(VisitorDBServcie.class);

	@Rule
	public FastQueryTestRule rule = new FastQueryTestRule();

	@Test
	public void saveToId1() {
		Long punitId = 174L;
		String vname = "OTU\\";
		String idCard = "&";
		String mobile = "\\";
		String email = "<";
		Byte gender = (byte) 0;
		String toAddr = "";
		String remark = "来访";
		Long lastDate = 1506233249L;
		Long createDate = 1506233208L;
		String iden = "J6X1HH15ExIkvqSNSHXb";
		Integer dId = 1;

		Visitor v = new Visitor(punitId, vname, idCard, mobile, email, gender, toAddr, remark, lastDate, iden, dId);
		v.setCreateDate(createDate);

		BigInteger bigInteger = vdbs.saveToId(v);
		assertThat(bigInteger.longValue(), greaterThanOrEqualTo(1l));

		int effect = vdbs.deleteByIden(iden);

		assertThat(effect, is(1));

	}
}
