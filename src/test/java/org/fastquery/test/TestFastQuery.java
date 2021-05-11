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

import java.lang.reflect.Method;

/**
 * @author mei.sir@aliyun.cn
 */
public class TestFastQuery
{

    static
    {
        // 测试自定义位置
        System.setProperty("fastquery.config.dir", System.getProperty("user.dir") + "/src/test/resources/testFiles");
    }

    public static String countSQLInference(String classAddr, String sql, String countField) throws Exception
    {
        Class<?> clazz = Class.forName(classAddr);
        Method method = clazz.getDeclaredMethod("getInstance");
        method.setAccessible(true);
        Object pageDialectObj = method.invoke(null);
        Method countSQLInferenceMethod = clazz.getMethod("countSQLInference", String.class, String.class);
        return countSQLInferenceMethod.invoke(pageDialectObj, sql, countField).toString();
    }
}
