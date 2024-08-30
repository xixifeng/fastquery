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
package org.fastquery.bean;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.ToString;
import org.fastquery.core.Id;
import org.fastquery.core.Table;

@Getter
@Setter
@Accessors(chain = true)
@ToString
@Table("area")
public class Area {
    /** 主键id */
    @Id
    private Long id;
    /** 国家 id */
    private Long countryId;
    /** 类型 (Type){'country','first','second','third','fourth','fifth'} 可选值，1：国家，2：一级，3：二级，4：三级，5：四级，6：五级 */
    private AreaType type;
    /** 地区中文名称 */
    private String areaName;
    /** 地区英文名称 */
    private String areaEnName;
    /** 国旗名称 */
    private String flagName;
    /** 代码 */
    private String code;
    /** 时间偏移量（相对于格林威治时间，单位：分钟） */
    private Integer timeOffset;
    /** 排序 */
    private Integer sort;
    /** 父 id */
    private Long pId;
    /** 电话代码 */
    private String phoneCode;
    /** userSignId */
    private Long sid;
}

