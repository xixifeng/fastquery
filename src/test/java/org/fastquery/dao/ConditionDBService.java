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

import org.fastquery.bean.Student;
import org.fastquery.bean.UserInfo;
import org.fastquery.core.Param;
import org.fastquery.core.Query;
import org.fastquery.core.Repository;
import org.fastquery.filter.After;
import org.fastquery.filter.ConditionDBAfterFilter;
import org.fastquery.filter.MyAfterFilter;
import org.fastquery.page.Page;
import org.fastquery.page.Pageable;
import org.fastquery.where.Condition;
import org.fastquery.where.Judge;

/**
 * @author mei.sir@aliyun.cn
 */
@After(ConditionDBAfterFilter.class)
public interface ConditionDBService extends Repository
{

    @Query("select * ${tname} #{#where} limit 3")
    @Condition(value = " $nameWhere", ignoreScript = ":nameWhere==null")
    @Condition(value = " $ageCon", ignoreScript = ":ageCon==null")
    List<Student> findUserInfo(@Param("nameWhere") String w1, @Param("ageCon") String w2, @Param("tname") String tname);

    @Query("select * ${tname} #{#where}")
    @Condition(value = " $nameWhere", ignoreNull = false)
    List<Student> findUserInfo2(String w1, @Param("nameWhere") String w2, @Param("tname") String tname);

    class LikeNameJudge extends Judge
    {
        @Override
        public boolean ignore()
        {
            // 获取方法中名称为"age"的参数值
            int age = this.getParameter("age", int.class);
            // 获取方法中名称为"name"的参数值
            String name = this.getParameter("name", String.class);

            return age > 18 && name != null && name.contains("Rex");
        }
    }

    @Query("select id,name,age from `userinfo` #{#where}")
    @Condition("age > :age")
    @Condition(value = "and name like :name", ignore = LikeNameJudge.class)
    Page<UserInfo> find(@Param("age") int age, @Param("name") String name, Pageable pageable);

    @Query("select id,name,age from `userinfo` #{#where}")
    @Condition("age > :age")
    @Condition(value = "and name like :name", ignoreScript = ":age > 18 && :name!=null && :name.contains(\"Rex\")")
    Page<UserInfo> find2(@Param("age") int age, @Param("name") String name, Pageable pageable);


    @Query("select id,name,age from `userinfo` #{#where}")
    @Condition("age > :age")
    @Condition(value = "and name like :name", if$ = ":age > 18 && :name!=null && :name.contains(\"Rex\")")
    Page<UserInfo> find3(@Param("age") int age, @Param("name") String name, Pageable pageable);


    @Query("select id,name,age from `userinfo` #{#where}")
    @Condition("age > :age")
    @Condition(value = "and name like :name", if$ = ":age > 18 && :name!=null && :name.contains(\"Rex\")", else$ = "and name = :name")
    Page<UserInfo> find4(@Param("age") int age, @Param("name") String name, Pageable pageable);
}
