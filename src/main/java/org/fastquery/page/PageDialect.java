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

package org.fastquery.page;


/**
 * 分页方言,抽象出不同数据库SQL分页的共性,差异性各自去实现.
 *
 * @author mei.sir@aliyun.cn
 */
public interface PageDialect
{

    /**
     * 获取分页语句
     *
     * @param querySQL 查询语句
     * @param offset   数据的偏移量
     * @param pageSize 从offset位置(从0开始计数)向后取pageSize条记录
     * @return 分页SQL语句
     */
    default String getCurrentPageSQL(String querySQL, int offset, int pageSize)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("WITH query AS (SELECT inner_query.*, ROW_NUMBER() OVER (ORDER BY CURRENT_TIMESTAMP) as __row_index___ FROM ( ");
        sb.append(querySQL);
        sb.append(" ) inner_query ) SELECT * FROM query WHERE __row_index___ > ");
        sb.append(offset);
        sb.append(" AND __row_index___ <= ");
        sb.append(offset + pageSize);

        return sb.toString();
    }

    /**
     * 用户若没有指定count语句,那么需要推导它
     *
     * @param querySQL   查询语句
     * @param countField count字段
     * @return count语句
     */
    default String countSQLInference(String querySQL, String countField)
    {

        String tmp = querySQL.toLowerCase();
        // 计算求和语句
        // 把select 与 from 之间的 内容变为 count(countField)
        int fromIndex = tmp.lastIndexOf("from") - 1;
        StringBuilder sb = new StringBuilder();
        sb.append("select count(");
        sb.append(countField);
        sb.append(')');
        sb.append(querySQL.substring(fromIndex));

        // 求和语句不需要order by(排序)
        // (?i) : 表示不区分大小写
        // 过滤order by 后面的字符串(包含本身)
        //countSQL = countSQL.replaceFirst("(?i)(order by )(.|\n)+", "")

        return sb.toString();
    }

}
