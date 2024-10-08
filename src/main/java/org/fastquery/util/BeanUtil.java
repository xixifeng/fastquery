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
import java.lang.reflect.Modifier;
import java.util.*;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.fastquery.core.*;
import org.fastquery.struct.Predicate;
import org.fastquery.struct.SQLValue;
import org.fastquery.core.SelectField;
import com.alibaba.fastjson.JSON;

/**
 * @author xixifeng (fastquery@126.com)
 */
@Slf4j
@UtilityClass
public final class BeanUtil
{
    private static final String UPDATE = "update ";
    private static final String NOT_ID = " 必须有@Id标识,并且主键不能为null";

    private static final String WHERE = " where ";
    public static final String AND = " and ";
    public static final String OR = " or ";

    private static final String ORDER_BY = "order by";
    private static final String BLANK_1 = " ";
    private static final String EQ_WILD=" = ?";

    private static String escapeSql(String str)
    {
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
    public static String toInsertSQL(Object bean)
    {
        String values = toValue(getFields(bean.getClass()), bean, true);
        return bean2InsertSQL(bean, null, values, false);
    }

    public static String toInsertSQL(String dbName, Object bean)
    {
        String values = toValue(getFields(bean.getClass()), bean, true);
        return bean2InsertSQL(bean, dbName, values, false);
    }

    // 忽略条件: 如果不是包装类型或者字段上标识有Transient
    private static boolean allowField(Field field)
    {
        return TypeUtil.isWarrp(field.getType()) && !Modifier.isStatic(field.getModifiers()) && field.getDeclaredAnnotation(Transient.class) == null;
    }

    private static <B> String toFields(Field[] fields, B bean)
    {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        for (Field field : fields)
        {
            if (allowField(field))
            {
                Object obj = TypeUtil.getFieldVal(bean, field);
                if (field.getAnnotation(Id.class) == null || obj != null)
                {
                    // 接纳的值:
                    // 1. 不是主键的字段
                    // 或
                    // 2. 不为null的字段
                    sb.append(field.getName());
                    sb.append(',');
                }
            }
        }
        sb.setCharAt(sb.length() - 1, ')');
        return sb.toString();
    }

    /**
     * 将bean 转换成这样的格式: ('12','sunny','20')
     *
     * @param <B>    实体
     * @param fields 实体字段集
     * @param bean   实体
     * @return sql value部分
     */
    private static <B> String toValue(Field[] fields, B bean, boolean containMark)
    {
        StringBuilder sb = new StringBuilder();
        if (containMark)
        {
            sb.append("values");
        }
        sb.append('(');
        for (Field field : fields)
        {
            if (allowField(field))
            {
                Object val = TypeUtil.getFieldVal(bean, field);

                if (val != null)
                {
                    if (val instanceof Number || val instanceof Boolean)
                    {
                        sb.append(val);
                    }
                    else
                    {
                        val = TypeUtil.enumSet2Val(val);
                        sb.append('\'');
                        sb.append(escapeSql(val.toString()));
                        sb.append('\'');
                    }
                    sb.append(',');
                }
                else if (field.getAnnotation(Id.class) == null)
                {
                    sb.append("DEFAULT,");
                }
            }
        }
        sb.setCharAt(sb.length() - 1, ')');
        return sb.toString();
    }

    // use select one
    // [0] where 部分， [1] sql语言中"?"对应的实参
    private static <B> SQLValue toWhere(Field[] fields, B bean, boolean ignoreId)
    {
        if( bean instanceof Predicate)
        {
            Predicate p = (Predicate) bean;
            SQLValue sqlValue = p.getSqlValue();
            if(sqlValue != null)
            {
                return sqlValue;
            }
        }

        StringBuilder sb = new StringBuilder();
        List<Object> values = new ArrayList<>();
        for (Field field : fields)
        {
            if (allowField(field))
            {
                Object val = TypeUtil.getFieldVal(bean, field);
                if (ignoreId)
                {
                    if (val != null && field.getAnnotation(Id.class) == null)
                    {
                        sb.append(AND);
                        sb.append(field.getName());
                        sb.append(EQ_WILD);
                        values.add(val);
                    }
                }
                else if (val != null)
                {
                    sb.append(AND);
                    sb.append(field.getName());
                    sb.append(EQ_WILD);
                    values.add(val);
                }
            }
        }

        SQLValue sqlValue = new SQLValue();
        sqlValue.setSql(sb.toString());
        sqlValue.setValues(values);
        return sqlValue;
    }

    public static SQLValue toSelectSQL(Object bean, boolean unequal, boolean or, String dbName, boolean contain, String... fields)
    {
        Class<?> cls = bean.getClass();
        Field[] fs = getFields(cls);

        // 表名称
        String tableName = getTableName(dbName, cls);
        SQLValue objects = toWhere(fs, bean,false);
        List<Object> values = objects.getValues();// sql语言中"?"对应的实参
        SelectField<?> selectField = new SelectField<>(cls, contain, fields);
        String where = objects.getSql();
        if (!values.isEmpty())
        {
            where = WHERE + delOperator(where);
        }
        String sql = String.format("select %s from %s%s limit 1", selectField.getFields(), tableName, where); // 待执行的sql
        SQLValue sv = new SQLValue();
        sv.setSql(sql);
        sv.setValues(values);
        return sv;
    }
    public static SQLValue toSelectSQL(Object bean, String dbName, boolean contain, String... fields)
    {
        return toSelectSQL(bean, false, false, dbName, contain, fields);
    }

    public static SQLValue toCount(Object bean, String dbName)
    {
        Class<?> cls = bean.getClass();
        Field[] fields = getFields(cls);
        Object[] objs = getKeyAndVal(bean, fields, null);
        Object key = objs[1];
        String keyFeild = objs[0].toString();
        // 表名称
        String tableName = getTableName(dbName, cls);
        SQLValue objects = toWhere(fields,bean,true);
        List<Object> values = objects.getValues();// sql语言中"?"对应的实参
        String sql; // 待执行的sql
        if (key == null)
        {
            String where = StringUtils.EMPTY;
            if (!values.isEmpty())
            {
                where = WHERE + delOperator(objects.getSql());
            }
            sql = String.format("select count(id) from %s%s", tableName, where);
        }
        else
        {
            sql = String.format("select count(id) from %s where %s = %s%s", tableName, keyFeild, key, objects.getSql());
        }
        SQLValue sv = new SQLValue();
        sv.setSql(sql);
        sv.setValues(values);
        return sv;
    }
    // use select one end

    /**
     * 把实体集合转换成sql中的values部分
     *
     * @param fields field数组
     * @param beans  实体集合
     * @param <B>    实体类型
     * @return values部分
     */
    private static <B> String toValues(Field[] fields, Iterable<B> beans)
    {
        StringBuilder sbValues = new StringBuilder();
        sbValues.append("values");
        for (B b : beans)
        {
            sbValues.append(toValue(fields, b, false));
            sbValues.append(',');
        }
        sbValues.deleteCharAt(sbValues.length() - 1);
        return sbValues.toString();
    }

    /**
     * 转换insert 语句
     *
     * @param <B>          实体
     * @param beans        如果结合为空,则返回null
     * @param dbName       如果为null,表名称之前不会有前缀
     * @param ignoreRepeat 忽略重复
     * @return 插入语句
     */
    public static <B> String toInsertSQL(Iterable<B> beans, String dbName, boolean ignoreRepeat)
    {
        if (beans == null)
        {
            return null;
        }
        else
        {
            Iterator<B> iterator = beans.iterator();
            if (!iterator.hasNext())
            {
                return null;
            }
            else
            {
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

    private static String bean2InsertSQL(Object bean, String dbName, String values, boolean ignoreRepeat)
    {
        // 集合中的第一个bean
        Class<?> clazz = bean.getClass();
        String tableName = getTableName(dbName, clazz);
        Field[] fields = getFields(clazz);
        // 表字段
        String fs = toFields(fields, bean);

        // insert into 语句
        String insertStr;
        if (ignoreRepeat)
        {
            insertStr = "insert ignore into";
        }
        else
        {
            insertStr = "insert into";
        }
        return insertStr +
                ' ' +
                tableName +
                fs +
                ' ' +
                values;
    }

    public static String arr2InsertSQL(Object[] beans, String dbName, boolean ignoreRepeat)
    {
        Iterable<Object> list = Arrays.asList(beans);
        return toInsertSQL(list, dbName, ignoreRepeat);
    }

    // 或取主键的名称 和 主键的值
    private static Object[] getKeyAndVal(Object bean, Field[] files, Object val)
    {

        Object[] objs = new Object[2];
        objs[1] = val;

        // 获取主键的名称
        for (Field field : files)
        {
            if (field.getAnnotation(Id.class) != null)
            {
                objs[0] = field.getName();
                if (val == null)
                {
                    // 顺便取主键的值
                    objs[1] = TypeUtil.getFieldVal(bean, field);
                }
                break;
            }
        }

        if (objs[0] == null)
        {
            throw new RepositoryException(bean + " 需要用@Id在实体上标识主键");
        }

        return objs;
    }

    /**
     * 转换查询语句, 实体上必须包含主键字段 <br>
     * <p>
     * 注意: 如果主键值为null,则返回null,没有主键值,那么根据什么来查询呢?
     *
     * @param bean         实体 或 class
     * @param key          主键值, 如果传递null,那么自动获取,获取到的为null那么报错. 指定的值优先
     * @param dbName       数据库名称
     * @param selectEntity true 查实体, 反之,查主键值
     * @param selectFields 查询哪些字段
     * @return sql语句
     */
    public static String toSelectSQL(Object bean, Object key, String dbName, boolean selectEntity, String selectFields)
    {
        Class<?> cls = (bean instanceof Class) ? (Class<?>) bean : bean.getClass();
        Object[] objs = getKeyAndVal(bean, getFields(cls), key);
        key = objs[1];
        if (key == null)
        {
            return null;
        }
        else
        {
            String keyFeild = objs[0].toString();
            // 表名称
            String tableName = getTableName(dbName, cls);
            if (selectEntity)
            {
                return String.format("select %s from %s where %s = %s", selectFields, tableName, keyFeild, key);
            }
            else
            {
                return String.format("select %s from %s where %s = %s", keyFeild, tableName, keyFeild, key);
            }
        }
    }

    public static Field[] getFields(Class<?> cls)
    {
        Field[] selfFields = cls.getDeclaredFields();
        Field[] superFields = cls.getSuperclass().getDeclaredFields();

        int l1 = selfFields.length;
        int l2 = superFields.length;

        Field[] nf = new Field[l1 + l2];
        System.arraycopy(selfFields, 0, nf, 0, l1);
        System.arraycopy(superFields, 0, nf, l1, l2);
        return nf;
    }

    public static String getTableName(String dbName, Class<?> cls)
    {
        String tableName = getEntitySimpleName(cls);
        if (dbName != null)
        {
            return dbName + '.' + tableName;
        }
        else
        {
            return tableName;
        }
    }

    /**
     * 如果这个bean已经包含主键的值,就以bean的主键值为准 <br>
     * [0]: 更新语句, [1]:参数值集合类型:List&lt;Object&gt; [2](是否存在第3个值取决于toSQL是否设置true): 根据主键查的sql语句
     *
     * @param bean   待更新的实体
     * @param dbName 数据库名称,可以为null
     * @param toSQL  是否返回根据主键查的sql语句
     * @return 更新语句信息
     */
    public static Object[] toUpdateSQL(Object bean, String dbName, boolean toSQL)
    {
        List<Object> args = new ArrayList<>();
        Class<?> cls = bean.getClass();
        // 表名称
        String tableName = getTableName(dbName, cls);

        String keyFeild;
        Object key;
        Object[] objs = getKeyAndVal(bean, getFields(cls), null);
        keyFeild = (String) objs[0];
        key = objs[1];

        if (keyFeild == null || key == null)
        {
            throw new RepositoryException(cls + NOT_ID);
        }

        // update UserInfo set name=?,age=? where id=?4
        StringBuilder sb = new StringBuilder(UPDATE);
        sb.append(tableName);
        sb.append(" set");
        int len = sb.length();
        try
        {
            Field[] fields = getFields(cls);
            for (Field field : fields)
            {
                if (allowField(field))
                {

                    Object val = TypeUtil.getFieldVal(bean, field);
                    Id id = field.getAnnotation(Id.class);
                    if (val != null && id == null)
                    {
                        val = TypeUtil.enumSet2Val(val);
                        args.add(val);
                        sb.append(' ');
                        sb.append(field.getName());
                        sb.append("=?,");

                    }
                }
            }

            if (sb.length() == len)
            {
                log.warn("传递的实体,没有什么可以修改,{}", bean);
                return ArrayUtils.EMPTY_OBJECT_ARRAY;
            }
            else
            {
                // 去掉sb最后的一个字符
                sb.deleteCharAt(sb.length() - 1);
                sb.append(WHERE);
                sb.append(keyFeild);
                sb.append("=?");
                args.add(key);
                Object[] updateinfo = new Object[3];
                updateinfo[0] = sb.toString();
                updateinfo[1] = args;
                if (toSQL)
                {
                    updateinfo[2] = String.format("select * from %s where %s = %s", tableName, keyFeild, key);
                }
                return updateinfo;
            }
        }
        catch (Exception e)
        {
            throw new RepositoryException(e);
        }
    }

    /**
     * [0]: 更新语句 [1]:参数值集合类型:List&lt;Object&gt;
     *
     * @param bean   实体
     * @param dbName 数据库名称
     * @param where  条件
     * @return 更新语句信息
     */
    public static Object[] toUpdateSQL(Object bean, String dbName, String where)
    {
        List<String> wps = TypeUtil.matches(where, RegexCache.COLON_REG_PATT);
        List<Object> args = new ArrayList<>();
        Class<?> cls = bean.getClass();
        // 表名称
        String tableName = getTableName(dbName, cls);

        // update UserInfo set name=?,age=? where id=?4
        StringBuilder sb = new StringBuilder(UPDATE);
        sb.append(tableName);
        sb.append(" set");
        int len = sb.length();
        try
        {
            Field[] fields = getFields(cls);
            for (Field field : fields)
            {
                if (allowField(field))
                {
                    Object val = TypeUtil.getFieldVal(bean, field);
                    if (val != null && !wps.contains(":" + field.getName()))
                    {
                        args.add(val);
                        sb.append(' ');
                        sb.append(field.getName());
                        sb.append("=?,");

                    }
                }
            }
            if (sb.length() == len)
            {
                log.warn("传递的实体,没有什么可修改,{}", bean);
                return ArrayUtils.EMPTY_OBJECT_ARRAY;
            }
            else
            {
                // where的后面部分 和 追加sql参数
                String whef = RegExUtils.replaceAll(where, RegexCache.COLON_REG_PATT, StrConst.QUE);
                for (String wp : wps)
                {
                    Object val = new PropertyDescriptor(wp.replace(":", StringUtils.EMPTY), cls).getReadMethod().invoke(bean);
                    if (val == null)
                    {
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
        }
        catch (Exception e)
        {
            throw new RepositoryException(e);
        }
    }

    private static Field getKey(Class<?> clazz, Field[] fields)
    {
        for (Field field : fields)
        {
            if (field.getAnnotation(Id.class) != null)
            {
                return field;
            }
        }
        throw new RepositoryException(clazz + NOT_ID);
    }

    /**
     * 将一个集合转换称批量update语句
     *
     * @param <B>    实体
     * @param beans  实体集合
     * @param dbName 数据库名称
     * @return update SQL
     */
    public static <B> String toUpdateSQL(Iterable<B> beans, String dbName)
    {
        if (beans == null)
        {
            return null;
        }
        else
        {
            Iterator<B> iterator = beans.iterator();
            if (!iterator.hasNext())
            {
                return null;
            }
            else
            {

                // 集合中的第一个bean
                B bean = iterator.next();
                Class<?> clazz = bean.getClass();

                // 表名称
                String tableName = getTableName(dbName, clazz);

                // 找出主键字段
                Field[] fields = getFields(clazz);
                Field key = getKey(clazz, fields);

                return UPDATE +
                        tableName +
                        " set " +
                        getSets(beans, fields, key) +
                        WHERE +
                        key.getName() +
                        " in(" +
                        getIds(beans, key) +
                        ')';
            }
        }

    }

    // sql中的set项
    private static <B> String getSets(Iterable<B> beans, Field[] fields, Field key)
    {
        StringBuilder sets = new StringBuilder();
        for (Field field : fields)
        {
            if (field != key && allowField(field))
            {
                String fieldName = field.getName();
                int caseStartIndex = sets.length();
                sets.append(fieldName);
                sets.append(" = case ");
                sets.append(key.getName());
                sets.append(' ');
                boolean exists = false; // 判断当前 field 的值，是否全部为 null，如果全部为 null, 那么要移除这个字段的 case sql 语句
                for (B b : beans)
                {
                    Object keyVal = TypeUtil.getFieldVal(b, key);
                    Object fieldVal = TypeUtil.getFieldVal(b, field);
                    int currentLen = sets.length();
                    sets.append("when "); // ➀
                    sets.append(keyVal); // ➁
                    sets.append(" then "); // ➂
                    if (fieldVal == null) // 满足，则销毁 ➀ -> ➂ 这三行
                    {
                        sets.delete(currentLen - 1, sets.length() - 1);
                    }
                    else
                    {   exists = true;
                        if (fieldVal instanceof Boolean)
                        {
                            sets.append(fieldVal);
                            sets.append(' ');
                        }
                        else
                        {
                            String s = fieldVal.toString();
                            if (fieldVal instanceof EnumSet)
                            {
                                s = StringUtils.substringBetween(s, "[", "]");
                                s = StringUtils.replace(s, ", ", ",");
                            }
                            sets.append('\'');
                            sets.append(s.replace("'","''"));
                            sets.append("' ");
                        }
                    }
                }
                sets.append("else ");
                sets.append(fieldName);
                sets.append(" end,");
                if(!exists)
                {
                    sets.delete(caseStartIndex, sets.length());
                }
            }
        }
        if(sets.length() != 0)
        {
            sets.deleteCharAt(sets.length() - 1);
            return sets.toString();
        }
        else
        {
            return "id=id";
        }
    }

    private static <B> StringBuilder getIds(Iterable<B> beans, Field key)
    {
        StringBuilder ids = new StringBuilder();
        for (B b : beans)
        {
            Object keyVal = TypeUtil.getFieldVal(b, key);
            if (keyVal == null)
            {
                throw new RepositoryException("主键的值不能为null");
            }
            ids.append(keyVal);
            ids.append(',');
        }
        ids.deleteCharAt(ids.length() - 1);
        return ids;
    }

    public static String toDelete(String tableName, String keyName, long keyVal, String dbName)
    {
        StringBuilder sq = new StringBuilder();
        sq.append("delete from ");
        if (dbName != null)
        {
            sq.append(dbName).append('.').append(tableName);
        }
        else
        {
            sq.append(tableName);
        }
        sq.append(WHERE);
        sq.append(keyName);
        sq.append('=');
        sq.append(keyVal);

        return sq.toString();
    }

    // 返回sql中in查询需要的值
    static Object parseList(Object obj)
    {
        if (obj == null)
        {
            return null;
        }
        else
        {
            Class<?> cls = obj.getClass();
            if (cls.isArray() || obj instanceof Iterable)
            {
                String strs = JSON.toJSONString(obj);
                return strs.substring(1, strs.length() - 1);
            }
        }
        return obj;
    }

    public static long toId(Object entity)
    {
        Class<?> cls = entity.getClass();
        Object key = null;
        Field[] files = getFields(cls);

        for (Field field : files)
        {
            if (field.getAnnotation(Id.class) != null)
            {
                key = TypeUtil.getFieldVal(entity, field);
                break;
            }
        }

        if (key == null)
        {
            throw new RepositoryException(cls + NOT_ID);
        }
        else
        {
            return Long.parseLong(key.toString());
        }
    }

    private static String getEntitySimpleName(Class<?> clazz)
    {
        Table t = clazz.getAnnotation(Table.class);
        return t != null ? t.value() : clazz.getSimpleName();
    }

    public static List<Field> mapFields(Object bean)
    {
        List<Field> list = new ArrayList<>();
        Class<?> cls = (bean instanceof Class) ? (Class<?>) bean : bean.getClass();
        Field[] fields = getFields(cls);
        for (Field field : fields)
        {
            if (allowField(field))
            {
                list.add(field);
            }
        }
        return list;
    }

    /**
     * 以 and 作为分隔符，拆分成多条单条件查询
     *
     * @param sql 一个包含多条件的sql
     * @return 多条sql
     */
    public static String[] toEachOne(String sql)
    {
        String[] strs = StringUtils.splitByWholeSeparator(sql, AND);
        int len = strs.length;
        String[] sqls = new String[len];
        sqls[0] = strs[0];
        if (len > 1)
        {
            String base = StringUtils.substringBefore(strs[0], WHERE) + WHERE;
            for (int i = 1; i < strs.length; i++)
            {
                sqls[i] = base + strs[i];
            }
        }
        return sqls;
    }

    public static SQLValue getSqlValue14(Class<?> clazz, Object equals, boolean contain, String[] fields)
    {
        String selectFields = new SelectField<>(clazz, contain, fields).getFields();
        log.debug("selectFields: {}", selectFields);

        StringBuilder sb = new StringBuilder();
        SQLValue sqlValue = new SQLValue();
        sb.append("select ");
        sb.append(selectFields);
        sb.append(" from ");
        sb.append(getTableName(null, clazz));

            Field[] fs = getFields(clazz);
            SQLValue objects = toWhere(fs, equals, false);
            List<Object> values = objects.getValues();

            String sql = objects.getSql();
            if(!sql.isEmpty())
            {
                String wh = delOperator(sql);
                wh = wh.trim();
                if(!wh.startsWith(ORDER_BY))
                {
                    sb.append(WHERE);
                }
                else
                {
                    sb.append(BLANK_1);
                }
                sb.append(wh);
                sb.append(BLANK_1);
            }

            sqlValue.addValues(values);

        sqlValue.setSql(sb.toString());
        return sqlValue;
    }

    // 删除为首的 and 或 or
    private static String delOperator(String sql)
    {
        String wh = BLANK_1 + sql.trim();
        if(wh.startsWith(AND) || wh.startsWith(OR))
        {
            return wh.substring(4);
        }
        else
        {
            return wh;
        }
    }

    public static SQLValue getSqlValue(Class<?> clazz, String fieldName, List<Object> fieldValues, Object equals, boolean contain, String[] fields)
    {
        String selectFields = new SelectField<>(clazz, contain, fields).getFields();
        log.debug("selectFields: {}", selectFields);

        StringBuilder sb = new StringBuilder();
        SQLValue sqlValue = new SQLValue();
        sb.append("select ");
        sb.append(selectFields);
        sb.append(" from ");
        sb.append(getTableName(null, clazz));
        sb.append(WHERE);
        sb.append(fieldName);
        if (fieldValues == null || fieldValues.isEmpty())
        {
            sb.append(" in (null) ");
        }
        else
        {
            sqlValue.setValues(fieldValues);
            sb.append(" in (");
            sb.append(TypeUtil.repeatChar(fieldValues.size(), '?'));
            sb.append(") ");
        }

        // equal
        if(equals != null)
        {
            Field[] fs = getFields(clazz);
            SQLValue objects = toWhere(fs, equals, false);
            sb.append(objects.getSql());
            sb.append(' ');
            sqlValue.addValues(objects.getValues());
        }
        sqlValue.setSql(sb.toString());
        return sqlValue;
    }

}
