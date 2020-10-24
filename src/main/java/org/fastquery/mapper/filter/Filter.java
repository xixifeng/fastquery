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

package org.fastquery.mapper.filter;

import org.fastquery.core.RepositoryException;
import org.w3c.dom.Element;

/**
 * @author xixifeng (fastquery@126.com)
 */
@FunctionalInterface
public interface Filter
{

    /**
     * 过滤
     *
     * @param xmlName xml名称
     * @param element element 待检测的Element
     * @return 元素
     */
    Element doFilter(String xmlName, Element element);

    /**
     * 终止(扯断链条)
     *
     * @param errmsg 终止理由
     */
    default void abortWith(String errmsg)
    {
        throw new RepositoryException(errmsg);
    }

}
