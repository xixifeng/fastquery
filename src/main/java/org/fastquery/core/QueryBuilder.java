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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.fastquery.dialect.DialectScheduler;
import org.fastquery.page.PageDialect;
import org.fastquery.page.Pageable;
import org.fastquery.struct.SQLValue;
import org.fastquery.util.TypeUtil;

/**
 * @author mei.sir@aliyun.cn
 */
public class QueryBuilder
{

    private static final Pattern COLON_REG_PATT = Pattern.compile(":[A-Za-z0-9]+");

    private final String query;
    private String countQuery;

    private ConditionList conditions;
    private final Map<String, Object> parameters;

    public QueryBuilder(String query, Map<String, Object> parameters)
    {
        this.query = query;
        this.parameters = parameters;
    }

    public QueryBuilder(String query, ConditionList conditions, Map<String, Object> parameters)
    {
        this.query = query;
        this.conditions = conditions;
        this.parameters = parameters;
    }

    public QueryBuilder(String query, String countQuery, ConditionList conditions, Map<String, Object> parameters)
    {
        this.query = query;
        this.countQuery = countQuery;
        this.conditions = conditions;
        this.parameters = parameters;
    }

    public SQLValue getQuerySQLValue()
    {
        if (conditions != null)
        {
            String conditionsBuilder = conditionBuilder();
            String querySQL = RegExUtils.replaceFirst(query,RegexCache.WHERE_REG_PATT,conditionsBuilder);
            return colonProcess(querySQL);
        }
        else
        {
            return colonProcess(query);
        }
    }

    public List<SQLValue> getQuerySQLValues()
    {
        List<SQLValue> list = new ArrayList<>();
        list.add(getQuerySQLValue());
        return list;
    }

    public List<SQLValue> getPageQuerySQLValue()
    {

        Pageable pageable = QueryContext.getPageable();
        int offset = pageable.getOffset();
        int pageSize = pageable.getPageSize();

        List<SQLValue> list = new ArrayList<>(2);

        PageDialect pageDialect = DialectScheduler.getCurrentPageDialect();
        SQLValue querySQLValue = getQuerySQLValue();
        String sql = querySQLValue.getSql();
        // 当前页查询语句
        String currentPageSQL = pageDialect.getCurrentPageSQL(sql, offset, pageSize);
        querySQLValue.setSql(currentPageSQL);
        list.add(querySQLValue);

        MethodInfo method = QueryContext.getMethodInfo();
        if (method.isCount())
        { // 表明需要求和
            if (conditions != null)
            {
                String conditionsBuilder = conditionBuilder();
                countQuery = RegExUtils.replaceFirst(countQuery,RegexCache.WHERE_REG_PATT,conditionsBuilder);
            }
            SQLValue countQuerySQLValue = colonProcess(countQuery);
            list.add(countQuerySQLValue);
        }
        else
        { // 不求和那么计算出下一页的第一条记录
            offset = offset + pageSize;
            String nextRecordSQL = pageDialect.getCurrentPageSQL(sql, offset, 1);
            SQLValue nextPageSQLValue = new SQLValue();
            nextPageSQLValue.setSql(nextRecordSQL);
            nextPageSQLValue.setValues(querySQLValue.getValues());
            list.add(nextPageSQLValue);
        }

        return list;
    }

    private SQLValue colonProcess(String sqlTpl)
    {
        List<Object> indexValue = new ArrayList<>();
        List<String> colons = TypeUtil.matches(sqlTpl, COLON_REG_PATT);
        for (String colon : colons)
        {
            String key = colon.replace(":", StringUtils.EMPTY);
            Object val = parameters.get(key);
            if (val != null && !val.toString().trim().equals(StringUtils.EMPTY))
            {
                if (val instanceof Iterable)
                {
                    @SuppressWarnings("unchecked")
                    Iterable<Object> iterable = (Iterable<Object>) val;
                    Iterator<Object> iterator = iterable.iterator();
                    StringBuilder sb = new StringBuilder();
                    while (iterator.hasNext())
                    {
                        indexValue.add(iterator.next());
                        sb.append('?');
                    }
                    Pattern p = RegexCache.getPattern(colon);
                    sqlTpl = RegExUtils.replaceFirst(sqlTpl,p,sb.toString());
                }
                else if (val.getClass().isArray())
                {
                    List<Object> list = TypeUtil.toList(val);
                    StringBuilder sb = new StringBuilder();
                    list.forEach(v -> {
                        indexValue.add(v);
                        sb.append('?');
                    });
                    Pattern p = RegexCache.getPattern(colon);
                    sqlTpl = RegExUtils.replaceFirst(sqlTpl,p,sb.toString());
                }
                else
                {
                    indexValue.add(val);
                }
            }
        }

        sqlTpl = RegExUtils.replaceAll(sqlTpl, RegexCache.COLON_REG_PATT, StrConst.QUE);

        SQLValue sqlValue = new SQLValue();
        sqlValue.setSql(sqlTpl);
        sqlValue.setValues(indexValue);

        return sqlValue;
    }

    private String conditionBuilder()
    {
        final StringBuilder conditionsBuilder = new StringBuilder();

        conditions.stream().filter(condition -> { // 确定参与运算的条件
            List<String> colons = TypeUtil.matches(condition, COLON_REG_PATT);
            for (String colon : colons)
            {
                String key = colon.replace(":", StringUtils.EMPTY);
                Object val = parameters.get(key);
                if (val == null || val.toString().trim().equals(StringUtils.EMPTY))
                {
                    return false;
                }
            }
            return true;
        }).forEach(condition -> {
            conditionsBuilder.append(condition);
            conditionsBuilder.append(' ');
        });

        // 去掉首位空格，最后的 or 或 and
        String str = conditionsBuilder.toString().trim();
        int len = str.length();
        if(" or )".equals(StringUtils.substring(str,-5))) {
            str = str.substring(0,len - 5) + " )";
        } else if(" and".equals(StringUtils.substring(str,-4))) {
            str = str.substring(0,len - 4);
        }

        return len > 0 ? "where " + str : str;
    }

}
