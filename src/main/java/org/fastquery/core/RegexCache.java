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

package org.fastquery.core;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 正则编译缓存
 *
 * @author xixifeng (fastquery@126.com)
 */
@Slf4j
public final class RegexCache
{

    private RegexCache()
    {
        throw new IllegalStateException("Placeholder is Utility class");
    }

    public static final Pattern MOR_BLANK_PATT = Pattern.compile("\\s+");

    static final Pattern TABLE_REG_PATT = Pattern.compile("#\\{#table}");

    static final Pattern ID_REG_PATT = Pattern.compile("#\\{#id}");

    public static final Pattern SETS_REG_PATT = Pattern.compile("#\\{#sets}");
    public static final Pattern WHERE_REG_PATT = Pattern.compile("#\\{#where}");

    public static final Pattern LIMIT_RGE_PATT = Pattern.compile("#\\{#limit}");

    public static final Pattern SP1_REG_PATT = Pattern.compile("\\?\\d+");


    /**
     * 搜索出"?"后面的数字
     */
    public static final Pattern SEARCH_NUM_PATT = Pattern.compile("(?<=\\?)\\d+");

    /**
     * 匹配冒号表达式
     */
    public static final Pattern COLON_REG_PATT = Pattern.compile(":+[A-Za-z0-9]+");

    /**
     * 匹配EL表达式,可能包含有_ . ( ) 如: 在模板中调用 ${_method.getString()}
     */
    public static final Pattern EL_REG_PATT = Pattern.compile("\\$\\{?[A-Za-z0-9_.()]+}?");

    /**
     * 匹配EL表达式或匹配冒号表达式
     */
    public static final Pattern EL_OR_COLON_PATT = Pattern.compile("\\$\\{?[A-Za-z0-9_.()]+}?|:+[A-Za-z0-9]+");

    /**
     * 匹配微笑表达式
     */
    public static final Pattern SMILE_PATT = Pattern.compile("`-[^`]*\\?[^`]*-`");

    public static final Pattern SMILE_BIG_PATT = Pattern.compile("`-[^`]*[^`]*-`");

    public static final Pattern PERCENT_PATT = Pattern.compile("%+");

    private static final Map<String, Pattern> patterns = new HashMap<>();

    private static synchronized Pattern put(String regex)
    {
        int len = patterns.size();
        if (len > 50)
        {
            log.warn("正则的编译缓存已达到{}个，需要优化",len);
        }
        Pattern pattern = Pattern.compile(regex);
        // 调用 Map.put 返回是当前 put 之前，根据 key 获取的 val
        // 那么，一个 key 第一次 put 后，必然返回 null
        patterns.put(regex, pattern);
        return pattern;
    }

    public static Pattern getPattern(String regex)
    {
        Pattern pattern = patterns.get(regex);
        if (pattern != null)
        {
            return pattern;
        }
        else
        {
            return put(regex);
        }
    }
}
