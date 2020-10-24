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

package org.fastquery.analysis;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

import org.fastquery.page.Page;
import org.fastquery.util.TypeUtil;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 查询返回值安全检测,既然是查询,如果方法的返回值是void,显然是不受允许的.
 *
 * @author xixifeng (fastquery@126.com)
 */
class QueryReturnTypeFilter implements MethodFilter
{

    @Override
    public void doFilter(Method method)
    {
        String errmsg = String.format("为这个方法设置的返回值错误,其返回值类型支持类型如下:%n%s %n%s %n%s %n%s %n%s %n%s %n%s %n%s %n%s %n%s %n%s %n%s %n%s %n",
                "1). long/int 用于统计总行数", "2). boolean 判断是否存在", "3). Map<String,Object>", "4). List<Map<String,Object>>",
                "5). List<Map<String,String>>", "6). List<实体>", "7). Page", "8). JSONObject", "9). JSONArray",
                "10). Integer,Double,Long,Short,Byte,Character,Float,String 八种基本类型(除了Boolean)",
                "11). Integer[],Double[],Long[],Short[],Byte[],Character[],Float[]", "12). 自定义实体数组", "13).自定义实体,必须包含有默认的构造函数");

        Type genericReturnType = method.getGenericReturnType();
        Class<?> returnType = method.getReturnType();

        // 如果不在允许类型范围之内
        if (returnType != long.class &&
                returnType != int.class &&
                returnType != boolean.class &&
                !TypeUtil.isMapSO(genericReturnType) &&
                !TypeUtil.isListMapSO(genericReturnType) &&
                returnType != List.class &&
                returnType != Page.class &&
                returnType != JSONObject.class &&
                returnType != JSONArray.class &&
                !TypeUtil.isWarrp(returnType) &&
                !(returnType != null && TypeUtil.isWarrp(returnType.getComponentType())) && // 如果不是 "包装类型[]"
                !(returnType != null && TypeUtil.hasDefaultConstructor(returnType.getComponentType())) && // 如果不是 "bean[]"
                !TypeUtil.hasDefaultConstructor(returnType) // 不是bean
        )
        {
            this.abortWith(method, errmsg);
        }

    }
}
