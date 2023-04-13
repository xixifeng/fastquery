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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * #{#表达式} 解析
 *
 * @author mei.sir@aliyun.cn
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SharpExprParser
{
    public static Set<String> matchesNotrepeat(String str)
    {
        Objects.requireNonNull(str);

        Set<String> sets = new HashSet<>();

        find(str, sets);

        return sets;
    }

    private static void find(String str, Set<String> sets)
    {
        int startIndex = 0;
        int endIndex;
        boolean loop = true;
        while (loop)
        {
            startIndex = str.indexOf("#{#", startIndex);
            if (startIndex != -1)
            {
                endIndex = str.indexOf('}', startIndex + 1);
                if (endIndex != -1)
                {
                    sets.add(str.substring(startIndex, endIndex + 1));
                    startIndex = endIndex + 1;
                }
                else
                {
                    loop = false;
                }
            }
            else
            {
                break;
            }
        }
    }

}
