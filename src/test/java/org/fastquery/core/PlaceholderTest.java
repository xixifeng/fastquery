/*
 * Copyright (c) 2016-2017, fastquery.org and/or its affiliates. All rights reserved.
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

package org.fastquery.core;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.fastquery.util.TypeUtil;
import org.junit.Test;

/**
 * 测试正则
 * 
 * @author mei.sir@aliyun.cn
 */
public class PlaceholderTest {

	private static final Logger LOG = Logger.getLogger(PlaceholderTest.class);

	@Test
	public void placeholder() {
		// 匹配 (?4,?5,?6)的正则(允许有首尾空格)
		String reg1 = Placeholder.INV_REG;
		assertThat(Pattern.matches(reg1, "(?3,?7,?8)"), is(true));
		assertThat(Pattern.matches(reg1, "( ?3,?7 ,?8 ) "), is(true));
		assertThat(Pattern.matches(reg1, " ( ?3 ,?7 , ?8 )"), is(true));
		assertThat(Pattern.matches(reg1, "     (?3,   ?7,  ?8 )"), is(true));
		assertThat(Pattern.matches(reg1, " (?3, ?7,   ?8)"), is(true));
		assertThat(Pattern.matches(reg1, "( ?3,     ?7,?8)"), is(true));
		assertThat(Pattern.matches(reg1, "( ?3,?7, ?8 )      "), is(true));
		assertThat(Pattern.matches(reg1, "( ?3, ?7, ?8) "), is(true));

		assertThat(Pattern.matches(reg1, "( ?3, ?7 ?8) "), is(false));
		assertThat(Pattern.matches(reg1, "( ?3?7?8) "), is(false));
		assertThat(Pattern.matches(reg1, "( ?s,?7, ?8) "), is(false));
		assertThat(Pattern.matches(reg1, "( ?3, ?7, ?8)s "), is(false));
		assertThat(Pattern.matches(reg1, "(?3, ?7, ?8)12 "), is(false));

		assertThat(Pattern.matches(reg1, "(?3?7?8)"), is(false));
		assertThat(Pattern.matches(reg1, "( ?3666,?7 ?8 ) "), is(false));
		assertThat(Pattern.matches(reg1, " ( ?3777 32?7 , ?8 )"), is(false));
		assertThat(Pattern.matches(reg1, "     (?3xx,   ?7,  ?8 )"), is(false));
		assertThat(Pattern.matches(reg1, " (?3a, ?7,   ?8)"), is(false));
		assertThat(Pattern.matches(reg1, "( ?3,  263, ?7,?8)"), is(false));
		assertThat(Pattern.matches(reg1, "( ?3,?7, ?8,? )      "), is(false));
		assertThat(Pattern.matches(reg1, "( ?3, ?x5, ?8) "), is(false));

		// 不区分大小写匹配格式 "?8 and ?9"
		reg1 = Placeholder.ANDV_REG;
		assertThat(Pattern.matches(reg1, "?12 AnD ?456"), is(true));
		assertThat(Pattern.matches(reg1, "?1 AnD ?45"), is(true));
		assertThat(Pattern.matches(reg1, "?3 AnD ?6"), is(true));
		assertThat(Pattern.matches(reg1, "?3 AnD ?456"), is(true));
		assertThat(Pattern.matches(reg1, " ?123     AnD ?456 "), is(true));
		assertThat(Pattern.matches(reg1, "    ?123 AnD ?456         "), is(true));
		assertThat(Pattern.matches(reg1, "    ?123 AnD     ?456"), is(true));
		assertThat(Pattern.matches(reg1, "?123      AnD ?456"), is(true));
		assertThat(Pattern.matches(reg1, "?123 AnD        ?456 "), is(true));

		assertThat(Pattern.matches(reg1, "?12AnD ?456"), is(false));
		assertThat(Pattern.matches(reg1, "?1 AnD?45"), is(false));
		assertThat(Pattern.matches(reg1, "?3 AndD ?6"), is(false));
		assertThat(Pattern.matches(reg1, "?3 AnD ?45x"), is(false));
		assertThat(Pattern.matches(reg1, " ?123  AAnD ?456 "), is(false));
		assertThat(Pattern.matches(reg1, "    ? AnD ?456         "), is(false));
		assertThat(Pattern.matches(reg1, "    ?123 AnD     ?"), is(false));
		assertThat(Pattern.matches(reg1, "?123      AnND ?456"), is(false));
		assertThat(Pattern.matches(reg1, "? 123 AnD ?456 "), is(false));

		// 匹配格式 "?2"(允许首尾空格)
		reg1 = Placeholder.SP2_REG;
		assertThat(Pattern.matches(reg1, "?1"), is(true));
		assertThat(Pattern.matches(reg1, "?12"), is(true));
		assertThat(Pattern.matches(reg1, "?13"), is(true));
		assertThat(Pattern.matches(reg1, " ?1 "), is(true));
		assertThat(Pattern.matches(reg1, "?12 "), is(true));
		assertThat(Pattern.matches(reg1, " ?123"), is(true));
		assertThat(Pattern.matches(reg1, "?1       "), is(true));
		assertThat(Pattern.matches(reg1, " ?1234242"), is(true));
		assertThat(Pattern.matches(reg1, "?1 "), is(true));
		assertThat(Pattern.matches(reg1, " ?1365    "), is(true));

		assertThat(Pattern.matches(reg1, " ? 1"), is(false));
		assertThat(Pattern.matches(reg1, " ?1x"), is(false));
		assertThat(Pattern.matches(reg1, " ?S"), is(false));
		assertThat(Pattern.matches(reg1, " ?a"), is(false));
		assertThat(Pattern.matches(reg1, " ?1 1"), is(false));
		assertThat(Pattern.matches(reg1, " ?3,3"), is(false));

		assertThat(Pattern.matches(reg1, "%?1"), is(false));
	}

	@Test
	public void Q_MATCH() {
		String str = "select * from UserInfo where name like %?  and age like _? and akjgew %     ?_    % and sge ?";

		List<String> ssms = TypeUtil.matches(str, Placeholder.Q_MATCH);
		assertThat(ssms.size(), is(3));

		assertThat(ssms.get(0), equalTo(" %?  "));

		assertThat(ssms.get(1), equalTo(" _? "));

		assertThat(ssms.get(2), equalTo(" %     ?_    % "));

		ssms.forEach(m -> LOG.debug(m));
	}
}
