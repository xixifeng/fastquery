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

import lombok.extern.slf4j.Slf4j;
import org.fastquery.core.Placeholder;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.InvocationTargetException;

/**
 * @author xixifeng (fastquery@126.com)
 **/
@Slf4j
public class RepositoryFactory<T>  implements FactoryBean<T>
{
    private Class<T> dbInterface;

    public RepositoryFactory(Class<T> dbInterface) {
        this.dbInterface = dbInterface;
    }


    @Override
    public T getObject() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException
    {
        log.info("正在获取 {} 实例", dbInterface);
        String name = dbInterface.getName() + Placeholder.DB_SUF;
        return (T) FqClassLoader.getInstance().loadClass(name).getMethod("g").invoke(null);
    }

    @Override
    public Class<?> getObjectType()
    {
        return dbInterface;
    }

    @Override
    public boolean isSingleton()
    {
        return true;
    }
}
