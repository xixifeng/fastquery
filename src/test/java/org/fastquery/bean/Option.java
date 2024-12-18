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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author xixifeng (fastquery@126.com)
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Option
{
    private static final String EMPTY = "";

    private int value;
    private String label = EMPTY;
    private String enLabel = EMPTY;

    @Override
    public String toString()
    {
        return String.valueOf(value);
    }
    public Option(int value)
    {
        this.value = value;
    }

    public Option(int value, String label)
    {
        this.value = value;
        this.label = label;
    }
}
