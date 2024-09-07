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
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.extern.slf4j.Slf4j;
import org.fastquery.bean.Gender;
import org.fastquery.bean.Ruits;
import org.fastquery.bean.TypeFeature;
import org.fastquery.dao.TypeFeatureDBService;
import org.fastquery.page.Page;
import org.fastquery.page.PageableImpl;
import org.fastquery.page.Slice;
import org.fastquery.service.FQuery;
import org.fastquery.struct.NulOperator;
import org.fastquery.struct.SQLOperator;
import org.fastquery.struct.SQLValue;
import org.junit.Rule;
import org.junit.Test;

import java.math.BigInteger;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author xixifeng (fastquery@126.com)
 */
@Slf4j
public class TypeFeatureDBServiceTest extends TestFastQuery
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
        log.debug("tfs:{},len:{}", tfs, tfs.size());
        assertThat(tfs, not(empty()));
        tfs.forEach(t -> assertThat(t.getRuits().containsAll(EnumSet.of(Ruits.苹果, Ruits.橘子)), is(true)));
    }

    @Test
    public void findByRuitsPage()
    {
        Ruits one = Ruits.苹果;
        Ruits two = Ruits.橘子;
        Page<TypeFeature> page = db.findByRuitsPage(one, two, new PageableImpl(1,3));
        List<TypeFeature> typeFeatures = page.getContent();
        log.info(JSON.toJSONString(page,SerializerFeature.PrettyFormat));
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
    public void findByRuitsPage2()
    {
        Page<TypeFeature> page = db.findByRuitsPage(new PageableImpl(1,3));
        log.info(JSON.toJSONString(page,SerializerFeature.PrettyFormat));
        assertThat(page.isFirst(),is(true));
        assertThat(page.isHasContent(),is(true));
        assertThat(page.isHasNext(), is(true));
        assertThat(page.isHasPrevious(), is(false));
        assertThat(page.isLast(), is(false));
        Slice slice = page.getNextPageable();
        assertThat(slice.getNumber(), is(2));
        assertThat(slice.getSize(), is(3));
        assertThat(page.getSize(),is(3));
        assertThat(page.getTotalElements(), is(33L));
        assertThat(page.getTotalPages(),is(11));
        // 测试每一页
        for (int i = 2; i <= 11 ; i++)
        {
            db.findByRuitsPage(new PageableImpl(i,3));
        }
    }

    @Test
    public void findGenders()
    {
        List<Gender> genders = db.findGenders();
        assertThat(genders.isEmpty(), is(false));
        genders.forEach(gender -> assertThat(gender, either(is(Gender.男)).or(is(Gender.女))));
    }

    @Test
    public void findRuits()
    {
        EnumSet<Ruits> opts = EnumSet.of(Ruits.苹果, Ruits.香蕉, Ruits.西瓜, Ruits.芒果, Ruits.橘子, Ruits.梨, Ruits.葡萄, Ruits.樱桃);
        List<EnumSet<Ruits>> ruits = db.findRuits();
        assertThat(ruits.isEmpty(), is(false));
        ruits.forEach(e -> assertThat(opts.containsAll(e), is(true)));
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
        assertThat(newTypeFeature.getActionLogObj().isEmpty(),is(true));
        assertThat(newTypeFeature.getContactArray().isEmpty(),is(true));
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
    public void findPageByPredicate0()
    {
        TypeFeature typeFeature = new TypeFeature();
        typeFeature.setGender(Gender.男);
        // 根据 id 集合查
        List<Long> ids = new ArrayList<>();
        ids.add(1L);
        ids.add(2L);
        ids.add(3L);

        typeFeature.and(typeFeature::id, SQLOperator.IN, ids);
        typeFeature.and(typeFeature::gender, SQLOperator.EQ, typeFeature.getGender());
        typeFeature.orderBy("id desc");

        List<TypeFeature> list = db.findPageByPredicate(typeFeature, false, 1, 100, true, "id", "name").getContent();
        assertThat(list.size(), is(3));
        List<Long> allId = list.stream().collect(ArrayList::new, (collection, e) -> collection.add(e.getId()), ArrayList::addAll);
        assertThat(allId.toString(),equalTo("[3, 2, 1]"));
    }

    @Test
    public void findPageByPredicate1()
    {
        TypeFeature typeFeature = new TypeFeature();
        typeFeature.setGender(Gender.男);
        // 根据 id 集合查
        typeFeature.and(typeFeature::gender, NulOperator.ISNULL);
        typeFeature.and(typeFeature::name, NulOperator.ISNOTNULL);
        typeFeature.orderBy("id desc");

        SQLValue sqlValue = typeFeature.getSqlValue();
        String sql = sqlValue.getSql();
        assertThat(sql, equalTo(" and gender is null and name is not null order by id desc"));

        db.findPageByPredicate(typeFeature, false, 1, 37, true, "id", "name").getContent();
        List<String> sqls = rule.getExecutedSQLs();
        assertThat(sqls.get(0), equalTo("select id,name from type_feature where gender is null and name is not null order by id desc  limit 0,37"));
    }

    @Test
    public void findPageByPredicate2()
    {
        TypeFeature typeFeature = new TypeFeature();
        typeFeature.orderBy();
        db.findPageByPredicate(typeFeature, false, 1, 3, true, "id", "name").getContent();
        List<String> sqls = rule.getExecutedSQLs();
        assertThat(sqls.get(0), equalTo("select id,name from type_feature order by id desc  limit 0,3"));
        assertThat(sqls.get(1), equalTo("select count(id) from type_feature order by id desc "));
    }

    @Test
    public void findPageByPredicate3()
    {
        TypeFeature typeFeature = new TypeFeature();
        typeFeature.setGender(Gender.女);
        typeFeature.setName("Lily");
        typeFeature.setSort(0);
        db.findPageByPredicate(typeFeature, false, 1, 3, true, "id", "name").getContent();
        List<String> sqls = rule.getExecutedSQLs();
        assertThat(sqls.get(0), equalTo("select id,name from type_feature where name = ? and gender = ? and sort = ?  limit 0,3"));
        assertThat(sqls.get(1), equalTo("select count(id) from type_feature where name = ? and gender = ? and sort = ? "));
    }

    @Test
    public void findPageByPredicate4()
    {
        TypeFeature typeFeature = new TypeFeature();
        typeFeature.setGender(Gender.女);
        typeFeature.setName("Lilyx#$");
        typeFeature.setSort(0);
        Page<TypeFeature> page = db.findPageByPredicate(typeFeature, true, 1, 3, true, "id", "name");
        assertThat(page.isLast(),is(true));
    }

    @Test
    public void findPageByPredicate5()
    {
        TypeFeature typeFeature = new TypeFeature();
        typeFeature.and(typeFeature::ruits,NulOperator.ISNULL);
        typeFeature.and(typeFeature::gender,SQLOperator.EQ, Gender.男);
        typeFeature.orderBy();

        SQLValue sqlValue = typeFeature.getSqlValue();
        String sql = sqlValue.getSql();
        assertThat(sql, equalTo(" and ruits is null and gender = ? order by id desc"));
        db.findPageByPredicate(typeFeature, false, 1, 3, true, "id", "name");

        assertThat(rule.getExecutedSQLs().get(0), equalTo("select id,name from type_feature where ruits is null and gender = ? order by id desc  limit 0,3"));
    }


    @Test
    public void updateRuits(){
        Collection<TypeFeature> entities = new ArrayList<>();
        TypeFeature t1 = new TypeFeature(1L,"蒙奇",Gender.男,EnumSet.of(Ruits.梨,Ruits.香蕉,Ruits.橘子),1);
        TypeFeature t2 = new TypeFeature(2L,"丽莎",Gender.女,EnumSet.of(Ruits.西瓜,Ruits.葡萄),2);
        TypeFeature t3 = new TypeFeature(3L,"老张",Gender.男,EnumSet.of(Ruits.香蕉),3);
        TypeFeature t4 = new TypeFeature(4L,"朱万",Gender.男,EnumSet.noneOf(Ruits.class),4);
        TypeFeature t5 = new TypeFeature(5L,"小李",Gender.女,null,5);
        TypeFeature t6 = new TypeFeature(6L,"小兰",null,null,6);
        entities.add(t1);
        entities.add(t2);
        entities.add(t3);
        entities.add(t4);
        entities.add(t5);
        entities.add(t6);
        int effect = db.update(entities);
        assertThat(effect,is(6));
        TypeFeature t = db.findOneById(1L);
        assertThat(t.getName(),equalTo("蒙奇"));
        assertThat(t.getGender().toString(), equalTo("男"));
        assertThat(t.getRuits().containsAll(EnumSet.of(Ruits.梨,Ruits.香蕉,Ruits.橘子)),is(true));
        assertThat(t.getRuits().containsAll(EnumSet.of(Ruits.西瓜,Ruits.葡萄)),is(false));
        assertThat(t.getSort(),is(1));

        t = db.findOneById(4L);
        assertThat(t.getName(),equalTo("朱万"));
        assertThat(t.getGender().toString(), equalTo("男"));
        assertThat(t.getRuits().isEmpty(),is(true));
        assertThat(t.getSort(),is(4));

        t = db.findOneById(6L);
        assertThat(t.getName(),equalTo("小兰"));
        assertThat(t.getGender().toString(), equalTo("女"));
        assertThat(t.getRuits().containsAll(EnumSet.of(Ruits.西瓜,Ruits.樱桃)),is(true));
        assertThat(t.getSort(),is(6));
    }

    @Test
    public void save()
    {
        TypeFeature tf = new TypeFeature();
        tf.setName("令狐一飞");
        tf.setGender(Gender.男);
        tf.setRuits(EnumSet.of(Ruits.芒果,Ruits.梨));
        TypeFeature typeFeature = db.save(tf);
        assertThat(typeFeature.getActionLogObj().isEmpty(),is(true));
        assertThat(typeFeature.getContactArray().isEmpty(),is(true));
        assertThat(typeFeature.getSort(),is(3));
    }

    @Test
    public void saveOrUpdate()
    {
        TypeFeature tf = new TypeFeature();
        tf.setId(40L);
        tf.setName("令狐一飞");
        tf.setGender(Gender.男);
        tf.setRuits(EnumSet.of(Ruits.芒果,Ruits.梨));

        TypeFeature typeFeature = db.saveOrUpdate(tf);
        assertThat(typeFeature.getActionLogObj().isEmpty(),is(true));
        assertThat(typeFeature.getContactArray().isEmpty(),is(true));
        assertThat(typeFeature.getSort(),is(3));
    }

    @Test
    public void saveCollection()
    {
        TypeFeature tf1 = new TypeFeature();
        tf1.setName("令狐一飞");
        tf1.setGender(Gender.男);
        tf1.setRuits(EnumSet.of(Ruits.芒果,Ruits.梨));

        TypeFeature tf2 = new TypeFeature();
        tf2.setName("夏婵");
        tf2.setGender(Gender.女);
        tf2.setRuits(EnumSet.of(Ruits.苹果,Ruits.橘子));

        List<TypeFeature> list = Arrays.asList(tf1,tf2);

        int effect = db.save(false,list);
        assertThat(effect,is(2));
    }

    @Test
    public void saveArray()
    {
        TypeFeature tf1 = new TypeFeature();
        tf1.setName("令狐一飞");
        tf1.setGender(Gender.男);
        tf1.setRuits(EnumSet.of(Ruits.芒果,Ruits.梨));

        TypeFeature tf2 = new TypeFeature();
        tf2.setName("夏婵");
        tf2.setGender(Gender.女);
        tf2.setRuits(EnumSet.of(Ruits.苹果,Ruits.橘子));

        int effect = db.saveArray(false,tf1,tf2);
        assertThat(effect,is(2));
    }

    @Test
    public void insert()
    {
        TypeFeature tf1 = new TypeFeature();
        tf1.setName("令狐一飞");
        tf1.setGender(Gender.男);
        tf1.setRuits(EnumSet.of(Ruits.芒果,Ruits.梨));


        int effect = db.insert(tf1);
        assertThat(effect,is(1));

        effect = db.insert(null);
        assertThat(effect,is(0));
    }

    @Test
    public void saveToId()
    {
        TypeFeature tf1 = new TypeFeature();
        tf1.setName("令狐一飞");
        tf1.setGender(Gender.男);
        tf1.setRuits(EnumSet.of(Ruits.芒果,Ruits.梨));

        BigInteger id = db.saveToId(tf1);
        assertThat(id.longValue(),greaterThan(1L));
    }

    @Test
    public void findContainJSON()
    {
        TypeFeature tf = db.findContainJSON(1L);
        assertThat(tf.getActionLogObj(),instanceOf(JSONObject.class));
        assertThat(tf.getContactArray(),instanceOf(JSONArray.class));
    }

    @Test
    public void findByContainJSONPage()
    {
        Page<TypeFeature> page = db.findByContainJSONPage(new PageableImpl(1,3));
        List<TypeFeature> typeFeatures = page.getContent();
        typeFeatures.forEach(typeFeature -> assertThat(typeFeature.getActionLogObj(),instanceOf(JSONObject.class)));
        typeFeatures.forEach(typeFeature -> assertThat(typeFeature.getContactArray(),instanceOf(JSONArray.class)));
        log.info(">>>>>>:{}", JSON.toJSONString(page,SerializerFeature.PrettyFormat));
    }
    @Test
    public void findById()
    {
        TypeFeature typeFeature = db.find(TypeFeature.class,5L);
        assertThat(typeFeature.getContactArray().isEmpty(),is(true));
        assertThat(typeFeature.getActionLogObj().isEmpty(),is(true));
        typeFeature = db.find(TypeFeature.class,6L);
        assertThat(typeFeature.getContactArray().isEmpty(),is(true));
        assertThat(typeFeature.getActionLogObj().isEmpty(),is(true));
    }

    @Test
    public void findActionLogById()
    {
        JSONObject json = db.findActionLogById(1L);
        Object obj1 = json.get("actionLogObj");
        assertThat(obj1, instanceOf(JSONObject.class));
        JSONObject actionLog = (JSONObject) obj1;
        assertThat(actionLog.containsKey("mail"),is(true));
        assertThat(actionLog.containsKey("name"),is(true));
        assertThat(actionLog.containsKey("address"),is(true));

        Object obj2 = json.get("contactArray");
        assertThat(obj2, instanceOf(JSONArray.class));
        JSONArray contact = (JSONArray) obj2;
        assertThat(contact.get(0).toString(),equalTo("{\"1\":\"2903438098\"}"));
        assertThat(contact.get(1).toString(),equalTo("{\"2\":\"example@email.com\"}"));
    }

    @Test
    public void find()
    {
        TypeFeature typeFeature = db.find(TypeFeature.class,1L);
        assertThat(typeFeature.getActionLogObj(), instanceOf(JSONObject.class));
        assertThat(typeFeature.getContactArray(), instanceOf(JSONArray.class));
    }

    @Test
    public void update()
    {
        TypeFeature typeFeature = new TypeFeature();
        typeFeature.setId(4L);
        JSONObject jsonObject = JSON.parseObject("{\"mail\": \"xiaozhang@gmail.com\", \"name\": \"小张\", \"address\": \"Shanghai\"}");
        JSONArray jsonArray = JSON.parseArray("[{\"7\":\"90380989648\"},{\"8\":\"axgbxc@email.com\"}]");
        typeFeature.setActionLogObj(jsonObject);
        typeFeature.setContactArray(jsonArray);
        int effect = db.executeUpdate(typeFeature);
        assertThat(effect,is(1));
        TypeFeature tf = db.find(TypeFeature.class,4L);
        JSONObject actionLog = tf.getActionLogObj();
        assertThat(actionLog.containsKey("mail"),is(true));
        assertThat(actionLog.containsKey("name"),is(true));
        assertThat(actionLog.containsKey("address"),is(true));
        assertThat(actionLog.getString("mail"),equalTo("xiaozhang@gmail.com"));
        assertThat(actionLog.getString("name"),equalTo("小张"));
        assertThat(actionLog.getString("address"),equalTo("Shanghai"));
        JSONArray contact = tf.getContactArray();
        assertThat(contact.get(0).toString(),equalTo("{\"7\":\"90380989648\"}"));
        assertThat(contact.get(1).toString(),equalTo("{\"8\":\"axgbxc@email.com\"}"));
    }

    @Test
    public void predicate1()
    {
        TypeFeature typeFeature = new TypeFeature();
        typeFeature.setGender(Gender.女);

        long count = db.count(typeFeature);
        typeFeature.increment(TypeFeature::sort, -1);
        typeFeature.and(typeFeature::gender, SQLOperator.EQ, typeFeature.getGender());
        typeFeature.finish();

        int effect = db.executeUpdate(typeFeature);
        assertThat(count==effect,is(true));

        SQLValue sqlValue = typeFeature.getSqlValue();
        String sql = sqlValue.getSql();
        List<Object> values = sqlValue.getValues();
        assertThat(sql, equalTo("update type_feature set sort=sort+-1 where gender = ?"));
        assertThat(values, notNullValue());
        assertThat(values.size(), equalTo(1));
        assertThat(values.get(0), equalTo(typeFeature.getGender().toString()));
    }

    @Test
    public void predicate2()
    {
        long id = 19;
        String name = "Nicky";
        EnumSet<Ruits> ruits = EnumSet.of(Ruits.梨,Ruits.芒果, Ruits.苹果);
        TypeFeature typeFeature = new TypeFeature();
        typeFeature.set(typeFeature::ruits, ruits);
        typeFeature.set(typeFeature::name, name);
        typeFeature.and(typeFeature::id, SQLOperator.EQ, id);
        typeFeature.finish();

        int effect = db.executeUpdate(typeFeature);
        assertThat(effect,is(1));

        SQLValue sqlValue = typeFeature.getSqlValue();
        String sql = sqlValue.getSql();
        List<Object> values = sqlValue.getValues();
        assertThat(sql, equalTo("update type_feature set ruits=?,name=? where id = ?"));
        assertThat(values, notNullValue());
        assertThat(values.size(), equalTo(3));
        assertThat(values.get(0), equalTo("苹果,芒果,梨"));
        assertThat(values.get(1), equalTo(name));
        assertThat(values.get(2), equalTo(id));

        TypeFeature oneById = db.findOneById(id);
        assertThat(oneById.getName(),equalTo(name));
        assertThat(oneById.getRuits(),equalTo(ruits));
    }
}
