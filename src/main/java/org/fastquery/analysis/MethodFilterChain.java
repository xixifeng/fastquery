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
import java.util.ArrayList;
import java.util.List;

/**
 * 责任链
 *
 * @author xixifeng (fastquery@126.com)
 */
class MethodFilterChain implements MethodFilter
{

    private final List<MethodFilter> methodFilters = new ArrayList<>();

    void addFilter(MethodFilter methodFilter)
    {
        methodFilters.add(methodFilter);
    }

    @Override
    public void doFilter(Method method)
    {
        for (MethodFilter methodFilter : methodFilters)
        {
            methodFilter.doFilter(method);
        }
        methodFilters.clear();
    }

}
