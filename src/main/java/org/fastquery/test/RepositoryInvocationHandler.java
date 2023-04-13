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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.fastquery.core.MethodInfo;
import org.fastquery.core.Modifying;
import org.fastquery.core.Query;
import org.fastquery.core.QueryBuilder;
import org.fastquery.core.QueryByNamed;
import org.fastquery.core.QueryContext;
import org.fastquery.core.QueryParser;
import org.fastquery.core.Repository;
import org.fastquery.core.RepositoryException;
import org.fastquery.page.Page;
import org.fastquery.util.TypeUtil;
import org.junit.runner.Description;

/**
 * @author mei.sir@aliyun.cn
 */
@Slf4j
public class RepositoryInvocationHandler implements InvocationHandler
{
    private final Repository repository;
    private final FastQueryTestRule rule;
    private final Description description;

    public RepositoryInvocationHandler(Repository repository, FastQueryTestRule rule, Description description)
    {
        this.repository = repository;
        this.rule = rule;
        this.description = description;
    }

    private void before() throws NoSuchMethodException, NoSuchFieldException, IllegalAccessException
    {
        Class<QueryContext> qcclazz = QueryContext.class;
        Method getQueryContextMethod = qcclazz.getDeclaredMethod("getQueryContext");
        getQueryContextMethod.setAccessible(true);
        Field debugField = qcclazz.getDeclaredField("debug");
        debugField.setAccessible(true);
        debugField.set(null, true);
    }

    private void after() throws NoSuchMethodException, NoSuchFieldException, IllegalAccessException, InvocationTargetException
    {
        Class<QueryParser> clazz = QueryParser.class;
        log.debug("RepositoryInvocationHandler:当前线程:{}", Thread.currentThread());
        MethodInfo currentMethod = QueryContext.getMethodInfo();
        Method modifyParserMethod = clazz.getDeclaredMethod("modifyParser");
        Method queryParserMethod = clazz.getDeclaredMethod("queryParser");
        modifyParserMethod.setAccessible(true);
        queryParserMethod.setAccessible(true);
        log.debug("modifyParserMethod:{}", modifyParserMethod);
        log.info("currentMethod:{}", currentMethod);
        Modifying modifying = currentMethod.getModifying();
        Class<?> returnType = QueryContext.getReturnType();
        QueryByNamed queryById = currentMethod.getQueryByNamed();
        Query query = currentMethod.getQuery();

        Class<FastQueryTestRule> qcclazz = FastQueryTestRule.class;
        Field sqlValueField = qcclazz.getDeclaredField("sqlValue");
        Field sqlValuesField = qcclazz.getDeclaredField("sqlValues");
        sqlValueField.setAccessible(true);
        sqlValuesField.setAccessible(true);
        QueryBuilder queryBuilder = TypeUtil.getQueryBuilder();
        if (modifying != null)
        {
            modifyingRule(currentMethod, modifyParserMethod, sqlValuesField, queryBuilder);
        }
        else if (returnType == Page.class)
        {
            pageRule(currentMethod, queryById, query, sqlValuesField, queryBuilder);
        }
        else if (queryById != null || query != null)
        {
            queryAndByIdRule(currentMethod, queryParserMethod, sqlValueField, queryBuilder);
        }
        else
        {
            log.error("暂时没考虑");
        }

        Field executedSQLsField = qcclazz.getDeclaredField("executedSQLs");
        executedSQLsField.setAccessible(true);

        Class<QueryContext> contextClass = QueryContext.class;
        Field sqlsField = contextClass.getDeclaredField("sqls");
        sqlsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<String> currSQLS = (List<String>) sqlsField.get(QueryContextHelper.getQueryContext());
        List<String> list = new ArrayList<>(currSQLS);
        currSQLS.clear(); // 被人赋值之后就clear
        executedSQLsField.set(rule, list);
    }

    private void queryAndByIdRule(MethodInfo currentMethod, Method queryParserMethod, Field sqlValueField, QueryBuilder queryBuilder)
            throws IllegalAccessException, InvocationTargetException
    {
        if (currentMethod.isContainQueryBuilderParam())
        {
            sqlValueField.set(rule, queryBuilder.getQuerySQLValue());
        }
        else
        {
            sqlValueField.set(rule, queryParserMethod.invoke(null));
        }
    }

    private void pageRule(MethodInfo currentMethod, QueryByNamed queryById, Query query, Field sqlValuesField, QueryBuilder queryBuilder)
            throws IllegalAccessException
    {
        if (queryById != null)
        {
            sqlValuesField.set(rule, QueryParser.pageParserByNamed());
        }
        else if (query != null)
        {
            if (currentMethod.isContainQueryBuilderParam())
            {
                sqlValuesField.set(rule, queryBuilder.getPageQuerySQLValue());
            }
            else
            {
                sqlValuesField.set(rule, QueryParser.pageParser());
            }
        }
    }

    private void modifyingRule(MethodInfo currentMethod, Method modifyParserMethod, Field sqlValuesField, QueryBuilder queryBuilder)
            throws IllegalAccessException, InvocationTargetException
    {
        // 改操作涉及多条sql语句
        if (currentMethod.isContainQueryBuilderParam())
        {
            sqlValuesField.set(rule, queryBuilder.getQuerySQLValues());
        }
        else
        {
            sqlValuesField.set(rule, modifyParserMethod.invoke(null));
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
    {
        try
        {
            if (method.getDeclaringClass() == Object.class)
            { // 如果拦截的方法是继承之Object,那么直接放行
                return method.invoke(repository, args);
            }

            before();
            Object result;
            result = method.invoke(repository, args);
            after();
            return result;
        }
        catch (Exception e)
        {
            throw new RepositoryException(e);
        }

    }

}
