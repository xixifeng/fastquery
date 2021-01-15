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

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.fastquery.bean.Gender;
import org.fastquery.bean.Ruits;
import org.fastquery.bean.TypeFeature;
import org.fastquery.dao.TypeFeatureDBService;
import org.fastquery.page.Page;
import org.fastquery.page.PageableImpl;
import org.fastquery.page.Slice;
import org.fastquery.service.FQuery;
import org.fastquery.struct.SQLValue;
import org.junit.Rule;
import org.junit.Test;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author xixifeng (fastquery@126.com)
 */
@Slf4j
public class TypeFeatureDBServiceTest extends FastQueryTest
{
    private TypeFeatureDBService db = FQuery.getRepository(TypeFeatureDBService.class);

    @Rule
    public FastQueryTestRule rule = new FastQueryTestRule();

    @Test
    public void notNull()
    {
        assertThat(db, notNullValue());
    }

    @Test
    public void findByGender()
    {
        Gender gender = Gender.男;
        List<TypeFeature> tfs = db.findByGender(gender);
        assertThat(tfs, notNullValue());
        assertThat(tfs.size(), is(14));
        tfs.forEach(t -> assertThat(t.getGender(), equalTo(Gender.男)));
    }

    @Test
    public void findRuitsWithNullEmpty()
    {
        TypeFeature t1 = db.findRuitsWithNullEmpty("阿飞");
        assertThat(t1.getRuits(),nullValue());
        TypeFeature t2 = db.findRuitsWithNullEmpty("peter");
        assertThat(t2.getRuits().isEmpty(),is(true));

        List<TypeFeature> list = db.findRuitsWithNullEmpty("peter","阿飞");
        assertThat(list.size(), is(2));
        list.forEach(t -> {
            EnumSet<Ruits> rs = t.getRuits();
            assertThat(rs == null || rs.isEmpty(), is(true));
        });
    }

    @Test
    public void findByRuits()
    {
        List<TypeFeature> tfs = db.findByRuits(Ruits.苹果, Ruits.橘子);
        log.error("tfs:{},len:{}", tfs, tfs.size());
        assertThat(tfs, not(empty()));
        tfs.forEach(t -> {
            assertThat(t.getRuits().containsAll(EnumSet.of(Ruits.苹果, Ruits.橘子)), is(true));
        });
    }

    @Test
    public void findByRuitsPage()
    {
        Ruits one = Ruits.苹果;
        Ruits two = Ruits.橘子;
        Page<TypeFeature> page = db.findByRuitsPage(one, two, new PageableImpl(1,3));
        List<TypeFeature> typeFeatures = page.getContent();
        log.info(JSON.toJSONString(page,true));
        typeFeatures.forEach(t -> {
            assertThat(t.getRuits().containsAll(EnumSet.of(Ruits.苹果, Ruits.橘子)), is(true));
            assertThat(t.getRuits().containsAll(EnumSet.of(Ruits.梨)), is(false));
        });
        assertThat(page.isFirst(),is(true));
        assertThat(page.isHasContent(),is(true));
        assertThat(page.isHasNext(), is(true));
        assertThat(page.isHasPrevious(), is(false));
        assertThat(page.isLast(), is(false));
        Slice slice = page.getNextPageable();
        assertThat(slice.getNumber(), is(2));
        assertThat(slice.getSize(), is(3));
        assertThat(page.getSize(),is(3));
        assertThat(page.getTotalElements(), is(4L));
        assertThat(page.getTotalPages(),is(2));
    }

    @Test
    public void findGenders()
    {
        List<Gender> genders = db.findGenders();
        assertThat(genders.isEmpty(), is(false));
        genders.forEach(gender -> {
            assertThat(gender, either(is(Gender.男)).or(is(Gender.女)));
        });
    }

    @Test
    public void findRuits()
    {
        EnumSet<Ruits> opts = EnumSet.of(Ruits.苹果, Ruits.香蕉, Ruits.西瓜, Ruits.芒果, Ruits.橘子, Ruits.梨, Ruits.葡萄, Ruits.樱桃);
        List<EnumSet<Ruits>> ruits = db.findRuits();
        assertThat(ruits.isEmpty(), is(false));
        ruits.forEach(e -> {
            assertThat(opts.containsAll(e), is(true));
        });
    }

    @Test
    public void findGenderById()
    {
        Gender gender = db.findGenderById(1L);
        assertThat(gender, notNullValue());
        assertThat(gender, equalTo(Gender.男));
    }

    @Test
    public void findEnumSetById()
    {
        EnumSet<Ruits> ruits = db.findEnumSetById(1L);
        assertThat(ruits, equalTo(EnumSet.of(Ruits.香蕉, Ruits.西瓜, Ruits.芒果, Ruits.橘子, Ruits.梨)));
    }

