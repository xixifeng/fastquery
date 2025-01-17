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

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.*;
import java.util.function.LongSupplier;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.fastquery.dialect.DialectScheduler;
import org.fastquery.page.Page;
import org.fastquery.page.PageDialect;
import org.fastquery.page.Pageable;
import org.fastquery.page.Slice;
import org.fastquery.struct.Predicate;
import org.fastquery.struct.RespUpdate;
import org.fastquery.struct.SQLValue;
import org.fastquery.util.BeanUtil;
import org.fastquery.util.FastQueryJSONObject;
import org.fastquery.util.TypeUtil;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import static org.fastquery.handler.ModifyingHandler.*;
import static org.fastquery.handler.QueryHandler.*;

/**
 * @author xixifeng (fastquery@126.com)
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class QueryProcess
{
    // 改操作
    static Object modifying()
    {
        MethodInfo method = QueryContext.getMethodInfo();
        Class<?> returnType = QueryContext.getReturnType();

        // 获取待执行的sql
        List<SQLValue> sqlValues= QueryParser.modifyParser();

        // 执行
        List<RespUpdate> respUpdates = DB.modify(sqlValues, QueryContext.isRequirePk());

        Long autoIncKey = respUpdates.get(0).getPk();

        if (returnType == void.class)
        {
            return voidType();
        }
        else if (returnType == int.class)
        {
            return intType(respUpdates);
        }
        else if (returnType == int[].class)
        {
            int len = respUpdates.size();
            int[] effects = new int[len];
            for (int i = 0; i < len; i++)
            {
                effects[i] = respUpdates.get(i).getEffect();
            }
            return effects;
        }
        else if (returnType == Map.class)
        {
            return mapType(autoIncKey, TypeUtil.mapValueTyep(method));
        }
        else if (returnType == JSONObject.class)
        {
            return jsonObjectType(autoIncKey);
        }
        else if (returnType == boolean.class)
        {
            return booleanType(respUpdates);
        }
        else
        { // 把值强制转换成 returnType
            return beanType(autoIncKey);
        }

    }

    // 查操作
    static Object query()
    {

        MethodInfo method = QueryContext.getMethodInfo();
        Class<?> returnType = QueryContext.getReturnType();
        SQLValue sqlValue = QueryParser.queryParser();
        List<Map<String, Object>> keyvals = DB.find(sqlValue);

        // 上面的try发生异常了,才会导致keyvals为null, 不过异常一旦捕获到就throw了,因此,程序执行到这里keyvals不可能为null.
        if (returnType == long.class)
        {
            return longType(keyvals);
        }
        else if (returnType == int.class)
        {
            return intType(keyvals);
        }
        else if (returnType == boolean.class)
        {
            return booleanType(keyvals);
        }
        else if (returnType == Map.class)
        {
            return mapType(keyvals, TypeUtil.mapValueTyep(method));
        }
        else if (TypeUtil.isListMapSO(method.getGenericReturnType()))
        {
            return listType(keyvals, TypeUtil.listMapValueTyep(method));
        }
        else if (returnType == List.class)
        {
            return list(keyvals);
        }
        else if (returnType == JSONObject.class)
        {
            return jsonObjeType(keyvals);
        }
        else if (returnType == JSONArray.class)
        {
            return jsonArrayType(keyvals);
        }
        else if (TypeUtil.isWarrp(returnType))
        {
            return wrapperType(method, returnType, keyvals);
        }
        else if (TypeUtil.isWarrp(returnType.getComponentType()) || TypeUtil.hasDefaultConstructor(returnType.getComponentType()))
        {
            // 基本类型数组, bean数组
            return wrapperAarryType(returnType, keyvals);
        }
        else
        {
            return beanType(keyvals);
        }
    }

    private static final String ORDER_BLANK = "#{?0}";

    private static String proNextSQL(String sql)
    {
        int start = sql.indexOf('(');
        String replacement = null;
        if(start != -1)
        {
            int end = sql.lastIndexOf(')');
            replacement = sql.substring(start, end + 1);
            sql = sql.replace(replacement, ORDER_BLANK);
        }
        sql = sql.toLowerCase();
        int firstFrom = sql.indexOf(" from ");
        sql = sql.substring(firstFrom);

        int orderIndex = sql.lastIndexOf(" order ");
        if(orderIndex != -1)
        {
            int limitIndex = sql.lastIndexOf(" limit ");
            sql = sql.replace(sql.substring(orderIndex,limitIndex), "");
        }
        if(replacement != null)
        {
            sql = sql.replace(ORDER_BLANK, replacement);
        }
        sql = "select 1" + sql;
        return sql;
    }

    // 分页查询
    static Object queryPage(List<SQLValue> sqlValues)
    {
        List<Map<String, Object>> keyvals = DB.find(sqlValues.get(0));
        Pageable pageable = QueryContext.getPageable();
        int size = pageable.getPageSize(); // 每页多少条数据
        long totalElements = -1L; // 总行数,如果不求和默认-1L
        int totalPages = -1; // 总页数,如果不求和默认-1
        int number = pageable.getPageIndex(); // 当前页码
        boolean hasContent = !keyvals.isEmpty();// 这页有内容吗?
        boolean hasNext; // 有下一页吗? 在这里不用给默认值,如下一定会给他赋值.
        boolean isLast;

        MethodInfo method = QueryContext.getMethodInfo();
        if (hasContent)
        {
            if (method.isCount())
            { // 需要求和
                List<Map<String, Object>> results = DB.find(sqlValues.get(1));
                totalElements = !results.isEmpty() ? ((Number) results.get(0).values().iterator().next()).longValue() : 0;

                // 计算总页数
                totalPages = (int) (totalElements / size);
                if (((int) totalElements) % size != 0)
                {
                    totalPages += 1;
                }
                hasNext = number < totalPages;
                isLast = number == totalPages;
            }
            else if(keyvals.size() == size) // 实际查出的数据(keyvals)永远 <= size, 如果等于 size 不能表明是否有下一页
            {
                SQLValue sqlValue = sqlValues.get(1);
                sqlValue.setSql(proNextSQL(sqlValue.getSql()));
                List<Map<String, Object>> nextvalues = DB.find(sqlValue);
                boolean next = nextvalues.isEmpty();
                hasNext = !next; // 下一页有数据
                isLast = next; // 下一页没有数据了,表明这是最后一页了.
            }
            else // 实际查出的数据永远 < size
            {
                hasNext = false;
                isLast = true;
            }
        }
        else
        {
            totalElements = 0;
            totalPages = 0;
            hasNext = false;
            isLast = true;
        }

        List<?> list = convertContent(keyvals, method);

        return new PageImpl(size, number, list, totalElements, totalPages, hasNext, isLast);
    }

    private static List<?> convertContent(List<Map<String, Object>> keyvals, MethodInfo method)
    {
        List<?> list = keyvals;
        // Page<T> 中的 T如果是一个实体,那么需要把 HashMap 转换成实体
        // method.getGenericReturnType()
        if (!method.getGenericReturnType().getTypeName().contains("Page<java.util.Map<java.lang.String, java.lang.Object>>"))
        {
            // 则说明是一个T是一个实体
            Type type = method.getGenericReturnType();
            if (type instanceof ParameterizedType)
            {
                ParameterizedType parameterizedType = (ParameterizedType) method.getGenericReturnType();
                Type actualType = parameterizedType.getActualTypeArguments()[0];
                Class<?> bean;
                if(actualType instanceof Class)
                {
                    bean = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                }
                else
                {
                    if(method.getId().value() != MethodId.QUERY14)
                    {
                        bean = (Class<?>) QueryContext.getArgs()[0];
                    }
                    else
                    {
                        bean = QueryContext.getArgs()[0].getClass();
                    }
                }
                list = TypeUtil.listMap2ListBean(keyvals, bean);
            }
        }
        return list;
    }

    static Object methodQuery(Id id)
    {
        MethodInfo method = QueryContext.getMethodInfo();
        Object[] iargs = QueryContext.getArgs();
        // 检验实体
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++)
        {
            if (iargs[i] != null && parameters[i].getParameterizedType() instanceof TypeVariable)
            { // 这个类型是变量类型吗?
                Field[] fields = BeanUtil.getFields(iargs[i].getClass());
                for (Field field : fields)
                {
                    if (field.getType().isPrimitive())
                    {
                        throw new RepositoryException(String.format("%s这个实体的成员变量%s %s %s不允许是基本类型", iargs[i].getClass().getName(),
                                Modifier.toString(field.getModifiers()), field.getType().getName(), field.getName()));
                    }
                }
            }
        }
        // 检验实体 end

        switch (id.value())
        {
            case MethodId.QUERY:
                return q();
            case MethodId.QUERY0:
                return q0();
            case MethodId.QUERY1:
                return q1();
            case MethodId.QUERY2:
                return q2();
            case MethodId.QUERY4:
                return q4();
            case MethodId.QUERY5:
                return q5();
            case MethodId.QUERY6:
                return q6();
            case MethodId.QUERY7:
                return q7();
            case MethodId.QUERY8:
                return q8();
            case MethodId.QUERY9:
                return q9();
            case MethodId.QUERY10:
                return q10();
            case MethodId.QUERY11:
                return q11();
            case MethodId.QUERY12:
                return q12();
            case MethodId.QUERY13:
                return q13();
            case MethodId.QUERY14:
                return q14();
            default:
                break;
        }
        return null;
    }

    static Object methodQuery()
    {
        return null;
    }

    private static Object q()
    {
        Object[] iargs = QueryContext.getArgs();
        Object bean = iargs[0];
        String sql = BeanUtil.toInsertSQL(null, bean);
        Object keyObj = DB.update(sql, false);
        if (keyObj == null)
        {
            return BigInteger.valueOf(-1);
        }
        else
        {
            return new BigInteger(keyObj.toString());
        }
    }

    private static Object q0()
    {
        Object[] iargs = QueryContext.getArgs();
        Object bean = iargs[0];
        if(bean == null)
        {
            return 0;
        }
        String sql = BeanUtil.toInsertSQL(null, bean);
        log.info(sql);
        return DB.update(sql, true);
    }

    private static Object q1()
    {
        Object[] iargs = QueryContext.getArgs();
        Object bean = iargs[0];
        if(bean == null)
        {
            return 0;
        }

        if( bean instanceof Predicate)
        {
            Predicate p = (Predicate) bean;
            SQLValue sqlValue = p.getSqlValue();
            if(sqlValue != null)
            {
                String tableName = BeanUtil.getTableName(null, bean.getClass());
                sqlValue.setSql("update " + tableName + sqlValue.getSql());
                return DB.modify(Collections.singletonList(sqlValue),false).get(0).getEffect();
            }
        }

        return DB.update(bean, null, null);
    }

    private static Object q2()
    {
        Object[] iargs = QueryContext.getArgs();
        Object bean = iargs[0];
        if(bean == null)
        {
            return 0;
        }
        String sql = BeanUtil.toSelectSQL(bean, null, null, false, null);
        if (sql != null && DB.exists(sql))
        {
            // 更新
            return DB.update(bean, null, null);
        }
        else
        {
            // 保存
            return DB.update((iargs.length == 3) ? BeanUtil.toInsertSQL(iargs[1].toString(), bean) : BeanUtil.toInsertSQL(bean), true);
        }
    }

    private static Object q4()
    {
        String sql;
        boolean ignoreRepeat;
        Object[] iargs = QueryContext.getArgs();
        ignoreRepeat = (boolean) iargs[0];
        Object entitiesObj = iargs[iargs.length - 1];
        if (entitiesObj == null)
        {
            return 0;
        }

        if (entitiesObj.getClass().isArray())
        {
            Object[] arryObj = (Object[]) entitiesObj;
            if (arryObj.length == 0)
            {
                return 0;
            }
            sql = BeanUtil.arr2InsertSQL(arryObj, null, ignoreRepeat);
        }
        else
        {
            @SuppressWarnings("unchecked")
            Collection<Object> coll = (Collection<Object>) entitiesObj;
            if (coll.isEmpty())
            {
                return 0;
            }
            sql = BeanUtil.toInsertSQL(coll, null, ignoreRepeat);
        }
        log.info(sql);
        return DB.update(sql, true);
    }

    private static Object q5()
    {
        Object[] iargs = QueryContext.getArgs();
        @SuppressWarnings("unchecked")
        Collection<Object> entities = (Collection<Object>) iargs[iargs.length - 1];
        if (entities == null || entities.isEmpty())
        {
            return 0;
        }

        String sql = BeanUtil.toUpdateSQL(entities, null);
        log.info(sql);
        return DB.update(sql, true);
    }

    private static Object q6()
    {
        String sqlFile;
        Object[] iargs = QueryContext.getArgs();
        String f = (String) iargs[0];
        if (f != null)
        {
            String[] quotes = null;
            if (iargs.length == 2)
            {
                quotes = (String[]) iargs[1];
            }
            sqlFile = new File(f).isFile() ? f : FastQueryJSONObject.getBasedir() + iargs[0];
            return DB.executeBatch(sqlFile, quotes, (stat, s) -> {
                try
                {
                    stat.addBatch(s);
                }
                catch (SQLException e)
                {
                    throw new RepositoryException(e);
                }
            });
        }
        else
        {
            return ArrayUtils.EMPTY_INT_ARRAY;
        }
    }

    private static Object q7()
    {
        Object[] iargs = QueryContext.getArgs();
        Class<?> clazz = (Class<?>) iargs[0];
        boolean contain = (boolean) iargs[iargs.length - 2];
        String[] fields = (String[]) iargs[iargs.length - 1];
        String selectFields = new SelectField<>(clazz, contain, fields).getFields();
        long i = (Long) iargs[1]; // 主键

        return DB.select(BeanUtil.toSelectSQL(clazz, i, null, true, selectFields), clazz);
    }

    private static Object q8()
    {
        Object[] iargs = QueryContext.getArgs();
        String tableName = (String) iargs[0]; // 表名称
        String name = (String) iargs[1]; // 主键名
        if (StringUtils.isEmpty(tableName) || StringUtils.isEmpty(name) || StringUtils.isEmpty(tableName.trim())
                || StringUtils.isEmpty(name.trim()))
        {
            return 0;
        }
        else
        {
            long key = (Long) iargs[2]; // 主键值
            return DB.update(BeanUtil.toDelete(tableName, name, key, null), true);
        }
    }

    private static Object q9()
    {
        try
        {
            TxContext.start();
            long asInt = ((LongSupplier) (QueryContext.getArgs()[0])).getAsLong();
            TxContext.getTxContext().commit();
            return asInt;
        }
        catch (Exception e)
        {
            TxContext.getTxContext().rollback();
            throw new RepositoryException(e);
        }
        finally
        {
            TxContext.end();
        }
    }

    private static Object q10()
    {
        Object[] iargs = QueryContext.getArgs();
        Object entity = iargs[0];
        SQLValue sv = BeanUtil.toCount(entity, null);
        List<Map<String, Object>> list = DB.find(sv);
        return list.get(0).values().iterator().next();
    }

    private static Object q11()
    {
        Object[] iargs = QueryContext.getArgs();
        Object entity = iargs[0];
        Objects.requireNonNull(entity, "findOne 入参 entity 不能为 null");

        boolean contain = (boolean) iargs[1];
        String[] fields = (String[]) iargs[2];
        SQLValue sv = BeanUtil.toSelectSQL(entity, null, contain, fields);

        List<Map<String, Object>> list = DB.find(sv);
        if (list.isEmpty())
        {
            log.info("findOne 实际没有查到任何记录，返回 null");
            return null;
        }
        else if (list.size() > 1)
        {
            throw new RepositoryException("findOne 只能查询一条记录，可是实际返回多条记录。查多条记录请用 findPage 查询");
        }
        else
        {
            return TypeUtil.map2Obj(entity.getClass(), list.get(0));
        }
    }

    private static Object q12()
    {
        Object[] iargs = QueryContext.getArgs();
        Object entity = iargs[0];
        if(entity == null)
        {
         return false;
        }
        else
        {
            SQLValue sv = BeanUtil.toSelectSQL(entity, null, true, "1");
            return DB.exists(sv);
        }
    }

    private static Object q13()
    {
        Object[] iargs = QueryContext.getArgs();
        Object entity = iargs[0];
        SQLValue sv = BeanUtil.toSelectSQL(entity, null, true, "1");
        String sql = sv.getSql();
        String[] eachs = BeanUtil.toEachOne(sql);
        if (eachs.length != 1)
        {
            List<Object> vals = sv.getValues();
            for (int i = 0; i < eachs.length; i++)
            {
                List<Object> list = new ArrayList<>();
                list.add(vals.get(i));
                sv.setSql(eachs[i]);
                sv.setValues(list);
                if (DB.exists(sv))
                {
                    return StringUtils.substringsBetween(eachs[i], " where ", " = ")[0];
                }
            }
        }

        return null;
    }

    private static Object q14()
    {
        MethodInfo method = QueryContext.getMethodInfo();
        Pageable pageable = QueryContext.getPageable();
        int offset = pageable.getOffset();
        int pageSize = pageable.getPageSize();

        Object[] iargs = QueryContext.getArgs();
        Object equals = iargs[0];
        if(equals == null)
        {
            PageImpl emptyPage = new PageImpl();
            emptyPage.isFirst = true;
            emptyPage.isLast = true;
            return emptyPage;
        }
        Class<?> clazz = equals.getClass();
        boolean contain = (boolean) iargs[4];
        String[] fields = (String[]) iargs[5];
        log.debug("clazz:{}, contain:{}, fields:{}",
                clazz, contain, fields);
        SQLValue sqlValue = BeanUtil.getSqlValue14(clazz, equals, contain, fields);
        String querySQL = sqlValue.getSql();

        PageDialect pageDialect = DialectScheduler.getCurrentPageDialect();
        String currentPageSQL = pageDialect.getCurrentPageSQL(querySQL, offset, pageSize);
        sqlValue.setSql(currentPageSQL);
        List<SQLValue> sqlValues = new ArrayList<>(2);
        sqlValues.add(sqlValue);

        if (method.isCount())
        {
            String countPageSQL = pageDialect.countSQLInference(querySQL, "id");
            SQLValue countSqlValue = new SQLValue(countPageSQL, sqlValue.getValues());
            sqlValues.add(countSqlValue);
        }
        else
        {
            offset = offset + pageSize;
            String nextRecordSQL = pageDialect.getCurrentPageSQL(querySQL, offset, 1);
            SQLValue sv = new SQLValue(nextRecordSQL, sqlValue.getValues());
            sqlValues.add(sv);
        }

        return queryPage(sqlValues);
    }

    @SuppressWarnings("rawtypes")
    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static class PageImpl implements Page
    {

        private int size; // 每页行数
        private int number; // 当前页码,从0开始
        private int numberOfElements; // 当前页的真实记录行数
        private List<?> content = Collections.emptyList(); // 当前页的结果集

        private long totalElements; // 总行数
        private int totalPages; // 总页码
        private boolean hasContent; // 是否有结果集

        private boolean isFirst; // 是否是第一页
        private boolean isLast; // 是否是最后一页

        private boolean hasNext; // 是否有下一页
        private boolean hasPrevious; // 是否有上一页

        private Slice nextPageable = new Slice(); // 下一页的Pageable对象
        private Slice previousPageable = nextPageable; // 上一页的Pageable对象

        private PageImpl(int size, int number, List<?> content, long totalElements, int totalPages, boolean hasNext, boolean isLast)
        {
            this.size = size;
            this.numberOfElements = content.size();
            this.number = number;
            this.content = content;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
            this.hasContent = !content.isEmpty();
            this.hasNext = hasNext;
            this.hasPrevious = (number > 1) && hasContent;// number不是第1页且当前页有数据,就可以断言它有上一页.
            this.isFirst = number == 1;
            this.isLast = isLast;
            this.nextPageable = new Slice((!isLast) ? (number + 1) : number, size);
            this.previousPageable = new Slice((!isFirst) ? (number - 1) : number, size);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Page convert(Class clazz)
        {
            PageImpl page = new PageImpl();
            page.size = this.size;
            page.number = this.number;
            page.numberOfElements = this.numberOfElements;

            List bs = new ArrayList<>();
            // JSON.toJavaObject(new JSONObject((Map<String, Object>) map), clazz)
            this.content.forEach(map -> bs.add(TypeUtil.map2Obj(clazz, (Map<String, Object>) map)));
            page.content = bs;

            page.totalElements = this.totalElements;
            page.totalPages = this.totalPages;
            page.hasContent = this.hasContent;

            page.isFirst = this.isFirst;
            page.isLast = this.isLast;

            page.hasNext = this.hasNext;
            page.hasPrevious = this.hasPrevious;

            page.nextPageable = new Slice(this.getNextPageable().getNumber(), this.getNextPageable().getSize());
            page.previousPageable = new Slice(this.getPreviousPageable().getNumber(), this.getPreviousPageable().getSize());
            return page;
        }

    }
}
