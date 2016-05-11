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

package org.fastquery.util;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.fastquery.core.Placeholder;
import org.fastquery.core.Query;
import org.fastquery.util.TypeUtil;
import org.fastquery.where.COperator;
import org.fastquery.where.Condition;
import org.fastquery.where.Operator;
import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import static org.hamcrest.Matchers.*;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class TypeUtilTest implements Opcodes {

	@Test
	public void testGetTypeInfo() {
		Object[] strs = TypeUtil.getTypeInfo("I");
		assertThat(strs[0], equalTo("java/lang/Integer"));
		assertThat(strs[1], equalTo("intValue"));
		assertThat(strs[2], equalTo("()I"));
		assertThat(strs[3], equalTo(IRETURN));

		strs = TypeUtil.getTypeInfo("Z");
		assertThat(strs[0], equalTo("java/lang/Boolean"));
		assertThat(strs[1], equalTo("booleanValue"));
		assertThat(strs[2], equalTo("()Z"));
		assertThat(strs[3], equalTo(IRETURN));

		strs = TypeUtil.getTypeInfo("B");
		assertThat(strs[0], equalTo("java/lang/Byte"));
		assertThat(strs[1], equalTo("byteValue"));
		assertThat(strs[2], equalTo("()B"));
		assertThat(strs[3], equalTo(IRETURN));

		strs = TypeUtil.getTypeInfo("C");
		assertThat(strs[0], equalTo("java/lang/Character"));
		assertThat(strs[1], equalTo("charValue"));
		assertThat(strs[2], equalTo("()C"));
		assertThat(strs[3], equalTo(IRETURN));

		strs = TypeUtil.getTypeInfo("D");
		assertThat(strs[0], equalTo("java/lang/Double"));
		assertThat(strs[1], equalTo("doubleValue"));
		assertThat(strs[2], equalTo("()D"));
		assertThat(strs[3], equalTo(DRETURN));

		strs = TypeUtil.getTypeInfo("F");
		assertThat(strs[0], equalTo("java/lang/Float"));
		assertThat(strs[1], equalTo("floatValue"));
		assertThat(strs[2], equalTo("()F"));
		assertThat(strs[3], equalTo(FRETURN));

		strs = TypeUtil.getTypeInfo("J");
		assertThat(strs[0], equalTo("java/lang/Long"));
		assertThat(strs[1], equalTo("longValue"));
		assertThat(strs[2], equalTo("()J"));
		assertThat(strs[3], equalTo(LRETURN));

		strs = TypeUtil.getTypeInfo("S");
		assertThat(strs[0], equalTo("java/lang/Short"));
		assertThat(strs[1], equalTo("shortValue"));
		assertThat(strs[2], equalTo("()S"));
		assertThat(strs[3], equalTo(IRETURN));

	}

	@Test
	public void testGetMethod() {
		List<Class<?>> clazzs = JarListClass.jarClasses("/web/progm/java/jdk1.8.0_45/jre/lib");
		clazzs.addAll( JarListClass.jarClasses("/web/progm/java/jdk1.8.0_45/jre/lib"));		
		
		long start = System.currentTimeMillis();
		for (Class<?> clazz : clazzs) {
			Method[] methods = clazz.getMethods();
			for (Method method : methods) {
				String methodName = method.getName();
				String methodDescriptor = Type.getType(method).getDescriptor();
				Method m2 = TypeUtil.getMethod(clazz, methodName, methodDescriptor);
				assertThat(method.getParameterCount(), equalTo(m2.getParameterCount()));
				Parameter[] parameters = method.getParameters();
				Parameter[] parameters2 = m2.getParameters();
				for (int i = 0; i < parameters.length; i++) {
					assertThat((parameters[i].getType() == parameters2[i].getType()), is(true));
				}
				assertThat(methodName, equalTo(m2.getName()));
				assertThat(methodDescriptor, equalTo(Type.getType(m2).getDescriptor()));
			}
		}
		System.out.println( "testGetMethod,共测试了"+clazzs.size()+"个类, 用时: " + (System.currentTimeMillis() - start) +" 毫秒!");
	}

	// 重大发现
	@Test
	public void testClass(){
		List<Class<?>> clazzs = JarListClass.jarClasses("/web/progm/java/jdk1.8.0_45/jre/lib");
		clazzs.addAll( JarListClass.jarClasses("/web/progm/java/jdk1.8.0_45/jre/lib"));
		for (Class<?> clazz : clazzs) {
			Method[] methods = clazz.getDeclaredMethods();
			for (Method method : methods) {
				if(method.getDeclaringClass()!=clazz){
					System.out.println("当前方法的声明类为:" + method.getDeclaringClass());
					System.out.println("当前clazz为:" + clazz);
					System.exit(2);
				}
			}
		}
	}
	
	@Test
	public void testMatches() throws ClassNotFoundException {

	}

	@Test
	public void testMatchesNotrepeat() {
	}

	@Test
	public void testMatcheAll() {
	}

	@Test
	public void testGetSQLParameter() {
	}

	@Test
	public void testContainsIgnoreCase() {
	}

	@Test
	public void testHasDefaultConstructor() {
	}

	@Test
	public void testFindId() {
	}

	// 别删除用做测试用
	@Query("select * from Student #{#where} order by desc")
	// 增加一些条件
	@Condition(l = "field1", o = Operator.EQ, r = "?1") // ?1的值,如果是null,
														// 该行条件将不参与运算
	@Condition(c = COperator.AND, l = "field2", o = Operator.EQ, r = "?2")
	@Condition(c = COperator.AND, l = "field3", o = Operator.EQ, r = "?3", ignoreNull = false) // ?3的值是null,该条件也参与运算.
	@Condition(c = COperator.OR, l = "age", o = Operator.IN, r = "(?3,?7,?8)") // age
																				// in(?3,?7?8)
	@Condition(c = COperator.AND, l = "name", o = { Operator.NOT, Operator.LIKE }, r = "?7") // 等效于
																								// name
																								// not
																								// like
																								// ?7
	@Condition(c = COperator.OR, l = "info", o = Operator.BETWEEN, r = "?8 and ?9") // 等效于
																					// info
																					// between
																					// ?8
																					// and
																					// ?9
	public void method01() {
	}

	/*
	 * 
	 * public void conditionParameterFilter() throws NoSuchMethodException,
	 * SecurityException{ ConditionParameterFilter conditionParameterFilter =
	 * new ConditionParameterFilter(); Method method =
	 * TypeUtilTest.class.getMethod("method01");
	 * conditionParameterFilter.doFilter(method); }
	 * 
	 * @Test public void testGetQuerySQL1() throws NoSuchMethodException,
	 * SecurityException { Method method =
	 * TypeUtilTest.class.getMethod("method01"); String sql =
	 * TypeUtil.getQuerySQL(method, method.getAnnotation(Query.class));
	 * assertThat(sql, equalToIgnoringCase(
	 * "select * from Student   field1 = ?1 and field2 = ?2 and field3 = ?3 or age in (?3,?7,?8) and name not like ?7 or info BETWEEN ?8 and ?9 ORDER BY DESC"
	 * )); assertThat(sql, equalTo(
	 * "select * from Student   field1 = ?1 AND field2 = ?2 AND field3 = ?3 OR age IN (?3,?7,?8) AND name NOT LIKE ?7 OR info BETWEEN ?8 and ?9 order by desc"
	 * )); }
	 */
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
	public void filterComments(){
		assertThat(TypeUtil.filterComments("/* \n abc */123\n /* 123 */"), equalTo("123\n "));
		assertThat(TypeUtil.filterComments("/*** * 111*/abc/*111*/222/*** *333*/"),equalTo("abc222"));
	}
	
	public Map<String, String > todo1(){
		return null;
	}
	public Map<String, Integer > todo2(){
		return null;
	}
	public Map<String, Object > todo3(){
		return null;
	}
	public List<Map<String, Object >> todo4(){
		return null;
	}
	
	@Test
	public void isMapSO() throws NoSuchMethodException, SecurityException{
		java.lang.reflect.Type type1 = TypeUtilTest.class.getMethod("todo1").getGenericReturnType();
		assertThat(TypeUtil.isMapSO(type1), is(false));
		
		java.lang.reflect.Type type2 = TypeUtilTest.class.getMethod("todo2").getGenericReturnType();
		assertThat(TypeUtil.isMapSO(type2), is(false));
		
		java.lang.reflect.Type type3 = TypeUtilTest.class.getMethod("todo3").getGenericReturnType();
		assertThat(TypeUtil.isMapSO(type3), is(true));
	}
	
	@Test
	public void isListMapSO() throws NoSuchMethodException, SecurityException{		
		java.lang.reflect.Type type1 = TypeUtilTest.class.getMethod("todo1").getGenericReturnType();
		assertThat(TypeUtil.isListMapSO(type1), is(false));
		
		java.lang.reflect.Type type2 = TypeUtilTest.class.getMethod("todo2").getGenericReturnType();
		assertThat(TypeUtil.isListMapSO(type2), is(false));
		
		java.lang.reflect.Type type3 = TypeUtilTest.class.getMethod("todo3").getGenericReturnType();
		assertThat(TypeUtil.isListMapSO(type3), is(false));
		
		java.lang.reflect.Type type4 = TypeUtilTest.class.getMethod("todo4").getGenericReturnType();
		assertThat(TypeUtil.isListMapSO(type4), is(true));
	}
}





