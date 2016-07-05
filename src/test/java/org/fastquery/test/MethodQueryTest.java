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

package org.fastquery.test;

import org.fastquery.bean.Student;
import org.fastquery.example.StudentDBService;
import org.fastquery.page.Page;
import org.fastquery.page.PageableImpl;
import org.fastquery.service.FQuery;
import org.fastquery.spec.MyNativeSpec1;
import org.fastquery.spec.MyNativeSpec2;
import org.fastquery.sql.NativeSpec;
import org.fastquery.sql.Predicate;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.Matchers.*;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.SelectQuery.JoinType;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class MethodQueryTest {

	private StudentDBService studentDBService;
	
	@Before
	public void before(){
		studentDBService = FQuery.getRepository(StudentDBService.class);
	}

	@Test
	public void methodFind1(){
		
		NativeSpec nativeSpec = new NativeSpec() {
			@Override
			public Predicate toPredicate(SelectQuery selectQuery) {
				// 待查找的表
				selectQuery.addCustomFromTable("Student"); 
				// 待查找的列
				selectQuery.addCustomColumns("no,name,sex,age,dept");
				// 动态增加一个条件
				selectQuery.addCondition(BinaryCondition.equalTo("no", "?1"));
				
				return build(selectQuery, "9512101");
			}
		};
		
		JSONArray jsonarr = studentDBService.find2JSON(nativeSpec,"xk3");
		System.out.println(JSON.toJSONString(jsonarr, true));
		assertThat(jsonarr.size(),is(1));
		assertThat(jsonarr.getJSONObject(0).get("no"),equalTo("9512101"));
	}
	
	@Test
	public void methodFind2(){
		
		// 数据源名称
		String dataSourceName = "xk4";
		
		// 连接池配置
		Properties properties = new Properties();
		properties.setProperty("driverClass", "com.mysql.jdbc.Driver");
		properties.setProperty("jdbcUrl", "jdbc:mysql://192.168.8.10:3306/xk4");
		properties.setProperty("user", "xk4");
		properties.setProperty("password", "abc4");
		
		// 创建一个数据源
		FQuery.createDataSource(dataSourceName, properties);
		
		NativeSpec nativeSpec = new NativeSpec() {
			@Override
			public Predicate toPredicate(SelectQuery selectQuery) {
				// 待查找的表
				selectQuery.addCustomFromTable("Student"); 
				// 待查找的列
				selectQuery.addCustomColumns("no,name,sex,age,dept");
				// 动态增加一个条件
				selectQuery.addCondition(BinaryCondition.equalTo("no", "?1"));
				
				return build(selectQuery, "9512101");
			}
		};
		
		JSONArray jsonarr = studentDBService.find2JSON(nativeSpec,"xk4");
		System.out.println(JSON.toJSONString(jsonarr, true));
		assertThat(jsonarr.size(),is(1));
		assertThat(jsonarr.getJSONObject(0).get("no"),equalTo("9512101"));
	}
	
	
	// select * from student
	@Test
	public void methodFind3(){
		List<Map<String, Object>> maps = studentDBService.find2ListMap(new MyNativeSpec1());
		assertThat(maps, not(empty()));
		
		List<Student> students = studentDBService.find2Bean(new MyNativeSpec1(), Student.class);
		// 遍历千三条看看
		for (int index = 0; index < 3; index++) {
			System.out.println(students.get(index));
		}
		assertThat(students, not(empty()));
		
		
		String countField = "no";
		String countsql = null;
		Page<Map<String, Object>> page =  studentDBService.find(new MyNativeSpec1(), new PageableImpl(1, 3), countField, countsql, false);
		assertThat(page.isHasContent(), is(true));
		assertThat(page.isFirst(), is(true));
		assertThat(page.getNumberOfElements(), not(equalTo(-1)));
		assertThat(page.getTotalElements(), not(equalTo(-1)));
	}
	
	// SELECT count(id) FROM student s JOIN sc on s.no = sc.studentNo JOIN course c on c.no = sc.courseNo;
	// SELECT count(id) FROM student s JOIN sc on s.no = sc.studentNo JOIN course c on c.no = sc.courseNo;
	@Test
	public void methodFind4(){
		
		// 1). 准备一个 NativeSpec
		NativeSpec spec = new NativeSpec() {
			@Override
			public Predicate toPredicate(SelectQuery selectQuery) {
				// 待查询的列
				selectQuery.addCustomColumns("s.name,s.sex,s.age,s.dept,c.name as courseName");
				// 待查询的表
				selectQuery.addCustomFromTable("student s");
				// 增加一个自定义关联
				selectQuery.addCustomJoin(" JOIN sc on s.no = sc.studentNo");
				// 再增加一个自定义关联
				selectQuery.addCustomJoin(" JOIN course c on c.no = sc.courseNo");
				// 增加一个自定义条件,下行等价于: (s.age >= ?1) AND (s.sex = ?2) 
				selectQuery.addCondition(ComboCondition.and(BinaryCondition.greaterThan("s.age", "?1", true),BinaryCondition.equalTo("s.sex", "?2")));
				// 注意:build(SelectQuery selectQuery,Object... parameters),其中parameters表示SQL语句中所需的参数.
				// 参数通常都是有外界传递进来的,在此采用prepared模式,目的是为了防止SQL注入.
				// 3 对应 ?1
				// "男" 对应 ?2
				return this.build(selectQuery,3,"男"); 
			}
		};
		
		// 2). 调用find
		String countField = "s.no"; // 求和字段,默认是"id"
		String countsql = null;   // 求和语句
		Page<Map<String, Object>> maps = studentDBService.find(spec, new PageableImpl(1, 5), countField, countsql, false);
		System.out.println(JSON.toJSONString(maps, true));
	}
	
	
	@Test
	public void methodFind5() {
		// 参考: http://mxm910821.iteye.com/blog/1701822
		studentDBService.executeBatch("update.sql", "out.txt");
	}
	
}
