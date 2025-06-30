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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import com.alibaba.fastjson.JSONArray;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fastquery.util.TypeUtil;
import org.fastquery.struct.RespUpdate;
import org.fastquery.struct.SQLValue;
import org.fastquery.util.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * DB 最基本操作
 *
 * @author mei.sir@aliyun.cn
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DB
{
    private static void statSetObject(PreparedStatement stat, int parameterIndex, Object object) throws SQLException
    {
        if (object instanceof Enum)
        {
            Enum<?> e = (Enum<?>) object;
            int index = e.ordinal() + 1; // 数据库中枚举元素对应的数字索引
            stat.setObject(parameterIndex, index);
        }
        else if (object instanceof JSON)
        {
            stat.setObject(parameterIndex, object.toString());
        }
        else
        {
            stat.setObject(parameterIndex, object);
        }
    }

    public static List<Map<String, Object>> find(SQLValue sqlValue)
    {
        String sql = sqlValue.getSql();
        List<Object> objs = sqlValue.getValues();
        Connection conn = QueryContext.getConn();
        QueryContext.addSqls(sql);
        info(sql, objs);
        try (PreparedStatement stat = conn.prepareStatement(sql);
             ResultSet rs = executeQueryWithParams(stat, objs))
        {
            return rs2Map(rs);
        }
        catch (Exception e)
        {
            throw new RepositoryException(e.getMessage(), e);
        }
    }

    // 参数设置和查询执行的封装方法
    private static ResultSet executeQueryWithParams(PreparedStatement stat, List<Object> objs) throws SQLException
    {
        setParameters(objs, stat);
        return stat.executeQuery();
    }

    /**
     * @param sqlValues 待执行的SQL集
     * @param hasPK     是否需要返回主健
     * @return 改操作响应数据
     */
    static List<RespUpdate> modify(List<SQLValue> sqlValues, boolean hasPK)
    {
        List<RespUpdate> rus;
        Connection conn = QueryContext.getConn(); // 由QueryContext自动关闭
        try
        {
            QueryContext.disableAutoCommit(); // 关闭自动提交
            rus = modify(sqlValues, hasPK, conn);
            QueryContext.commit(); // 提交事务
        }
        catch (Exception e)
        {
            rollbackQuietly();
            throw new RepositoryException(e.getMessage(), e);
        }

        return rus;
    }

    private static List<RespUpdate> modify(List<SQLValue> sqlValues, boolean hasPK, Connection conn) throws SQLException
    {
        List<RespUpdate> rus = new ArrayList<>();
        for (SQLValue sqlValue : sqlValues)
        {
            String sql = sqlValue.getSql();
            QueryContext.addSqls(sql);
            info(sql, sqlValue.getValues());
            List<Object> values = sqlValue.getValues();
            RespUpdate ru = new RespUpdate();
            try (PreparedStatement stat = (hasPK ? conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS) : conn.prepareStatement(sql)))
            {
                setParameters(values, stat);
                ru.setEffect(stat.executeUpdate());
                if (hasPK)
                {
                    try (ResultSet rs = stat.getGeneratedKeys())
                    {
                        if (rs.next())
                        {
                            ru.setPk(rs.getLong(1));
                        }
                    }
                }
            }
            rus.add(ru);
        }

        return rus;
    }

    private static void setParameters(List<Object> values, PreparedStatement stat) throws SQLException
    {
        int len = values.size();
        for (int i = 0; i < len; i++)
        {
            statSetObject(stat, i + 1, values.get(i));
        }
    }

    /**
     * 改操作,若:isEffect=true,返回影响行数;若:isEffect=false,返回主键值.
     *
     * @param sql      语句
     * @param isEffect 是否返回影响行数
     * @return 影响行数 或 主键值
     */
    static Object update(String sql, boolean isEffect)
    {
        Connection conn = QueryContext.getConn();
        QueryContext.addSqls(sql);
        log.info(sql);
        try
        {
            QueryContext.disableAutoCommit();
            try (PreparedStatement stat = (isEffect ? conn.prepareStatement(sql) : conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)))
            {
                int e = stat.executeUpdate();

                Object result = null;
                if (isEffect)
                {
                    result = e;
                }
                else
                {
                    try (ResultSet rs = stat.getGeneratedKeys())
                    {
                        // 获取主键
                        if (rs.next())
                        {
                            result = rs.getObject(1);
                        }
                    }
                }
                QueryContext.commit();
                return result;

            }
        }
        catch (SQLException e)
        {
            rollbackQuietly();
            throw new RepositoryException(e);
        }
    }

    private static void rollbackQuietly()
    {
        try
        {
            QueryContext.rollback();
        }
        catch (SQLException e1)
        {
            throw new RepositoryException(e1);
        }
    }

    static int update(Object bean)
    {
        Object[] updateInfo = BeanUtil.toUpdateSQL(bean, null, false);
        if (updateInfo == null || updateInfo.length == 0)
        {
            return 0;
        }
        String sql = updateInfo[0].toString();
        log.info(sql);
        @SuppressWarnings("unchecked")
        List<Object> args = (List<Object>) updateInfo[1];
        int count = args.size();
        int effect;
        Connection conn = QueryContext.getConn();
        QueryContext.addSqls(sql);
        info(sql, args);
        try
        {
            QueryContext.disableAutoCommit();
            try (PreparedStatement stat = conn.prepareStatement(sql))
            {
                for (int i = 1; i <= count; i++)
                {
                    statSetObject(stat, i, args.get(i - 1));
                }
                effect = stat.executeUpdate();
                QueryContext.commit();
            }
        }
        catch (SQLException e)
        {
            if (conn != null)
            {
                rollbackQuietly();
            }
            throw new RepositoryException(e);
        }
        return effect;
    }

    // 查询一条数据然后转换成一个实体
    static Object select(String sql, Object bean)
    {
        log.info(sql);
        QueryContext.addSqls(sql);
        Class<?> cls = (bean instanceof Class) ? (Class<?>) bean : bean.getClass();
        Connection conn = QueryContext.getConn();
        try (Statement stat = conn.createStatement(); ResultSet rs = stat.executeQuery(sql))
        {
            List<Map<String, Object>> maps = rs2Map(rs);
            if (maps.isEmpty())
            {
                return null;
            }
            //return JSON.toJavaObject(new JSONObject(maps.get(0)), cls)
            return TypeUtil.map2Obj(cls, maps.get(0));
        }
        catch (Exception e)
        {
            throw new RepositoryException(e);
        }
    }

    static boolean exists(String sql)
    {
        QueryContext.addSqls(sql);
        Connection conn = QueryContext.getConn();
        try (Statement stat = conn.createStatement(); ResultSet rs = stat.executeQuery(sql))
        {
            return rs.next();
        }
        catch (SQLException e)
        {
            throw new RepositoryException(e);
        }
    }

    public static boolean exists(SQLValue sqlValue)
    {
        String sql = sqlValue.getSql();
        List<Object> objs = sqlValue.getValues();
        Connection conn = QueryContext.getConn();
        QueryContext.addSqls(sql);
        info(sql, objs);
        try (PreparedStatement stat = conn.prepareStatement(sql))
        {
            // 设置sql参数值
            setParameters(objs, stat);
            try (ResultSet rs = stat.executeQuery())
            {
                return rs.next();
            }
        }
        catch (Exception e)
        {
            throw new RepositoryException(e.getMessage(), e);
        }
    }

    /**
     * 将 rs 的结果集 转换成 List&lt;Map&gt;,rs没有结果则返回空对象(该方法永不返回null).
     *
     * @param rs 结果集
     * @return List map结果集
     * @throws SQLException SQL异常
     */
    private static List<Map<String, Object>> rs2Map(ResultSet rs) throws SQLException
    {

        List<Map<String, Object>> keyvals = new ArrayList<>();
        Map<String, Object> keyval;
        // 获取列信息
        ResultSetMetaData resultSetMetaData = rs.getMetaData();
        // 总列数
        int columnCount;

        String key;
        Object obj;
        while (rs.next())
        {
            columnCount = resultSetMetaData.getColumnCount();
            keyval = new HashMap<>();
            for (int i = 1; i <= columnCount; i++)
            {
                key = resultSetMetaData.getColumnLabel(i); // 获取列别名,若没有别名那么就获取本身名称(getColumnName)
                obj = rs.getObject(i);
                String columnType = resultSetMetaData.getColumnTypeName(i);
                if ("JSON".equals(columnType))
                {
                    String columnName = resultSetMetaData.getColumnName(i);
                    obj = processJsonValue(obj, columnName);
                }
                keyval.put(key, obj);
            }
            keyvals.add(keyval);
        }
        return keyvals;
    }

    private static Object processJsonValue(Object obj, String columnName)
    {
        if (obj == null || "null".equals(obj))
        {
            if (columnName.endsWith("Obj"))
            {
                obj = new JSONObject();
            }
            else if (columnName.endsWith("Array"))
            {
                obj = new JSONArray();
            }
        }
        else
        {
            obj = JSON.parse(obj.toString());
        }
        return obj;
    }

    /**
     * 输出执行日志
     *
     * @param sql  sql语句
     * @param objs 参数
     */
    private static void info(String sql, List<Object> objs)
    {
        if (log.isInfoEnabled())
        { // 这个输出要做很多事情,在此判断一下很有必要,生产环境通常是warn级别
            StringBuilder sb = new StringBuilder("\n正在准备执行SQL:");
            sb.append(sql);
            sb.append(StringUtils.LF);
            if (objs != null && !objs.isEmpty())
            {
                int len = objs.size();
                for (int i = 0; i < len; i++)
                {
                    sb.append(String.format("第%d个\"?\"对应的参数值是:%s;%n", i + 1, objs.get(i)));
                }
            }
            log.info(sb.toString());
        }
    }
}
