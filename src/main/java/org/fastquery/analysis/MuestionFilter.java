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
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.fastquery.core.RegexCache;
import org.fastquery.core.Query;
import org.fastquery.core.StrConst;
import org.fastquery.util.TypeUtil;

/**
 * 拦截 SQL中 ?N+ 能否从方法参数中找到匹配
 *
 * @author mei.sir@aliyun.cn
 */
class MuestionFilter implements MethodFilter
{

    @Override
    public void doFilter(Method method)
    {
        int parameterCount = method.getParameterCount();
        Query[] queries = method.getAnnotationsByType(Query.class);
        for (Query query : queries)
        {
            String value = query.value();
            Set<String> strs = TypeUtil.matchesNotrepeat(value, RegexCache.SP1_REG_PATT);
            strs.forEach(str -> {
                int index = Integer.parseInt(str.replace(StrConst.QUE, StringUtils.EMPTY));
                if (index > parameterCount)
                {
                    this.abortWith(method,
                            String.format("%n@Query(%s)中的\"?%d\"表示指定该方法的第%d个参数,可是该方法一共只有%d个参数%n", value, index, index, parameterCount));
                }
            });
        }
    }

}
