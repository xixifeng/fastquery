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

import org.fastquery.bean.Gender;
import org.fastquery.bean.Ruits;
import org.fastquery.bean.TypeFeature;
import org.fastquery.core.Modifying;
import org.fastquery.core.Param;
import org.fastquery.core.Query;
import org.fastquery.core.QueryRepository;

import java.util.EnumSet;
import java.util.List;

/**
 *
 * @author xixifeng (fastquery@126.com)
 */
public interface TypeFeatureDBService extends QueryRepository {

    @Query("select id, name, gender, ruits from type_feature where gender = ?1")
    List<TypeFeature> findByGender(Gender gender);

    // 查询爱吃苹果和香蕉的
    @Query("select id, name, gender, ruits from type_feature where find_in_set('${one}',ruits) and find_in_set('${two}',ruits)")
    List<TypeFeature> findByRuits(@Param("one") Ruits one,@Param("two") Ruits two);

    @Query("select gender from type_feature limit 3")
    List<Gender> findGenders();

    @Query("select ruits from type_feature limit 3")
    List<EnumSet<Ruits>> findRuits();

    @Query("select id, name, gender , ruits from type_feature where id = ?1")
    TypeFeature findOneById(Long id);

    @Query("select gender from type_feature where id = ?1")
    Gender findGenderById(Long id);

    @Query("select ruits from type_feature where id = ?1")
    EnumSet<Ruits> findEnumSetById(Long id);

    @Modifying
    @Query("update type_feature set gender = :gender where id = ?1")
    int updateTypeFeature(Long id, @Param("gender") Gender gender);

}
