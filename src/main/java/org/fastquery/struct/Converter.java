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
package org.fastquery.struct;

import lombok.extern.slf4j.Slf4j;
import org.fastquery.core.RepositoryException;
import org.fastquery.util.TypeUtil;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author xixifeng (fastquery@126.com)
 */
@Slf4j
public class Converter
{
    private final Class<?> sourceType;
    private final Class<?> targetType;
    private final Class<?> converterClazz;

    private static final List<Converter> converters = new ArrayList<>();

    public Converter(String sourceType, String targetType, String converter)
    {
        try
        {
            this.sourceType = Class.forName(sourceType);
            this.targetType = Class.forName(targetType);
            this.converterClazz = Class.forName(converter);
        }
        catch (ClassNotFoundException e)
        {
            throw new RepositoryException(e);
        }
    }

    public static void add(Converter newConverter)
    {
       if(!exist(newConverter))
       {
          converters.add(newConverter);
       }
       else
       {
           log.warn("重复的类型或转换器，未添加: {}", newConverter);
       }
    }

    private static boolean exist(Converter newConverter)
    {
        for (Converter c : converters)
        {
            if (c.targetType == newConverter.targetType || c.targetType.isAssignableFrom(newConverter.targetType) || newConverter.targetType.isAssignableFrom(c.targetType) || c.converterClazz == newConverter.converterClazz)
            {
                throw new RepositoryException(c + " 与 " + newConverter + " 存在类型冲突。");
            }
        }
        return false;
    }

    public static AttributeConverter findConverter(Class<?> targetType)
    {
        if (targetType != null)
        {
            for (Converter c : converters)
            {
                if (c.targetType == targetType || c.targetType.isAssignableFrom(targetType))
                {
                    return AttributeConverter.newInstance(c.converterClazz);
                }
            }
        }
        return null;
    }

    public static void fit(Object source, Object target)
    {
        Class<?> sourceClass = source.getClass();
        Class<?> targetClass = target.getClass();

        List<Field> targetFields = TypeUtil.getAllFields(targetClass);
        for (Field targetField : targetFields)
        {
            String targetFieldName = targetField.getName();
            Field sourceField = ReflectionUtils.findField(sourceClass, targetFieldName);
            if (sourceField != null)
            {
                Class<?> sourceFieldType = sourceField.getType();
                Class<?> targetFieldType = targetField.getType();
                Converter c = find(sourceFieldType, targetFieldType);
                Class<?> converterClazz = null;
                if (c != null)
                {
                    converterClazz = c.converterClazz;
                    convert(source, sourceField, target, targetField, converterClazz);
                }
                annotationConvert(source, sourceField, target, targetField, converterClazz);
            }
        }
    }

    public static void fit(Map<String, Object> map, Field targetField)
    {
        Class<?> targetFieldType = targetField.getType();
        String targetFieldName = targetField.getName();
        Object sourceFieldVal = map.get(targetFieldName);
        if (sourceFieldVal != null)
        {

            Class<?> sourceFieldType = sourceFieldVal.getClass();
            Converter c = find(sourceFieldType, targetFieldType);
            Class<?> converterClazz = null;
            if (c != null)
            {
                converterClazz = c.converterClazz;
                change(AttributeConverter.newInstance(converterClazz), sourceFieldVal, map, targetField);
            }

            Convert convert = targetField.getAnnotation(Convert.class);
            if (convert != null)
            {
                Class<?> clazz = convert.value();
                if(converterClazz != clazz) // 全局和局部排除被同一个转换器执行
                {
                    change(AttributeConverter.newInstance(clazz), sourceFieldVal, map, targetField);
                }
            }

        }
    }

    private static void change(AttributeConverter clazz, Object sourceFieldVal, Map<String, Object> map, Field targetField)
    {
        String targetFieldName = targetField.getName();
        Class<?> targetFieldType = targetField.getType();
        Object o = clazz.toEntityField(sourceFieldVal,targetFieldType);
        map.put(targetFieldName, o);
    }

    private static void annotationConvert(Object source, Field sourceField, Object target, Field targetField,Class<?> converterClazz)
    {
        // 目标字段明确指定转换规则
        Convert convert = targetField.getAnnotation(Convert.class);
        if (convert != null && sourceField != null && targetField.getType() != sourceField.getType())
        {
            Class<? extends AttributeConverter> clazz = convert.value();
            if(clazz != converterClazz)
            {
                convert(source, sourceField, target, targetField, clazz);
            }
        }

    }

    private static void convert(Object source, Field sourceField, Object target, Field targetField, Class<?> converterClazz)
    {
        try
        {
            ReflectionUtils.makeAccessible(sourceField);
            Object sourceFieldVal = sourceField.get(source);
            if (sourceFieldVal != null)
            {
                ReflectionUtils.makeAccessible(targetField);
                AttributeConverter attributeConverter = AttributeConverter.newInstance(converterClazz);
                Object targetFieldVal = attributeConverter.toEntityField(sourceFieldVal,targetField.getType());
                ReflectionUtils.setField(targetField, target, targetFieldVal);
            }
        }
        catch (IllegalAccessException e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    private static Converter find(Class<?> sourceType, Class<?> targetType)
    {
        for (Converter c : converters)
        {
            if ((c.sourceType == sourceType || c.sourceType.isAssignableFrom(sourceType)) && (c.targetType == targetType || c.targetType.isAssignableFrom(targetType)))
            {
                return c;
            }
        }
        return null;
    }
}
