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

package org.fastquery.service;

import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import lombok.experimental.UtilityClass;
import org.fastquery.core.Repository;
import org.fastquery.core.RepositoryException;
import org.fastquery.core.StrConst;
import org.fastquery.dsm.FQueryProperties;

/**
 * @author xixifeng (fastquery@126.com)
 */
@UtilityClass
public class FQuery
{ // NO_UCD
    /**
     * 获取 Repository
     *
     * @param <T>   接口
     * @param clazz 接口class
     * @return 接口的实例
     */
    @SuppressWarnings("unchecked")
    public static <T extends Repository> T getRepository(Class<T> clazz)
    {
        String name = clazz.getName() + StrConst.DB_SUF;
        try
        {
            return (T) GenerateRepositoryImpl.getInstance().getClassLoader().loadClass(name).getMethod("g").invoke(null);
        }
        catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException
                | ClassNotFoundException e)
        {
            throw new RepositoryException("获取代理实现类异常", e);
        }
    }

    /**
     * 创建一个数据源
     *
     * @param dataSourceName 数据源的名称,不能重复.
     * @param properties     连接池的配置
     */
    public static void createDataSource(String dataSourceName, Properties properties)
    { // NO_UCD
        FQueryProperties.createDataSource(dataSourceName, properties);
    }
}
