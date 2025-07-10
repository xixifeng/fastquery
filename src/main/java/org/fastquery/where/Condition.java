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
 * 用于构建动态 SQL 条件的注解。支持基于参数值的忽略规则、自定义判断器、脚本判断等灵活机制。可重复使用标识。
 *
 * @author xixifeng (fastquery@126.com)
 */
@Repeatable(Conditions.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
public @interface Condition
{

    /**
     * SQL 片段，表示该条件的具体内容。<br>
     * 例如：{@code "AND name = :1"}。
     *
     * @return SQL 条件片段
     */
    String value();

    /**
     * 忽略条件的规则。<br>
     * 当前条件中涉及的变量值将逐个与该规则数组中的正则表达式进行匹配，<br>
     * 只要有任意一项匹配成功，即忽略该条件。
     *
     * @return 正则表达式数组，用于判断是否忽略该条件
     */
    String[] ignoreRule() default {};

    /**
     * 是否忽略值为 {@code null} 的参数。<br>
     * 当为 {@code true}（默认）时，如果参数为 null，该条件将不参与 SQL 构建。
     *
     * @return 是否忽略 null 值参数
     */
    boolean ignoreNull() default true;

    /**
     * 是否忽略值为空字符串（{@code ""}）的参数。<br>
     * 当为 {@code true}（默认）时，如果参数为 ""，该条件将不参与 SQL 构建。
     *
     * @return 是否忽略空字符串参数
     */
    boolean ignoreEmpty() default true;

    /**
     * 自定义判断逻辑类，用于决定是否忽略当前条件。<br>
     * 必须实现 {@link Judge} 抽象类，默认使用 {@link DefaultJudge}。
     *
     * @return 判断器类，用于动态控制条件是否保留
     */
    Class<? extends Judge> ignore() default DefaultJudge.class;

    /**
     * 通过运行 Java 脚本表达式，判断是否忽略当前条件。<br>
     * 脚本运行结果必须为布尔类型；当结果为 {@code true}，该条件将被忽略；为 {@code false}，则保留该条件。<br>
     * 默认脚本为 {@code "false"}，表示不忽略（始终保留）。
     *
     * @return Java 脚本表达式，返回值必须为布尔类型
     */
    String ignoreScript() default "false";

    /**
     * 指定一个 Java 表达式（脚本）用于判断是否保留当前条件项。<br>
     * 当表达式结果为 {@code true} 时，保留该 Condition；<br>
     * 当结果为 {@code false}，则使用 {@link #otherwise()} 指定的值作为新的条件项。<br>
     * 注意：若 {@code otherwise} 的值为 {@code ""}，表示忽略该 Condition（即不追加该条件）。
     *
     * @return 用于判断的 Java 脚本表达式
     */
    String when() default "true";

    /**
     * 当 {@link #when()} 条件为 {@code false} 时，将使用该值作为新的条件内容。<br>
     * 若该值为 {@code ""}，则表示忽略该 Condition（即不追加该条件）。
     *
     * @return {@code when} 条件不成立时的备选值
     */
    String otherwise() default StringUtils.EMPTY;
}
