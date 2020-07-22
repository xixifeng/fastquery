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

import org.apache.commons.lang3.ArrayUtils;
import org.fastquery.util.BeanUtil;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author xixifeng (fastquery@126.com)
 */
public class SelectField<T> {

    private Class<T> clazz;
    private boolean contain;
    private String[] fields = {};

    public SelectField(Class<T> clazz, boolean contain, String... fields) {
        Objects.requireNonNull(clazz,"clazz 不能为null");
        this.clazz = clazz;
        this.contain = contain;
        if(fields != null) {
            this.fields = fields;
        }
    }

    public String getFields(){
        List<Field> fields = BeanUtil.mapFields(this.clazz);
        StringBuilder sb = new StringBuilder(6 * fields.size()); // 一个 field 大概包含 6 个字符
        fields.forEach(f -> {
            String fieldName = f.getName();
            if(contain) {
                 if(this.fields.length == 0 || ArrayUtils.contains(this.fields, fieldName)) {
                     sb.append(',');
                     sb.append(f.getName());
                 }
            } else {
                if(this.fields.length == 0 || !ArrayUtils.contains(this.fields, fieldName)) {
                    sb.append(',');
                    sb.append(f.getName());
                }
            }
        });
        int len = sb.length();
        if (len > 0) {
            sb.deleteCharAt(0);
        } else {
            sb.append(1);
        }

        return sb.toString();
    }

    public Class<T> getClazz() {
        return clazz;
    }
}
