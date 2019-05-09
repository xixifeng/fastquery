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

import java.lang.reflect.Method;

import org.fastquery.core.Param;
import org.fastquery.util.QueryContextUtil;
import org.fastquery.where.Script2Class;
import org.fastquery.where.Set;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * 
 * @author mei.sir@aliyun.cn
 */
public class Script2ClassTest {
	
	@Set("AA")
	@Set(value="BB",ignoreScript=":age.intValue() >18 && :name!=null && :name.contains(\"Rex\")")
	@Set("CC")
	public void todo(@Param("age")Integer age,@Param("name")String name) {
	}
	
	private static String processParam(String script, Method method) throws Exception {
		Method m = Script2Class.class.getDeclaredMethod("processParam", String.class,Method.class);
		m.setAccessible(true);
		return (String) m.invoke(null, script,method);
	}
	
	// 测试Script2Class中的私有静态方法(processParam).
	@Test
	public void processParam() throws Exception {
		Method method = Script2ClassTest.class.getMethod("todo", Integer.class,String.class);
		String script = ":age.intValue() > 18 && :name!=null && :name.contains(\"Rex\")";
		String code = processParam(script, method);
		assertThat(code, equalTo("((java.lang.Integer)this.getParameter(\"age\")).intValue() > 18 && ((java.lang.String)this.getParameter(\"name\"))!=null && ((java.lang.String)this.getParameter(\"name\")).contains(\"Rex\")"));
	}

	@BeforeClass
	public static void before() throws Exception {
		Script2Class.generate(Script2ClassTest.class);
		QueryContextUtil.startQueryContext();
	}
	
	@AfterClass
	public static void after() throws Exception {
		QueryContextUtil.clearQueryContext();
	}
	
	@Test
	public void script1() throws Exception {
		
		// 给上下文 设置 method 和 参数
		Method method = Script2ClassTest.class.getMethod("todo", Integer.class,String.class);
		QueryContextUtil.setCurrentMethod(method);
				
		QueryContextUtil.setCurrentArgs(17,"RexLeifeng");
		boolean b = Script2Class.getJudge(1).ignore();
		assertThat(b, is(false));
		
		QueryContextUtil.setCurrentArgs(18,"RexLeifeng");
		b = Script2Class.getJudge(1).ignore();
		assertThat(b, is(false));
		
		QueryContextUtil.setCurrentArgs(19,"RexLeifeng");
		b = Script2Class.getJudge(1).ignore();
		assertThat(b, is(true));
		
		QueryContextUtil.setCurrentArgs(19,"Leifeng");
		b = Script2Class.getJudge(1).ignore();
		assertThat(b, is(false));
		
		QueryContextUtil.setCurrentArgs(19,null);
		b = Script2Class.getJudge(1).ignore();
		assertThat(b, is(false));
	} 

}
