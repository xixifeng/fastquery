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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.fastquery.core.RepositoryException;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * @author xixifeng (fastquery@126.com)
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FastQueryJSONObject
{

    private static final String DEBUG = "debug";

    private static JSONObject jo;

    static synchronized void setJsonObject(JSONObject o)
    {
        if (jo == null)
        {
            jo = o;
        }
        else
        {
            throw new RepositoryException("fastquery.json不能重复装载");
        }
    }

    /**
     * 获取基准路径
     *
     * @return basedir
     */
    public static String getBasedir()
    {
        return jo.getString("basedir");
    }

    public static boolean getDebug()
    {
        return (boolean) jo.getOrDefault(DEBUG, false);
    }

    public static List<String> getQueries()
    {
        List<String> strs = new ArrayList<>();
        JSONArray jsonArray = jo.getJSONArray("queries");
        if (jsonArray == null)
        {
            return strs;
        }
        jsonArray.forEach(s -> strs.add(s.toString()));
        return strs;
    }

    public static int getSlowQueryTime()
    {
        return jo.getIntValue("slowQueryTime");
    }

    static void check()
    {
        // 1). queries属性要么不配置,要么配置正确
        List<String> strs = getQueries();
        for (String str : strs)
        {
            if (StringUtils.EMPTY.equals(str))
            {
                continue;
            }
            if (str.charAt(0) == '/')
            {
                throw new RepositoryException(String.format("fastquery.json-> queries配置错误,\"%s\"的开头不应该有\"/\"", str));
            }
            if (str.charAt(str.length() - 1) != '/')
            {
                throw new RepositoryException(String.format("fastquery.json-> queries配置错误,\"%s\"的末尾必须加\"/\"", str));
            }
        }

        // 2). debug
        if (jo.containsKey(DEBUG))
        {
            Object debug = jo.get(DEBUG);
            if (!(debug instanceof Boolean))
            {
                throw new RepositoryException("fastquery.json -> debug 它的值只能是true或false,也可以不用配置,要配,请配置正确");
            }
        }

        // 3). slowQueryTime
        String slowQueryTime = jo.getString("slowQueryTime");
        if (slowQueryTime != null && !Pattern.matches("\\d+", slowQueryTime))
        {
            throw new RepositoryException("fastquery.json -> slowQueryTime 它的值只能是数字");
        }
    }
}
