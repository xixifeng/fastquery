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

import org.fastquery.bean.Gender;
import org.fastquery.bean.TypeFeature;
import org.fastquery.dao.TypeFeatureDBService;
import org.fastquery.page.Page;
import org.fastquery.service.FQuery;
import org.fastquery.struct.SQLValue;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 *
 * @author xixifeng (fastquery@126.com)
 */
public class TypeFeatureDBServiceTest extends FastQueryTest {

    private Logger LOG = LoggerFactory.getLogger(TypeFeatureDBServiceTest.class);

    private TypeFeatureDBService db = FQuery.getRepository(TypeFeatureDBService.class);

    @Rule
    public FastQueryTestRule rule = new FastQueryTestRule();

    @Test
    public void notNull(){
        assertThat(db,notNullValue());
    }

    @Test
    public void findByGender() {
        Gender gender = Gender.男;
        List<TypeFeature> tfs = db.findByGender(gender);
        assertThat(tfs,notNullValue());
        assertThat(tfs.size(),is(15));
        tfs.forEach(t -> {
          assertThat(t.getGender(),equalTo(Gender.男));
        });
    }

    @Test
    public void updateTypeFeature() {
        Long id = 3L;
        Gender gender = Gender.女;
        int effect = db.updateTypeFeature(id,gender);
        SQLValue sqlValue = rule.getSQLValue();
        String sql = sqlValue.getSql();
        assertThat(sql,equalTo("update type_feature set gender = ? where id = ?"));

        assertThat(effect,is(1));
        TypeFeature typeFeature = db.findOneById(id);
        assertThat(typeFeature.getId(),equalTo(id));
        assertThat(typeFeature.getName(),equalTo("王五"));
        assertThat(typeFeature.getGender(),equalTo(gender));
        sqlValue = rule.getSQLValue();
        sql = sqlValue.getSql();
        assertThat(sql,equalTo("select id, name, gender from type_feature where id = ?"));

        typeFeature = db.find(TypeFeature.class,id);
        assertThat(typeFeature.getId(),equalTo(id));
        assertThat(typeFeature.getName(),equalTo("王五"));
        assertThat(typeFeature.getGender(),equalTo(gender));
        sqlValue = rule.getSQLValue();
        sql = sqlValue.getSql();
        assertThat(sql,equalTo("select id, name, gender from type_feature where id = ?"));
    }

    @Test
    public void findOne() {
        TypeFeature typeFeature = new TypeFeature();
        typeFeature.setGender(Gender.女);
        Page<TypeFeature> tf = db.findPage(typeFeature,null,false,1,7,true);
        List<TypeFeature> tfs = tf.getContent();
        tfs.forEach(t -> {
            Gender gender = t.getGender();
            assertThat(gender,equalTo(Gender.女));
            assertThat(gender.getEnName(),is("Woman"));
        });
        assertThat(tf.getNumberOfElements(),is(7));
        LOG.info("Gender.女, toString:{}",Gender.女);
    }

}
