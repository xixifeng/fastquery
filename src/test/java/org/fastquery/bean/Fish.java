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

/**
 * @author mei.sir@aliyun.cn
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Fish extends Named
{

    @Id
    private Integer id;
    private Integer num;

    public Fish(Integer id, String name, Integer num)
    {
        super(name);
        this.id = id;
        this.num = num;
    }

    public Fish(String name, Integer num)
    {
        this.num = num;
    }
}
