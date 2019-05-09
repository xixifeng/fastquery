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

import java.util.List;

import static org.hamcrest.Matchers.*;

import org.fastquery.bean.Course;
import org.fastquery.dao.SetDBService;
import org.fastquery.service.FQuery;
import org.fastquery.struct.SQLValue;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author mei.sir@aliyun.cn
 */
@RunWith(Theories.class)
public class SetDBServiceTest extends FastQueryTest  {

	private static final Logger LOG = LoggerFactory.getLogger(SetDBServiceTest.class);
	
	private static SetDBService db = FQuery.getRepository(SetDBService.class);
	
	@Rule
	public FastQueryTestRule rule = new FastQueryTestRule();
	
	// name, credit, semester, period, no	
	@DataPoints("names")
	public static String[] names = {"JAVA编程设计","",null};
	
	@DataPoints("credits")
	public static Integer[] credits = {6,0,null};
	
	@DataPoints("semesters")
	public static Integer[] semesters = {9,0,null};
	
	@DataPoints("periods")
	public static Integer[] periods = {50,0,null};
	
	@DataPoint("no")
	public static String no = "c03";
	
	// 有81种组合,此方法也会被调用这么多次
	@Theory
	public void testUpdateCourse1$1(@FromDataPoints("names")String name,@FromDataPoints("credits")Integer credit, @FromDataPoints("semesters")Integer semester, @FromDataPoints("periods")Integer period, @FromDataPoints("no")String no) {
		LOG.info("当前参数 name={}, credit={}, semester={}, period={}, no={} ",name, credit, semester, period, no);		
		if((name == null || "".equals(name)) && credit == null && semester == null && period == null) {
			// 这种情形 已在SetDBServiceTest2中测试
			return ;
		}  else {
			
			Course courseOld = db.findCourse(no);
			int effect = db.updateCourse(name, credit, semester, period, no);
			assertThat(effect, is(1));
			List<SQLValue> sqlValues = rule.getListSQLValue();
			assertThat(sqlValues.size(), is(1));
			
			Course courseNew = db.findCourse(no);
			
			if(name!=null && !"".equals(name)) { // name 不等于null 且 不等于 "",那么name必然会修改,则 Old name != New name
				assertThat(courseOld.getName(), not(equalTo(courseNew.getName())));
			} else {
				assertThat(courseOld.getName(), equalTo(courseNew.getName()));
			}
			
			if(credit!=null) {
				assertThat(courseOld.getCredit(), not(equalTo(courseNew.getCredit())));
			} else {
				assertThat(courseOld.getCredit(), equalTo(courseNew.getCredit()));
			}
			
			if(semester!=null) {
				assertThat(courseOld.getSemester(), not(equalTo(courseNew.getSemester())));
			} else {
				assertThat(courseOld.getSemester(), equalTo(courseNew.getSemester()));
			}
			
			if(period!=null) {
				assertThat(courseOld.getPeriod(), not(equalTo(courseNew.getPeriod())));
			} else {
				assertThat(courseOld.getPeriod(), equalTo(courseNew.getPeriod()));
			}
			
			
		}
	}
	
	@Test
	public void updateCourse1$2() {
		String name = "计算机算法";
		Integer credit = 4;
		String no = "c08";
		int effect = db.updateCourse(name, credit, no);
		assertThat(effect, is(1));
		assertThat(rule.getSQLValue().getSql(), equalTo("update `Course` set `credit` = ?  where no = ?"));
	}
	
	@Test
	public void updateCourse2$1() {
		String name = "计算机算法";
		Integer credit = 4;
		String no = "c08";
		int effect = db.updateCourse2(name, credit, no);
		assertThat(effect, is(1));
		assertThat(rule.getSQLValue().getSql(), equalTo("update `Course` set `credit` = ?  where no = ?"));
	}
	
	@Test
	public void updateCourse2$2() {
		String name = null;
		Integer credit = 4;
		String no = "c08";
		int effect = db.updateCourse2(name, credit, 9, 5, no);
		assertThat(effect, is(1));
		assertThat(rule.getSQLValue().getSql(), equalTo("update `Course` set `credit` = ?,`semester` = ?,`period` = ?  where no = ?"));
	}
	
	@Test
	public void updateCourse3$1() {
		String name = "计算机算法";
		Integer credit = 4;
		String no = "c08";
		int effect = db.updateCourse3(name, credit, no);
		assertThat(effect, is(1));
		assertThat(rule.getSQLValue().getSql(), equalTo("update `Course` set `name` = ?,`credit` = ?  where no = ?"));
	}
	
	@Test
	public void updateCourse3$2() {
		String name = "编程技术";
		Integer credit = 4;
		String no = "c08";
		int effect = db.updateCourse3(name, credit, no);
		assertThat(effect, is(1));
		assertThat(rule.getSQLValue().getSql(), equalTo("update `Course` set `credit` = ?  where no = ?"));
	}

	@Test
	public void updateCourse4$1() {
		String name = "计算机算法";
		Integer credit = 4;
		String no = "c08";
		int effect = db.updateCourse4(name, credit, no);
		assertThat(effect, is(1));
		assertThat(rule.getSQLValue().getSql(), equalTo("update `Course` set `name` = ?,`credit` = ?  where no = ?"));
	}
	
	@Test
	public void updateCourse4$2() {
		String name = "编程技术";
		Integer credit = 4;
		String no = "c08";
		int effect = db.updateCourse4(name, credit, no);
		assertThat(effect, is(1));
		assertThat(rule.getSQLValue().getSql(), equalTo("update `Course` set `name` = name,`credit` = ?  where no = ?"));
	}
	
}
