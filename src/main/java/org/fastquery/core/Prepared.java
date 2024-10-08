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

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.List;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fastquery.mapper.QueryPool;
import org.fastquery.page.Page;
import org.fastquery.struct.SQLValue;
import org.fastquery.util.FastQueryJSONObject;

import static org.fastquery.core.QueryProcess.*;
/**
 * @author xixifeng (fastquery@126.com)
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Prepared
{ // NO_UCD
    /**
     * 执行方法
     *
     * @param methodInfo 当前接口方法
     * @param args       方法参数 注意: 此处参数列表的成员,永远都是包装类型(已经验证)
     * @param target     目标 Repository
     * @return 执行之后的值
     */
    public static Object excute(MethodInfo methodInfo, Object[] args, Repository target)
    {
        long start = System.currentTimeMillis();
        Method method = methodInfo.getMethod();
        try
        {
            Class<?> iclazz = target.getInterfaceClass();

            // 如果是调试模式
            if (FastQueryJSONObject.getDebug())
            {
                QueryPool.reset(iclazz.getName());
            }

            // QueryContext 生命开始
            QueryContext.start(iclazz, methodInfo, args);

            log.debug("准备执行方法:{}", method);
            return businessProcess();
        }
        catch (Exception e)
        {
            if(QueryContext.getQueryContext() == null)
            {
                throw new RepositoryException(e);
            }
            else
            {
                StringBuilder sb = new StringBuilder();
                String msg = e.getMessage();
                if (msg != null)
                {
                    sb.append(msg);
                }

                sb.append("\n发生方法:").append(QueryContext.getMethodInfo().toGenericString());
                sb.append("\n执行过的sql:");

                List<String> sqls = QueryContext.getSqls();
                if (sqls != null)
                {
                    sqls.forEach(sql -> {
                        sb.append(sql);
                        sb.append('\n');
                    });
                }
                throw new RepositoryException(sb.toString(), e);
            }
        }
        finally
        {
            // QueryContext 生命终止
            try
            {
                QueryContext.clear();
            }
            catch (SQLException e)
            {
                throw new RepositoryException("数据库连接无法释放", e);
            }

            long slowQueryTime = FastQueryJSONObject.getSlowQueryTime();
            if (slowQueryTime > 0)
            {
                long end = System.currentTimeMillis();
                long x = end - start;
                if (x >= slowQueryTime)
                {
                    log.warn("slowQueryTime: 执行{} 耗时: {} 毫秒", method, x);
                }
            }
        }
    }

    private static Object businessProcess()
    {
        MethodInfo methodInfo = QueryContext.getMethodInfo();
        Class<?> returnType = QueryContext.getReturnType();
        Query[] queries = methodInfo.getQueries();
        Modifying modifying = methodInfo.getModifying();
        QueryByNamed queryById = methodInfo.getQueryByNamed();

        boolean hasSQLTpl = hasSQLTpl(queries, queryById); // 有SQL模板吗?

        if (hasSQLTpl)
        {
            if (returnType == Page.class)
            { // 分页
                return pagePro(queryById);
            }
            else if (modifying != null)
            { // 改
                return modifying();
            }
            else
            { // 查
                return query();
            }
        }
        else
        {
            Id id = methodInfo.getId();
            if (id != null)
            { // 有@id
                return methodQuery(id);
            }
            else
            { // 无@id
                return methodQuery();
            }
        }
    }

    private static Object pagePro(QueryByNamed queryById)
    {
            List<SQLValue> sqlValues = queryById == null ? QueryParser.pageParser() : QueryParser.pageParserByNamed();
            return queryPage(sqlValues);
    }

    private static boolean hasSQLTpl(Query[] queries, QueryByNamed queryById)
    {
        return queries.length > 0 || queryById != null;
    }
}
