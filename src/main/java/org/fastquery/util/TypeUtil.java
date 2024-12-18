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

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSONArray;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.fastquery.core.*;
import org.fastquery.asm.Script2Class;
import org.fastquery.mapper.QueryPool;
import org.fastquery.page.PageIndex;
import org.fastquery.page.PageSize;
import org.fastquery.struct.AttributeConverter;
import org.fastquery.struct.Convert;
import org.fastquery.struct.ParamMap;
import org.fastquery.where.Condition;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * @author xixifeng (fastquery@126.com)
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TypeUtil
{
    // 给定一个"正则匹配"匹配在另一个字符串,把匹配上的字符串存入一个数组里. 这样一来即可以用,又可以统计出现次数!
    public static List<String> matches(String str, Pattern regex)
    {
        List<String> empty = new ArrayList<>();
        if (str == null)
        {
            return empty;
        }
        return matcheAll(regex, str, empty);
    }

    // 集合中没有重复
    public static Set<String> matchesNotrepeat(String str, Pattern regex)
    {
        Set<String> empty = new HashSet<>();
        if (str == null)
        {
            return empty;
        }
        return matcheAll(regex, str, empty);
    }

    private static <E extends Collection<String>> E matcheAll(Pattern regex, String str, E collection)
    {
        // 将给定的正则表达式编译到模式中。
        // 创建匹配给定输入与此模式的匹配器。
        Matcher m = regex.matcher(str);
        String val;
        // 尝试查找与该模式匹配的输入序列的下一个子序列。
        while (m.find())
        {
            // 返回由以前匹配操作所匹配的输入子序列。
            val = m.group();
            collection.add(val);
        }
        return collection;
    }

    /**
     * 获取sql中包含的参数与方法参数的对应关系. <br>
     * sql参数也称之为是sql占位符叫法不一,请不要扣字眼. <br>
     * 例如: int[4]={2,2,3,1,4} <br>
     * 表示: sql中的第1个参数 对应方法的第2个参数 <br>
     * sql中的第2个参数 对应方法的第2个参数 <br>
     * sql中的第3个参数 对应方法的第3个参数 <br>
     * sql中的第4个参数 对应方法的第1个参数 <br>
     * sql中的第5个参数 对应方法的第4个参数 <br>
     *
     * @param sql sql语句
     * @return sql
     */
    public static int[] getSQLParameter(String sql)
    {
        List<String> subs = matches(sql, RegexCache.SP1_REG_PATT);
        int len = subs.size();
        int[] ints = new int[len];
        for (int i = 0; i < len; i++)
        {
            ints[i] = Integer.parseInt(subs.get(i).replace(StrConst.QUE, StringUtils.EMPTY));
        }
        return ints;
    }

    /**
     * ParamMap.rps: 返回sql中的"?"需要变化的地方.例如:若返回Map&lt;Integer, Integer&gt; -&gt; {1=3,5=2} <br>
     * 表示sql中的第1个(从0开始计数)"?"号,应该调整3个"?"号,问号彼此用","号隔开 <br>
     * 完成上次替换之后的新sql中的第5个(从0开始计数)"?"号,应该调整2个"?"号,问号彼此用","号隔开 <br>
     * 强调: 完成上次替换之后的新sql,这句话要理解! <br>
     * <p>
     * ParamMap.objs: 返回sql参数的值.例如: 若返回List-&gt;{1,3.3,"aa"} <br>
     * 表示: sql中的第1个参数的值对应1 <br>
     * 表示: sql中的第2个参数的值对应3.3 <br>
     * 表示: sql中的第3个参数的值对应"aa" <br>
     *
     * @param indexMap 标记着,sql中的参数,对应着哪个方法参数 <br>
     *                 例如: int[4]={2,2,3,1,4} <br>
     *                 表示: sql中的第1个参数 对应方法的第2个参数 <br>
     *                 sql中的第2个参数 对应方法的第2个参数 <br>
     *                 sql中的第3个参数 对应方法的第3个参数 <br>
     *                 sql中的第4个参数 对应方法的第1个参数 <br>
     *                 sql中的第5个参数 对应方法的第4个参数 <br>
     *                 <p>
     *                 请特别注意: sql参数与方法参数的对应,并不代表就是值的对应. <br>
     * @return sql处理信号集
     */
    public static ParamMap getParamMap(int[] indexMap)
    {
        Object[] args = QueryContext.getArgs();
        Map<Integer, Integer> rps = new TreeMap<>();
        List<Object> objs = new ArrayList<>();
        int increment = 0;
        for (int i = 0; i < indexMap.length; i++)
        {
            int paramIndex = indexMap[i] - 1;
            // 取出sql参数所对应的方法参数
            Object mp = args[paramIndex]; // 这个值有可能是null
            if (mp == null)
            {
                objs.add(getParamDefVal(paramIndex));
            }
            else
            {
                Class<?> mpClazz = mp.getClass();
                int count;
                if (mp instanceof Iterable)
                {
                    @SuppressWarnings("unchecked")
                    Iterable<Object> iterable = (Iterable<Object>) mp;
                    Iterator<Object> iterator = iterable.iterator();
                    count = 0;
                    while (iterator.hasNext())
                    {
                        ++count;
                        objs.add(iterator.next());
                    }
                    increment = exted(rps, objs, increment, i, count);
                }
                else if (mpClazz.isArray())
                {
                    // asList 是一个可变变量
                    List<Object> arrs = toList(mp);
                    objs.addAll(arrs);
                    count = arrs.size();
                    increment = exted(rps, objs, increment, i, count);
                }
                else
                {
                    objs.add(mp);
                }
            }
        }

        return new ParamMap(rps, objs);

    }

    // 做两件事
    // 1. 如果SQL中的第i个参数是个集合并且这个集合为空,那么就给这个SQL参数的真实值设置为null
    // 2. 用于记录,第i个"?"需要替换成count个"?"
    private static int exted(Map<Integer, Integer> rps, List<Object> objs, int increment, int i, int count)
    {
        if (count == 0)
        {
            objs.add(null);
        }
        else
        {
            rps.put(i + increment, count); // 这个put用于记录,第i个"?"需要替换成count个"?"
            increment += count - 1; // 增量个数(除开本身一个)
        }
        return increment;
    }

    private static Object getParamDefVal(int paramIndex)
    {
        Param param = QueryContext.getMethodInfo().getParameters()[paramIndex].getAnnotation(Param.class);
        if (param != null && !param.defaultVal().trim().equals(StringUtils.EMPTY))
        {
            return param.defaultVal();
        }
        else
        {
            return null;
        }
    }

    private static String overChar(int overlap)
    {
        if (overlap < 1)
        {
            return StringUtils.EMPTY;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < overlap; i++)
        {
            sb.append("?,");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    /**
     * 将第index个'?'换成overlap个'?','?'号之间用逗号(,)隔开
     *
     * @param str     被处理的字符串
     * @param index   索引
     * @param overlap 个数
     * @return 处理后的字符串
     */
    public static String replace(String str, int index, int overlap)
    {
        if (str == null || index < 0 || overlap < 1)
            return StringUtils.EMPTY;
        StringBuilder sb = new StringBuilder(str);
        int arisen = 0; // 记录"?"是第几次出现
        int len = sb.length();
        for (int i = 0; i < len; i++)
        { // 在这里用len 而不是
            // sb.length(),那是因为sb一但被改变了,这个for循环就会结束,因此用len是没问题的
            if (sb.charAt(i) == '?' && index == arisen++)
            {
                sb.replace(i, i + 1, overChar(overlap));
                break;
            }
        }
        return sb.toString();
    }

    /**
     * 判断在str字符串中,在不区分大小写的前提下是否包含有给定的word
     *
     * @param str  目标字符串
     * @param word 待差找的单词
     * @return 存在:true,反之,false
     */
    public static boolean containsIgnoreCase(String str, String word)
    {

        // 将给定的正则表达式编译到模式中。
        // 在不区分大小写的前提下,匹配是否包含有单词 word
        // 注意:单词的分隔不仅是空格,例如: hello{good<yes(ok)jetty 123abc 包含有单词 hello good yes ok 等等. (已验证)
        Pattern p = Pattern.compile("(?i)\\b" + word + "\\b");

        // 创建匹配给定输入与此模式的匹配器。
        Matcher m = p.matcher(str);

        // 尝试查找与该模式匹配的输入序列的下一个子序列。
        while (m.find())
        {
            // m.group() 表示返回由以前匹配操作所匹配的输入子序列。
            if (m.group().equalsIgnoreCase(word))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * 检查对象是否存在默认构造函数
     *
     * @param clazz 待检查的class
     * @return 有:true, 无:false
     * @throws SecurityException 安全异常
     */
    public static boolean hasDefaultConstructor(Class<?> clazz)
    {
        if (clazz == null)
        {
            return false;
        }
        try
        {
            clazz.getConstructor();
        }
        catch (Exception e)
        {
            return false;
        }
        return true;
    }

    /**
     * 从给定的参数列表中,搜寻出是第几个参数带有指定的注解.注意:从0开始计数
     *
     * @param clazz      需要查询的注解
     * @param parameters 被查的参数列表
     * @return 第几个
     */
    public static int findAnnotationIndex(Class<? extends Annotation> clazz, Parameter[] parameters)
    {
        for (int i = 0; i < parameters.length; i++)
        {
            if (parameters[i].getAnnotation(clazz) != null)
            {
                return i;
            }
        }
        return -1;
    }

    /**
     * 查询标识有指定注解的参数
     *
     * @param clazz      待查找的类型
     * @param parameters 参数类型集
     * @return 参数类型
     */
    public static Parameter findParameter(Class<? extends Annotation> clazz, Parameter[] parameters)
    {
        for (Parameter parameter : parameters)
        {
            if (parameter.getAnnotation(clazz) != null)
            {
                return parameter;
            }
        }
        return null;
    }

    /**
     * parameters 中是否存在 clazz,存在就返回true,反之返回false
     *
     * @param clazz      待差找的类型
     * @param parameters 参数类型
     * @return 有返回true, 无:false
     */
    public static boolean hasType(Class<?> clazz, Parameter[] parameters)
    {
        for (Parameter parameter : parameters)
        {
            if (parameter.getType() == clazz)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * 统计一个注解在参数列表中出现的次数
     *
     * @param clazz      注解的class
     * @param parameters 参数类型
     * @return 出现次数
     */
    public static int countRepeated(Class<? extends Annotation> clazz, Parameter[] parameters)
    {
        int count = 0;
        for (Parameter parameter : parameters)
        {
            if (parameter.getAnnotation(clazz) != null)
            {
                count += 1;
            }
        }
        return count;
    }

    /**
     * 从给定的参数列表中,搜寻出是第几个参数带有@Id注解.注意:从0开始计数
     *
     * @param parameters 参数类型
     * @return 若返回 -1 表示没有找到
     */
    public static int findId(Parameter[] parameters)
    {
        return findAnnotationIndex(Id.class, parameters);
    }

    private static String getReplacement(Object obj, String defVal)
    {
        return Matcher.quoteReplacement(obj != null ? obj.toString() : defVal);
    }

    // 将 str中的 ${tagName} 和 $tagName 统统替换成 replacement
    private static String replaceAllEL(String str, String tagName, String replacement)
    {
        Pattern p1 = RegexCache.getPattern("\\$\\{" + tagName + "}");
        Pattern p2 = RegexCache.getPattern("\\$" + tagName + "\\b");
        str = RegExUtils.replaceAll(str, p1, replacement);
        return RegExUtils.replaceAll(str, p2, replacement);
    }

    /**
     * 处理 @Param 模板参数
     *
     * @param method 方法
     * @param args   给这个方法传递的参数值
     * @param sql    sql语句
     * @return sql
     */
    static String paramFilter(MethodInfo method, Object[] args, String sql)
    {
        String s = sql.replace("::", ":");
        // 替换@Param
        Annotation[][] annotations = method.getParameterAnnotations();
        QueryByNamed queryByNamed = method.getQueryByNamed();
        int len = annotations.length;
        for (int i = 0; i < len; i++)
        {
            Annotation[] anns = annotations[i];
            for (Annotation ann : anns)
            {
                if (ann.annotationType() == Param.class)
                {
                    Param param = (Param) ann;
                    Object objx = args[i];
                    objx = BeanUtil.parseList(objx);
                    // 这里的replaceAll的先后顺序很重要
                    // '{' 是正则语法的关键字,必须转义
                    if (queryByNamed == null)
                    {
                        String replacement = getReplacement(objx, param.defaultVal());
                        s = replaceAllEL(s, param.value(), replacement);
                    }

                    // 将 ":xx" 格式的 替换成 "?num"
                    // 替换时必须加单词分界符(\\b),举例说明: sql中同时存在":ABCD",":A",
                    // 不加单词分界符,":A"替换成"?num"后,会使":ABCD"变成":?numBCD"
                    Pattern pattern = RegexCache.getPattern(":" + param.value() + "\\b");
                    s = RegExUtils.replaceAll(s, pattern, StrConst.QUE + (i + 1));
                }
            }
        }
        // 替换@Param End
        return s;
    }

    /**
     * 处理 @Param 模板参数 仅仅只处理把 ":name" 替换成 "?数字"
     *
     * @param method 方法
     * @param sql    sql语句
     * @return sql
     */
    public static String paramNameFilter(MethodInfo method, String sql)
    {
        String s = sql;
        // 替换@Param
        Annotation[][] annotations = method.getParameterAnnotations();
        int len = annotations.length;
        for (int i = 0; i < len; i++)
        {
            Annotation[] anns = annotations[i];
            for (Annotation ann : anns)
            {
                if (ann.annotationType() == Param.class)
                {
                    Param param = (Param) ann;
                    // Pattern.quote(":"+param.value())
                    Pattern p = RegexCache.getPattern(":" + param.value() + "\\b");
                    s = RegExUtils.replaceAll(s, p, StrConst.QUE + (i + 1));
                }
            }
        }
        // 替换@Param End
        return s;
    }

    private static boolean getFactor1(Condition condition, Object arg)
    {
        return (arg == null) && condition.ignoreNull();
    }

    private static boolean getFactor2(Condition condition, Object arg)
    {
        return arg != null && StringUtils.EMPTY.equals(arg.toString()) && condition.ignoreEmpty();
    }

    private static boolean getFactor3(Condition condition, int index)
    {
        return (!condition.ignoreScript().equals("false")) && Script2Class.getJudge(index).ignore();
    }

    private static boolean getFactor4(Condition condition)
    {
        try
        {
            return condition.ignore().getDeclaredConstructor().newInstance().ignore();
        }
        catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e1)
        {
            // 这个异常其实永远也发生不了,该异常已经通过静态分析,提升到初始化阶段了

            log.error("{} 必须有一个不带参数且用public修饰的构造方法.反之,作废", condition.ignore());
            return false;
        }
    }

    private static boolean getFactor5(Condition condition, Object arg)
    {

        String[] allows = condition.allowRule();

        if (allows.length != 0)
        { // 表明,允许的范围并不是全部,而是有所限定
            if (arg == null)
            { // 范围有明确指定,还传递null,那么必然忽略
                return true;
            }
            else
            {
                for (String allow : allows)
                {
                    // 因为注解的特性 allows的集合中的成员永远不可能出现null
                    if (!RegexCache.getPattern(allow).matcher(arg.toString()).matches())
                    { // 不在允许范围立即忽略
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static boolean getFactor6(Condition condition, Object arg)
    {
        if (arg != null)
        {
            String[] ignores = condition.ignoreRule();
            for (String ignore : ignores)
            {
                if (RegexCache.getPattern(ignore).matcher(arg.toString()).matches())
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 裁决是否忽略指定的条件
     *
     * @param condition         条件
     * @param value             Condition.value处理之后的条件值
     * @param conditionPosition 条件在方法上的位置索引
     * @return null表示忽略这个条件, 反之返回这个条件的值
     */
    private static String ignoreCondition(Condition condition, String value, int conditionPosition)
    {

        // 忽略因子列表,任何一个都可以导致忽略
        Set<String> pars = TypeUtil.matchesNotrepeat(value, RegexCache.SP1_REG_PATT);
        Object[] args = QueryContext.getArgs();
        for (String par : pars)
        {
            int index = Integer.parseInt(par.replace(StrConst.QUE, StringUtils.EMPTY)); // 计数是1开始的
            Object arg = args[index - 1];
            if (getFactor1(condition, arg) || getFactor2(condition, arg) || getFactor5(condition, arg) || getFactor6(condition, arg))
            {
                return null;
            }
            else if (arg == null)
            { // 当前?num 处是null也要保留条件,那么就要考略null跟一些运算符不能运算的问题(纠正它)
                value = extReplaceAll(value, index);
            }
        }

        if (getFactor3(condition, conditionPosition) || getFactor4(condition))
        {
            return null;
        }

        // if$ , else$ 解析
        if (!"true".equals(condition.if$()) && !Script2Class.getJudge(conditionPosition).ignore())
        { // 如果if结果为假
            String elseValue = condition.else$();
            if (StringUtils.EMPTY.equals(elseValue))
            {
                return null;
            }
            else
            {
                return elseVal(elseValue);
            }
        }
        else
        {
            return value;
        }
    }

    private static String elseVal(String elseValue)
    {
        elseValue = paramFilter(QueryContext.getMethodInfo(), QueryContext.getArgs(), elseValue);
        Set<String> pars = TypeUtil.matchesNotrepeat(elseValue, RegexCache.SP1_REG_PATT);
        Object[] args = QueryContext.getArgs();
        for (String par : pars)
        {
            int index = Integer.parseInt(par.replace(StrConst.QUE, StringUtils.EMPTY)); // 计数是1开始的
            if (args[index - 1] == null)
            { // ?num 对应的实参是null,要附加处理下
                elseValue = extReplaceAll(elseValue, index);
            }
        }
        return elseValue;
    }

    /**
     * 考虑条件是否参与运算问题.
     *
     * @param method 方法
     * @param args   参数集
     * @return where部分sql
     */
    public static String getWhereSQL(MethodInfo method, Object[] args)
    {
        StringBuilder sb = new StringBuilder();

        // 追加条件
        Condition[] conditions = method.getConditions();
        for (int i = 0; i < conditions.length; i++)
        {
            String value = conditions[i].value();
            value = paramFilter(method, args, value);
            value = ignoreCondition(conditions[i], value, i);
            if (value != null)
            {
                int sblen = sb.length();
                // sb的长度是0 或者 最后一个字符就是不是空格
                if (sblen == 0 || sb.charAt(sblen - 1) != ' ')
                {
                    sb.append(' ');
                    sblen += 1;
                }
                if (sblen == 1 && i != 0)
                { // 条件成立,表示这个SQL条件的前面还不存在条件,// 那么第一个条件的链接符,必须去掉.
                    // (where后面不能直接跟运算符号)
                    sb.append(removePart(value));
                }
                else
                {
                    sb.append(value);
                }

            }
        }
        // 追加条件 End

        if (!StringUtils.EMPTY.equals(sb.toString()))
        {
            sb.insert(0, "where");
        }

        return sb.toString();
    }

    private static String extReplaceAll(String value, int index)
    {
        // 如果传递null 还要求参与运算.
        // sql中null无法跟比较运算符(如 =, <, 或者 <>),一起运算,必须使用 is null 和 is not null 操作符.
        value = RegExUtils.replaceAll(value, RegexCache.MOR_BLANK_PATT, StringUtils.SPACE); // 把多个空白换成一个空格
        value = value.replace("=?", "= ?"); // 将"=?" 替换成 "= ?"
        value = value.replace(" = ?" + index, " is null");
        value = value.replace(" <> ?" + index, " is not null");
        value = value.replace(" != ?" + index, " is not null");
        return value;
    }

    /**
     * 获取完整的SQL语句
     *
     * @param method  方法
     * @param queries 注解集
     * @param args    参数集
     * @return sql集
     */
    public static List<String> getQuerySQL(MethodInfo method, Query[] queries, Object[] args)
    {
        List<String> sqls = new ArrayList<>();

        // 如果是QueryByNamed
        if (method.getQueryByNamed() != null)
        {
            String s = QueryPool.render(true);
            s = paramFilter(method, args, s);
            sqls.add(s);
            return sqls;
        }

        for (Query query : queries)
        {
            String sql = query.value();
            sql = paramFilter(method, args, sql);
            String sets = SetParser.process();
            if (sets != null)
            {
                sql = RegExUtils.replaceFirst(sql, RegexCache.SETS_REG_PATT, Matcher.quoteReplacement(sets));
            }
            sql = RegExUtils.replaceFirst(sql, RegexCache.WHERE_REG_PATT, Matcher.quoteReplacement(getWhereSQL(method, args)));
            sqls.add(sql);
        }
        return sqls;
    }

    /**
     * 过滤java语法中的注释
     *
     * @param str 等待过滤的字符串
     * @return 处理之后的字符串
     */
    static String filterComments(String str)
    {
        // 这些正则只是在初始化阶段用一次，因此不必做缓存
        // 过滤 //
        String s = str.replaceAll("//(.)+\\n", StringUtils.EMPTY);
        s = s.replaceAll("//(.)?\\n", StringUtils.EMPTY);
        // 过滤多行注释
        s = s.replaceAll("/\\*[\\s\\S]*?\\*/", StringUtils.EMPTY);
        return s;
    }

    /**
     * 判断类型是否是Map&lt;String,Object&gt;或Map&lt;String,String&gt;
     *
     * @param type 类型
     * @return y:true/n:false
     */
    public static boolean isMapSO(java.lang.reflect.Type type)
    {
        return "java.util.Map<java.lang.String, java.lang.Object>".equals(type.toString())
                || "java.util.Map<java.lang.String, java.lang.String>".equals(type.toString());
    }

    /**
     * 判断是否是List&lt;Map&lt;String,Object&gt;&gt;或List&lt;Map&lt;String,String&gt;&gt;
     *
     * @param type 类型
     * @return y:true/n:false
     */
    public static boolean isListMapSO(java.lang.reflect.Type type)
    {
        return "java.util.List<java.util.Map<java.lang.String, java.lang.Object>>".equals(type.toString())
                || "java.util.List<java.util.Map<java.lang.String, java.lang.String>>".equals(type.toString());
    }

    public static <B> List<B> listMap2ListBean(List<Map<String, Object>> maps, Class<B> b)
    {
        List<B> bs = new ArrayList<>();
        maps.forEach(map -> bs.add((B) TypeUtil.map2Obj(b, map)));
        return bs;
    }

    /**
     * 查找出标识有注解的参数的具体值,没有找到返回null
     *
     * @param clazz      注解class
     * @param parameters 方法的参数类型列表
     * @param args       该方的具体参数
     * @return 值
     */
    public static Object findAnnotationParameterVal(Class<? extends Annotation> clazz, Parameter[] parameters, Object... args)
    {
        for (int i = 0; i < parameters.length; i++)
        {
            if (parameters[i].getAnnotation(clazz) != null)
            {
                return args[i];
            }
        }
        return null;
    }

    /**
     * 标识有PageIndex注解的参数的具体的实参.
     *
     * @param parameters 类型集合
     * @param args       实参
     * @return 位置索引
     */
    public static int findPageIndex(Parameter[] parameters, Object... args)
    {
        Object obj = findAnnotationParameterVal(PageIndex.class, parameters, args);
        return obj != null ? (int) obj : -1;
    }

    /**
     * 标识有PageSize注解的参数的具体的实参.
     *
     * @param parameters 参数类型
     * @param args       参数值
     * @return 位置索引
     */
    public static int findPageSize(Parameter[] parameters, Object... args)
    {
        Object obj = findAnnotationParameterVal(PageSize.class, parameters, args);
        return obj != null ? (int) obj : -1;
    }

    /**
     * 去除 str 的首尾空白, 如果还存在空白, 就把第1个空白前面的部分删除掉(同时也删除第一个空白). 返回删除之后所留下的字符串, 注意返回之前都会trim<br>
     * 特别注意: 空白: 包行tab或空格... <br>
     * 例如: str = "a b c" 则返回 "b c" <br>
     * 如果传递null, 则返回null, 如果没有什么可删除的,则原样返回.
     *
     * @param str 待处理字符串
     * @return 处理之后的字符串
     */
    private static String removePart(String str)
    {
        if (str == null)
        {
            return null;
        }
        else
        {
            // 为了方便处理先把首尾空白去掉,把省下的空白换成空格
            str = RegExUtils.replaceAll(str.trim(), RegexCache.MOR_BLANK_PATT, StringUtils.SPACE);
            StringBuilder sb = new StringBuilder(str);
            int i = sb.indexOf(StringUtils.SPACE); // 查找第一个空格
            if (i != -1)
            { // 如果还存在空格, 就把第1个空格前面的部分删除掉
                sb.delete(0, i + 1);
            }

            return sb.toString().trim();
        }
    }

    /**
     * 如果在str中,"where"的下一个单词如果是"or"或者"and",那么就删除(忽略大小写)
     *
     * @param str 待处理的字符串
     * @return 处理之后的字符串
     */
    public static String parWhere(String str)
    { // 不可能传递null进来

        // <where> 不存在大小写问题,因为在初始化阶段已经严格要求<where>,</where> 只能是小写
        final String openWhere = "<where>";
        final String closeWhere = "</where>";
        String[] wheres = StringUtils.substringsBetween(str, openWhere, closeWhere);
        if (wheres != null)
        {
            for (String where : wheres)
            {

                // sorce 不会受 where 的变化的变化
                String sorce = where; // 把值copy一份
                Pattern p1 = RegexCache.getPattern("(?i)^where\\b");
                Pattern p2 = RegexCache.getPattern("(?i)^or\\b");
                Pattern p3 = RegexCache.getPattern("(?i)^and\\b");
                where = RegExUtils.replaceFirst(where.trim(), p1, StringUtils.EMPTY);
                // 如果第一个单词是"or"或者and,则去掉
                where = RegExUtils.replaceFirst(where.trim(), p2, StringUtils.EMPTY);
                where = RegExUtils.replaceFirst(where.trim(), p3, StringUtils.EMPTY);
                where = where.trim();

                Pattern p4 = RegexCache.getPattern(Pattern.quote(openWhere + sorce + closeWhere));
                // 注意: 这里用quote是因为 sorce 很可能会包含有正则符号
                if (StringUtils.EMPTY.equals(where))
                {
                    str = RegExUtils.replaceFirst(str,p4,StringUtils.EMPTY);
                }
                else
                {
                    str = RegExUtils.replaceFirst(str,p4,Matcher.quoteReplacement("where " + where));
                }
            }
        }

        return str;
    }

    /**
     * 先去除首尾空被,获取第一个空白前面的字符串
     *
     * @param str 待处理的字符串
     * @return 处理之后的字符串
     */
    public static String getFirstWord(String str)
    {
        if (str == null)
        {
            return null;
        }
        else
        {
            String word = RegExUtils.replaceAll(str.trim(), RegexCache.MOR_BLANK_PATT, StringUtils.SPACE);
            int index = word.indexOf(' ');
            if (index == -1)
            {
                return str;
            }
            else
            {
                return word.substring(0, index);
            }
        }
    }

    public static boolean isWarrp(Class<?> ct)
    {
        if (ct == null)
        {
            return false;
        }
        else
        {
            return ct == String.class || ct == Byte.class || ct == Short.class || ct == Integer.class || ct == Long.class || ct == Float.class
                    || ct == Double.class || ct == Character.class || ct == Boolean.class || Enum.class.isAssignableFrom(ct) || EnumSet.class == ct || ct == JSONObject.class || ct == JSONArray.class;
        }
    }

    /**
     * 获取返回值map泛型的value的类型
     *
     * @param method 方法
     * @return clazz
     */
    public static Class<?> mapValueTyep(MethodInfo method)
    {
        ParameterizedType type = (ParameterizedType) method.getGenericReturnType();
        return (Class<?>) type.getActualTypeArguments()[1];
    }

    /**
     * 获取返回值listmap泛型的value的类型
     *
     * @param method 方法
     * @return clazz
     */
    public static Class<?> listMapValueTyep(MethodInfo method)
    {
        ParameterizedType type = (ParameterizedType) method.getGenericReturnType();
        type = (ParameterizedType) type.getActualTypeArguments()[0];
        return (Class<?>) type.getActualTypeArguments()[1];
    }

    /**
     * 将一个数组类型转换成Object[]. 注意: 如果传递的不是一个数组,那么抛出{@link ClassCastException}异常
     *
     * @param array 数组
     * @return 数组
     */
    public static List<Object> toList(Object array)
    {
        Objects.requireNonNull(array);
        if (array.getClass().isArray())
        {
            int len = Array.getLength(array);
            List<Object> objs = new ArrayList<>();
            for (int i = 0; i < len; i++)
            {
                objs.add(Array.get(array, i));
            }
            return objs;
        }
        else
        {
            throw new ClassCastException("你传递的不是一个数组");
        }
    }

    public static EnumSet toEnumSet(String value, Class<Enum> clazz)
    {
        if (value == null)
        {
            return null;
        }
        else
        {
            List<Enum> enums = new ArrayList<>();
            String[] names = StringUtils.split(value, ',');
            for (String name : names)
            {
                Enum e = EnumUtils.getEnum(clazz, name);
                Objects.requireNonNull(e, name + " 转换成 " + clazz + " 失败！");
                enums.add(e);
            }
            if (enums.isEmpty())
            {
                return EnumSet.noneOf(clazz);
            }
            else
            {
                return EnumSet.copyOf(enums);
            }
        }
    }

    public static Object map2Obj(Class<?> beanType, Map<String, Object> map)
    {
        // xxx
        // 1. 从 beanType 中找出 EnumSet<Ruits> 属性,及对应的枚举类型
        Map<String, Type> enumSetFeilds = new HashMap<>();
        Field[] fields = beanType.getDeclaredFields();
        for (Field field : fields)
        {
            String name = field.getName();
            if (field.getType() == EnumSet.class && field.getGenericType() instanceof ParameterizedType)
            {
                ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
                if (parameterizedType.getActualTypeArguments()[0].getClass() == Class.class)
                {
                    enumSetFeilds.put(name, parameterizedType.getActualTypeArguments()[0]);
                }
                else
                {
                    throw new RepositoryException("禁止出现EnumSet<?通配符>");
                }
            }

            Convert convert = field.getAnnotation(Convert.class);
            if(convert != null)
            {
                Class<? extends AttributeConverter> clazz = convert.value();
                AttributeConverter attributeConverter;
                try
                {
                    attributeConverter = clazz.getDeclaredConstructor().newInstance();
                }
                catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                       NoSuchMethodException e)
                {
                    throw new RepositoryException(e);
                }

                Object o = attributeConverter.toEntityField(map.get(name));
                map.put(name, o);
            }
        }

        // 2. 从 map 取出 EnumSet 本来要对应的字符串
        Map<String, String> enumSetValues = new HashMap<>();
        enumSetFeilds.keySet().forEach(f -> {
            String val = (String) map.get(f);
            if (val != null)
            {
                enumSetValues.put(f, val);
                map.put(f, null);
            }
        });

        // 3. 实现反序列化
        Object obj = JSON.toJavaObject(new JSONObject(map), beanType);
        enumSetValues.forEach((k, v) -> {
            List<Enum> enums = new ArrayList<>();
            String[] names = StringUtils.split(v, ',');
            for (String name : names)
            {
                Enum e = EnumUtils.getEnum((Class<Enum>) enumSetFeilds.get(k), name);
                Objects.requireNonNull(e, "不能为 null");
                enums.add(e);
            }
            EnumSet enumSet;
            if (enums.isEmpty())
            {
                enumSet = EnumSet.noneOf((Class<Enum>) enumSetFeilds.get(k));
            }
            else
            {
                enumSet = EnumSet.copyOf(enums);
            }
            getFieldAndSetVal(beanType, obj, k, enumSet);
        });
        // xxx end
        return obj;
    }

    public static <E extends Enum<E>> Object enumSet2Val(Object obj)
    {
        if (obj instanceof EnumSet)
        {
            @SuppressWarnings("unchecked")
            EnumSet<E> enumSet = (EnumSet<E>) obj;
            Iterator<E> iterator = enumSet.iterator();
            StringBuilder sb = new StringBuilder();
            while (iterator.hasNext())
            {
                sb.append(iterator.next().name());
                sb.append(',');
            }
            int len = sb.length();
            if (len > 1)
            {
                sb.deleteCharAt(len - 1);
            }
            return sb.toString();
        }
        else
        {
            return obj;
        }
    }

    public static String repeatChar(int size, char x)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++)
        {
            sb.append(x);
            sb.append(',');
        }
        if (size > 0)
        {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    public static <B> Object getFieldVal(B bean, Field field)
    {
        Object val;
        try
        {
            field.setAccessible(true);
            val = field.get(bean);
        }
        catch (IllegalAccessException | IllegalArgumentException e)
        {
            throw new RepositoryException(e);
        }
        return val;
    }

    public static void getFieldAndSetVal(Class<?> beanType, Object bean, String fieldName, Object val)
    {
        try
        {
            Field field = beanType.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(bean, val);
        }
        catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException e)
        {
            throw new RepositoryException(e);
        }
    }
}
