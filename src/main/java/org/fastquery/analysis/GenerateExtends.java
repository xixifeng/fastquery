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

import org.fastquery.core.Modifying;
import org.fastquery.core.Query;
import org.fastquery.core.QueryByNamed;

/**
 * 生成 Repository的实现扩展, 在生成的时候,额外要做的事情(extend)
 *
 * @author xixifeng (fastquery@126.com)
 */
public class GenerateExtends
{

    private GenerateExtends()
    {
    }

    /**
     * 在生成Repository实现类之前,做安全检测
     *
     * @param repositoryClazz 待检测repository
     */
    public static void safeCheck(Class<?> repositoryClazz)
    {

        MethodFilterChain filterChain;

        Method[] methods = repositoryClazz.getDeclaredMethods();

        for (Method method : methods)
        {

            Modifying modifying = method.getAnnotation(Modifying.class);
            Query[] querys = method.getAnnotationsByType(Query.class);
            QueryByNamed queryByNamed = method.getAnnotation(QueryByNamed.class);

            // 拦截全局
            filterChain = new MethodFilterChain();
            filterChain.addFilter(new ReturnTypeFilter());
            filterChain.addFilter(new InterceptorFilter()); // @Before,@After拦截器安全校验
            filterChain.addFilter(new PageableFilter());
            filterChain.addFilter(new SharpFilter()); // #{#表达式} 合法检测
            filterChain.addFilter(new ModifyingDependencyFilter());
            filterChain.addFilter(new MethodAnnotationFilter());
            filterChain.addFilter(new SQLFilter());
            filterChain.addFilter(new ConditionParameterFilter());
            filterChain.addFilter(new SourceFilter());
            filterChain.addFilter(new OutFilter());
            filterChain.addFilter(new MarkFilter());
            filterChain.addFilter(new MuestionFilter());
            filterChain.addFilter(new SetFilter());

            if (modifying != null)
            { // 改

                filterChain.addFilter(new AnnotationSynxFilter());
                filterChain.addFilter(new ArgsFilter());
                filterChain.addFilter(new ModifyingReturnTypeFilter());

            }
            else
            { // 查
                if (querys.length > 0)
                { // @Query查
                    filterChain.addFilter(new QueryReturnTypeFilter());
                    filterChain.addFilter(new ParameterFilter());
                    filterChain.addFilter(new NotAllowedRepeat());
                    filterChain.addFilter(new PageFilter());
                }
                else if (queryByNamed != null)
                { // @queryByNamed查
                    filterChain.addFilter(new TplPageFilter());
                    filterChain.addFilter(new QueriesFileFilter());
                }
            }

            // 执行Filter
            filterChain.doFilter(method);

        }
    }
}
