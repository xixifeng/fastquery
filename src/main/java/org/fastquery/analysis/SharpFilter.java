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

package org.fastquery.analysis;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fastquery.core.Query;
import org.fastquery.util.SharpExprParser;
import org.fastquery.where.Condition;

/**
 * #{#表达式} 合法检测
 *
 * @author mei.sir@aliyun.cn
 */
class SharpFilter implements MethodFilter
{

    @Override
    public void doFilter(Method method)
    {
        Set<String> allows = Stream.of("#{#sets}", "#{#condition}", "#{#table}", "#{#id}", "#{#limit}", "#{#where}").collect(Collectors.toCollection(HashSet::new));
        Query[] queries = method.getAnnotationsByType(Query.class);
        for (Query query : queries)
        {
            String sql = query.value();
            Set<String> finds = SharpExprParser.matchesNotrepeat(sql);
            finds.forEach(s -> {
                if (!allows.contains(s))
                {
                    this.abortWith(method, sql + " 中出现了一个不支持的表达式: " + s + " 请核对是否书写错误.");
                }
                else if ("#{#sets}".equals(s) && method.getAnnotationsByType(org.fastquery.where.Set.class).length == 0)
                {
                    // 如果存在 #{#set} 那么必须存在 @Set
                    this.abortWith(method, sql + " 中存在 #{#sets} 那么该方法必须标识 @Set");
                }
                else if ("#{#where}".equals(s) && method.getAnnotationsByType(Condition.class).length == 0)
                {
                    // 如果存在 #{#where} 那么必须存在 @Condition
                    this.abortWith(method, sql + " 中存在 #{#where} 那么该方法必须标识 @Condition");
                }
            });
        }
    }

}
