/*
 * Copyright (c) 2016-2016, fastquery.org and/or its affiliates. All rights reserved.
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

package org.fastquery.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class QueryValidator {
   private QueryValidator(){}
   public static void check(){
	   List<String> queries = new ArrayList<>();
	   
	   Map<String, String> countQueryMap = QueryPool.getCountQueryMap();
	   countQueryMap.forEach((k,v) -> queries.add(v));
	   
	   Map<String, Set<QueryMapper>> mapQueryMapper = QueryPool.getMapQueryMapper();
	   Set<Entry<String, Set<QueryMapper>>> queryMappers = mapQueryMapper.entrySet();
	   for (Entry<String, Set<QueryMapper>> entry : queryMappers) {
		   Set<QueryMapper> qms = entry.getValue();
		   qms.forEach(q -> queries.add(q.getTemplate()));
	   }
	   
	   // 1). query中不能出现 ";"
	   for (String query : queries) {
		   if(query.indexOf(';') != -1) {
			   throw new ExceptionInInitializerError("禁止出现\";\"号, 错误位置>>>>>>>>>>>>>>: " + query);
		   }
	   }
	   
   }
	
}
