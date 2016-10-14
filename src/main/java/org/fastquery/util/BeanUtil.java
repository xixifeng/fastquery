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

package org.fastquery.util;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import org.fastquery.core.RepositoryException;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public final class BeanUtil {
	
	private BeanUtil(){}
	
	/**
	 * 将多个bean 转换成 insert sql语句, 注意:多个bean,其类型必须相等
	 * @param beans
	 * @return
	 */
	public static String toInsertSQL(Object...beans) {
		
		Class<?> clazz = beans[0].getClass();
		String tableName = clazz.getSimpleName();
		
		StringBuilder sqlsb = new StringBuilder("insert into "); 
		sqlsb.append(tableName);
		sqlsb.append("(");
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			sqlsb.append(field.getName());
			sqlsb.append(',');
		}
		sqlsb.deleteCharAt(sqlsb.length()-1);
		sqlsb.append(") values");
		try {
			for (Object bean : beans) {
				sqlsb.append('(');
				for (Field field : fields) {
					Object val = new PropertyDescriptor(field.getName(),clazz).getReadMethod().invoke(bean);
					if(val != null){
						sqlsb.append("'"+val+"',");
					} else {
						sqlsb.append(val);
						sqlsb.append(',');
					}
				}
				sqlsb.deleteCharAt(sqlsb.length()-1);
				sqlsb.append("),");
			}	
		} catch (Exception e) {
			throw new RepositoryException(e);
		}
		sqlsb.deleteCharAt(sqlsb.length()-1);
		return sqlsb.toString();
	}
	
	
}
