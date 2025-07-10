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

package org.fastquery.where;

import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于构建动态 SQL 的 SET 片段。支持通过参数值、判断器、脚本等方式灵活控制是否应用该项设置。同时可以标识多个 Set.
 *
 * @author xixifeng (fastquery@126.com)
 */
@Repeatable(Sets.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
public @interface Set
{

    /**
     * SQL 设置项（通常为 UPDATE 语句中的 SET 子句内容）。<br>
     * 例如：{@code @Set("name = :1")}。
     *
     * @return SQL 设置表达式
     */
    String value();

    /**
     * 是否忽略参数值为 {@code null} 的设置项。<br>
     * 当设置为 {@code true}（默认）时，若变量值为 {@code null}，该设置项将不参与 SQL 构建。
     *
     * @return 是否忽略 null 值
     */
    boolean ignoreNull() default true;

    /**
     * 是否忽略参数值为空字符串（{@code ""}）的设置项。<br>
     * 当设置为 {@code true}（默认）时，若变量值为 {@code ""}，该设置项将不参与 SQL 构建。
     *
     * @return 是否忽略空字符串
     */
    boolean ignoreEmpty() default true;

    /**
     * 自定义判断器，用于动态决定是否忽略该设置项。<br>
     * 指定的类必须实现 {@link Judge} 抽象类。默认使用 {@link DefaultJudge}。
     *
     * @return 判断逻辑实现类
     */
    Class<? extends Judge> ignore() default DefaultJudge.class;
    
    /**
     * 指定一个 Java 表达式（脚本）用于动态判断是否保留该设置项。<br>
     * 当表达式结果为 {@code true}，保留该项；否则使用 {@link #otherwise()} 指定的值替代新的设置项。<br>
     * 若 {@code otherwise} 值为 {@code ""}，表示忽略该设置项（不参与 SQL 构建）。
     *
     * @return Java 脚本表达式，用于保留/替代控制
     */
    String when() default "true";


    /**
     * 当 {@link #when()} 表达式结果为 {@code false} 时，使用的替代 set 项。<br>
     * 若该值为 {@code ""}，则表示忽略该设置项。
     *
     * @return {@code when} 条件不成立时的备选值
     */
    String otherwise() default StringUtils.EMPTY;
}
