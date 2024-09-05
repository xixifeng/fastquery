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
import org.fastquery.struct.Chip;
import org.fastquery.struct.Predicate;

/**
 * @author xixifeng (fastquery@126.com)
 */
@Setter
@Getter
public class Bandit extends Predicate<Bandit>
{
    private Long id;
    private String name;
    private Integer age;
    private Integer sort;
    private Long createDateTime;
    private Long lastUpdateDateTime;

    public Chip<Long,Bandit> id()
    {
        return new Chip<>("id");
    }

    public Chip<String,Bandit> name()
    {
        return new Chip<>("name");
    }

    public Chip<Integer,Bandit> sort()
    {
        return new Chip<>("sort");
    }

    public static Chip<Integer,Bandit> age()
    {
        return new Chip<>("age");
    }

    public Chip<Long,Bandit> createDateTime()
    {
        return new Chip<>("createDateTime");
    }

    public Chip<Long,Bandit> lastUpdateDateTime()
    {
        return new Chip<>("lastUpdateDateTime");
    }
}

