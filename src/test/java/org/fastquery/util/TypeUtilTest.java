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

package org.fastquery.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.fastquery.bean.Ruits;
import org.fastquery.core.Id;
import org.fastquery.core.MethodInfo;
import org.fastquery.core.Param;
import org.fastquery.core.Placeholder;
import org.fastquery.core.Query;
import org.fastquery.struct.Reference;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author xixifeng (fastquery@126.com)
 */
@Slf4j
public class TypeUtilTest
{
    @Test
    public void testMatches()
    {
        String sql = "select id,name,age from #{#limit} `userinfo` #{#where}";
        List<String> strs = TypeUtil.matches(sql, Placeholder.LIMIT_RGE);
        assertThat(strs.size(), equalTo(1));
        assertThat(strs.get(0), equalTo("#{#limit}"));
    }

    public void todo(@Id Integer page, Integer size)
    {
    }

    @Test
    public void findAnnotationIndex() throws NoSuchMethodException, SecurityException
    {
        Method method = TypeUtilTest.class.getMethod("todo", Integer.class, Integer.class);
        Parameter[] parameters = method.getParameters();
        assertThat(TypeUtil.findAnnotationIndex(Id.class, parameters), is(0));
    }

    // 别删除用做测试用
    @Query("select * from Student #{#where} order by desc")
    public void method01()
    {
    }

    @Test
    public void filterComments()
    {
        assertThat(TypeUtil.filterComments("/* \n abc */123\n /* 123 */"), equalTo("123\n "));
        assertThat(TypeUtil.filterComments("/*** * 111*/abc/*111*/222/*** *333*/"), equalTo("abc222"));
    }

    public Map<String, String> todo1()
    {
        return null;
    }

    public Map<String, Integer> todo2()
    {
        return null;
    }

    public Map<String, Object> todo3()
    {
        return null;
    }

    public List<Map<String, Object>> todo4()
    {
        return null;
    }

    @Test
    public void isMapSO() throws NoSuchMethodException, SecurityException
    {
        java.lang.reflect.Type type1 = TypeUtilTest.class.getMethod("todo1").getGenericReturnType();
        assertThat(TypeUtil.isMapSO(type1), is(true));

        java.lang.reflect.Type type2 = TypeUtilTest.class.getMethod("todo2").getGenericReturnType();
        assertThat(TypeUtil.isMapSO(type2), is(false));

        java.lang.reflect.Type type3 = TypeUtilTest.class.getMethod("todo3").getGenericReturnType();
        assertThat(TypeUtil.isMapSO(type3), is(true));
    }

    @Test
    public void isListMapSO() throws NoSuchMethodException, SecurityException
    {
        java.lang.reflect.Type type1 = TypeUtilTest.class.getMethod("todo1").getGenericReturnType();
        assertThat(TypeUtil.isListMapSO(type1), is(false));

        java.lang.reflect.Type type2 = TypeUtilTest.class.getMethod("todo2").getGenericReturnType();
        assertThat(TypeUtil.isListMapSO(type2), is(false));

        java.lang.reflect.Type type3 = TypeUtilTest.class.getMethod("todo3").getGenericReturnType();
        assertThat(TypeUtil.isListMapSO(type3), is(false));

        java.lang.reflect.Type type4 = TypeUtilTest.class.getMethod("todo4").getGenericReturnType();
        assertThat(TypeUtil.isListMapSO(type4), is(true));
    }

    private static String removePart(String str) throws Exception
    {
        Method method = TypeUtil.class.getDeclaredMethod("removePart", String.class);
        method.setAccessible(true);
        return (String) method.invoke(null, str);
    }

    @Test
    public void removePart() throws Exception
    {
        String sub = removePart("a ");
        assertThat(sub, equalTo("a"));

        sub = removePart("    a ");
        assertThat(sub, equalTo("a"));

        sub = removePart("    a                 ");
        assertThat(sub, equalTo("a"));

        sub = removePart(" a b");
        assertThat(sub, equalTo("b"));

        sub = removePart(" a     b");
        assertThat(sub, equalTo("b"));

        // 注意: 如下a与b之间的空白是键入Tab键产生的
        sub = removePart(" a		b");
        assertThat(sub, equalTo("b"));

        sub = removePart(" a\t\b\nb");
        assertThat(sub, equalTo("b"));
    }

    @Test
    public void parWhere()
    {
        String sql = "select * from Student <where>where and id = :id</where>";
        String nq = TypeUtil.parWhere(sql);
        assertThat(nq, equalTo("select * from Student where id = :id"));

        sql = "select * from Student <where>and id = :id</where>";
        nq = TypeUtil.parWhere(sql);
        assertThat(nq, equalTo("select * from Student where id = :id"));

        sql = "select * from Student <where>  	\n</where>";
        nq = TypeUtil.parWhere(sql);
        assertThat(nq, equalTo("select * from Student "));

    }

