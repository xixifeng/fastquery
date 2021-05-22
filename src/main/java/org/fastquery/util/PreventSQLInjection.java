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

import org.apache.commons.lang3.StringUtils;

/**
 * @author mei.sir@aliyun.cn
 */
public class PreventSQLInjection
{

    // 敏感sql关键字
    private static final String[] KEYS =
            {
                    "drop", "select", "declare", "information_schema.columns", "use",
                    "insert", "update", "mid", "delete", "like'", "truncate", "and",
                    "by", "sitename", "create", "from", "where", "xp_cmdshell", "table",
                    "order", "--", "//", "or", "#", "%", "like", "'", "count", "column_name",
                    "*", "+", "union", "chr", "net user", ",", "execute", "-", "master", "/",
                    "group_concat", "char", "table_schema", ";", "grant", "exec"
            };

    private PreventSQLInjection()
    {
    }

    /**
     * 判断是否是注入SQL
     *
     * @param str 待检测的字符串
     * @return 如果是敏感字符串就返回true, 反之false
     */
    public static boolean isInjectStr(String str)
    {
        if (str != null && !StringUtils.EMPTY.equals(str.trim()))
        {
            String s = str.toLowerCase();
            for (String key : KEYS)
            {
                if (s.trim().contains(key))
                {
                    return true;
                }
            }
        }
        return false;
    }
}
