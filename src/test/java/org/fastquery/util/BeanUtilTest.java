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

package org.fastquery.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.alibaba.fastjson.JSON;
import org.fastquery.bean.Fish;
import org.fastquery.bean.Student;
import org.fastquery.bean.TypeTest;
import org.fastquery.bean.UserInfo;
import org.fastquery.struct.SQLValue;
import org.fastquery.core.SelectField;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class BeanUtilTest {

	private static final Logger LOG = LoggerFactory.getLogger(BeanUtilTest.class);

	final static class T {
		private Integer key;

		public Integer getKey() {
			return key;
		}

		public void setKey(Integer key) {
			this.key = key;
		}
	}

	@Test
	public void testToInsertSQL() {
		UserInfo userInfo = new UserInfo(33, "想向公主", 18);
		String sql = BeanUtil.toInsertSQL(userInfo);
		assertThat(sql, equalTo("insert into UserInfo(id,name,age) values(33,'想向公主',18)"));

		// 主键设置为null
		userInfo.setId(null);
		sql = BeanUtil.toInsertSQL(userInfo);
		assertThat(sql, equalTo("insert into UserInfo(name,age) values('想向公主',18)"));

		userInfo.setName(null);
		sql = BeanUtil.toInsertSQL(userInfo);
		assertThat(sql, equalTo("insert into UserInfo(name,age) values(null,18)"));

		userInfo.setAge(null);
		sql = BeanUtil.toInsertSQL(userInfo);
		assertThat(sql, equalTo("insert into UserInfo(name,age) values(null,null)"));

		T t = new T();
		sql = BeanUtil.toInsertSQL(t);
		assertThat(sql, equalTo("insert into T(key) values(null)"));
	}

	@Test
	public void testToInsertSQL2() {
		UserInfo userInfo = new UserInfo(33, "想向公主", 18);
		String sql = BeanUtil.toInsertSQL(userInfo);
		assertThat(sql, equalTo("insert into UserInfo(id,name,age) values(33,'想向公主',18)"));

		// 主键设置为null
		userInfo.setId(null);
		sql = BeanUtil.toInsertSQL(userInfo);
		assertThat(sql, equalTo("insert into UserInfo(name,age) values('想向公主',18)"));

		userInfo.setName(null);
		sql = BeanUtil.toInsertSQL(userInfo);
		assertThat(sql, equalTo("insert into UserInfo(name,age) values(null,18)"));

		userInfo.setAge(null);
		sql = BeanUtil.toInsertSQL(userInfo);
		assertThat(sql, equalTo("insert into UserInfo(name,age) values(null,null)"));

		T t = new T();
		sql = BeanUtil.toInsertSQL(t);
		assertThat(sql, equalTo("insert into T(key) values(null)"));
	}

	@Test
	public void beansToInsertSQL1() {
		List<UserInfo> userInfos = new ArrayList<>();
		String str = BeanUtil.toInsertSQL(userInfos, null, false);
		assertThat(str, nullValue());
		str = BeanUtil.toInsertSQL(null, null, false);
		assertThat(str, nullValue());
	}

	@Test
	public void beansToInsertSQL2() {
		List<UserInfo> userInfos = new ArrayList<>();
		userInfos.add(new UserInfo("牵牛花", 3));
		String str = BeanUtil.toInsertSQL(userInfos, null, false);
		assertThat(str, equalToIgnoringCase("insert into UserInfo(name,age) values('牵牛花',3)"));
		userInfos.clear();

		userInfos.add(new UserInfo("牵牛花", null));
		str = BeanUtil.toInsertSQL(userInfos, null, false);
		assertThat(str, equalToIgnoringCase("insert into UserInfo(name,age) values('牵牛花',null)"));
		userInfos.clear();

		userInfos.add(new UserInfo(null, 3));
		str = BeanUtil.toInsertSQL(userInfos, null, false);
		assertThat(str, equalToIgnoringCase("insert into UserInfo(name,age) values(null,3)"));
		userInfos.clear();

		userInfos.add(new UserInfo(null, null));
		str = BeanUtil.toInsertSQL(userInfos, null, false);
		assertThat(str, equalToIgnoringCase("insert into UserInfo(name,age) values(null,null)"));
		userInfos.clear();

		userInfos.add(new UserInfo(null, "abc", null));
		str = BeanUtil.toInsertSQL(userInfos, null, false);
		assertThat(str, equalToIgnoringCase("insert into UserInfo(name,age) values('abc',null)"));
		userInfos.clear();

		userInfos.add(new UserInfo(null, "叶'兰", null));
		str = BeanUtil.toInsertSQL(userInfos, null, false);
		assertThat(str, equalToIgnoringCase("insert into UserInfo(name,age) values('叶''兰',null)"));
	}

	@Test
	public void beansToInsertSQL3() {
		List<UserInfo> userInfos = new ArrayList<>();
		userInfos.add(new UserInfo("牵牛花", 3));
		userInfos.add(new UserInfo(10, "松'鼠", 5));
		String str = BeanUtil.toInsertSQL(userInfos, null, false);
		assertThat(str, equalToIgnoringCase("insert into UserInfo(name,age) values('牵牛花',3),(10,'松''鼠',5)"));
	}

	@Test
	public void toSelectSQL1() {
		UserInfo userInfo = new UserInfo(33, "函数式编程", 18);

		String sql = BeanUtil.toSelectSQL(userInfo, 36, "xk",true,new SelectField<UserInfo>(UserInfo.class, false).getFields());
		assertThat(sql, equalTo("select id,name,age from xk.UserInfo where id = 36"));
	}
	
	@Test
	public void toSelectSQL2() {
		UserInfo userInfo = new UserInfo(33, "函数式编程", 18);

		String sql = BeanUtil.toSelectSQL(userInfo, 36, "xk",false,null);
		assertThat(sql, equalTo("select id from xk.UserInfo where id = 36"));
	}

	@Test
	public void toSelectSQL3(){
		UserInfo userInfo = new UserInfo(33, "函数式编程", 18);
		SQLValue sqlValue = BeanUtil.toSelectSQL(userInfo, null,true,"id","name");
		String sql = sqlValue.getSql();
		List<Object> list = sqlValue.getValues();
		assertThat(list.size(), is(3));
		assertThat(sql,equalTo("select id,name from UserInfo where id = ? and name = ? and age = ? limit 1"));
		assertThat(list.get(0),equalTo(33));
		assertThat(list.get(1),is("函数式编程"));
		assertThat(list.get(2),is(18));

		userInfo = new UserInfo(33, null,18);
		sqlValue = BeanUtil.toSelectSQL(userInfo, null,true);
		sql = sqlValue.getSql();
		list = sqlValue.getValues();
		assertThat(list.size(), is(2));
		assertThat(sql,equalTo("select id,name,age from UserInfo where id = ? and age = ? limit 1"));
		assertThat(list.get(0),is(33));
		assertThat(list.get(1),is(18));

		userInfo = new UserInfo(33, null,null);
		sqlValue = BeanUtil.toSelectSQL(userInfo, null,false,"name");
		sql = sqlValue.getSql();
		list = sqlValue.getValues();
		assertThat(list.size(), is(1));
		assertThat(sql,equalTo("select id,age from UserInfo where id = ? limit 1"));
		assertThat(list.get(0),is(33));

		userInfo = new UserInfo(null, null,null);
		sqlValue = BeanUtil.toSelectSQL(userInfo, null,false,"name","id");
		sql = sqlValue.getSql();
		list = sqlValue.getValues();
		assertThat(list.size(), is(0));
		assertThat(sql,equalTo("select age from UserInfo limit 1"));
	}

	@Test
	public void toCount(){
		UserInfo userInfo = new UserInfo(33, "函数式编程", 18);
		SQLValue sqlValue = BeanUtil.toCount(userInfo, null);
		String sql = sqlValue.getSql();
		List<Object> list = sqlValue.getValues();
		assertThat(list.size(), is(2));
		assertThat(sql,equalTo("select count(id) from UserInfo where id = 33 and name = ? and age = ?"));
		assertThat(list.get(0),equalTo("函数式编程"));
		assertThat(list.get(1),is(18));

		userInfo = new UserInfo(33, null,18);
		sqlValue = BeanUtil.toCount(userInfo, null);
		sql = sqlValue.getSql();
		list = sqlValue.getValues();
		assertThat(list.size(), is(1));
		assertThat(sql,equalTo("select count(id) from UserInfo where id = 33 and age = ?"));
		assertThat(list.get(0),is(18));

		userInfo = new UserInfo(33, null,null);
		sqlValue = BeanUtil.toCount(userInfo, null);
		sql = sqlValue.getSql();
		list = sqlValue.getValues();
		assertThat(list.size(), is(0));
		assertThat(sql,equalTo("select count(id) from UserInfo where id = 33"));

		userInfo = new UserInfo(null, null,null);
		sqlValue = BeanUtil.toCount(userInfo, null);
		sql = sqlValue.getSql();
		list = sqlValue.getValues();
		assertThat(list.size(), is(0));
		assertThat(sql,equalTo("select count(id) from UserInfo"));

		userInfo = new UserInfo(null, "小燕子",18);
		sqlValue = BeanUtil.toCount(userInfo, null);
		sql = sqlValue.getSql();
		list = sqlValue.getValues();
		assertThat(list.size(), is(2));
		assertThat(sql,equalTo("select count(id) from UserInfo where name = ? and age = ?"));
		assertThat(list.get(0),equalTo("小燕子"));
		assertThat(list.get(1),is(18));

		userInfo = new UserInfo(null, "小燕子",null);
		sqlValue = BeanUtil.toCount(userInfo, null);
		sql = sqlValue.getSql();
		list = sqlValue.getValues();
		assertThat(list.size(), is(1));
		assertThat(sql,equalTo("select count(id) from UserInfo where name = ?"));
		assertThat(list.get(0),equalTo("小燕子"));

		userInfo = new UserInfo(null, null,13);
		sqlValue = BeanUtil.toCount(userInfo, null);
		sql = sqlValue.getSql();
		list = sqlValue.getValues();
		assertThat(list.size(), is(1));
		assertThat(sql,equalTo("select count(id) from UserInfo where age = ?"));
		assertThat(list.get(0),is(13));
	}

	@Test
	public void testParseList() {
		String[] strs = { "AA", "BB", "CC" };
		Object vs = BeanUtil.parseList(strs);
		assertThat(vs.toString(), equalTo("\"AA\",\"BB\",\"CC\""));

		List<String> strings = new ArrayList<>();
		strings.add("aa");
		strings.add("bb");
		strings.add("cc");
		vs = BeanUtil.parseList(strings);
		assertThat(vs.toString(), equalTo("\"aa\",\"bb\",\"cc\""));
	}
	
	private static <B> String toFields(Field[] fields, B bean) throws Exception {
		Method method = BeanUtil.class.getDeclaredMethod("toFields", fields.getClass(), Object.class);
		method.setAccessible(true);
		return (String) method.invoke(null,fields, bean);
	}
	
	@Test
	public void toFields() throws Exception {
		Class<UserInfo> clazz = UserInfo.class;
		Field[] fields = clazz.getDeclaredFields();
		UserInfo bean = new UserInfo(null, "叶'兰", null);
		String str = toFields(fields, bean);
		assertThat(str, equalTo("(name,age)"));

		bean = new UserInfo(1, "叶'兰", null);
		str = toFields(fields, bean);
		assertThat(str, equalTo("(id,name,age)"));
	}

	private static <B> String toValue(Field[] fields, B bean,boolean containMark) throws Exception {
		Method method = BeanUtil.class.getDeclaredMethod("toValue", Field[].class,Object.class,boolean.class);
		method.setAccessible(true);
		return (String) method.invoke(null, fields,bean,containMark);
	}
	
	@Test
	public void toValue() throws Exception {
		UserInfo u = new UserInfo();
		Class<UserInfo> clazz = UserInfo.class;
		Field[] fields = clazz.getDeclaredFields();
		String str = toValue(fields, u,false);
		assertThat(str, equalTo("('',null)"));

		u = new UserInfo(null, "叶'兰", null);
		str = toValue(fields, u,false);
		assertThat(str, equalTo("('叶''兰',null)"));

		u = new UserInfo(1, "叶'兰", 2);
		str = toValue(fields, u,false);
		assertThat(str, equalTo("(1,'叶''兰',2)"));

		str = toValue(fields, u,true);
		assertThat(str, equalTo("values(1,'叶''兰',2)"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testToUpdateSQL() {
		UserInfo userInfo1 = new UserInfo(33, "想向公主", 18);
		Object[] updateInfo = BeanUtil.toUpdateSQL(userInfo1, "xk", true);
		assertThat(updateInfo[0].toString(), equalTo("update xk.UserInfo set name=?, age=? where id=?"));
		List<Object> args = (List<Object>) updateInfo[1];
		assertThat(args.get(0).toString(), equalTo("想向公主"));
		assertThat(args.get(1).toString(), equalTo("18"));
		assertThat(args.get(2).toString(), equalTo("33"));
		assertThat(updateInfo[2].toString(), equalTo("select * from xk.UserInfo where id = 33"));

		userInfo1 = new UserInfo(38, "向公主", 23);
		updateInfo = BeanUtil.toUpdateSQL(userInfo1, null, true);
		assertThat(updateInfo[0].toString(), equalTo("update UserInfo set name=?, age=? where id=?"));
		args = (List<Object>) updateInfo[1];
		assertThat(args.get(0).toString(), equalTo("向公主"));
		assertThat(args.get(1).toString(), equalTo("23"));
		assertThat(args.get(2).toString(), equalTo("38"));
		assertThat(updateInfo[2].toString(), equalTo("select * from UserInfo where id = 38"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testToUpdateSQL2() {
		Integer id = 33;
		String name = "想向公主";
		Integer age = 18;
		UserInfo userInfo1 = new UserInfo(id, name, age);
		Object[] updateInfo = BeanUtil.toUpdateSQL(userInfo1, "xk", "name = :name");
		assertThat(updateInfo[0].toString(), equalTo("update xk.UserInfo set id=?, age=? where name = ?"));
		List<Object> args = (List<Object>) updateInfo[1];
		assertThat(args.size(), is(3));
		assertThat(args.get(0), equalTo(id));
		assertThat(args.get(1), equalTo(age));
		assertThat(args.get(2), equalTo(name));

		id = 33;
		name = "想向公主";
		age = 18;
		userInfo1 = new UserInfo(id, name, age);
		updateInfo = BeanUtil.toUpdateSQL(userInfo1, null, "name = :name");
		assertThat(updateInfo[0].toString(), equalTo("update UserInfo set id=?, age=? where name = ?"));
		args = (List<Object>) updateInfo[1];
		assertThat(args.size(), is(3));
		assertThat(args.get(0), equalTo(id));
		assertThat(args.get(1), equalTo(age));
		assertThat(args.get(2), equalTo(name));

		// 根据姓名修改姓名
		Student student = new Student("113", "zhangsan", "男", 18, "计算机");
		student.setDept(null);
		updateInfo = BeanUtil.toUpdateSQL(student, null, "name = '张三'");
		assertThat(updateInfo[0].toString(), equalTo("update Student set no=?, name=?, sex=?, age=? where name = '张三'"));

	}

	@Test
	public void toUpdate3() {
		String sql = BeanUtil.toInsertSQL(new UserInfo(null, "zhansan", 30));
		assertThat(sql, equalTo("insert into UserInfo(name,age) values('zhansan',30)"));
	}

	@Test
	public void toUpdateSQL1() {
		List<UserInfo> userInfos = new ArrayList<>();
		userInfos.add(new UserInfo(77, "茝若", 18));
		userInfos.add(new UserInfo(88, "芸兮", null));
		userInfos.add(new UserInfo(99, "梓", 16));

		String sql = BeanUtil.toUpdateSQL(userInfos, null);

		assertThat(sql, equalTo(
				"update UserInfo set name = case id when 77 then '茝若' when 88 then '芸兮' when 99 then '梓' else name end,age = case id when 77 then '18' when 88 then age when 99 then '16' else age end where id in(77,88,99)"));
	}

	@Test
	public void toUpdateSQL2() {
		List<UserInfo> userInfos = new ArrayList<>();
		userInfos.add(new UserInfo(77, "茝若", 18));
		userInfos.add(new UserInfo(88, null, null));
		userInfos.add(new UserInfo(99, "梓", 16));

		String sql = BeanUtil.toUpdateSQL(userInfos, null);
		assertThat(sql, equalTo(
				"update UserInfo set name = case id when 77 then '茝若' when 88 then name when 99 then '梓' else name end,age = case id when 77 then '18' when 88 then age when 99 then '16' else age end where id in(77,88,99)"));
	}

	@Test
	public void toUpdateSQL3() {
		List<UserInfo> userInfos = new ArrayList<>();
		userInfos.add(new UserInfo(77, "茝若", null));
		userInfos.add(new UserInfo(88, null, null));
		userInfos.add(new UserInfo(99, "梓", null));

		String sql = BeanUtil.toUpdateSQL(userInfos, null);

		assertThat(sql, equalTo(
				"update UserInfo set name = case id when 77 then '茝若' when 88 then name when 99 then '梓' else name end,age = case id when 77 then age when 88 then age when 99 then age else age end where id in(77,88,99)"));
	}

	@Test
	public void toUpdateSQL4() {
		List<UserInfo> userInfos = new ArrayList<>();
		userInfos.add(new UserInfo(77, null, null));
		userInfos.add(new UserInfo(88, null, null));
		userInfos.add(new UserInfo(99, null, 16));

		String sql = BeanUtil.toUpdateSQL(userInfos, null);
		assertThat(sql, equalTo(
				"update UserInfo set name = case id when 77 then name when 88 then name when 99 then name else name end,age = case id when 77 then age when 88 then age when 99 then '16' else age end where id in(77,88,99)"));
	}

	@Test
	public void toUpdateSQL5() {
		TypeTest tt1 = new TypeTest(1L,true,false,true,"男");
		TypeTest tt2 = new TypeTest(3L,false,true,false,"女");
		TypeTest tt3 = new TypeTest(5L,true,false,true,"男");
		List<TypeTest> list = Stream.of(tt1,tt2,tt3).collect(Collectors.toList());

		String sql = BeanUtil.toUpdateSQL(list, null);
		assertThat(sql, equalTo(
				"update TypeTest set deleted = case id when 1 then true when 3 then false when 5 then true else deleted end,activated = case id when 1 then false when 3 then true when 5 then false else activated end,auth = case id when 1 then true when 3 then false when 5 then true else auth end,gender = case id when 1 then '男' when 3 then '女' when 5 then '男' else gender end where id in(1,3,5)"));
	}

	@Test
	public void toDelete() {
		String tableName = "Student";
		String keyName = "uuid";
		long keyVal = 18;
		String dbName = "Pe";
		String sql = BeanUtil.toDelete(tableName, keyName, keyVal, dbName);
		assertThat(sql, equalTo("delete from Pe.Student where uuid=18"));
	}

	@Test
	public void testReset() {
		UserInfo u2 = BeanUtil.newBeanVarNull(UserInfo.class);
		assertThat(u2.getId(), nullValue());
		assertThat(u2.getName(), nullValue());
		assertThat(u2.getAge(), nullValue());
	}
	
	private static Field getField(String name) {
		Field[] fields = BeanUtil.getFields(Fish.class);
		for (Field field : fields) {
			if(field.getName().equals(name)) {
				return field;
			}
		}
		return null;
	}
	@Test
	public void feild() {
		Field[] fields = BeanUtil.getFields(Fish.class);
		assertThat(fields.length, is(3));
		assertThat(getField("id"), notNullValue());
		assertThat(getField("name"), notNullValue());
		assertThat(getField("num"), notNullValue());		
	}

	@Test
	public void toEachOne() {
		String sql = "select xx,yy,zz from table where aa = bb and cc=dd and ee =ff";
		String[] sqls = BeanUtil.toEachOne(sql);
		LOG.debug("sqls:{}",JSON.toJSONString(sqls));
		assertThat(sqls.length,is(3));
		assertThat(sqls[0],equalTo("select xx,yy,zz from table where aa = bb"));
		assertThat(sqls[1],equalTo("select xx,yy,zz from table where cc=dd"));
		assertThat(sqls[2],equalTo("select xx,yy,zz from table where ee =ff"));
	}
	
	
}
