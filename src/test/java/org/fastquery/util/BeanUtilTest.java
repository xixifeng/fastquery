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
	}

}
