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

package org.fastquery.dsm;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * 对 fastquery.json 文件的映射 <br>
 *
 * 注意: 已经根据dataSourceName重写了hashCode和equals,用于标识该对象在比较时作为唯一标识. 修改这个属性要特别注意.
 * 如果把这个对象放入到hash集合中,而在外界修改了这个属性,那么会出现内存溢出
 *
 * @author xixifeng (fastquery@126.com)
 */
@Setter
@Getter
@EqualsAndHashCode(of = "dataSourceName")
public class FastQueryJson
{
    private String config;
    private String dataSourceName; // 重写它hashCode和equals,用它来标识唯一标识.
    private Set<String> basePackages = new HashSet<>();
}
