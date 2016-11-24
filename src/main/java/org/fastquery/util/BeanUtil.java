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

import org.apache.log4j.Logger;
import org.fastquery.core.Id;
import org.fastquery.core.Placeholder;
import org.fastquery.core.RepositoryException;

import com.alibaba.fastjson.JSONArray;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public final class BeanUtil {
	
	private static final Logger LOG = Logger.getLogger(BeanUtil.class);

	private BeanUtil() {
	}

	/**
	 * 将1个bean 转换成 insert sql语句, 注意: 主键值为null,将不参与运算.
	 * 
	 * @param beans
	 * @return
	 */
	public static String toInsertSQL(Object bean,boolean dbNamePrefix) {

		int idOfSet = -1; // 用于记录主键名应该在sql中的什么位置
		
		Class<?> clazz = bean.getClass();
		String tableName = clazz.getSimpleName();

		StringBuilder sqlsb = new StringBuilder("insert into ");
		if(dbNamePrefix) {
		  sqlsb.append("`${dbpre}`.");
		}
		sqlsb.append("`");
		sqlsb.append(tableName);
		sqlsb.append("`");
		sqlsb.append("(");
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			if(field.getType().isArray() || !TypeUtil.isWarrp(field.getType())){
				continue;
			}
			// 如果是主键,记录一下应该插入的位置
			if(field.getAnnotation(Id.class) !=null ) {
				idOfSet = sqlsb.length();	
			} else {
				sqlsb.append("`");
				sqlsb.append(field.getName());
				sqlsb.append("`");
			}
			sqlsb.append(',');
		}
		sqlsb.deleteCharAt(sqlsb.length() - 1);
		sqlsb.append(") values");
		try {
				sqlsb.append('(');
				for (Field field : fields) {
					if(field.getType().isArray() || !TypeUtil.isWarrp(field.getType())){
						continue;
					}
					Object val = new PropertyDescriptor(field.getName(), clazz).getReadMethod().invoke(bean);
					if(field.getAnnotation(Id.class)!=null) {
						if (val != null) {
							sqlsb.insert(idOfSet, new StringBuilder().append("`").append(field.getName()).append("`"));
							sqlsb.append("'" + val + "',");
						} else {
							if(sqlsb.charAt(idOfSet) == ',') {
								sqlsb.deleteCharAt(idOfSet);	
							}
						}
					} else {
						if (val != null) {
							sqlsb.append("'" + val + "',");
						} else {
							sqlsb.append(val);
							sqlsb.append(',');
						}
					}
				}
				sqlsb.deleteCharAt(sqlsb.length() - 1);
				sqlsb.append("),");
		} catch (Exception e) {
			throw new RepositoryException(e);
		}
		sqlsb.deleteCharAt(sqlsb.length() - 1);
		return sqlsb.toString();
	}
	
	public static String toInsertSQL(String dbName, Object bean) {
		return toInsertSQL(bean, true).replace("${dbpre}", dbName);
	}
	
	/**
	 * 将多个bean 转换成 insert sql语句, 注意:多个bean,其类型必须相等
	 * 
	 * @param beans
	 * @return
	 */
	/*
	public static String toInsertSQL(Object... beans) {

		Class<?> clazz = beans[0].getClass();
		String tableName = clazz.getSimpleName();

		StringBuilder sqlsb = new StringBuilder("insert into ");
		sqlsb.append(tableName);
		sqlsb.append("(");
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			if(field.getType().isArray() || !TypeUtil.isWarrp(field.getType())){
				continue;
			}
			sqlsb.append(field.getName());
			sqlsb.append(',');
		}
		sqlsb.deleteCharAt(sqlsb.length() - 1);
		sqlsb.append(") values");
		try {
			for (Object bean : beans) {
				sqlsb.append('(');
				for (Field field : fields) {
					if(field.getType().isArray() || !TypeUtil.isWarrp(field.getType())){
						continue;
					}
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
			if(field.getType().isArray() || !TypeUtil.isWarrp(field.getType())){
				continue;
			}
			sqlsb.append(field.getName());
			sqlsb.append(',');
		}
		sqlsb.deleteCharAt(sqlsb.length() - 1);
		sqlsb.append(") values");
		try {
			for (Object bean : beans) {
				sqlsb.append('(');
				for (Field field : fields) {
					if(field.getType().isArray() || !TypeUtil.isWarrp(field.getType())){
						continue;
					}
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
	*/
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
			if(field.getType().isArray() || !TypeUtil.isWarrp(field.getType())){
				continue;
			}
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
			if(field.getType().isArray() || !TypeUtil.isWarrp(field.getType())){
				continue;
			}
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
		int len = sb.length();
		try {
			Field[] fields = cls.getDeclaredFields();
			for (Field field : fields) {
				if(field.getType().isArray() || !TypeUtil.isWarrp(field.getType())){
					continue;
				}
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
			if(sb.length()==len) {
				LOG.warn("传递的实体,没有什么可以修改," + bean);
				return null;
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
	
	/**
	 * [0]: 更新语句 [1]:参数值集合类型:List<Object> 
	 * @param bean
	 * @param dbName
	 * @return
	 */
	public static Object[] toUpdateSQL(Object bean,String dbName,String where) {
		List<String> wps = TypeUtil.matches(where.replace(",", " ,"), Placeholder.SL_REG);
		Object[] updateinfo = new Object[2];
		List<Object> args = new ArrayList<>();
		Class<?> cls = bean.getClass();
		// 表名称
		String tableName = cls.getSimpleName();
		if(dbName != null) {
			tableName = new StringBuilder().append('`').append(dbName).append("`.`").append(tableName).append('`').toString();
		} else {
			tableName = '`' + tableName + '`';
		}
		
		// update UserInfo set name=?,age=? where id=?4
		StringBuilder sb = new StringBuilder("update ");
		sb.append(tableName);
		sb.append(" set");
		int len = sb.length();
		try {
			Field[] fields = cls.getDeclaredFields();
			for (Field field : fields) {
				if(field.getType().isArray() || !TypeUtil.isWarrp(field.getType())){
					continue;
				}
				Object val = new PropertyDescriptor(field.getName(), cls).getReadMethod().invoke(bean);
				if(val!=null && !wps.contains(":"+field.getName())) {
					args.add(val);
					sb.append(" `");
					sb.append(field.getName());
					sb.append('`');
					sb.append("=?,");
					
				}
			}
			if(sb.length()==len) {
				LOG.warn("传递的实体,没有什么可修改," + bean);
				return null;
			}
			// where的后面部分 和 追加sql参数
			String whef = where.replace(",", " ,").replaceAll(Placeholder.SL_REG, "?");
			for (String wp : wps) {
				Object val = new PropertyDescriptor(wp.replace(":", ""), cls).getReadMethod().invoke(bean);
				if(val==null) {
					throw new RepositoryException("条件的值不能为null");
				}
				args.add(val);
			}
			// // where的后面部分 和 追加sql参数 End
			
			// 去掉sb最后的一个字符
			sb.deleteCharAt(sb.length() - 1);
			sb.append(" where ");
			sb.append(whef);
			updateinfo[0] = sb.toString();
			updateinfo[1] = args;
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
		if(cls.isArray() || !TypeUtil.isWarrp(cls) || obj instanceof Iterable){
			String strs = JSONArray.toJSONString(obj);
			return strs.substring(1, strs.length()-1);
		}
		return obj;
	}
	
	/**
	 * 创建一个bean实例,成员变量的值全部都为null <br>
	 * 注意:这个bean的成员变量必须都是包装类型
	 * @param beanClass
	 * @return
	 */
	public static <S>  S newBeanVarNull(Class<S> beanClass) {
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