    @Test
    public void findOneById()
    {
        TypeFeature typeFeature = db.findOneById(1L);
        assertThat(typeFeature.getName(), equalTo("张三"));
        assertThat(typeFeature.getGender(), equalTo(Gender.男));
        EnumSet<Ruits> ruits = typeFeature.getRuits();
        assertThat(ruits, notNullValue());
        // 香蕉,西瓜,芒果,橘子,梨
        assertThat(ruits, equalTo(EnumSet.of(Ruits.香蕉, Ruits.西瓜, Ruits.芒果, Ruits.橘子, Ruits.梨)));
    }

    @Test
    public void updateTypeFeature()
    {
        Long id = 3L;
        Gender gender = Gender.女;
        int effect = db.updateTypeFeature(id, gender);
        SQLValue sqlValue = rule.getSQLValue();
        String sql = sqlValue.getSql();
        assertThat(sql, equalTo("update type_feature set gender = ? where id = ?"));

        assertThat(effect, is(1));
        TypeFeature typeFeature = db.findOneById(id);
        assertThat(typeFeature.getId(), equalTo(id));
        assertThat(typeFeature.getName(), equalTo("王五"));
        assertThat(typeFeature.getGender(), equalTo(gender));
        sqlValue = rule.getSQLValue();
        sql = sqlValue.getSql();
        assertThat(sql, equalTo("select id, name, gender , ruits, sort from type_feature where id = ?"));

        typeFeature = db.find(TypeFeature.class, id);
        assertThat(typeFeature.getId(), equalTo(id));
        assertThat(typeFeature.getName(), equalTo("王五"));
        assertThat(typeFeature.getGender(), equalTo(gender));
        sqlValue = rule.getSQLValue();
        sql = sqlValue.getSql();
        assertThat(sql, equalTo("select id, name, gender , ruits, sort from type_feature where id = ?"));
    }

    @Test
    public void updateTypeFeature2()
    {
        Long id = 13L;
        String name = "小草";
        Gender gender = Gender.女;
        int sort = 8;
        EnumSet<Ruits> ruits = EnumSet.of(Ruits.西瓜, Ruits.樱桃, Ruits.橘子);
        TypeFeature typeFeature = new TypeFeature();
        typeFeature.setId(id);
        typeFeature.setName(name);
        typeFeature.setGender(gender);
        typeFeature.setRuits(ruits);
        typeFeature.setSort(sort);
        int i = db.executeUpdate(typeFeature);
        assertThat(i,is(1));
        TypeFeature t = db.findOneById(id);
        assertThat(t.getId(), is(id));
        assertThat(t.getName(), is(name));
        assertThat(t.getRuits().containsAll(ruits), is(true));
        assertThat(t.getSort(),is(sort));
    }

    @Test
    public void findOne()
    {
        TypeFeature typeFeature = new TypeFeature();
        typeFeature.setGender(Gender.女);
        Page<TypeFeature> tf = db.findPage(typeFeature, null, null, false, 1, 7, true);
        List<TypeFeature> tfs = tf.getContent();
        tfs.forEach(t -> {
            Gender gender = t.getGender();
            assertThat(gender, equalTo(Gender.女));
            assertThat(gender.getEnName(), is("Woman"));
        });
        assertThat(tf.getNumberOfElements(), is(7));
        log.info("Gender.女, toString:{}", Gender.女);
    }

    @Test
    public void saveTypeFeature()
    {
        Gender gender = Gender.男;
        String name = "乐迪";
        EnumSet<Ruits> ruits = EnumSet.of(Ruits.橘子, Ruits.苹果);
        TypeFeature typeFeature = new TypeFeature();
        typeFeature.setGender(gender);
        typeFeature.setName(name);
        typeFeature.setRuits(ruits);
        TypeFeature newTypeFeature = db.save(typeFeature);
        assertThat(newTypeFeature.getGender(), equalTo(gender));
        assertThat(newTypeFeature.getName(), equalTo(name));
        assertThat(newTypeFeature.getRuits(), equalTo(ruits));
    }

    @Test
    public void findOne1()
    {
        TypeFeature typeFeature = new TypeFeature();
        typeFeature.setId(1L);
        TypeFeature tf = db.findOne(typeFeature, true);
        assertThat(tf, notNullValue());
        assertThat(tf.getRuits().toString(), equalTo("[香蕉, 西瓜, 芒果, 橘子, 梨]"));
    }

    @Test
    public void findByIn1()
    {
        List<String> fieldValues = Arrays.asList("aa", "bb", "cc");
        List<TypeFeature> list = db.findByIn(TypeFeature.class, "name", fieldValues, 3, true, "id", "name");
        assertThat(list, is(empty()));
    }

    @Test
    public void findByIn2()
    {
        List<Long> fieldValues = Arrays.asList(1L, 2L, 3L);
        List<TypeFeature> list = db.findByIn(TypeFeature.class, "id", fieldValues, 3, true, "name");
        assertThat(list.size(), is(3));
    }

}
