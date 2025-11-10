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

import org.fastquery.core.RepositoryException;

import java.lang.reflect.InvocationTargetException;

/**
 * 通用属性转换接口，用于在实体字段与数据库字段之间进行双向转换。
 *
 * 框架在读取数据库记录或写入数据时，可通过实现该接口来自定义类型转换逻辑。
 * 例如，将数据库中的用于表示状态的整数映射为枚举类型。
 *
 * @param <A> 实体字段的类型（Entity Field Type）
 * @param <B> 数据库字段的类型（Database Field Type），对应 JDBC 层从 MySQL 中取出的 Java 类型。
 *            常见映射如下：
 *            <pre>
 *            对应 MySQL 类型       对应 JDBC 类型（B）
 *            -----------------------------------------
 *            INTEGER              Integer
 *            BIGINT               Long
 *            FLOAT                Float
 *            DOUBLE               Double
 *            TINYINT(1)           Boolean / Integer
 *            BLOB                 byte[]
 *            VARCHAR              String
 *            DATE                 java.sql.Date
 *            TIME                 java.sql.Time
 *            TIMESTAMP            java.sql.Timestamp
 *            </pre>
 *
 * @author xixifeng (fastquery@126.com)
 */
public interface AttributeConverter<A,B>
{

    /**
     * 将数据库字段值转换为实体字段值。
     *
     * @param dbField 数据库字段的值
     * @param clazz   实体字段的类型，用于在必要时创建或推断具体类型
     * @return 转换后的实体字段值
     */
    A toEntityField(B dbField, Class<? extends A> clazz);

    /**
     * 将实体字段值转换为数据库字段值。
     *
     * @param entityField 实体字段的值
     * @return 转换后的数据库字段值
     */
    B toDBField (A entityField);


    /**
     * 创建指定转换器类的实例。
     * <p>
     * 该方法要求目标类具有无参构造方法，否则会抛出 {@link RepositoryException}。
     *
     * @param clazz 转换器类的 {@link Class} 对象
     * @return 转换器实例
     * @throws RepositoryException 如果实例化失败或无可访问的无参构造方法
     */
    static AttributeConverter newInstance(Class<?> clazz)
    {
        try
        {
            return (AttributeConverter) clazz.getDeclaredConstructor().newInstance();
        }
        catch (InstantiationException | IllegalAccessException | InvocationTargetException |
               NoSuchMethodException e)
        {
            throw new RepositoryException(e);
        }
    }
}
