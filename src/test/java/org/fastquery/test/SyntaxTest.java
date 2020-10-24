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

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.fastquery.core.Param;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThrows;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author xixifeng (fastquery@126.com)
 */
public class SyntaxTest extends FastQueryTest
{

    private static final Logger LOG = LoggerFactory.getLogger(SyntaxTest.class);

    public static class Ca {
        public  void show(){
            System.out.println("吃饭了");
        }
    }

    @Test
    public void testNew() throws IllegalAccessException, InstantiationException
    {
        Ca.class.newInstance().show();
    }

    @Test
    public void listEmpty()
    {
        List<Map<String, Object>> maps = new ArrayList<>();
        assertThrows(IndexOutOfBoundsException.class,() -> maps.get(0));
    }

    @Test
    public void test1() throws NoSuchMethodException, SecurityException
    {
        Method method = SyntaxTest.class.getMethod("todo", String.class, String.class, int.class);
        Annotation[][] annotations = method.getParameterAnnotations();
        Annotation annotation = annotations[0][0];
        assertThat(annotation instanceof Param, is(true));
        annotation = annotations[1][0];
        assertThat(annotation instanceof Param, is(true));

        Parameter[] parameters = method.getParameters();
        Param param = parameters[0].getAnnotation(Param.class);
        assertThat(param, notNullValue());

        param = parameters[1].getAnnotation(Param.class);
        assertThat(param, notNullValue());

        param = parameters[2].getAnnotation(Param.class);
        assertThat(param, nullValue());
    }

    public void todo(@Param("abc") String sx, @Param("efg") String efg, int s)
    {
    }

    @Test
    public void testReg()
    {
        LOG.debug(String.valueOf(Pattern.matches("", "")));
        // s.replaceAll(""+param.value()+"\\b", "?"+(i+1));
        assertThat("abckdwgew:name&".replaceAll(":name\\b", "?"), equalTo("abckdwgew?&"));
        assertThat("abckdwgew:name &".replaceAll("name\\b", "?"), equalTo("abckdwgew:? &"));
        LOG.debug("-->: " + ("abckdwgew:name222 &".replaceAll("name\\b", "?")));
        assertThat(":name22".replaceAll("name\\b", "?"), equalTo(":name22"));
        assertThat(":name22 ".replaceAll("name\\b", "?"), equalTo(":name22 "));
        assertThat(":name,".replaceAll("name\\b", "?"), equalTo(":?,"));
    }
}