    @Test
    public void getFirstWord()
    {

        assertThat(TypeUtil.getFirstWord(null), nullValue());
        assertThat(TypeUtil.getFirstWord(""), is(""));

        String str = "3/3 source files have been analyzed";
        assertThat(TypeUtil.getFirstWord(str), is("3/3"));

        str = "   			files have been analyzed";
        assertThat(TypeUtil.getFirstWord(str), is("files"));

    }

    @Test
    public void matches()
    {
        String where = "name=:name and age = :age or sex = :sex";
        List<String> strs = TypeUtil.matches(where, ":\\S+\\b");
        assertThat(strs.size(), is(3));
        assertThat(strs.get(0), equalTo(":name"));
        assertThat(strs.get(1), equalTo(":age"));
        assertThat(strs.get(2), equalTo(":sex"));
    }

    public void m1(@Param("i") int i, @Param("i1") int i1, @Param("i2") int i2)
    {
    }

    public void m2(@Param("$^i") int i, @Param("$i1$") int i1, @Param("^i2") int i2)
    {
    }

    private static String paramFilter(Method method, Object[] args, String sql)
            throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        Method pf = TypeUtil.class.getDeclaredMethod("paramFilter", MethodInfo.class, Object[].class, String.class);
        pf.setAccessible(true);
        return pf.invoke(null, new MethodInfo(method), args, sql).toString();
    }

    @Test
    public void paramFilter()
            throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        Method m1 = TypeUtilTest.class.getMethod("m1", int.class, int.class, int.class);
        Object[] agrs = {11, 22, 33};
        String sql = "abc :i and :i1 where :i2";
        String str = paramFilter(m1, agrs, sql);
        assertThat(str, equalTo("abc ?1 and ?2 where ?3"));
        sql = "abc :i1 and :i2 where :i";
        str = paramFilter(m1, agrs, sql);
        assertThat(str, equalTo("abc ?2 and ?3 where ?1"));
    }

    @Test
    public void paramNameFilter() throws NoSuchMethodException, SecurityException
    {
        Method m1 = TypeUtilTest.class.getMethod("m1", int.class, int.class, int.class);
        String sql = "abc :i and :i1 where :i2";
        String str = TypeUtil.paramNameFilter(new MethodInfo(m1), sql);
        assertThat(str, equalTo("abc ?1 and ?2 where ?3"));
    }

    @Test
    public void test2()
    {
        String str = "\t\nabc\n\t";
        assertThat(str.trim(), equalTo("abc"));
    }

    private static String overChar(int overlap) throws Exception
    {
        Method method = TypeUtil.class.getDeclaredMethod("overChar", int.class);
        method.setAccessible(true);
        return (String) method.invoke(null, overlap);
    }

    @Test
    public void overChar() throws Exception
    {
        String str = overChar(-1);
        assertThat(str, equalTo(""));

        str = overChar(0);
        assertThat(str, equalTo(""));

        str = overChar(1);
        assertThat(str, equalTo("?"));

        str = overChar(2);
        assertThat(str, equalTo("?,?"));

        str = overChar(3);
        assertThat(str, equalTo("?,?,?"));

        str = overChar(8);
        assertThat(str, equalTo("?,?,?,?,?,?,?,?"));
    }

    @Test
    public void replace1()
    {
        String str = TypeUtil.replace(null, 0, 1);
        assertThat(str, is(""));

        str = TypeUtil.replace("", 0, 1);
        assertThat(str, is(""));

        str = TypeUtil.replace(null, -1, 0);
        assertThat(str, is(""));
    }

    @Test
    public void replace2()
    {
        String src = "kljgwkg?gwgw47478978?jioj2?87983lkjksj";
        int count = StringUtils.countMatches(src, "?");
        int repat = 3;
        String str = TypeUtil.replace(src, 0, repat);
        assertThat(StringUtils.countMatches(str, "?"), is(count + repat - 1));

        for (int i = 0; i < 10; i++)
        {
            count = StringUtils.countMatches(src, "?");
            repat = i + 1;
            str = TypeUtil.replace(src, 0, repat);
            assertThat(StringUtils.countMatches(str, "?"), is(count + repat - 1));
        }

        str = TypeUtil.replace(src, 0, 3);
        assertThat(str, equalTo("kljgwkg?,?,?gwgw47478978?jioj2?87983lkjksj"));

        str = TypeUtil.replace(src, 1, 2);
        assertThat(str, equalTo("kljgwkg?gwgw47478978?,?jioj2?87983lkjksj"));

        str = TypeUtil.replace(src, 2, 5);
        assertThat(str, equalTo("kljgwkg?gwgw47478978?jioj2?,?,?,?,?87983lkjksj"));

    }

    @Test
    public void replace3()
    {
        String src = "??????????kjlwkgj5?32415456?45lkjkljgw";
        String str = TypeUtil.replace(src, 1, 5);
        assertThat(str, equalTo("??,?,?,?,?????????kjlwkgj5?32415456?45lkjkljgw"));
        str = TypeUtil.replace(src, 20, 5);
        assertThat(str, equalTo("??????????kjlwkgj5?32415456?45lkjkljgw"));
    }

    @Test
    public void replace4()
    {
        String src = "";
        String str = TypeUtil.replace(src, 1, 5);
        assertThat(str, equalTo(""));
        str = TypeUtil.replace(src, 20, 5);
        assertThat(str, equalTo(""));
    }

    @Test
    public void replace5()
    {
        String src = "?5";
        String str = TypeUtil.replace(src, 0, 5);
        assertThat(str, equalTo("?,?,?,?,?5"));
    }

    @Test
    public void mapValueTyep()
            throws IllegalArgumentException, NoSuchMethodException, SecurityException
    {
        class A
        {
            @SuppressWarnings("unused")
            public Map<String, String> todo()
            {
                return null;
            }
        }

        Class<?> clazz = A.class;
        Method method = clazz.getMethod("todo");
        assertThat(TypeUtil.mapValueTyep(new MethodInfo(method)) == String.class, is(true));
    }

    @Test
    public void listMapValueTyep()
            throws IllegalArgumentException, NoSuchMethodException, SecurityException
    {
        class B
        {
            @SuppressWarnings("unused")
            public List<Map<String, String>> todo()
            {
                return null;
            }
        }

        Class<?> clazz = B.class;
        Method method = clazz.getMethod("todo");
        assertThat(TypeUtil.listMapValueTyep(new MethodInfo(method)) == String.class, is(true));
    }

    @SuppressWarnings("unchecked")
    private static List<Object> toList(Object array) throws Exception
    {
        Method method = TypeUtil.class.getDeclaredMethod("toList", Object.class);
        method.setAccessible(true);
        return (List<Object>) method.invoke(null, array);
    }

    @Test
    public void toList1()
    {
        try
        {
            toList(null);
        }
        catch (Exception e)
        {
            String stack = ExceptionUtils.getStackTrace(e);
            assertThat(stack, containsString("Caused by: java.lang.NullPointerException"));
        }
    }

    @Test
    public void toList2()
    {
        try
        {
            toList(1);
        }
        catch (Exception e)
        {
            String stack = ExceptionUtils.getStackTrace(e);
            assertThat(stack, containsString("Caused by: java.lang.ClassCastException: 你传递的不是一个数组"));
        }
    }

    @Test
    public void toList3() throws Exception
    {
        int[] ids = {1, 2, 3};
        List<Object> objects = toList(ids);
        for (int i = 0; i < ids.length; i++)
        {
            assertThat(ids[i], is(objects.get(i)));
        }
    }

    @Test
    public void toList4() throws Exception
    {
        Integer[] ids = {1, 2, 3};
        List<Object> objects = toList(ids);
        for (int i = 0; i < ids.length; i++)
        {
            assertThat(ids[i], equalTo(objects.get(i)));
        }
    }

    @Test
    public void toList5() throws Exception
    {
        Object ids = new Integer[]{1, 2, 3};
        List<Object> objects = toList(ids);
        assertThat(objects.get(0), equalTo(1));
        assertThat(objects.get(1), equalTo(2));
        assertThat(objects.get(2), equalTo(3));
    }

    @Test
    public void toList6() throws Exception
    {
        Object ids = new int[]{1, 2, 3};
        List<Object> objects = toList(ids);
        assertThat(objects.get(0), equalTo(1));
        assertThat(objects.get(1), equalTo(2));
        assertThat(objects.get(2), equalTo(3));
    }

    @Test
    public void statementReference()
    {
        String str = "jkge,list_$[kwjlkeg as id, gweklge.name as name, `e.jgke` , d.xo, number] kwe";
        Reference reference = TypeUtil.statementReference(str);
        assertThat(reference, notNullValue());
        assertThat(reference.getName(), equalTo("list_$"));
        List<String> feilds = reference.getFields();
        assertThat(feilds.get(0), equalTo("id"));
        assertThat(feilds.get(1), equalTo("name"));
        assertThat(feilds.get(2), equalTo("jgke"));
        assertThat(feilds.get(3), equalTo("xo"));
        assertThat(feilds.get(4), equalTo("number"));

        String newStr = TypeUtil.unStatementReference(str);
        assertThat(newStr, equalTo("jkge,kwjlkeg as id, gweklge.name as name, `e.jgke` , d.xo, number kwe"));
    }

    @Test
    public void enumSet2Val(){
        EnumSet<Ruits> ruits = EnumSet.of(Ruits.梨, Ruits.香蕉, Ruits.西瓜, Ruits.芒果, Ruits.橘子);
        String val = TypeUtil.enumSet2Val(ruits);
        log.info("val : {}", val);
        assertThat(val,equalTo("香蕉,西瓜,芒果,橘子,梨"));
    }

}
