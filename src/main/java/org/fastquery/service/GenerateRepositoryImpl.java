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

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.fastquery.asm.AsmRepository;
import org.fastquery.core.FQueryResourceImpl;
import org.fastquery.core.RepositoryException;
import org.fastquery.core.Resource;
import org.fastquery.core.StrConst;
import org.fastquery.dsm.FastQueryJson;
import org.fastquery.mapper.QueryPool;
import org.fastquery.util.ClassUtil;
import org.fastquery.util.FastQueryJSONObject;
import org.fastquery.util.LoadPrperties;

/**
 * @author xixifeng (fastquery@126.com)
 */
@Slf4j
class GenerateRepositoryImpl
{
    private static final FqClassLoader classLoader = FqClassLoader.getInstance();

    private static class LazyHolder
    {
        private static final GenerateRepositoryImpl INSTANCE = new GenerateRepositoryImpl();
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

    private <T> void generate(Class<T> repositoryClazz)
    {
        String name = repositoryClazz.getName() + StrConst.DB_SUF;

        byte[] bytes = AsmRepository.generateBytes(repositoryClazz);

        if(FastQueryJSONObject.getDebug())
        {
            File tmpDir = new File(System.getProperty("java.io.tmpdir"), "query");
            if(!tmpDir.exists() && tmpDir.mkdir())
            {
                log.debug("{} 之前不存在，已经创建完毕.", tmpDir);
            }
            File classFile = new File(tmpDir.getAbsolutePath(),name + ".class");
            try (FileOutputStream fos = new FileOutputStream(classFile)) {
                fos.write(bytes);
                log.debug("{}, 已经生成",classFile);
            } catch (Exception e) {
                throw new RepositoryException(e);
            }
        }

        classLoader.defineClassByName(name, bytes, bytes.length);
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

