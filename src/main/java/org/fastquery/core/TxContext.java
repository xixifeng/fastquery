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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.fastquery.struct.DC;

/**
 * tx 上下文
 *
 * @author mei.sir@aliyun.cn
 */
@Slf4j
class TxContext
{
    private static final ThreadLocal<TxContext> threadLocal = new ThreadLocal<>();

    private final List<DC> dclist = new ArrayList<>();

    static void start()
    {
        threadLocal.set(new TxContext());
    }

    static TxContext getTxContext()
    {
        return threadLocal.get();
    }

    static boolean enabled()
    {
        return getTxContext() != null;
    }

    // 如果在列中已经存在了,就不添加
    Connection addConn(DataSource ds) throws SQLException
    {
        for (DC dc : dclist)
        {
            if (dc.getDs() == ds)
            {
                return dc.getConn();
            }
        }

        DC dc = new DC(ds, ds.getConnection());
        dc.getConn().setAutoCommit(false);
        dclist.add(dc);

        return dc.getConn();
    }

    void commit() throws SQLException
    {
        int len = dclist.size();
        for (int i = len - 1; i >= 0; i--)
        {
            dclist.get(i).getConn().commit();
        }
    }

    void rollback()
    {
        int len = dclist.size();
        for (int i = len - 1; i >= 0; i--)
        {
            try
            {
                dclist.get(i).getConn().rollback();
            }
            catch (SQLException e)
            {
                log.error("conn rollback败", e);
            }
        }
    }

    private void clear()
    {
        // 先关闭所有连接
        int len = dclist.size();
        for (int i = len - 1; i >= 0; i--)
        {
            try
            {
                dclist.get(i).getConn().close();
            }
            catch (SQLException e)
            {
                log.error("conn 关闭失败", e);
            }
        }

        // 清空集合
        dclist.clear();

        // 移出范围
        threadLocal.remove();
    }

    static void end()
    {
        getTxContext().clear();
    }
}
