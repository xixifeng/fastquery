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
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.fastquery.page.Page;
import org.fastquery.page.PageIndex;
import org.fastquery.page.PageSize;
import org.fastquery.page.Pageable;
import org.fastquery.util.TypeUtil;

/**
 * @author xixifeng (fastquery@126.com)
 */
class TplPageFilter implements MethodFilter
{

    @Override
    public void doFilter(Method method)
    {

        if (method.getReturnType() == Page.class)
        {

            // 1). Page<T> 中的T要么是Map,要么是一个实体.
            ParameterizedType type = (ParameterizedType) method.getGenericReturnType();
            Type[] types = type.getActualTypeArguments();
            Type t = types[0];
            if (ParameterizedType.class.isAssignableFrom(t.getClass())
                    && !("org.fastquery.page.Page<java.util.Map<java.lang.String, java.lang.Object>>".equals(type.getTypeName())))
            {
                this.abortWith(method, "Page<T> 中的T要么是Map<String,Object>,要么是一个实体.");
            }
            else if (Class.class.isAssignableFrom(t.getClass()) && !(TypeUtil.hasDefaultConstructor((Class<?>) t)))
            {
                this.abortWith(method, "Page<T> 中的T要么是Map<String,Object>,要么是一个实体.");
            }
            else
            {
                // 校验分页参数
                checkPageParam(method);
            }
        }
    }

    private void checkPageParam(Method method)
    {
        Parameter[] parameters = method.getParameters();
        // 2). 方法参数中,要么出现Pageable类型,要么存在@PageIndex和@PageSize
        if (!TypeUtil.hasType(Pageable.class, parameters) && !hasPageAnn(parameters))
        {
            this.abortWith(method, "这是分页,参数中要么存在Pageable类型的参数,要么存在@PageIndex和@PageSize");
        }
        else
        {
            PageableFilter.pageCheck(this, method, parameters);
        }
    }

    private boolean hasPageAnn(Parameter[] parameters)
    {
        return (TypeUtil.findAnnotationIndex(PageIndex.class, parameters) != -1) && TypeUtil.findAnnotationIndex(PageSize.class, parameters) != -1;
    }
}
