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

package org.fastquery.test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.fastquery.bean.UserInfo;
import org.fastquery.core.Param;
import org.fastquery.example.StudentDBService;
import org.fastquery.util.TypeUtil;
import org.junit.Test;
import org.objectweb.asm.Type;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class SyntaxTest {

	private static final Logger LOG = LoggerFactory.getLogger(SyntaxTest.class);

	@Test(expected = IndexOutOfBoundsException.class)
	public void listEmpty() {
		List<Map<String, Object>> maps = new ArrayList<>();
		maps.get(0); // 这样引用是错误的
	}

	@Test
	public void test1() throws NoSuchMethodException, SecurityException {
		Method method = SyntaxTest.class.getMethod("todo", String.class, String.class, int.class);
		Annotation[][] annotations = method.getParameterAnnotations();
		Annotation annotation = annotations[0][0];
		assertThat(annotation instanceof Param, is(true));
		annotation = annotations[1][0];
		assertThat(annotation instanceof Param, is(true));

		Parameter[] parameters = method.getParameters();
		Param param = parameters[0].getAnnotation(Param.class);
		assertThat(param, notNullValue());

		param = parameters[1].getAnnotation(Param.class);
		assertThat(param, notNullValue());

		param = parameters[2].getAnnotation(Param.class);
		assertThat(param, nullValue());
	}

	public void todo(@Param("abc") String sx, @Param("efg") String efg, int s) {
	}

	@Test
	public void testReg() {
		LOG.debug(String.valueOf(Pattern.matches("", "")));
		// s.replaceAll("\\:"+param.value()+"\\b", "?"+(i+1));
		assertThat("abckdwgew:name&".replaceAll("\\:name\\b", "?"), equalTo("abckdwgew?&"));
		assertThat("abckdwgew:name &".replaceAll("\\:name\\b", "?"), equalTo("abckdwgew? &"));
		LOG.debug("-->: " + ("abckdwgew:name222 &".replaceAll("\\:name\\b", "?")));
		assertThat(":name22".replaceAll("\\:name\\b", "?"), equalTo(":name22"));
		assertThat(":name22 ".replaceAll("\\:name\\b", "?"), equalTo(":name22 "));
		assertThat(":name,".replaceAll("\\:name\\b", "?"), equalTo("?,"));

	}

	@Test
	public void fastjson() {
		UserInfo userInfo = new UserInfo(1, null, null);
		LOG.debug(JSON.toJSONString(userInfo, SerializerFeature.WriteMapNullValue));
		Boolean b = false;
		LOG.debug(JSON.toJSONString(b, SerializerFeature.WriteNullBooleanAsFalse));
		LOG.debug("基本类型:" + boolean.class.isPrimitive());

		StringBuilder sb = new StringBuilder("abc");
		sb.insert(3, "d");
		LOG.debug(sb.toString());
	}

	@Test
	public void local() {
		Locale locale = Locale.getDefault();
		LOG.debug("Language:" + locale.getLanguage());
		LOG.debug("Country:" + locale.getCountry());
	}

	@Test
	public void ty() {
		// \\#\\{\\#abc\\}
		String reg = Pattern.quote("#{#abc\\}");
		Set<String> ss = TypeUtil.matchesNotrepeat("#{#abc}aa", reg);
		ss.forEach(s -> LOG.debug(s) );
	}

}
