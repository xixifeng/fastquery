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
import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import org.fastquery.bean.UserInfo;
import org.junit.Test;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class BeanUtilTest {

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
	
	
	@Test
	public void toSelectSQL(){
		UserInfo userInfo1 = new UserInfo(33,"想向公主",18);
		//UserInfo userInfo2 = new UserInfo(34,"程家洛",20);

		String str = BeanUtil.toSelectSQL(userInfo1, 36, "xk");
		System.out.println(str);
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
	
	@Test
	public void testReset(){
		UserInfo u2 = BeanUtil.newNullInstance(UserInfo.class); 
		assertThat(u2.getId(),nullValue());
		assertThat(u2.getName(),nullValue());
		assertThat(u2.getAge(),nullValue());
	}
}




