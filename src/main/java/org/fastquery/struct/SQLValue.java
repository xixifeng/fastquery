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

package org.fastquery.struct;
import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RegExUtils;
import org.fastquery.core.RegexCache;
import org.fastquery.core.StrConst;

/**
 * SQL和值
 *
 * @author mei.sir@aliyun.cn
 */
@Slf4j
@Setter
@Getter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class SQLValue
{
    private String sql; // 待执行的sql
    private List<Object> values = new ArrayList<>();// sql语言中"?"对应的实参

    public SQLValue(String sql, List<Object> values)
    {

        log.debug("SQL扩展之前:{}", sql);

        this.sql = sql;

        this.sql = RegExUtils.replaceAll(this.sql, RegexCache.SP1_REG_PATT, StrConst.QUE);
        this.values = values;
    }

    public void addValues(List<Object> values)
    {
        this.getValues().addAll(values);
    }

    public void setValues(List<Object> values)
    {
        this.values = new ArrayList<>(values);
        // this.values 中途会修改它，为了不破坏入参的源头，因此需要做浅 copy
    }
}
