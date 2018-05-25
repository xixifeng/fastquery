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

package org.fastquery.example;

import java.util.List;
import java.util.Map;

import org.fastquery.bean.Student;
import org.fastquery.core.Id;
import org.fastquery.core.Modifying;
import org.fastquery.core.Param;
import org.fastquery.core.Primarykey;
import org.fastquery.core.Query;
import org.fastquery.core.QueryByNamed;
import org.fastquery.core.QueryRepository;
import org.fastquery.filter.After;
import org.fastquery.filter.Before;
import org.fastquery.filter.MyAfterFilter;
import org.fastquery.filter.MyAfterFilter1;
import org.fastquery.filter.MyBeforeFilter1;
import org.fastquery.filter.MyBeforeFilter2;
import org.fastquery.filter.SkipFilter;
import org.fastquery.where.Condition;
import org.fastquery.where.I18n;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
@Before(MyBeforeFilter1.class)
@Before(MyBeforeFilter2.class)
// @Before(MyBeforeFilter3.class)
@After(MyAfterFilter.class)
// @After(MyAfterFilter2.class)
public interface StudentDBService extends QueryRepository {

	// 别删除注释,测试的时候有用!!!
	// 八中基本类型
	// 形参: int a,double b,long c,short d,byte e,boolean f,char g,float h
	// 实参: int a=1; double b=2;long c=3;short d=4;byte e=5;boolean f=true;char
	// g=7;float h=8;
	@Query("update student s set s.age=?3,s.name=?2 where  s.no=?1")
	@Modifying
	int update(String no, String name, int age);

	@Query("update student s set s.age=?2 where  s.no=?1")
	@Modifying
	int update(String no, int age);

	@Before(MyBeforeFilter1.class)
	@Before(MyBeforeFilter2.class)
	@After(MyAfterFilter1.class)
	// @After(MyAfterFilter2.class)
	@Query("select * from student")
	JSONArray findAll();

	@Query("select * from student")
	Student[] find();

	@I18n({ "name", "dept" })
	@Query("select * from student s where s.no=?1")
	JSONObject findOne(String no);

	@Modifying
	@I18n({ "name", "dept" })
	@Query("update Student set name = :name, dept = :dept where no = :no")
	int updateNameAndDept(@Param("name") String name, @Param("dept") String dept, @Param("no") String no);

	@Query("select * from student s where s.no=?1")
	Student findStudent(String no);

	@Query("select * from student s where s.no=?1")
	boolean exists(String no);

	@Query("select * from student s where s.no=?1")
	Student findByNo(String no);

	@Query("select count(no) from student")
	long count();

	// 求和返回int 类型
	@Query("select count(no) from student")
	int count2();

	@Query("select count(no) as count from student")
	JSONObject rows(); // --> {"count":8}

	// 增
	@Query("insert into student (no, name, sex, age, dept) values (?1, ?2, ?3, ?4, ?5)")
	@Modifying
	int add(String no, String name, String sex, int age, String dept);

	// 以实体bean格式返回当前保存的数据
	@Query("insert into student (no, name, sex, age, dept) values (?1, ?2, ?3, ?4, ?5)")
	@Modifying(table = "student", id = "no")
	// 注意: student的主键是字符串不会自增长,在此处需要用@Id标识
	Student addStudent(@Id String no, String name, String sex, int age, String dept);

	// 注意:SQL别名删除
	// 删
	@Query("delete s from student as s where s.no =  ?1") // 根据编号删除
	@Modifying
	int deleteByNo(String no);

	// select * from student s where s.sex='男' and s.age > 22

	// sql中的?1 表示当前方法的第一个参数
	// sql中的?2 表示当前方法的第二个参数
	// ?N 表示当前方法的第N个参数
	// 查询返回数组格式
	@Query("select no as no,name,sex,age,dept from student s where s.sex=?2 and s.age > ?1")
	Student[] findBySex(Integer age, String sex);

	// 查询返回JSON格式
	@Query("select * from student s where s.sex=?1 and s.age > ?2")
	JSONArray findBySex(String sex, Integer age);

	// 查询返回List Map
	@Query("select * from student s where s.sex=?1 and s.age > ?2")
	List<Map<String, Object>> findBySex2(String sex, Integer age);

	void updatesx(Integer[] i1, Integer[] i2, Integer[] i3);

	/**
	 * 查询某个表的主键字段信息, 注意: 一个表中有可能是联合主键,因此返回的是数组
	 * 
	 * @param table_name 表名称
	 * @param table_schema 所属数据库
	 */
	@Query("select * from information_schema.columns where table_name = ?1 and table_schema = ?2 and column_key='pri'")
	JSONArray findColumnKey(String table_name, String table_schema);

	@Modifying(id = "id", table = "userinfo")
	@Query("insert into #{#table} (name,age) values (?1, ?2)")
	Map<String, Object> addUserInfo(String name, Integer age);

	// 把查询出的字段值包装成字符串,而不是Object
	@Modifying(id = "id", table = "userinfo")
	@Query("insert into #{#table} (name,age) values (?1, ?2)")
	Map<String, String> addUserInfo2(String name, Integer age);

	// 增加一条数据,返回主键信息.
	@Modifying(id = "id", table = "userinfo")
	@Query("insert into #{#table} (name,age) values (?1, ?2)")
	Primarykey saveUserInfo(String name, Integer age);

	@Modifying(id = "id", table = "userinfo")
	@Query("insert into #{#table} (name,age) values (?1, ?2)")
	JSONObject saveUserInfo2(String name, Integer age);

	@Modifying(table = "userinfo")
	@Query("insert into #{#table} (name,age) values (?1, ?2)")
	int saveUserInfo3(String name, Integer age);

	@Modifying(table = "userinfo")
	@Query("update #{#table} as ui set ui.age = ?1 where ui.id=?2")
	JSONObject updateUserinfoById(Integer age, @Id int id);

	@Modifying
	@Query("DELETE FROM `userinfo` WHERE id=?1")
	boolean deleteUserinfoById(int id);

	@Query("select no from `course` limit 1")
	String findOneCourse();

	@Query("select age from Student limit 1")
	Integer findAgeByStudent();

	@SkipFilter
	@Query("select * from Student #{#where} order by age desc")
	// 增加一些条件
	@Condition("no like ?1") // ?1的值,如果是null, 该行条件将不参与运算
	@Condition("and name like ?2") // 参数 ?2,如果接收到的值为null,该条件不参与运算
	// 通过 ignoreNull=false 开启条件值即使是null也参与运算
	@Condition(value = "and age > ?3") // ?3接收到的值若为null,该条件也参与运算.
	@Condition("or dept in(?4,?5,?6)")
	@Condition("and name not like ?7")
	@Condition("or age between ?8 and ?9")
	Student[] findAllStudent(String no, String name, Integer age, String dept1, String dept2, String dept3, String name2, Integer age2, Integer age3);

	@Query("select * from Student #{#where} order by age desc")
	@Condition("name like ?1") // 第1个参数(?1)如果传递null, 该行条件将会被忽略
	@Condition("and age > ?2") // ?2 如果是null,这行条件将会被忽略
	List<Student> findAllStudent(String name, Integer age);

	@Query("select age from Student limit 3")
	List<Integer> findAges();

	@Query("select name from Student limit 3")
	List<String> findNames();

	@QueryByNamed
	public List<Student> findSomeStudent();

	@Query("select * from Student limit 1")
	Map<String, String> findTop1Student();

	public void db();

	@Query("call addStudent(?1,:name,?3,?4,:dept)")
	JSONObject callProcedure(String no, @Param("name") String name, String sex, int age, @Param("dept") String dept);

}
