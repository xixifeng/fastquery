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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import org.fastquery.core.Placeholder;
import org.fastquery.core.Query;
import org.fastquery.core.RepositoryException;
import org.fastquery.util.TypeUtil;
import org.fastquery.where.Condition;

/**
 * 该类的作用范围仅仅是当前包
 *
 * @author xixifeng (fastquery@126.com)
 */
class QueryFilterHelper
{

    private QueryFilterHelper()
    {
    }

    /**
     * 获取SQL语句,不考虑条件是否参与运算问题.
     *
     * @param method  方法
     * @param queries query集
     * @return SQL 语句
     */
    private static List<String> getQuerySQL(Method method, Query[] queries)
    {

        List<String> sqls = new ArrayList<>(queries.length);

        for (Query query : queries)
        {

            String sql = query.value();
            StringBuilder sb = new StringBuilder();
            // 追加条件
            Condition[] conditions = method.getAnnotationsByType(Condition.class);
            for (Condition condition : conditions)
            {
                sb.append(' ');
                sb.append(condition.value());
            }
            // 追加条件 End

            String where = sb.toString();
            if (!"".equals(where) && TypeUtil.matches(query.value(), Placeholder.WHERE_REG_PATT).size() != 1)
            {
                throw new RepositoryException(method + " 如果存在@Condition(条件注解),那么@Query中的value值,必须存在#{#where},有且只能出现一次");
            }
            sqls.add(sql.replaceFirst(Placeholder.WHERE_REG, Matcher.quoteReplacement(sb.toString())));
        }

        return sqls;
    }

    static List<String> getQuerySQL(Method method)
    {
        return getQuerySQL(method, method.getAnnotationsByType(Query.class));
    }
}
