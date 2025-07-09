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

package org.fastquery.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

import lombok.Getter;
import org.fastquery.page.NotCount;
import org.fastquery.page.PageIndex;
import org.fastquery.page.PageSize;
import org.fastquery.where.Condition;
import org.fastquery.where.Set;

/**
 * @author mei.sir@aliyun.cn
 */
@Getter
public class MethodInfo
{
    private final Method method;
    private final Modifying modifying;
    private final Query[] queries;
    private final Parameter[] parameters;
    private final Class<?> returnType;
    private final Id id;
    private final Transactional t;
    private final QueryByNamed queryByNamed;
    private final NotCount notCount;
    private final Condition[] conditions;
    private final Annotation[][] parameterAnnotations;
    private final Type genericReturnType;
    private final String name;
    private final Query query;
    private final Set[] sets;

    private final int sourcePosition;
    private final int notCountPosition;
    private final int pageIndexPosition;
    private final int pageSizePosition;

    private final Repository repository;
    private final Class<?> interfaceClazz;
    private final String interfaceName;

    public MethodInfo(Method method, Repository repository,Class<?> interfaceClazz)
    { // NO_UCD
        this.method = method;
        this.repository = repository;
        this.interfaceClazz = interfaceClazz;
        this.interfaceName = interfaceClazz != null ? interfaceClazz.getName() : null;
        this.modifying = method.getAnnotation(Modifying.class);
        this.queries = method.getAnnotationsByType(Query.class);
        this.parameters = method.getParameters();
        this.returnType = method.getReturnType();
        this.id = method.getAnnotation(Id.class);
        this.t = method.getAnnotation(Transactional.class);
        this.queryByNamed = method.getAnnotation(QueryByNamed.class);
        this.notCount = method.getAnnotation(NotCount.class);
        this.conditions = method.getAnnotationsByType(Condition.class);
        this.parameterAnnotations = method.getParameterAnnotations();
        this.genericReturnType = method.getGenericReturnType();
        this.name = method.getName();
        this.query = method.getAnnotation(Query.class);
        this.sets = method.getAnnotationsByType(Set.class);

        // 顺序：Source, PageIndex, PageSize, NotCount
        int[] indexes = getIndexes();
        this.sourcePosition = indexes[0];
        this.pageIndexPosition = indexes[1];
        this.pageSizePosition = indexes[2];
        this.notCountPosition = indexes[3];
    }

    public String findSource()
    {
        return sourcePosition == -1 ? null : (String) QueryContext.getArgs()[sourcePosition];
    }

    public int findPageIndex()
    {
        return pageIndexPosition == -1 ? -1 : (int) QueryContext.getArgs()[pageIndexPosition];
    }

    public int findPageSize()
    {
        return pageSizePosition == -1 ? -1 : (int) QueryContext.getArgs()[pageSizePosition];
    }

    public boolean isCount()
    {   // 检测到需要分页了，才会执行这个方法
        return notCount == null && (notCountPosition == -1 || !(Boolean) QueryContext.getArgs()[notCountPosition]);
    }

    private int[] getIndexes()
    {
        // 顺序：Source, PageIndex, PageSize, NotCount
        int[] indexes = {-1, -1, -1, -1}; // 默认 -1 表示未找到
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (parameter.isAnnotationPresent(Source.class)) {
                indexes[0] = i;
            } else if (parameter.isAnnotationPresent(PageIndex.class)) {
                indexes[1] = i;
            } else if (parameter.isAnnotationPresent(PageSize.class)) {
                indexes[2] = i;
            } else if (parameter.isAnnotationPresent(NotCount.class)) {
                indexes[3] = i;
            }
        }
        return indexes;
    }

    public static String toReadableSignature(Method method) {
        StringBuilder sb = new StringBuilder();
        sb.append(method.getDeclaringClass().getName()); // 类的全限定名
        sb.append("#");
        sb.append(method.getName()); // 方法名
        sb.append("(");
        Class<?>[] paramTypes = method.getParameterTypes();
        for (int i = 0; i < paramTypes.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(paramTypes[i].getSimpleName()); // 参数类型（简短名）
        }
        sb.append(")");
        return sb.toString();
    }

    public String toGenericString()
    {
        return this.method.toGenericString();
    }
}
