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

package org.fastquery.util;

import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.fastquery.core.ConnectionPoolProvider;
import org.fastquery.core.Resource;
import org.fastquery.dsm.FastQueryJson;
import org.fastquery.dsm.FQueryProperties;

/**
 * @author xixifeng (fastquery@126.com)
 */
@Slf4j
@UtilityClass
public class LoadPrperties
{
    /**
     * 装载配置并且初始化数据源,该方法消耗大,只能被调用一次
     *
     * @param fqueryResource fquery.json 资源文件
     * @return set格式 fquery.json
     */
    public static Set<FastQueryJson> load(Resource fqueryResource)
    {
        Set<FastQueryJson> fqProperties = PropertiesUtil.getFQueryProperties(fqueryResource);
        String namedConfig;
        Set<String> basePackages;
        String config;

        Map<String, String> poolMap = XMLParse.toMap(fqueryResource, "pools.xml", "providers", "pools");
        if (fqueryResource.exist("pool-extend.xml"))
        {
            poolMap.putAll(XMLParse.toMap(fqueryResource, "pool-extend.xml", "providers", "pools"));
        }

        for (FastQueryJson fQueryPropertie : fqProperties)
        {
            config = fQueryPropertie.getConfig(); // 获取fastquery.json 中的config属性
            namedConfig = fQueryPropertie.getDataSourceName();

            String provider = poolMap.get(config);

            if (provider == null)
            {
                throw new ExceptionInInitializerError("根据" + config + "没有找到数据源提供者");
            }
            else if (FQueryProperties.findDataSource(namedConfig) == null && namedConfig != null)
            {
                ConnectionPoolProvider poolProvider;
                try
                {
                    Class<?> providerClazz = Class.forName(provider);
                    poolProvider = (ConnectionPoolProvider) providerClazz.getDeclaredConstructor().newInstance();
                }
                catch (Exception e)
                {
                    throw new ExceptionInInitializerError(provider + "初始化失败");
                }
                DataSource dataSource = poolProvider.getDataSource(fqueryResource, namedConfig);
                FQueryProperties.putDataSource(namedConfig, dataSource);
                log.debug("创建数据源:{},名称为:{}", dataSource, namedConfig);
            }

            basePackages = fQueryPropertie.getBasePackages();
            for (String basePackage : basePackages)
            {
                FQueryProperties.putDataSourceIndex(basePackage, namedConfig);
            }
        }

        return fqProperties;
    }

}
