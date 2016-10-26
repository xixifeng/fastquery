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

package org.fastquery.dao;

import org.fastquery.bean.Student;
import org.fastquery.bean.UserInfo;
import org.fastquery.core.Modifying;
import org.fastquery.core.Param;
import org.fastquery.core.QueryByNamed;
import org.fastquery.core.QueryRepository;
import org.fastquery.page.Page;
import org.fastquery.page.Pageable;

import com.alibaba.fastjson.JSONArray;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public interface QueryByNamedDBExample extends QueryRepository {

	// 从该类的配置文件里寻找id="findUserInfoAll"节点,然后绑定其SQL代码段
	@QueryByNamed("findUserInfoAll")
	JSONArray findUserInfoAll();

	@QueryByNamed("findUserInfoOne")
	UserInfo findUserInfoOne(@Param("id") Integer id);

	@QueryByNamed("findUserInfoByNameAndAge")
	JSONArray findUserInfoByNameAndAge(@Param("name") String name, @Param("age") Integer age);

	@QueryByNamed("findPage") // 引用id为"findPage"的分页模板
	Page<Student> findPage(Pageable pageable, @Param("no") String no, @Param("name") String name,
			@Param("age") Integer age);
	
	@Modifying
	@QueryByNamed("updateUserInfoById")
	int updateUserInfoById(@Param("id") int id,@Param("name") String name,@Param("age") int age);
}
