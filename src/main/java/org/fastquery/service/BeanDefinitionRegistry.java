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
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author xixifeng (fastquery@126.com)
 **/
@Slf4j
@Component
public class BeanDefinitionRegistry implements BeanDefinitionRegistryPostProcessor
{
    @Override
    public void postProcessBeanDefinitionRegistry(org.springframework.beans.factory.support.BeanDefinitionRegistry registry)
    {
        List<Class<?>> classes = GenerateRepositoryImpl.getInstance().getClasses();
        classes.forEach(beanClass -> {
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition();
            GenericBeanDefinition definition = (GenericBeanDefinition) builder.getRawBeanDefinition();
            definition.getConstructorArgumentValues().addGenericArgumentValue(beanClass);
            definition.setBeanClass(RepositoryFactory.class);
            registry.registerBeanDefinition(beanClass.getSimpleName(), definition);
        });
        GenerateRepositoryImpl.getInstance().cleanClasses();
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
    {
        // Do nothing because of X and Y.
    }

}