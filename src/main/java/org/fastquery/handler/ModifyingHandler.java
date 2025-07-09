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

package org.fastquery.handler;

import java.util.List;

import lombok.experimental.UtilityClass;
import org.fastquery.struct.RespUpdate;

/**
 * @author xixifeng (fastquery@126.com)
 */
@UtilityClass
public final class ModifyingHandler
{
    // 对于改操作可根据其返回值分类如下(也就是说只允许这这些类型,在生成类之前已经做预处理,越界类型是进来不了的)
    // 1). 返回值是void
    // 2). 返回值是int,int[]
    // 3). 返回值是Map<String,Object> 只对insert或update有效
    // 4). 返回值是JSONObject类型 只对insert或update有效
    // 5). 返回值是Primarykey
    // 6). 返回值是boolean
    // 7). 返回值是实体 只对insert或update有效
    // 为什么要分类?
    // 如果全部集中处理的话,代码堆积会很多,可读性差,不利于扩展.
    // 对于复杂的事情,一定要找适合的模式,尽可能地分化成的小的模块
    public static Object voidType()
    {
        return null;
    }

    public static boolean booleanType(List<RespUpdate> respUpdates)
    {
        return intType(respUpdates) >= 0;
    }

    public static int intType(List<RespUpdate> respUpdates)
    {
        int sum = 0;
        for (RespUpdate respUpdate : respUpdates)
        {
            sum += respUpdate.getEffect();
        }
        return sum;
    }
}
