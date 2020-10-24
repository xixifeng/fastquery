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

package org.fastquery.analysis;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;

import org.fastquery.util.FastQueryJSONObject;

/**
 * 要来校验,当前拦截到的方法,是否需要*.queries.xml文件
 *
 * @author xixifeng (fastquery@126.com)
 */
class QueriesFileFilter implements MethodFilter
{

    @Override
    public void doFilter(Method method)
    {

        String suffix = ".queries.xml";

        String className = method.getDeclaringClass().getName();
        String fcd = System.getProperty("fastquery.config.dir");

        if (!exits(className, fcd, suffix))
        {
            StringBuilder sb = getErrorMsg(suffix, className, fcd);
            this.abortWith(method, sb.toString());
        }

    }


    private StringBuilder getErrorMsg(String suffix, String className, String fcd)
    {
        StringBuilder sb = new StringBuilder("这个方法标识了注解@QueryByNamed,而没有找到文件:");
        sb.append(className);
        sb.append(suffix);
        sb.append("该文件可以放入claspath环境目录下");

        if (fcd != null && !"".equals(fcd))
        {
            sb.append(",据发现 \"fastquery.config.dir\" 指定了目录 ");
            sb.append(fcd);
            sb.append(" 因此,也可以放入到这里面来");
        }
        return sb;
    }

    private boolean exits(String className, String fcd, String suffix)
    {

        List<String> pers = FastQueryJSONObject.getQueries();
        pers.add("");

        for (String per : pers)
        {
            String perxml = new StringBuilder().append(per).append(className).append(suffix).toString();
            URL url = QueriesFileFilter.class.getClassLoader().getResource(perxml);
            boolean urlExits = url != null;
            if (urlExits || (fcd != null && !"".equals(fcd) && new File(fcd, per + className + ".queries.xml").exists()))
            {
                return true;
            }
        }

        return false;

    }

}
