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

package org.fastquery.jersey;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.fastquery.core.Repository;
import org.fastquery.core.RepositoryException;

/**
 * @author xixifeng (fastquery@126.com)
 */
class FqClassLoader extends ClassLoader
{

    private final FQueryBinder binder;
    private final List<String> resourceNames = new ArrayList<>();

    FqClassLoader(ClassLoader webClassLoader, FQueryBinder binder)
    {
        super(webClassLoader);
        this.binder = binder;
    }

    final Class<?> defineClassByName(String name, byte[] b, boolean isWebQuery)
    {
        Class<?> clazz = defineClass(name, b, 0, b.length);
        if (isWebQuery)
        {
            resourceNames.add(name);
        }
        else
        {
            try
            {
                Repository repository = (Repository) clazz.getMethod("g").invoke(null);
                binder.bind(repository).to(repository.getInterfaceClass());
            }
            catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e)
            {
                throw new RepositoryException(e);
            }
        }
        return clazz;
    }

    Class<?>[] getResourceClasses() throws ClassNotFoundException
    {
        int len = resourceNames.size();
        if (len != 0)
        {
            Class<?>[] classes = new Class<?>[len];
            for (int i = 0; i < len; i++)
            {
                classes[i] = this.loadClass(resourceNames.get(i));
            }
            resourceNames.clear();
            return classes;
        }
        else
        {
            return ArrayUtils.EMPTY_CLASS_ARRAY;
        }
    }
}
