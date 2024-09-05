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
import org.fastquery.core.Transient;
import org.fastquery.struct.Chip;

/**
 * @author xixifeng (fastquery@126.com)
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Student
{

    private String no;
    private String name;
    private String sex;
    private Integer age;
    private String dept;
    @Transient
    private String description;
    @Transient
    private Long ssid;

    public static Chip<String,Bandit> no()
    {
        return new Chip<>("no");
    }

    public static Chip<String,Bandit> name()
    {
        return new Chip<>("name");
    }

    public static Chip<String,Bandit> sex()
    {
        return new Chip<>("sex");
    }

    public static Chip<Integer,Bandit> age()
    {
        return new Chip<>("age");
    }

    public static Chip<String,Bandit> dept()
    {
        return new Chip<>("dept");
    }

    public Student(String no, String name, String sex, Integer age, String dept)
    {
        this.no = no;
        this.name = name;
        this.sex = sex;
        this.age = age;
        this.dept = dept;
    }

    public Student(String no, String name, String sex, Integer age, String dept, String description)
    {
        this.no = no;
        this.name = name;
        this.sex = sex;
        this.age = age;
        this.dept = dept;
        this.description = description;
    }
}
