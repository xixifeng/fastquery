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
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fastquery.core.Param;
import org.fastquery.core.StrConst;
import org.junit.Test;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThrows;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author xixifeng (fastquery@126.com)
 */
@Slf4j
public class SyntaxTest extends TestFastQuery
{
    @Test
    public void listEmpty()
    {
        List<Map<String, Object>> maps = new ArrayList<>();
        assertThrows(IndexOutOfBoundsException.class, () -> maps.get(0));
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
        log.debug(String.valueOf(Pattern.matches(StringUtils.EMPTY, StringUtils.EMPTY)));
        assertThat("abckdwgew:name&".replaceAll(":name\\b", StrConst.QUE), equalTo("abckdwgew?&"));
        assertThat("abckdwgew:name &".replaceAll("name\\b", StrConst.QUE), equalTo("abckdwgew:? &"));
        log.debug("-->: " + ("abckdwgew:name222 &".replaceAll("name\\b", StrConst.QUE)));
        assertThat(":name22".replaceAll("name\\b", StrConst.QUE), equalTo(":name22"));
        assertThat(":name22 ".replaceAll("name\\b", StrConst.QUE), equalTo(":name22 "));
        assertThat(":name,".replaceAll("name\\b", StrConst.QUE), equalTo(":?,"));
    }

    @Test
    public void test16()
    {
        log.warn("0X16:{}",(byte) 0X14);
    }
}
