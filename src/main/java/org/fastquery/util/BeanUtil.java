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

package org.fastquery.util;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.fastquery.core.Id;
import org.fastquery.core.Placeholder;
import org.fastquery.core.RepositoryException;
import org.fastquery.core.Table;
import org.fastquery.core.Transient;

import com.alibaba.fastjson.JSONArray;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public final class BeanUtil {

	private static final Logger LOG = LoggerFactory.getLogger(BeanUtil.class);

	private BeanUtil() {
	}

	private static String escapeSql(String str) {
		String s = StringUtils.replace(str, "'", "''");
		return StringUtils.replace(s, "\\", "\\\\");
	}

	/**
	 * 将1个bean 转换成 insert sql语句, 注意: 主键值为null,将不参与运算.
	 * 
	 * @param bean 实体
	 * @return insert 语句
	 */
	public static String toInsertSQL(Object bean) {
		String values = toValue(bean.getClass().getDeclaredFields(), bean,true);
		return bean2InsertSQL(bean, null, values, false);
	}

	public static String toInsertSQL(String dbName, Object bean) {
		String values = toValue(bean.getClass().getDeclaredFields(), bean,true);
		return bean2InsertSQL(bean, dbName, values, false);
	}

	static <B> String toFields(Field[] fields, B bean) {
		StringBuilder sb = new StringBuilder();
		sb.append('(');
		for (Field field : fields) {
			if (field.getType().isArray() || !TypeUtil.isWarrp(field.getType()) || field.getDeclaredAnnotation(Transient.class) != null) {
				continue;
			}
			try {
				field.setAccessible(true);
				if (field.getAnnotation(Id.class) == null || field.get(bean) != null) {
					// 接纳的值:
					// 1. 不是主键的字段 
					// 或
					// 2. 不为null的字段
					sb.append('`');
					sb.append(field.getName());
					sb.append("`,");
				}
			} catch (IllegalAccessException | IllegalArgumentException e) {
				throw new RepositoryException(e);
			}
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append(')');
		return sb.toString();
	}

	/**
	 * 将bean 转换成这样的格式: ('12','sunny','20')
	 * 
	 * @param <B> 实体
	 * @param fields 实体字段集
	 * @param bean 实体
	 * @return sql value部分
	 */
	static <B> String toValue(Field[] fields, B bean,boolean containMark) {
		StringBuilder sb = new StringBuilder();
		if(containMark) {
			sb.append("values");
		}
		sb.append('(');
		for (Field field : fields) {
			if (field.getType().isArray() || !TypeUtil.isWarrp(field.getType()) || field.getDeclaredAnnotation(Transient.class) != null) {
				continue;
			}
			Object val = null;
			try {
				field.setAccessible(true);
				val = field.get(bean);
			} catch (IllegalAccessException | IllegalArgumentException e) {
				throw new RepositoryException(e);
			}
			if (val != null) {
				sb.append('\'');
				sb.append(escapeSql(val.toString()));
				sb.append("',");
			} else if (field.getAnnotation(Id.class) == null) {
				sb.append("null,");
			}
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append(')');
		return sb.toString();
	}

	/**
	 * 把实体集合转换成sql中的values部分
	 * 
	 * @param clazz 实体class
	 * @param fields 实体的字段
	 * @param beans 实体集
	 * @return sql 中的 values 部分
	 */
	private static <B> String toValues(Field[] fields, Iterable<B> beans) {
		StringBuilder sbValues = new StringBuilder();
		sbValues.append("values");
		for (B b : beans) {
			sbValues.append(toValue(fields, b,false));
			sbValues.append(',');
		}
		sbValues.deleteCharAt(sbValues.length() - 1);
		return sbValues.toString();
	}

	/**
	 * 转换insert 语句
	 * 
	 * @param <B> 实体
	 * @param beans 如果结合为空,则返回null
	 * @param dbName 如果为null,表名称之前不会有前缀
	 * @param ignoreRepeat 忽略重复
	 * @return 插入语句
	 */
	public static <B> String toInsertSQL(Iterable<B> beans, String dbName, boolean ignoreRepeat) {
		if (beans == null)
			return null;

		Iterator<B> iterator = beans.iterator();
		if (!iterator.hasNext()) {
			return null;
		}
		// 集合中的第一个bean
		B bean = iterator.next();
		@SuppressWarnings("unchecked")
		Class<B> clazz = (Class<B>) bean.getClass();
		
		// values 部分
		String values = toValues(clazz.getDeclaredFields(), beans);
		
		return bean2InsertSQL(bean, dbName, values, ignoreRepeat);

	}
	
	private static String bean2InsertSQL(Object bean, String dbName, String values,boolean ignoreRepeat) {
		// 集合中的第一个bean
		Class<?> clazz = bean.getClass();
		// 确立表名称
		StringBuilder sb = new StringBuilder();
		if (dbName != null) {
			sb.append('`');
			sb.append(dbName);
			sb.append("`.`");
			sb.append(getEntitySimpleName(clazz));
			sb.append('`');
		} else {
			sb.append('`');
			sb.append(getEntitySimpleName(clazz));
			sb.append('`');
		}
		Field[] fields = clazz.getDeclaredFields();

		// 表名称
		String tableName = sb.toString();

		// 表字段
		String fs = toFields(fields, bean);

		// insert into 语句
		String insertStr;
		if (ignoreRepeat) {
			insertStr = "insert ignore into";
		} else {
			insertStr = "insert into";
		}
		StringBuilder insertsql = new StringBuilder();
		insertsql.append(insertStr);
		insertsql.append(' ');
		insertsql.append(tableName);
		insertsql.append(fs);
		insertsql.append(' ');
		insertsql.append(values);
		return insertsql.toString();
	}

	public static String arr2InsertSQL(Object[] beans, String dbName, boolean ignoreRepeat) {
		Iterable<Object> list = Arrays.asList(beans);
		return toInsertSQL(list, dbName, ignoreRepeat);
	}

	// 或取主键的名称 和 主键的值
	private static Object[] getKeyAndVal(Object bean,Field[] files,Object val) {
		
		Object[] objs = new Object[2];
		objs[1] = val;
		
		// 获取主键的名称
		for (Field field : files) {
			if (field.getAnnotation(Id.class) != null) {
				objs[0] = field.getName();
				if (val == null) {
					// 顺便取主键的值
					try {
						field.setAccessible(true);
						objs[1] = field.get(bean);
					} catch (Exception e) {
						throw new RepositoryException(e);
					}
				}
				break;
			}
		}

		if (objs[0] == null) {
			throw new RepositoryException(bean + " 需要用@Id在实体上标识主键");
		}
		
		return objs;
	}
	
	/**
	 * 转换查询语句, 实体上必须包含主键字段 <br>
	 * 
	 * 注意: 如果主键值为null,则返回null,没有主键值,那么根据什么来查询呢?
	 * 
	 * @param bean 实体 或 class
	 * @param key 主键值, 如果传递null,那么自动获取,获取到的为null那么报错. 指定的值优先
	 * @param dbName 数据库名称
	 * @param selectEntity true 查实体, 反之,查主键值
	 * @return sql语句
	 */
	public static String toSelectSQL(Object bean, Object key, String dbName,boolean selectEntity) {
		Class<?> cls = (bean instanceof Class) ? (Class<?>) bean : bean.getClass();
		Object[] objs = getKeyAndVal(bean, cls.getDeclaredFields(), key);
		key = objs[1];
		if(key==null) {
			return null;
		} else {
			String keyFeild = objs[0].toString();
			// 表名称
			String tableName = getTableName(dbName, cls);		
			if(selectEntity) {
				return String.format("select %s from %s where `%s` = %s",selectFields(cls),tableName, keyFeild, key.toString());
			} else {
				return String.format("select `%s` from %s where `%s` = %s",keyFeild,tableName, keyFeild, key.toString());
			}	
		}
	}

	private static String getTableName(String dbName, Class<?> cls) {
		String tableName = cls.getSimpleName();
		if (dbName != null) {
			tableName = new StringBuilder().append('`').append(dbName).append("`.`").append(tableName).append('`').toString();
		} else {
			tableName = '`' + tableName + '`';
		}
		return tableName;
	}

	/**
	 * 如果这个bean已经包含主键的值,就已bean的主键值为准 <br>
	 * [0]: 更新语句, [1]:参数值集合类型:List&lt;Object&gt; [2](是否存在第3个值取决于toSQL是否设置true): 根据主键查的sql语句
	 * 
	 * @param bean 待更新的实体
	 * @param dbName 数据库名称,可以为null
	 * @param toSQL 是否返回根据主键查的sql语句
	 * @return 更新语句信息
	 */
	public static Object[] toUpdateSQL(Object bean, String dbName, boolean toSQL) {
		Object[] updateinfo = new Object[3];
		List<Object> args = new ArrayList<>();
		Class<?> cls = bean.getClass();
		// 表名称
		String tableName = getTableName(dbName, cls);

		Object key = null;
		String keyFeild = null;
		Object[] objs = getKeyAndVal(bean, cls.getDeclaredFields(), null);
		keyFeild = (String) objs[0];
		key = objs[1];

		if (keyFeild == null || key == null) {
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
				if (field.getType().isArray() || !TypeUtil.isWarrp(field.getType()) || field.getDeclaredAnnotation(Transient.class) != null) {
					continue;
				}
				field.setAccessible(true);
				Object val = field.get(bean);
				Id id = field.getAnnotation(Id.class);
				if (val != null && id == null) {
					args.add(val);

					sb.append(" `");
					sb.append(field.getName());
					sb.append('`');
					sb.append("=?,");

				}
			}
			if (sb.length() == len) {
				LOG.warn("传递的实体,没有什么可以修改,{}", bean);
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
			if (toSQL) {
				updateinfo[2] = String.format("select * from %s where `%s` = %s", tableName, keyFeild, key.toString());
			}
		} catch (Exception e) {
			throw new RepositoryException(e);
		}

		return updateinfo;

	}

	/**
	 * [0]: 更新语句 [1]:参数值集合类型:List&lt;Object&gt;
	 * 
	 * @param bean 实体
	 * @param dbName 数据库名称
	 * @param where 条件
	 * @return 更新语句信息
	 */
	public static Object[] toUpdateSQL(Object bean, String dbName, String where) {
		List<String> wps = TypeUtil.matches(where, Placeholder.COLON_REG);
		Object[] updateinfo = new Object[2];
		List<Object> args = new ArrayList<>();
		Class<?> cls = bean.getClass();
		// 表名称
		String tableName = cls.getSimpleName();
		if (dbName != null) {
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
				if (field.getType().isArray() || !TypeUtil.isWarrp(field.getType()) || field.getDeclaredAnnotation(Transient.class) != null) {
					continue;
				}
				field.setAccessible(true);
				Object val = field.get(bean);
				if (val != null && !wps.contains(":" + field.getName())) {
					args.add(val);
					sb.append(" `");
					sb.append(field.getName());
					sb.append('`');
					sb.append("=?,");

				}
			}
			if (sb.length() == len) {
				LOG.warn("传递的实体,没有什么可修改,{}", bean);
				return null;
			}
			// where的后面部分 和 追加sql参数
			String whef = where.replaceAll(Placeholder.COLON_REG, "?");
			for (String wp : wps) {
				Object val = new PropertyDescriptor(wp.replace(":", ""), cls).getReadMethod().invoke(bean);
				if (val == null) {
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

	/**
	 * 将一个集合转换称批量update语句
	 * 
	 * @param <B> 实体
	 * @param beans 实体集合
	 * @param dbName 数据库名称
	 * @return update SQL
	 */
	public static <B> String toUpdateSQL(Iterable<B> beans, String dbName) {
		if (beans == null)
			return null;
		Iterator<B> iterator = beans.iterator();
		if (!iterator.hasNext()) {
			return null;
		}

		// 集合中的第一个bean
		B bean = iterator.next();
		Class<?> clazz = bean.getClass();

		// 表名称
		// 确立表名称
		StringBuilder sb = new StringBuilder();
		if (dbName != null) {
			sb.append('`');
			sb.append(dbName);
			sb.append("`.`");
			sb.append(getEntitySimpleName(clazz));
			sb.append('`');
		} else {
			sb.append('`');
			sb.append(getEntitySimpleName(clazz));
			sb.append('`');
		}

		// 1. 表名称
		String tableName = sb.toString();

		// 2. 找出主键的名称
		String primaryKey = null;
		Field key = null;
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			if (field.getAnnotation(Id.class) != null) {
				primaryKey = field.getName();
				key = field;
				key.setAccessible(true);
				break;
			}
		}
		if (primaryKey == null) {
			throw new RepositoryException(clazz + " 必须有@Id标识,并且主键不能为null");
		}

		// 3.
		boolean addIds = true;
		StringBuilder ids = new StringBuilder();
		StringBuilder sets = new StringBuilder();
		for (Field field : fields) {
			if (field == key || field.getType().isArray() || !TypeUtil.isWarrp(field.getType())
					|| field.getDeclaredAnnotation(Transient.class) != null) {
				continue;
			}

			field.setAccessible(true);

			String fieldName = field.getName();
			sets.append('`');
			sets.append(fieldName);
			sets.append("` = case ");
			sets.append('`');
			sets.append(primaryKey);
			sets.append("` ");
			for (B b : beans) {
				Object keyVal;
				try {
					keyVal = key.get(b);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new RepositoryException("无法获取主键的值", e);
				}
				if (keyVal == null) {
					throw new RepositoryException("主键的值不能为null");
				}
				if (addIds) {
					ids.append(keyVal);
					ids.append(',');
				}
				Object fieldVal;
				try {
					fieldVal = field.get(b);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new RepositoryException("无法获取字段的值", e);
				}
				sets.append("when ");
				sets.append(keyVal);
				sets.append(" then ");
				if (fieldVal != null) {
					sets.append('\'');
					sets.append(fieldVal);
					sets.append("\' ");
				} else {
					sets.append('`');
					sets.append(fieldName);
					sets.append("` ");
				}
			}
			addIds = false;
			sets.append("else `");
			sets.append(fieldName);
			sets.append("` end,");
		}

		sets.deleteCharAt(sets.length() - 1);
		ids.deleteCharAt(ids.length() - 1);

		StringBuilder sql = new StringBuilder();
		sql.append("update ");
		sql.append(tableName);
		sql.append(" set ");
		sql.append(sets);
		sql.append(" where ");
		sql.append('`');
		sql.append(primaryKey);
		sql.append("` in(");
		sql.append(ids);
		sql.append(')');
		return sql.toString();
	}

	public static String toDelete(String tableName, String keyName, long keyVal, String dbName) {
		StringBuilder sq = new StringBuilder();
		sq.append("delete from `");
		if (dbName != null) {
			sq.append(dbName).append('`').append('.').append('`').append(tableName).append('`').toString();
		} else {
			sq.append(tableName).append('`').toString();
		}
		sq.append(" where ");
		sq.append('`');
		sq.append(keyName);
		sq.append('`');
		sq.append('=');
		sq.append(keyVal);

		return sq.toString();
	}

	// 返回sql中in查询需要的值
	static Object parseList(Object obj) {
		if (obj == null) {
			return null;
		} else {
			Class<?> cls = obj.getClass();
			if (cls.isArray() || !TypeUtil.isWarrp(cls) || obj instanceof Iterable) {
				String strs = JSONArray.toJSONString(obj);
				return strs.substring(1, strs.length() - 1);
			}	
		}
		return obj;
	}

	@SuppressWarnings("unchecked")
	private static <S> S newBeanVarNull(Class<S> clazz,Object bean) {
		Field[] fields = clazz.getDeclaredFields();
		try {
			if(bean==null) {
				bean = clazz.newInstance();
			}
			for (Field field : fields) {
				field.setAccessible(true);
				field.set(bean, null);
			}
		} catch (Exception e) {
			throw new RepositoryException(e);
		}
		return (S) bean;
	}
	
	/**
	 * 创建一个bean实例,成员变量的值全部都为null <br>
	 * 注意:这个bean的成员变量必须都是包装类型
	 * 
	 * @param <S> 实体
	 * @param beanClass 实体class
	 * @return 实体
	 */
	public static <S> S newBeanVarNull(Class<S> beanClass) {
		return newBeanVarNull(beanClass, null);
	}
	
	public static void newBeanVarNull(Object bean) {
		newBeanVarNull(bean.getClass(), bean);
	}

	public static long toId(Object entity) {
		Class<?> cls = entity.getClass();
		Object key = null;
		Field[] files = cls.getDeclaredFields();

		for (Field field : files) {
			if (field.getAnnotation(Id.class) != null) {
				try {
					field.setAccessible(true);
					key = field.get(entity);
				} catch (Exception e) {
					throw new RepositoryException(e);
				}
				break;
			}
		}

		if (key == null) {
			throw new RepositoryException(cls + " 必须有@Id标识,并且主键不能为null");
		}

		return Long.valueOf(key.toString());
	}

	private static String getEntitySimpleName(Class<?> clazz) {
		Table t = clazz.getAnnotation(Table.class);
		if(t != null) {
			return t.value();
		} else {
			return clazz.getSimpleName();
		}
	}
	
	private static List<Field> mapFields(Object bean) {
		List<Field> list = new ArrayList<>();
		Class<?> cls = (bean instanceof Class) ? (Class<?>) bean : bean.getClass();
		Field[] fields = cls.getDeclaredFields();
		for (Field field : fields) {
			// 映射的字段必须满足: a: 不是数组 b:必须是包装类型 c: 没有标识@Transient
			if(!field.getType().isArray() && TypeUtil.isWarrp(field.getType()) && field.getDeclaredAnnotation(Transient.class) == null) {
				list.add(field);
			}
		}
		return list;
	}
	
	public static String selectFields(Object bean) {
		Objects.requireNonNull(bean);
		List<Field> fields = mapFields(bean);
		StringBuilder sb = new StringBuilder(6*fields.size());
		fields.forEach( f -> {
			sb.append(',');
			sb.append('`');
			sb.append(f.getName());
			sb.append('`');
		});
		int len = sb.length();
		if(len > 0) {
			sb.deleteCharAt(0);
		} 
		
		return sb.toString();
	}
	
}
