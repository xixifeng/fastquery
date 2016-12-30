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

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.fastquery.core.Id;
import org.fastquery.core.Param;
import org.fastquery.core.Placeholder;
import org.fastquery.core.Query;
import org.fastquery.core.QueryRepository;
import org.fastquery.core.Repository;
import org.fastquery.filter.BeforeFilter;
import org.fastquery.util.TypeUtil;
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
	// 测试 TypeUtil.getMethod
	public void testGetMethod() {
		
		// 找一些jar包来测试TypeUtil.getMethod这个方法
		String dir1 = "/web/progm/java/jdk1.8.0_45/jre/lib";
		String dir2 = "/web/progm/java/jdk1.8.0_45/jre/lib";
		
		// 这两个目录的jar包中,大概有16744个类,如果能通过,表明这个TypeUtil.getMethod方法还是比较强壮的.
		
		List<Class<?>> clazzs = new ArrayList<>();
		if(new File(dir1).exists()) {
			clazzs.addAll( JarListClass.jarClasses(dir1));		
		}	
		if(new File(dir2).exists()) {
			clazzs.addAll( JarListClass.jarClasses(dir2));		
		}	
		
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
	
	@Test
	public void testMatches() throws ClassNotFoundException {
			String sql = "select id,name,age from #{#limit} `userinfo` #{#where}";
			List<String> strs = TypeUtil.matches(sql, Placeholder.LIMIT_RGE);
			assertThat(strs.size(), equalTo(1));
			assertThat(strs.get(0), equalTo("#{#limit}"));
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

	public void todo(@Id Integer page,Integer size){
	}
	@Test
	public void findAnnotationIndex() throws NoSuchMethodException, SecurityException{
		Method method = TypeUtilTest.class.getMethod("todo",Integer.class,Integer.class);
		Parameter[] parameters = method.getParameters();
		assertThat(TypeUtil.findAnnotationIndex(Id.class, parameters), is(0));
	}
	
	@Test
	public void testFindId() {
	}

	// 别删除用做测试用
	@Query("select * from Student #{#where} order by desc")
	public void method01() {
	}

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
	
	// 这些内部类,用做测试
	class BeforeFilter0 extends BeforeFilter<Repository> {
		@Override
		protected void doFilter(Repository repository, Method method, Object[] args) {
		}
	}
	class BeforeFilter1 extends BeforeFilter<DB1> {
		@Override
		protected void doFilter(DB1 db1, Method method, Object[] args) {
		}
	}
	class BeforeFilter2 extends BeforeFilter<DB2> {
		@Override
		protected void doFilter(DB2 db2, Method method, Object[] args) {
		}
	}
	class BeforeFilter3 extends BeforeFilter<DB3> {
		@Override
		protected void doFilter(DB3 db3, Method method, Object[] args) {
		}
	}
	class BeforeFilter4 extends BeforeFilter<DB4> {
		@Override
		protected void doFilter(DB4 db4, Method method, Object[] args) {
		}
	}
	class BeforeFilter5 extends BeforeFilter<DB5> {
		@Override
		protected void doFilter(DB5 db5, Method method, Object[] args) {
		}
	}
	class BeforeFilter6 extends BeforeFilter<DB6> {
		@Override
		protected void doFilter(DB6 db6, Method method, Object[] args) {
		}
	}
	// 这些内部类,用做测试
	class DB1 implements Repository {
	}
	abstract class DB2 implements QueryRepository {
	}
	class DB3 implements Repository {
	}
	abstract class DB4 implements QueryRepository {
		
	}
	class DB5 implements Repository {
	}
	abstract class DB6 implements QueryRepository {
	}
	
	@Test
	public void compareType(){
		assertThat(TypeUtil.compareType(BeforeFilter1.class, DB1.class), is(true));
		assertThat(TypeUtil.compareType(BeforeFilter2.class, DB2.class), is(true));
		assertThat(TypeUtil.compareType(BeforeFilter3.class, DB3.class), is(true));
		assertThat(TypeUtil.compareType(BeforeFilter4.class, DB4.class), is(true));
		assertThat(TypeUtil.compareType(BeforeFilter5.class, DB5.class), is(true));
		assertThat(TypeUtil.compareType(BeforeFilter6.class, DB6.class), is(true));

		assertThat(TypeUtil.compareType(BeforeFilter1.class, DB6.class), is(false));
		assertThat(TypeUtil.compareType(BeforeFilter2.class, DB5.class), is(false));
		assertThat(TypeUtil.compareType(BeforeFilter3.class, DB4.class), is(false));
		assertThat(TypeUtil.compareType(BeforeFilter4.class, DB3.class), is(false));
		assertThat(TypeUtil.compareType(BeforeFilter5.class, DB2.class), is(false));
		assertThat(TypeUtil.compareType(BeforeFilter6.class, DB1.class), is(false));
		
		assertThat(TypeUtil.compareType(BeforeFilter1.class, DB2.class), is(false));
		assertThat(TypeUtil.compareType(BeforeFilter2.class, DB1.class), is(false));
		assertThat(TypeUtil.compareType(BeforeFilter3.class, DB4.class), is(false));
		assertThat(TypeUtil.compareType(BeforeFilter4.class, DB3.class), is(false));
		assertThat(TypeUtil.compareType(BeforeFilter5.class, DB6.class), is(false));
		assertThat(TypeUtil.compareType(BeforeFilter6.class, DB5.class), is(false));
		
		assertThat(TypeUtil.compareType(BeforeFilter0.class, DB1.class), is(true));
		assertThat(TypeUtil.compareType(BeforeFilter0.class, DB2.class), is(true));
		assertThat(TypeUtil.compareType(BeforeFilter0.class, DB3.class), is(true));
		assertThat(TypeUtil.compareType(BeforeFilter0.class, DB4.class), is(true));
		assertThat(TypeUtil.compareType(BeforeFilter0.class, DB5.class), is(true));
		assertThat(TypeUtil.compareType(BeforeFilter0.class, DB6.class), is(true));
		
		
	}
	
	@Test
	public void split(){
		List<String> strs = TypeUtil.tokenizeToStringArray("A.B.C.D.f", ".");
		assertThat(strs.size(), is(5));
		assertThat(strs.get(0), equalTo("A"));
		assertThat(strs.get(1), equalTo("B"));
		assertThat(strs.get(2), equalTo("C"));
		assertThat(strs.get(3), equalTo("D"));
		assertThat(strs.get(4), equalTo("f"));
	}
	
	@Test
	public void removePart(){
		String sub = TypeUtil.removePart("a ");
		assertThat(sub, equalTo("a"));
		
		sub = TypeUtil.removePart("    a ");
		assertThat(sub, equalTo("a"));
		
		sub = TypeUtil.removePart("    a                 ");
		assertThat(sub, equalTo("a"));
		
		sub = TypeUtil.removePart(" a b");
		assertThat(sub, equalTo("b"));
		
		sub = TypeUtil.removePart(" a     b");
		assertThat(sub, equalTo("b"));
		
		// 注意: 如下a与b之间的空白是键入Tab键产生的
		sub = TypeUtil.removePart(" a		b");
		assertThat(sub, equalTo("b"));
		
		sub = TypeUtil.removePart(" a\t\b\nb");
		assertThat(sub, equalTo("b"));
	}
	
	@Test
	public void parWhere(){
		String sql = "select * from Student <where>where and id = :id</where>";
		String nq = TypeUtil.parWhere(sql);
		assertThat(nq, equalTo("select * from Student where id = :id"));
		
		sql = "select * from Student <where>and id = :id</where>";
		nq = TypeUtil.parWhere(sql);
		assertThat(nq, equalTo("select * from Student where id = :id"));
		
		sql = "select * from Student <where>  	\n</where>";
		nq = TypeUtil.parWhere(sql);
		assertThat(nq, equalTo("select * from Student "));
		
		
	}
	
	@Test
	public void getFirstWord(){
		
		assertThat(TypeUtil.getFirstWord(null), nullValue());
		assertThat(TypeUtil.getFirstWord(""), is(""));
		
		String str = "3/3 source files have been analyzed";
		assertThat(TypeUtil.getFirstWord(str), is("3/3"));
		
		str = "   			files have been analyzed";
		assertThat(TypeUtil.getFirstWord(str), is("files"));
		
	}
	
	@Test
	public void matches(){
		String where = "name=:name and age = :age or sex = :sex";
		List<String> strs = TypeUtil.matches(where, ":\\S+\\b");
		for (String str : strs) {
			System.out.println(str);
		}
	}
	
	public void m1(@Param("i") int i,@Param("i1")int i1,@Param("i2")int i2){		
	}
	
	public void m2(@Param("$^i") int i,@Param("$i1$")int i1,@Param("^i2")int i2){		
	}
	
	@Test
	public void paramFilter() throws NoSuchMethodException, SecurityException{
		Method m1 = TypeUtilTest.class.getMethod("m1", int.class,int.class,int.class);
		Object[] agrs = new Object[]{11,22,33};
		String sql = "abc :i and :i1 where :i2";
		String str = TypeUtil.paramFilter(m1, agrs, sql);
		assertThat(str, equalTo("abc ?1 and ?2 where ?3"));
		sql = "abc :i1 and :i2 where :i";
		str = TypeUtil.paramFilter(m1, agrs, sql);
		assertThat(str, equalTo("abc ?2 and ?3 where ?1"));
	}
	
	@Test
	public void paramNameFilter() throws NoSuchMethodException, SecurityException{
		Method m1 = TypeUtilTest.class.getMethod("m1", int.class,int.class,int.class);
		Object[] agrs = new Object[]{11,22,33};
		String sql = "abc :i and :i1 where :i2";
		String str = TypeUtil.paramNameFilter(m1, agrs, sql);
		assertThat(str, equalTo("abc ?1 and ?2 where ?3"));
	}
	
	@Test
	public void test2(){
		String str = "\t\nabc\n\t";
		assertThat(str.trim(), equalTo("abc"));
	}
	
	@Test
	public void overChar(){
		String str = TypeUtil.overChar(-1);
		assertThat(str, equalTo(""));
		
		str = TypeUtil.overChar(0);
		assertThat(str, equalTo(""));
		
		str = TypeUtil.overChar(1);
		assertThat(str, equalTo("?"));
		
		str = TypeUtil.overChar(2);
		assertThat(str, equalTo("?,?"));
		
		str = TypeUtil.overChar(3);
		assertThat(str, equalTo("?,?,?"));
		
		str = TypeUtil.overChar(8);
		assertThat(str, equalTo("?,?,?,?,?,?,?,?"));
	}

	@Test
	public void replace1(){ 
		String str = TypeUtil.replace(null,0,1);
		assertThat(str, is(""));
		
		str = TypeUtil.replace("",0,1);
		assertThat(str, is(""));
		
		str = TypeUtil.replace(null,-1,0);
		assertThat(str, is(""));
	}
	
	@Test
	public void replace2(){
		String src = "kljgwkg?gwgw47478978?jioj2?87983lkjksj";
		int count = StringUtils.countMatches(src, "?");
		int repat = 3;
		String str = TypeUtil.replace(src,0,repat);
		assertThat(StringUtils.countMatches(str, "?"), is(count + repat - 1));
		
		for (int i = 0; i < 10; i++) {
			count = StringUtils.countMatches(src, "?");
			repat = i + 1;
			str = TypeUtil.replace(src,0,repat);
			assertThat(StringUtils.countMatches(str, "?"), is(count + repat - 1));
		}
		
		str = TypeUtil.replace(src,0,3);
		assertThat(str, equalTo("kljgwkg?,?,?gwgw47478978?jioj2?87983lkjksj"));
		
		
		str = TypeUtil.replace(src,1,2);
		assertThat(str, equalTo("kljgwkg?gwgw47478978?,?jioj2?87983lkjksj"));
		
		str = TypeUtil.replace(src,2,5);
		assertThat(str, equalTo("kljgwkg?gwgw47478978?jioj2?,?,?,?,?87983lkjksj"));
		
	}
	
	
	@Test
	public void replace3(){
		String src = "??????????kjlwkgj5?32415456?45lkjkljgw";
		String str = TypeUtil.replace(src,1,5);
		assertThat(str, equalTo("??,?,?,?,?????????kjlwkgj5?32415456?45lkjkljgw"));
		str = TypeUtil.replace(src,20,5);
		assertThat(str, equalTo("??????????kjlwkgj5?32415456?45lkjkljgw"));
	}
	
	@Test
	public void replace4(){
		String src = "";
		String str = TypeUtil.replace(src,1,5);
		assertThat(str, equalTo(""));
		str = TypeUtil.replace(src,20,5);
		assertThat(str, equalTo(""));
	}
}





