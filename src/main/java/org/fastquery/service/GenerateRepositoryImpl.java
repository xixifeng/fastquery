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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.fastquery.asm.AsmRepository;
import org.fastquery.core.FQueryResourceImpl;
import org.fastquery.core.Placeholder;
import org.fastquery.core.Resource;
import org.fastquery.dsm.FastQueryJson;
import org.fastquery.mapper.QueryPool;
import org.fastquery.util.ClassUtil;
import org.fastquery.util.LoadPrperties;

/**
 * @author xixifeng (fastquery@126.com)
 */
@Slf4j
class GenerateRepositoryImpl
{
    private final FqClassLoader classLoader = FqClassLoader.getInstance();

    private static class LazyHolder
    {
        private static final GenerateRepositoryImpl INSTANCE = new GenerateRepositoryImpl();

        private LazyHolder()
        {
        }
    }

    static GenerateRepositoryImpl getInstance()
    {
        return LazyHolder.INSTANCE;
    }


    private List<Class<?>> clses = new ArrayList<>();

    private GenerateRepositoryImpl()
    {

        log.debug("GenerateRepositoryImpl 已实例化.");

        Resource resource = new FQueryResourceImpl(classLoader);

        // 1). 装载配置文件
        Set<FastQueryJson> fqPropertie = LoadPrperties.load(resource);

        // 3). 批量生成 Repository 的实现类
        fqPropertie.forEach(fQueryPropertie -> {
            Set<String> basePackages = fQueryPropertie.getBasePackages();
            basePackages.forEach(basePackage -> {
                List<Class<?>> classes = ClassUtil.getClasses(basePackage);
                clses.addAll(classes);
                // classes.forEach(this::generate)
                classes.forEach(rcls -> {
                    generate(rcls); // 生成
                    QueryPool.put(rcls.getName(), resource);
                });
            });
        });

        // 3). 生成 Repository之后的检测
        AsmRepository.after(clses);
    }

    @SuppressWarnings("unchecked")
    private <T> Class<? extends T> generate(Class<T> repositoryClazz)
    {
        String name = repositoryClazz.getName() + Placeholder.DB_SUF;

        byte[] bytes = AsmRepository.generateBytes(repositoryClazz);


        /**
         * <pre>
         // 把生成的文件存储起来
         try (java.io.FileOutputStream fos = new java.io.FileOutputStream("/data/tmp/fquery/" + name + ".class")) {
         fos.write(bytes);
         } catch (Exception e) {
         e.printStackTrace();
         } // 把生成的文件存储起来 end
         </pre>
         */


        return (Class<? extends T>) classLoader.defineClassByName(name, bytes, bytes.length);
    }

    public FqClassLoader getClassLoader()
    {
        return classLoader;
    }

    List<Class<?>> getClasses()
    {
        return clses;
    }

    void cleanClasses()
    {
        clses.clear();
        clses = null;
    }
}

