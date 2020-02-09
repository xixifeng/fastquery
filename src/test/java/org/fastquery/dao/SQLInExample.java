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

package org.fastquery.dao;

import java.util.List;
import java.util.Set;

import org.fastquery.bean.Student;
import org.fastquery.bean.UserInfo;
import org.fastquery.core.Param;
import org.fastquery.core.Query;
import org.fastquery.core.QueryRepository;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public interface SQLInExample extends QueryRepository {

	@Query("select * from UserInfo where name in (?1)")
	List<UserInfo> findByNameIn(String... names);

	@Query("select * from UserInfo where name in (?1) and id > ?2")
	List<UserInfo> findByNameListIn(List<String> names, Integer id);

	@Query("select * from UserInfo as u where u.id in (?1)")
	UserInfo[] findByIn(int... ids);
	
	@Query("select * from UserInfo as u where u.id in (?1)")
	UserInfo[] findByIn(String... ids);
	
	@Query("select * from UserInfo as u where u.id in (?1)")
	UserInfo[] findByIn(List<Integer> ids);

	@Query("select * from student where sex = :sex and age > :age and name in(:names)")
	List<Student> findByIn(@Param(value = "sex") String sex, @Param("age") Integer age,
			@Param("names") Set<String> names);

}
