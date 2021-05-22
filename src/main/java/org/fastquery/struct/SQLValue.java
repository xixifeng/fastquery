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
import java.util.List;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.fastquery.core.RegexCache;
import org.fastquery.core.QueryContext;
import org.fastquery.core.RepositoryException;
import org.fastquery.core.StrConst;
import org.fastquery.util.PreventSQLInjection;
import org.fastquery.util.TypeUtil;

/**
 * SQL和值
 *
 * @author mei.sir@aliyun.cn
 */
@Slf4j
@Setter
@Getter
public class SQLValue
{
    private String sql; // 待执行的sql
    private List<Object> values;// sql语言中"?"对应的实参

    public SQLValue()
    {
    }

    public SQLValue(String sql, List<Object> values)
    {

        log.info("SQL扩展之前:{}", sql);
        Object[] args = QueryContext.getArgs();
        // 1. 处理"% ? % "问题, 对应的正则 "[_\\s*%]+\\?[_\\s*%]+"
        List<String> ssms = percent(sql, values, args);

        // 2. 防SQL注入
        antiSQLInject(sql);

        if (!ssms.isEmpty())
        {
            this.sql = RegExUtils.replaceAll(sql, RegexCache.SMILE_PATT, StrConst.QUE);
        }
        else
        {
            this.sql = sql;
        }

        this.sql = RegExUtils.replaceAll(this.sql, RegexCache.SP1_REG_PATT, StrConst.QUE);
        this.values = values;
    }

    private void antiSQLInject(String sql)
    {
        List<String> ins = TypeUtil.matches(sql, RegexCache.SMILE_BIG_PATT);
        for (String in : ins)
        {
            if (PreventSQLInjection.isInjectStr(in) && TypeUtil.matches(in, RegexCache.SMILE_PATT).isEmpty())
            {
                String tip = in.replace("`-", StringUtils.EMPTY).replace("-`", StringUtils.EMPTY) + "中包含有危险关键字,正在尝试SQL注入";
                log.error(tip);
                throw new RepositoryException(tip);
            }
        }
    }

    private List<String> percent(String sql, List<Object> values, Object[] args)
    {
        List<String> ssms = TypeUtil.matches(sql, RegexCache.SMILE_PATT);
        for (String ssm : ssms)
        {
            int end = sql.indexOf(ssm);
            // 统计 sql中0-end范围中问号出现的次数
            int count = StringUtils.countMatches(sql.substring(0, end), '?');
            String numStr = TypeUtil.matches(ssm, RegexCache.SEARCH_NUM_PATT).get(0);
            ssm = RegExUtils.replaceAll(ssm, RegexCache.SP1_REG_PATT, StrConst.QUE); // 这部很重要,不然"?"后面的数字也会融入模板里
            int index = Integer.parseInt(numStr) - 1;

            Pattern p1 = RegexCache.getPattern("`-");
            Pattern p2 = RegexCache.getPattern("-`");
            Pattern p3 = RegexCache.getPattern("\\?");

            //values[i] 表示SQL从左至右第(i+1)次出现的?号所对应的实参值
            String val = RegExUtils.replaceFirst(ssm,p1,StringUtils.EMPTY);
            val = RegExUtils.replaceFirst(val,p2,StringUtils.EMPTY);
            val = RegExUtils.replaceFirst(val,p3,args[index] != null ? args[index].toString() : StringUtils.EMPTY);

            values.set(count, val);
            Object obj = values.get(count);
            if (obj != null && obj.getClass() == String.class && RegexCache.PERCENT_PATT.matcher(obj.toString()).matches())
            {
                throw new RepositoryException("这个SQL实参值禁止都是%组成");
            }
        }
        return ssms;
    }
}
