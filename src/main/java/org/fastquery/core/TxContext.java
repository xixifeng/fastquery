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
import javax.sql.DataSource;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * tx 上下文
 *
 * @author mei.sir@aliyun.cn
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class TxContext
{
    private static final ThreadLocal<TxContext> threadLocal = new ThreadLocal<>();

    private Connection connection;
    private DataSource dataSource;

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

    Connection setConn(DataSource ds) throws SQLException
    {
        if (connection == null)
        {
            connection = ds.getConnection();
            connection.setAutoCommit(false);
            dataSource = ds;
        }
        else if(ds != dataSource) { // connection 有值的情况下，外界出入不同数据源，应该拒绝掉
            throw new RepositoryException("tx 不支持多数据源");
        }
        return connection;
    }

    void commit() throws SQLException
    {
        if(connection != null) {
            connection.commit();
        }
    }

    void rollback()
    {
        if(connection != null) {
            try
            {
                connection.rollback();
            }
            catch (SQLException e)
            {
                throw new RepositoryException("回滚异常", e);
            }
        }
    }

    private void clear()
    {

        if(connection != null)
        {
            try
            {
                connection.close();
            }
            catch (SQLException e)
            {
                throw new RepositoryException("conn 关闭异常", e);
            }
        }

        // 移出范围
        threadLocal.remove();
    }

    static void end()
    {
        getTxContext().clear();
    }
}
