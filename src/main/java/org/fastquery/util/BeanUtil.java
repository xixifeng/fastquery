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
import java.util.*;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.fastquery.core.*;
import org.fastquery.struct.SQLValue;
import org.fastquery.core.SelectField;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import com.alibaba.fastjson.JSON;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public final class BeanUtil {

	private static final Logger LOG = LoggerFactory.getLogger(BeanUtil.class);
	private static final String UPDATE = "update ";
	private static final String NOT_ID = " 必须有@Id标识,并且主键不能为null";

	private static final String WHERE = " where ";

	private BeanUtil() {
	}

	private static String escapeSql(String str) {
		// 第一步将 "'" 替换成 "''"
		String s = StringUtils.replace(str, "'", "''");
		// 第二步将 "\\" 替换成 "\\\\"
		return StringUtils.replace(s, "\\", "\\\\");
	}

	/**
	 * 将1个bean 转换成 insert sql语句, 注意: 主键值为null,将不参与运算.
	 * 
	 * @param bean 实体
	 * @return insert 语句
	 */
	public static String toInsertSQL(Object bean) {
		String values = toValue(getFields(bean.getClass()), bean, true);		
		return bean2InsertSQL(bean, null, values, false);
	}

	public static String toInsertSQL(String dbName, Object bean) {
		String values = toValue(getFields(bean.getClass()), bean, true);
		return bean2InsertSQL(bean, dbName, values, false);
	}

	// 忽略条件: 如果不是包装类型或者字段上标识有Transient
	private static boolean allowField(Field field) {
		return TypeUtil.isWarrp(field.getType()) && field.getDeclaredAnnotation(Transient.class) == null;
	}

	private static <B> String toFields(Field[] fields, B bean) {
		StringBuilder sb = new StringBuilder();
		sb.append('(');
		for (Field field : fields) {
			if (allowField(field)) {
				try {
					field.setAccessible(true);
					if (field.getAnnotation(Id.class) == null || field.get(bean) != null) {
						// 接纳的值:
						// 1. 不是主键的字段
						// 或
						// 2. 不为null的字段
						sb.append(field.getName());
						sb.append(',');
					}
				} catch (IllegalAccessException | IllegalArgumentException e) {
					throw new RepositoryException(e);
				}
			}
		}
		sb.setCharAt(sb.length() - 1, ')');
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
	private static <B> String toValue(Field[] fields, B bean, boolean containMark) {
		StringBuilder sb = new StringBuilder();
		if (containMark) {
			sb.append("values");
		}
		sb.append('(');
		for (Field field : fields) {
			if (allowField(field)) {
				Object val;
				try {
					field.setAccessible(true);
					val = field.get(bean);
					if(val != null && field.getType() == EnumSet.class) {
						val = TypeUtil.enumSet2Val((EnumSet) val);
					}
				} catch (IllegalAccessException | IllegalArgumentException e) {
					throw new RepositoryException(e);
				}
				if (val != null) {
					if(val instanceof Number || val instanceof Boolean) {
						sb.append(val);
					} else {
						sb.append('\'');
						sb.append(escapeSql(val.toString()));
						sb.append('\'');
					}
					sb.append(',');
				} else if (field.getAnnotation(Id.class) == null) {
					sb.append("null,");
				}
			}
		}
		sb.setCharAt(sb.length() - 1, ')');
		return sb.toString();
	}

	// use select one
	// [0] where 部分， [1] sql语言中"?"对应的实参
	private static <B> Object[] toWhere(Field[] fields, B bean, boolean ignoreId) {
		StringBuilder sb = new StringBuilder();
		List<Object> values = new ArrayList<>();
		for (Field field : fields) {
			if (allowField(field)) {
				Object val;
				try {
					field.setAccessible(true);
					val = field.get(bean);
				} catch (IllegalAccessException | IllegalArgumentException e) {
					throw new RepositoryException(e);
				}
				if(ignoreId) {
					if (val != null && field.getAnnotation(Id.class) == null) {
						sb.append(" and ");
						sb.append(field.getName());
						sb.append(" = ?");
						values.add(val);
					}
				} else {
					if (val != null) {
						sb.append(" and ");
						sb.append(field.getName());
						sb.append(" = ?");
						values.add(val);
					}
				}
			}
		}
		Object[] objs = new Object[2];
		objs[0] = sb.toString();
		objs[1] = values;
		return objs;
	}
	public static SQLValue toSelectSQL(Object bean, String dbName,boolean contain, String... fields) {
		Class<?> cls = bean.getClass();
		Field[] fs = getFields(cls);

		// 表名称
		String tableName = getTableName(dbName, cls);
		Object[] objects = toWhere(fs,bean,false);
		List<Object> values = (List<Object>) objects[1];// sql语言中"?"对应的实参
		SelectField<?> selectField = new SelectField<>(cls, contain, fields);
		String where = "";
		if(values.size() != 0) {
			where = " where" + objects[0].toString().substring(4);
		}
		String sql = String.format("select %s from %s%s", selectField.getFields(), tableName, where); // 待执行的sql
		SQLValue sv = new SQLValue();
		sv.setSql(sql);
		sv.setValues(values);
		return sv;
	}
	public static SQLValue toCount(Object bean, String dbName) {
		Class<?> cls = bean.getClass();
		Field[] fields = getFields(cls);
		Object[] objs = getKeyAndVal(bean, fields, null);
		Object key = objs[1];
		String keyFeild = objs[0].toString();
		// 表名称
		String tableName = getTableName(dbName, cls);
		Object[] objects = toWhere(fields,bean,true);
		List<Object> values = (List<Object>) objects[1];// sql语言中"?"对应的实参
		String sql; // 待执行的sql
		if(key == null) {
			String where = "";
			if(!values.isEmpty()) {
				where = " where" + objects[0].toString().substring(4);
			}
			sql = String.format("select count(id) from %s%s", tableName, where);
		} else {
			sql = String.format("select count(id) from %s where %s = %s%s", tableName, keyFeild, key.toString(),objects[0]);
		}
		SQLValue sv = new SQLValue();
		sv.setSql(sql);
		sv.setValues(values);
		return sv;
	}
	// use select one end

	/**
	 *
	 * 
	 * @param clazz 实体class
	 * @param fields 实体的字段
	 * @param beans 实体集
	 * @return sql 中的 values 部分
	 */
	/**
	 * 把实体集合转换成sql中的values部分
	 *
	 * @param fields field数组
	 * @param beans 实体集合
	 * @param <B> 实体类型
	 * @return values部分
	 */
	private static <B> String toValues(Field[] fields, Iterable<B> beans) {
		StringBuilder sbValues = new StringBuilder();
		sbValues.append("values");
		for (B b : beans) {
			sbValues.append(toValue(fields, b, false));
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
		if (beans == null) {
			return null;
		} else {
			Iterator<B> iterator = beans.iterator();
			if (!iterator.hasNext()) {
				return null;
			} else {
				// 集合中的第一个bean
				B bean = iterator.next();
				@SuppressWarnings("unchecked")
				Class<B> clazz = (Class<B>) bean.getClass();

				// values 部分
				String values = toValues(getFields(clazz), beans);

				return bean2InsertSQL(bean, dbName, values, ignoreRepeat);
			}
		}
	}

	private static String bean2InsertSQL(Object bean, String dbName, String values, boolean ignoreRepeat) {
		// 集合中的第一个bean
		Class<?> clazz = bean.getClass();
		String tableName = getTableName(dbName, clazz);
		Field[] fields = getFields(clazz);
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
	private static Object[] getKeyAndVal(Object bean, Field[] files, Object val) {

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
	 * @param selectFields 查询哪些字段
	 * @return sql语句
	 */
	public static String toSelectSQL(Object bean, Object key, String dbName, boolean selectEntity,String selectFields) {
		Class<?> cls = (bean instanceof Class) ? (Class<?>) bean : bean.getClass();
		Object[] objs = getKeyAndVal(bean, getFields(cls), key);
		key = objs[1];
		if (key == null) {
			return null;
		} else {
			String keyFeild = objs[0].toString();
			// 表名称
			String tableName = getTableName(dbName, cls);
			if (selectEntity) {
				return String.format("select %s from %s where %s = %s", selectFields, tableName, keyFeild, key.toString());
			} else {
				return String.format("select %s from %s where %s = %s", keyFeild, tableName, keyFeild, key.toString());
			}
		}
	}
	
	public static QueryBuilder toSelectSQL(Class<?> cls, Object bean, Object likes, String dbName, String sort, String selectFields) {
		ConditionList conditions = new ConditionList();
		Map<String, Object> parameters = new HashMap<>();
		// 表名称
		String tableName = getTableName(dbName, cls);
		Field[] fields = cls.getDeclaredFields();
		for (Field field : fields) {
			if (allowField(field)) {
				field.setAccessible(true);

				if(bean != null) {
					Object fv;
					try {
						fv = field.get(bean);
					} catch (IllegalArgumentException | IllegalAccessException e) {
						throw new RepositoryException(e);
					}
					if (fv != null) {
						conditions.add(field.getName() + " = :" + field.getName() + " and");
						parameters.put(field.getName(), fv);// 同时把这个?号的值记下来
					}
				}

				if(likes != null) {
					Object lv;
					try
					{
						lv = field.get(likes);
					} catch (IllegalArgumentException | IllegalAccessException e) {
						throw new RepositoryException(e);
					}
					if(lv != null) {
						conditions.add(field.getName() + " like :" + field.getName() + " and");
						parameters.put(field.getName(), lv);// 同时把这个?号的值记下来
					}
				}
			}
		}
		String query;
		String countQuery;
		// 取出最后一个,去掉"and"
		if(!conditions.isEmpty()) {
			int lastIndex = conditions.size() - 1;
			String lastCondition = conditions.get(lastIndex);
			lastCondition = lastCondition.substring(0, lastCondition.length() - 3);
			conditions.set(lastIndex,lastCondition);
			query = String.format("select %s from %s #{#where} %s", selectFields, tableName, sort);
			countQuery = String.format("select count(id) from %s #{#where}", tableName);
		} else {
			query = String.format("select %s from %s %s", selectFields, tableName, sort);
			countQuery = String.format("select count(id) from %s", tableName);
		}
		
		return new QueryBuilder(query, countQuery, conditions, parameters);
	}

	public static Field[] getFields(Class<?> cls) {
		Field[] selfFields = cls.getDeclaredFields();
		Field[] superFields = cls.getSuperclass().getDeclaredFields();

		int l1 = selfFields.length;
		int l2 = superFields.length;

		Field[] nf = new Field[l1 + l2];
		System.arraycopy(selfFields, 0, nf, 0, l1);
		System.arraycopy(superFields, 0, nf, l1, l2);
		return nf;
	}

	private static String getTableName(String dbName, Class<?> cls) {
		String tableName = getEntitySimpleName(cls);
		if (dbName != null) {
			return new StringBuilder().append(dbName).append('.').append(tableName).toString();
		} else {
			return tableName;
		}
	}

	/**
	 * 如果这个bean已经包含主键的值,就以bean的主键值为准 <br>
	 * [0]: 更新语句, [1]:参数值集合类型:List&lt;Object&gt; [2](是否存在第3个值取决于toSQL是否设置true): 根据主键查的sql语句
	 * 
	 * @param bean 待更新的实体
	 * @param dbName 数据库名称,可以为null
	 * @param toSQL 是否返回根据主键查的sql语句
	 * @return 更新语句信息
	 */
	public static Object[] toUpdateSQL(Object bean, String dbName, boolean toSQL) {
		List<Object> args = new ArrayList<>();
		Class<?> cls = bean.getClass();
		// 表名称
		String tableName = getTableName(dbName, cls);

		String keyFeild;
		Object key;
		Object[] objs = getKeyAndVal(bean, getFields(cls), null);
		keyFeild = (String) objs[0];
		key = objs[1];

		if (keyFeild == null || key == null) {
			throw new RepositoryException(cls + NOT_ID);
		}

		// update UserInfo set name=?,age=? where id=?4
		StringBuilder sb = new StringBuilder(UPDATE);
		sb.append(tableName);
		sb.append(" set");
		int len = sb.length();
		try {
			Field[] fields = getFields(cls);
			for (Field field : fields) {
				if (allowField(field)) {
					field.setAccessible(true);
					Object val = field.get(bean);
					Id id = field.getAnnotation(Id.class);
					if (val != null && id == null) {
						args.add(val);
						sb.append(' ');
						sb.append(field.getName());
						sb.append("=?,");

					}
				}
			}

			if (sb.length() == len) {
				LOG.warn("传递的实体,没有什么可以修改,{}", bean);
				return ArrayUtils.EMPTY_OBJECT_ARRAY;
			} else {
				// 去掉sb最后的一个字符
				sb.deleteCharAt(sb.length() - 1);
				sb.append(WHERE);
				sb.append(keyFeild);
				sb.append("=?");
				args.add(key);
				Object[] updateinfo = new Object[3];
				updateinfo[0] = sb.toString();
				updateinfo[1] = args;
				if (toSQL) {
					updateinfo[2] = String.format("select * from %s where %s = %s", tableName, keyFeild, key.toString());
				}
				return updateinfo;
			}
		} catch (Exception e) {
			throw new RepositoryException(e);
		}
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
		List<Object> args = new ArrayList<>();
		Class<?> cls = bean.getClass();
		// 表名称
		String tableName = getTableName(dbName, cls);

		// update UserInfo set name=?,age=? where id=?4
		StringBuilder sb = new StringBuilder(UPDATE);
		sb.append(tableName);
		sb.append(" set");
		int len = sb.length();
		try {
			Field[] fields = getFields(cls);
			for (Field field : fields) {
				if (allowField(field)) {
					field.setAccessible(true);
					Object val = field.get(bean);
					if (val != null && !wps.contains(":" + field.getName())) {
						args.add(val);
						sb.append(' ');
						sb.append(field.getName());
						sb.append("=?,");

					}
				}
			}
			if (sb.length() == len) {
				LOG.warn("传递的实体,没有什么可修改,{}", bean);
				return ArrayUtils.EMPTY_OBJECT_ARRAY;
			} else {
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
				sb.append(WHERE);
				sb.append(whef);
				Object[] updateinfo = new Object[2];
				updateinfo[0] = sb.toString();
				updateinfo[1] = args;
				return updateinfo;
			}
		} catch (Exception e) {
			throw new RepositoryException(e);
		}
	}

	private static Field getKey(Class<?> clazz, Field[] fields) {
		for (Field field : fields) {
			if (field.getAnnotation(Id.class) != null) {
				return field;
			}
		}
		throw new RepositoryException(clazz + NOT_ID);
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
		if (beans == null) {
			return null;
		} else {
			Iterator<B> iterator = beans.iterator();
			if (!iterator.hasNext()) {
				return null;
			} else {

				// 集合中的第一个bean
				B bean = iterator.next();
				Class<?> clazz = bean.getClass();

				// 表名称
				String tableName = getTableName(dbName, clazz);

				// 找出主键字段
				Field[] fields = getFields(clazz);
				Field key = getKey(clazz, fields);
				key.setAccessible(true);

				StringBuilder sql = new StringBuilder();
				sql.append(UPDATE);
				sql.append(tableName);
				sql.append(" set ");
				sql.append(getSets(beans, fields, key));
				sql.append(WHERE);
				sql.append(key.getName());
				sql.append(" in(");
				sql.append(getIds(beans, key));
				sql.append(')');
				return sql.toString();
			}
		}

	}

	// sql中的set项
	private static <B> String getSets(Iterable<B> beans, Field[] fields, Field key) {
		StringBuilder sets = new StringBuilder();
		for (Field field : fields) {
			if (field != key && allowField(field)) {
				field.setAccessible(true);
				String fieldName = field.getName();
				sets.append(fieldName);
				sets.append(" = case ");
				sets.append(key.getName());
				sets.append(' ');
				for (B b : beans) {
					Object keyVal = getFieldVal(key, b);
					Object fieldVal = getFieldVal(field, b);
					sets.append("when ");
					sets.append(keyVal);
					sets.append(" then ");
					if (fieldVal != null) {
						sets.append('\'');
						sets.append(fieldVal);
						sets.append("' ");
					} else {
						sets.append(fieldName);
						sets.append(' ');
					}
				}
				sets.append("else ");
				sets.append(fieldName);
				sets.append(" end,");
			}
		}
		sets.deleteCharAt(sets.length() - 1);
		return sets.toString();
	}

	private static <B> StringBuilder getIds(Iterable<B> beans, Field key) {
		StringBuilder ids = new StringBuilder();
		for (B b : beans) {
			Object keyVal = getFieldVal(key, b);
			if (keyVal == null) {
				throw new RepositoryException("主键的值不能为null");
			}
			ids.append(keyVal);
			ids.append(',');
		}
		ids.deleteCharAt(ids.length() - 1);
		return ids;
	}

	private static Object getFieldVal(Field key, Object obj) {
		Object keyVal;
		try {
			keyVal = key.get(obj);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RepositoryException(key + "无法获取值", e);
		}
		return keyVal;
	}

	public static String toDelete(String tableName, String keyName, long keyVal, String dbName) {
		StringBuilder sq = new StringBuilder();
		sq.append("delete from ");
		if (dbName != null) {
			sq.append(dbName).append('.').append(tableName);
		} else {
			sq.append(tableName);
		}
		sq.append(WHERE);
		sq.append(keyName);
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
			if (cls.isArray() || obj instanceof Iterable) {
				String strs = JSON.toJSONString(obj);
				return strs.substring(1, strs.length() - 1);
			}
		}
		return obj;
	}

	@SuppressWarnings("unchecked")
	private static <S> S newBeanVarNull(Class<S> clazz, Object bean) {
		Field[] fields = getFields(clazz);
		try {
			if (bean == null) {
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
		Field[] files = getFields(cls);

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
			throw new RepositoryException(cls + NOT_ID);
		} else {
			return Long.parseLong(key.toString());
		}
	}

	private static String getEntitySimpleName(Class<?> clazz) {
		Table t = clazz.getAnnotation(Table.class);
		return t != null ? t.value() : clazz.getSimpleName();
	}

	public static List<Field> mapFields(Object bean) {
		List<Field> list = new ArrayList<>();
		Class<?> cls = (bean instanceof Class) ? (Class<?>) bean : bean.getClass();
		Field[] fields = getFields(cls);
		for (Field field : fields) {
			if (allowField(field)) {
				list.add(field);
			}
		}
		return list;
	}

	/**
	 * 以 and 作为分隔符，拆分成多条单条件查询
	 * @param sql 一个包含多条件的sql
	 * @return 多条sql
	 */
	public static String[] toEachOne(String sql) {
		String[] strs = StringUtils.splitByWholeSeparator(sql," and ");
		int len = strs.length;
		String[] sqls = new String[len];
		sqls[0] = strs[0];
		if(len > 1) {
			String base = StringUtils.substringBefore(strs[0]," where ") + " where ";
			for (int i = 1; i < strs.length; i++) {
				sqls[i] = base + strs[i];
			}
		}
		return sqls;
	}

	public static SQLValue getSqlValue(Class<?> clazz, String fieldName, List<Object> fieldValues, int rows, boolean contain, String[] fields)
	{
		String selectFields = new SelectField<>(clazz,contain,fields).getFields();
		LOG.debug("selectFields: {}",selectFields);

		StringBuilder sb = new StringBuilder();
		sb.append("select ");
		sb.append(selectFields);
		sb.append(" from ");
		sb.append(getTableName(null,clazz));
		sb.append(" where ");
		sb.append(fieldName);
		if(fieldValues==null || fieldValues.isEmpty())
		{
			sb.append(" in (null) limit ");
		}
		else
		{
			sb.append(" in (");
			sb.append(TypeUtil.repeatChar(fieldValues.size(),'?'));
			sb.append(") limit ");
		}
		sb.append(rows);

		SQLValue sqlValue = new SQLValue();
		sqlValue.setSql(sb.toString());
		sqlValue.setValues(fieldValues);
		return sqlValue;
	}
}
