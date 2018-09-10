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
import org.fastquery.core.RepositoryException;
import org.fastquery.where.Judge;
import org.junit.Test;

/**
 * 
 * @author mei.sir@aliyun.cn
 */
public class JudgeTest {

	public void todo1(String aa,@Param("bb")Integer bb,int cc) {
		
	}
	
	public void todo2(@Param("aa")String aa,Integer bb,int cc) {
		
	}
	
	public void todo3(String aa,Integer bb,@Param("cc")int cc) {
		
	}
	
	@Test
	public void testGetParamType1() throws NoSuchMethodException, SecurityException {
		Method method = JudgeTest.class.getMethod("todo1", String.class,Integer.class,int.class);
		Class<?> clazz = Judge.getParamType("bb", method);
		assertThat(clazz==Integer.class,is(true));
		
		method = JudgeTest.class.getMethod("todo2", String.class,Integer.class,int.class);
		clazz = Judge.getParamType("aa", method);
		assertThat(clazz==String.class,is(true));
		
		method = JudgeTest.class.getMethod("todo3", String.class,Integer.class,int.class);
		clazz = Judge.getParamType("cc", method);
		assertThat(clazz==int.class,is(true));
	}

	@Test(expected=RepositoryException.class)
	public void testGetParamType2() throws NoSuchMethodException, SecurityException {
		Method method = JudgeTest.class.getMethod("todo1", String.class,Integer.class,int.class);
		Class<?> clazz = Judge.getParamType("aa", method);
		assertThat(clazz==Integer.class,is(true));
	}

}
