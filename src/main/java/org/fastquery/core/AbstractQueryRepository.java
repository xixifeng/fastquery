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
import java.math.BigInteger;
import java.util.Collection;
import java.util.function.LongSupplier;

import org.fastquery.page.Page;

/**
 * @author mei.sir@aliyun.cn
 */
public abstract class AbstractQueryRepository implements QueryRepository
{

    private static final String EXECUTE_BATCH = "executeBatch";

    private static final String SAVE_TO_ID = "saveToId";

    private static final String SAVE_ARRAY = "saveArray";

    private static final String EXECUTE_UPDATE = "executeUpdate";

    private static final String EXECUTE_SAVE_OR_UPDATE = "executeSaveOrUpdate";

    private static final String UPDATE = "update";

    private static final String DELETE = "delete";

    private static final String SAVE = "save";

    private static final String INSERT = "insert";

    private static final Class<QueryRepository> c = QueryRepository.class;

    private final MethodInfo[] m = new MethodInfo[17];

    private void cache(int j, String name, Class<?>... parameterTypes)
    {
        Method localMethod;
        try
        {
            localMethod = c.getMethod(name, parameterTypes);
        }
        catch (Exception localException)
        {
            throw new RepositoryException(localException);
        }
        // 针对此处的m是线程安全的
        m[j] = new MethodInfo(localMethod);
    }

    @Override
    public int[] executeBatch(String paramString)
    {
        int j = 0;
        if (m[j] == null)
        {
            cache(j, EXECUTE_BATCH, String.class);
        }
        return (int[]) Prepared.excute(m[j], new Object[]{paramString}, this);
    }

    @Override
    public int[] executeBatch(String paramString1, String[] paramArrayOfString)
    {
        int j = 1;
        if (m[j] == null)
        {
            cache(j, EXECUTE_BATCH, String.class, String[].class);
        }
        return (int[]) Prepared.excute(m[j], new Object[]{paramString1, paramArrayOfString}, this);
    }

    @Override
    public long tx(LongSupplier paramSupplier)
    {
        int j = 2;
        if (m[j] == null)
        {
            cache(j, "tx", LongSupplier.class);
        }
        return (Long) Prepared.excute(m[j], new Object[]{paramSupplier}, this);
    }

    @Override
    public BigInteger saveToId(Object paramObject)
    {
        int j = 3;
        if (m[j] == null)
        {
            cache(j, SAVE_TO_ID, Object.class);
        }
        return (BigInteger) Prepared.excute(m[j], new Object[]{paramObject}, this);
    }

    @Override
    public int saveArray(boolean paramBoolean, Object... paramArrayOfObject)
    {
        int j = 4;
        if (m[j] == null)
        {
            cache(j, SAVE_ARRAY, boolean.class, Object[].class);
        }
        return (Integer) Prepared.excute(m[j], new Object[]{paramBoolean, paramArrayOfObject}, this);
    }

    @Override
    public int executeUpdate(Object paramObject)
    {
        int j = 5;
        if (m[j] == null)
        {
            cache(j, EXECUTE_UPDATE, Object.class);
        }
        return (Integer) Prepared.excute(m[j], new Object[]{paramObject}, this);
    }

    @Override
    public int executeSaveOrUpdate(Object paramObject)
    {
        int j = 6;
        if (m[j] == null)
        {
            cache(j, EXECUTE_SAVE_OR_UPDATE, Object.class);
        }
        return (Integer) Prepared.excute(m[j], new Object[]{paramObject}, this);
    }

    @Override
    public <E> int update(Collection<E> paramCollection)
    {
        int j = 7;
        if (m[j] == null)
        {
            cache(j, UPDATE, Collection.class);
        }
        return (Integer) Prepared.excute(m[j], new Object[]{paramCollection}, this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> E find(Class<E> clazz, long paramLong, boolean contain, String... fields)
    {
        int j = 8;
        if (m[j] == null)
        {
            cache(j, "find", Class.class, long.class, boolean.class, String[].class);
        }
        return (E) Prepared.excute(m[j], new Object[]{clazz, paramLong, contain, fields}, this);
    }

    @Override
    public int delete(String paramString1, String paramString2, long paramLong)
    {
        int j = 9;
        if (m[j] == null)
        {
            cache(j, DELETE, String.class, String.class, long.class);
        }
        return (Integer) Prepared.excute(m[j], new Object[]{paramString1, paramString2, paramLong}, this);
    }

    @Override
    public <B> int save(boolean paramBoolean, Collection<B> paramCollection)
    {
        int j = 10;
        if (m[j] == null)
        {
            cache(j, SAVE, boolean.class, Collection.class);
        }
        return (Integer) Prepared.excute(m[j], new Object[]{paramBoolean, paramCollection}, this);
    }

    @Override
    public int insert(Object paramObject)
    {
        int j = 11;
        if (m[j] == null)
        {
            cache(j, INSERT, Object.class);
        }
        return (Integer) Prepared.excute(m[j], new Object[]{paramObject}, this);
    }


    @Override
    public long count(Object entity)
    {
        int j = 12;
        if (m[j] == null)
        {
            cache(j, "count", Object.class);
        }
        return (Long) Prepared.excute(m[j], new Object[]{entity}, this);
    }

    @Override
    public <E> E findOne(E equals, boolean contain, String... fields)
    {
        int j = 13;
        if (m[j] == null)
        {
            cache(j, "findOne", Object.class, boolean.class, String[].class);
        }
        return (E) Prepared.excute(m[j], new Object[]{equals, contain, fields}, this);
    }

    @Override
    public boolean exists(Object entity)
    {
        int j = 14;
        if (m[j] == null)
        {
            cache(j, "exists", Object.class);
        }
        return (boolean) Prepared.excute(m[j], new Object[]{entity}, this);
    }

    @Override
    public String existsEachOn(Object entity)
    {
        int j = 15;
        if (m[j] == null)
        {
            cache(j, "existsEachOn", Object.class);
        }
        return (String) Prepared.excute(m[j], new Object[]{entity}, this);
    }

    @Override
    public <E> Page<E> findPageByPredicate(E equals, boolean notCount, int pageIndex, int pageSize, boolean contain, String... fields)
    {
        int j = 16;
        if (m[j] == null)
        {
            cache(j, "findPageByPredicate", Object.class, boolean.class, int.class, int.class, boolean.class, String[].class);
        }
        return (Page<E>) Prepared.excute(m[j], new Object[]{equals, notCount, pageIndex, pageSize, contain, fields}, this);
    }

}
