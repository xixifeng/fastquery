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
import org.fastquery.core.Modifying;

/**
 * 在QueryRepository中 方法上标识有{@link Modifying}
 *
 * @author xixifeng (fastquery@126.com)
 */
class ModifyingReturnTypeFilter implements MethodFilter
{

    @Override
    public void doFilter(Method method)
    {
        Class<?> returnType = method.getReturnType();
        String errmsg = String.format("该方法的返回值类型是：%s 不符合要求，仅支持以下类型作为返回值: %n%s \t\t- 表示无返回值，不关注操作结果;%n%s \t\t- 表示单个操作的影响行数;%n%s \t\t- 存储每个更新操作影响的行数，索引与操作顺序一致;%n%s \t- 表示操作是否成功，true 为成功，false 为失败.",
                returnType.getSimpleName(),
                "void",
                "int",
                "int[]",
                "boolean");
        if (returnType != void.class && returnType != int.class && returnType != int[].class && returnType != boolean.class)
        {
            this.abortWith(method, errmsg);
        }
    }
}
