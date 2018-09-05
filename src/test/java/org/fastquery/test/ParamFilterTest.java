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

import java.lang.reflect.Method;

import org.fastquery.core.Param;
import org.fastquery.util.TypeUtil;
import org.junit.Test;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * 
 * @author mei.sir@aliyun.cn
 */
public class ParamFilterTest extends FastQueryTest  {

	public static String paramFilter(Method method, Object[] args, String sql) throws Exception {
		Method m = TypeUtil.class.getDeclaredMethod("paramFilter", Method.class, Object[].class, String.class);
		m.setAccessible(true);
		return m.invoke(null, method, args, sql).toString();
	}

	public void m1(@Param("name1") String name, @Param("age1") Integer age) {
	}

	private Method getMethod1() throws NoSuchMethodException {
		return ParamFilterTest.class.getMethod("m1", String.class, Integer.class);
	}

	@Test
	public void paramFilter11() throws Exception {
		Method method = getMethod1();
		String name = "小王子";
		Integer age = 6;
		Object[] args = { name, age };
		String sql = "";
		String str = paramFilter(method, args, sql);
		assertThat(str, equalTo(sql));

		sql = "$name1_";
		str = paramFilter(method, args, sql);
		assertThat(str, equalTo(sql));

		sql = "$name1	_";
		str = paramFilter(method, args, sql);
		assertThat(str, equalTo(name + "	_"));

		sql = "_$name1";
		str = paramFilter(method, args, sql);
		assertThat(str, equalTo("_" + name));

		sql = "_$name1$name1";
		str = paramFilter(method, args, sql);
		assertThat(str, equalTo("_" + name + name));

		sql = "$name123$age12";
		str = paramFilter(method, args, sql);
		assertThat(str, equalTo(sql));
	}

	@Test
	public void paramFilter12() throws Exception {
		Method method = getMethod1();
		String name = "小王子";
		Integer age = 6;
		Object[] args = { name, age };
		String sql = "";
		String str = paramFilter(method, args, sql);
		assertThat(str, equalTo(sql));

		sql = "${name1_}";
		str = paramFilter(method, args, sql);
		assertThat(str, equalTo(sql));

		sql = "${name1}	_";
		str = paramFilter(method, args, sql);
		assertThat(str, equalTo(name + "	_"));

		sql = "_${name1}";
		str = paramFilter(method, args, sql);
		assertThat(str, equalTo("_" + name));

		sql = "_${name1}${name1}";
		str = paramFilter(method, args, sql);
		assertThat(str, equalTo("_" + name + name));

		sql = "${name123}${age12}";
		str = paramFilter(method, args, sql);
		assertThat(str, equalTo(sql));
	}

	@Test
	public void paramFilter13() throws Exception {
		Method method = getMethod1();
		String name = "小王子";
		Integer age = 6;
		Object[] args = { name, age };
		String sql = ":name1";
		String str = paramFilter(method, args, sql);
		assertThat(str, equalTo("?1"));

		sql = ":age1";
		str = paramFilter(method, args, sql);
		assertThat(str, equalTo("?2"));

		sql = ":age1 :name1";
		str = paramFilter(method, args, sql);
		assertThat(str, equalTo("?2 ?1"));

		sql = ":age1:name1";
		str = paramFilter(method, args, sql);
		assertThat(str, equalTo("?2?1"));

		sql = ":age:name";
		str = paramFilter(method, args, sql);
		assertThat(str, equalTo(":age:name"));
	}
}
