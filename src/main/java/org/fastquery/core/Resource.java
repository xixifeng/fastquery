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

import java.io.InputStream;

/**
 * 资源获取
 *
 * @author xixifeng (fastquery@126.com)
 */
public interface Resource
{

    /**
     * 获取name资源 <br>
     * 注意: 没有找到返回null
     *
     * @param name 资源名称
     * @return 输入流
     */
    InputStream getResourceAsStream(String name);

    /**
     * 当前是否存在 name 资源 <br>
     * 注意: 传递的name第一个字符不能是"/",不然找不到(已经验证)
     *
     * @param name 资源名称
     * @return 存在true, 反之, false
     */
    boolean exist(String name);
}
