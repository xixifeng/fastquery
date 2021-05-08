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

import lombok.*;
import org.fastquery.core.Id;
import org.fastquery.core.Table;
import org.fastquery.core.Transient;

import java.util.EnumSet;

/**
 * @author xixifeng (fastquery@126.com)
 */
@Table("type_feature")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class TypeFeature
{

    @Id
    private Long id;
    private String name;
    private Gender gender;
    private EnumSet<Ruits> ruits;
    private Integer sort;
    @Transient
    private Long ssid;

    public TypeFeature(Long id, String name, Gender gender, EnumSet<Ruits> ruits, Integer sort)
    {
        this.id = id;
        this.name = name;
        this.gender = gender;
        this.ruits = ruits;
        this.sort = sort;
    }
}
