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
import java.util.ArrayList;
import java.util.List;

import org.fastquery.core.Id;
import org.fastquery.core.RepositoryException;

import com.alibaba.fastjson.JSONArray;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public final class BeanUtil {

	private BeanUtil() {
	}

	/**
	 * 将多个bean 转换成 insert sql语句, 注意:多个bean,其类型必须相等
	 * 
	 * @param beans
	 * @return
	 */
	public static String toInsertSQL(Object... beans) {

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
		sqlsb.deleteCharAt(sqlsb.length() - 1);
		sqlsb.append(") values");
		try {
			for (Object bean : beans) {
				sqlsb.append('(');
				for (Field field : fields) {
					Object val = new PropertyDescriptor(field.getName(), clazz).getReadMethod().invoke(bean);
					if (val != null) {
						sqlsb.append("'" + val + "',");
					} else {
						sqlsb.append(val);
						sqlsb.append(',');
					}
				}
				sqlsb.deleteCharAt(sqlsb.length() - 1);
				sqlsb.append("),");
			}
		} catch (Exception e) {
			throw new RepositoryException(e);
		}
		sqlsb.deleteCharAt(sqlsb.length() - 1);
		return sqlsb.toString();
	}

	// 该方法有待重构
	public static String toInsertSQL(String dbName, Object... beans) {

		Class<?> clazz = beans[0].getClass();
		String tableName = clazz.getSimpleName();

		StringBuilder sqlsb = new StringBuilder("insert into ");
		sqlsb.append(dbName + "." + tableName);
		sqlsb.append("(");
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			sqlsb.append(field.getName());
			sqlsb.append(',');
		}
		sqlsb.deleteCharAt(sqlsb.length() - 1);
		sqlsb.append(") values");
		try {
			for (Object bean : beans) {
				sqlsb.append('(');
				for (Field field : fields) {
					Object val = new PropertyDescriptor(field.getName(), clazz).getReadMethod().invoke(bean);
					if (val != null) {
						sqlsb.append("'" + val + "',");
					} else {
						sqlsb.append(val);
						sqlsb.append(',');
					}
				}
				sqlsb.deleteCharAt(sqlsb.length() - 1);
				sqlsb.append("),");
			}
		} catch (Exception e) {
			throw new RepositoryException(e);
		}
		sqlsb.deleteCharAt(sqlsb.length() - 1);
		return sqlsb.toString();
	}

	/**
	 * 
	 * @param bean
	 * @param key 主键值, 如果传递null,那么自动获取,获取到的为null那么报错. 指定的值优先
	 * @param dbName
	 * @return
	 */
	public static String toSelectSQL(Object bean,Object key,String dbName) {
		Class<?> cls = bean.getClass();
		// 表名称
		String tableName = cls.getSimpleName();
		if(dbName != null) {
			tableName = new StringBuilder().append('`').append(dbName).append("`.`").append(tableName).append('`').toString();
		}else {
			tableName = '`' + tableName + '`';
		}
		String keyFeild = null;
		
		// 获取主键的名称
		Field[] files = cls.getDeclaredFields();
		for (Field field : files) {
			if(field.getAnnotation(Id.class)!=null){
				keyFeild = field.getName();
				if(key==null) {
					// 顺便取主键的值
					try {
						key = new PropertyDescriptor(field.getName(), cls).getReadMethod().invoke(bean);	
					} catch (Exception e) {
						throw new RepositoryException(e);
					}	
				}
				break;
			}
		}
		
		if(keyFeild==null || key == null){
			throw new RepositoryException(cls + " 需要主键标识并且主键的值不能为null");
		}
		
		return String.format("select * from %s where `%s` = %s",tableName,keyFeild,key.toString());
		
	}
	
	/**
	 * 如果这个bean已经包含主键的值,就已bean的主键值为准
	 * [0]: 更新语句, [1]:参数值集合类型:List<Object> [2]: 根据主键查的sql语句
	 * @param bean 待更新的实体
	 * @param key 主键的值
	 * @param dbName 数据库名称,可以为null
	 * @return
	 */
	public static Object[] toUpdateSQL(Object bean,String dbName) {
		Object key = null;
		Object[] updateinfo = new Object[3];
		List<Object> args = new ArrayList<>();
		Class<?> cls = bean.getClass();
		// 表名称
		String tableName = cls.getSimpleName();
		if(dbName != null) {
			tableName = new StringBuilder().append('`').append(dbName).append("`.`").append(tableName).append('`').toString();
		} else {
			tableName = '`' + tableName + '`';
		}
		
		String keyFeild = null;
		// 获取主键的名称
		Field[] files = cls.getDeclaredFields();
		
		for (Field field : files) {
			if(field.getAnnotation(Id.class)!=null){
				keyFeild = field.getName();
				// 顺便取主键的值
				try {
					key = new PropertyDescriptor(field.getName(), cls).getReadMethod().invoke(bean);	
				} catch (Exception e) {
					throw new RepositoryException(e);
				}
				break;
			}
		}
		
		if(keyFeild==null || key == null){
			throw new RepositoryException(cls + " 必须有@Id标识,并且主键不能为null");
		}
		
		// update UserInfo set name=?,age=? where id=?4
		StringBuilder sb = new StringBuilder("update ");
		sb.append(tableName);
		sb.append(" set");
		try {
			Field[] fields = cls.getDeclaredFields();
			for (Field field : fields) {
				Object val = new PropertyDescriptor(field.getName(), cls).getReadMethod().invoke(bean);
				Id id = field.getAnnotation(Id.class);
				if(val!=null && id == null) {
					args.add(val);
					
					sb.append(" `");
					sb.append(field.getName());
					sb.append('`');
					sb.append("=?,");
					
				}
			}
			// 去掉sb最后的一个字符
			sb.deleteCharAt(sb.length() - 1);
			sb.append(" where ");
			sb.append('`');
			sb.append(keyFeild);
			sb.append("`=?");
			args.add(key);
			updateinfo[0] = sb.toString();
			updateinfo[1] = args;
			updateinfo[2] = String.format("select * from %s where `%s` = %s",tableName,keyFeild,key.toString());
			
		} catch (Exception e) {
			throw new RepositoryException(e);
		}

		return updateinfo;
		
	}

	// 返回sql中in查询需要的值
	public static Object parseList(Object obj){
		if(obj==null){
			return null;
		}
		Class<?> cls = obj.getClass();
		if(cls.isArray() || obj instanceof Iterable){
			String strs = JSONArray.toJSONString(obj);
			return strs.substring(1, strs.length()-1);
		}
		return obj;
	}
	
	/**
	 * 创建一个bean实例,实例的成员变量值全部重置为null
	 * 
	 * @param beanClass
	 * @return
	 */
	public static <S>  S newNullInstance(Class<S> beanClass) {
		// new 一个新bean
		S ns;
		Field[] fields = beanClass.getDeclaredFields();
		try {
			ns = (S) beanClass.newInstance();
			for (Field field : fields) {
				new PropertyDescriptor(field.getName(), beanClass).getWriteMethod().invoke(ns, new Object[]{null});
			}
		} catch (Exception e) {
			throw new RepositoryException(e);
		}
		return ns;
	}
	
}
