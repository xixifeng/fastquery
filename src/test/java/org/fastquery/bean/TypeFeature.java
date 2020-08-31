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

import org.fastquery.core.Id;
import org.fastquery.core.Table;

import java.util.EnumSet;

/**
 *
 * @author xixifeng (fastquery@126.com)
 */
@Table("type_feature")
public class TypeFeature {

    @Id
    private Long id;
    private String name;
    private Gender gender;
    private EnumSet<Ruits> ruits;

    public EnumSet<Ruits> getRuits() {
        return ruits;
    }
    public void setRuits(EnumSet<Ruits> ruits) {
        this.ruits = ruits;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Gender getGender() {
        return gender;
    }
    public void setGender(Gender gender) {
        this.gender = gender;
    }

    @Override
    public String toString() {
        return "TypeFeature{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", gender=" + gender +
                ", ruits=" + ruits +
                '}';
    }
}
