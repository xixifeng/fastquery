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
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import org.fastquery.bean.Student;
import org.fastquery.bean.UserInfo;
import org.junit.Test;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class BeanUtilTest {
	
	final class T {
		private Integer key;
		public Integer getKey() {
			return key;
		}
		public void setKey(Integer key) {
			this.key = key;
		} 
	}
	
	/*
	@Test
	public void testToInsertSQL() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, IntrospectionException {
		UserInfo userInfo1 = new UserInfo(33,"想向公主",18);
		UserInfo userInfo2 = new UserInfo(34,"程家洛",20);
		UserInfo userInfo3 = new UserInfo(35,"于与同",null);
		String sql = BeanUtil.toInsertSQL(userInfo1,userInfo2,userInfo3);
		System.out.println(sql);
		
		sql = BeanUtil.toInsertSQL(userInfo1);
		assertThat(sql, equalTo("insert into UserInfo(id,name,age) values('33','想向公主','18')"));
	}
	*/
	
	@Test
	public void testToInsertSQL(){
		UserInfo userInfo = new UserInfo(33,"想向公主",18);
		String sql  = BeanUtil.toInsertSQL(userInfo,false);
		assertThat(sql, equalTo("insert into `UserInfo`(`id`,`name`,`age`) values('33','想向公主','18')"));
		
		// 主键设置为null
		userInfo.setId(null);
		sql  = BeanUtil.toInsertSQL(userInfo,false);
		assertThat(sql, equalTo("insert into `UserInfo`(`name`,`age`) values('想向公主','18')"));
		
		userInfo.setName(null);
		sql  = BeanUtil.toInsertSQL(userInfo,false);
		assertThat(sql, equalTo("insert into `UserInfo`(`name`,`age`) values(null,'18')"));
		
		userInfo.setAge(null);
		sql  = BeanUtil.toInsertSQL(userInfo,false);
		assertThat(sql, equalTo("insert into `UserInfo`(`name`,`age`) values(null,null)"));
		
		T t = new T();
		sql  = BeanUtil.toInsertSQL(t,false);
		assertThat(sql, equalTo("insert into `T`(`key`) values(null)"));
	}
	
	@Test
	public void testToInsertSQL2() {
		UserInfo userInfo = new UserInfo(33,"想向公主",18);
		String sql  = BeanUtil.toInsertSQL(userInfo,true);
		assertThat(sql, equalTo("insert into `${dbpre}`.`UserInfo`(`id`,`name`,`age`) values('33','想向公主','18')"));
		
		// 主键设置为null
		userInfo.setId(null);
		sql  = BeanUtil.toInsertSQL(userInfo,true);
		assertThat(sql, equalTo("insert into `${dbpre}`.`UserInfo`(`name`,`age`) values('想向公主','18')"));
		
		userInfo.setName(null);
		sql  = BeanUtil.toInsertSQL(userInfo,true);
		assertThat(sql, equalTo("insert into `${dbpre}`.`UserInfo`(`name`,`age`) values(null,'18')"));
		
		userInfo.setAge(null);
		sql  = BeanUtil.toInsertSQL(userInfo,true);
		assertThat(sql, equalTo("insert into `${dbpre}`.`UserInfo`(`name`,`age`) values(null,null)"));
		
		T t = new T();
		sql  = BeanUtil.toInsertSQL(t,true);
		assertThat(sql, equalTo("insert into `${dbpre}`.`T`(`key`) values(null)"));
	}
	
	@Test
	public void toSelectSQL(){
		UserInfo userInfo = new UserInfo(33,"想向公主",18);

		String sql = BeanUtil.toSelectSQL(userInfo, 36, "xk");
		
		assertThat(sql, equalTo("select * from `xk`.`UserInfo` where `id` = 36"));
	}

	@Test
	public void testParseList(){
		String[] strs = {"AA","BB","CC"};
		Object vs = BeanUtil.parseList(strs);
		assertThat(vs.toString(), equalTo("\"AA\",\"BB\",\"CC\""));
		
		List<String> strings = new ArrayList<>();
		strings.add("aa");
		strings.add("bb");
		strings.add("cc");
		vs = BeanUtil.parseList(strings);
		assertThat(vs.toString(), equalTo("\"aa\",\"bb\",\"cc\""));
	}
	
	
	@SuppressWarnings("unchecked")
	@Test
	public void testToUpdateSQL(){
		UserInfo userInfo1 = new UserInfo(33,"想向公主",18);
		Object[] updateInfo = BeanUtil.toUpdateSQL(userInfo1,"xk");
		assertThat(updateInfo[0].toString(), equalTo("update `xk`.`UserInfo` set `name`=?, `age`=? where `id`=?"));
		List<Object> args = (List<Object>) updateInfo[1];
		assertThat(args.get(0).toString(), equalTo("想向公主"));
		assertThat(args.get(1).toString(), equalTo("18"));
		assertThat(args.get(2).toString(), equalTo("33"));
		assertThat(updateInfo[2].toString(), equalTo("select * from `xk`.`UserInfo` where `id` = 33"));
		
		userInfo1 = new UserInfo(38,"向公主",23);
		updateInfo = BeanUtil.toUpdateSQL(userInfo1,null);
		assertThat(updateInfo[0].toString(), equalTo("update `UserInfo` set `name`=?, `age`=? where `id`=?"));
		args = (List<Object>) updateInfo[1];
		assertThat(args.get(0).toString(), equalTo("向公主"));
		assertThat(args.get(1).toString(), equalTo("23"));
		assertThat(args.get(2).toString(), equalTo("38"));
		assertThat(updateInfo[2].toString(), equalTo("select * from `UserInfo` where `id` = 38"));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testToUpdateSQL2(){
		Integer id = 33;
		String name = "想向公主";
		Integer age = 18;
		UserInfo userInfo1 = new UserInfo(id, name, age);
		Object[] updateInfo  = BeanUtil.toUpdateSQL(userInfo1,"xk", "name = :name");
		assertThat(updateInfo[0].toString(), equalTo("update `xk`.`UserInfo` set `id`=?, `age`=? where name = ?"));
		List<Object> args = (List<Object>) updateInfo[1];
		assertThat(args.size(), is(3));
		assertThat(args.get(0), equalTo(id));
		assertThat(args.get(1), equalTo(age));
		assertThat(args.get(2), equalTo(name));
		
		
		id = 33;
		name = "想向公主";
		age = 18;
		userInfo1 = new UserInfo(id, name, age);
		updateInfo  = BeanUtil.toUpdateSQL(userInfo1,null, "name = :name");
		assertThat(updateInfo[0].toString(), equalTo("update `UserInfo` set `id`=?, `age`=? where name = ?"));
		args = (List<Object>) updateInfo[1];
		assertThat(args.size(), is(3));
		assertThat(args.get(0), equalTo(id));
		assertThat(args.get(1), equalTo(age));
		assertThat(args.get(2), equalTo(name));
		
		
		// 根据姓名修改姓名
		Student student = new Student("113", "zhangsan", "男", 18, "计算机");
		student.setDept(null);
		updateInfo  = BeanUtil.toUpdateSQL(student,null,"name = '张三'");
		assertThat(updateInfo[0].toString(), equalTo("update `Student` set `no`=?, `name`=?, `sex`=?, `age`=? where name = '张三'"));
		
		
	}
	
	@Test
	public void toUpdate3(){
		String sql = BeanUtil.toInsertSQL(new UserInfo(null, "zhansan", 30),false);
		assertThat(sql, equalTo("insert into `UserInfo`(`name`,`age`) values('zhansan','30')"));
	}
	
	
	@Test
	public void testReset(){
		UserInfo u2 = BeanUtil.newBeanVarNull(UserInfo.class); 
		assertThat(u2.getId(),nullValue());
		assertThat(u2.getName(),nullValue());
		assertThat(u2.getAge(),nullValue());
	}
}




