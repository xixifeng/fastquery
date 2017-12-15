/*
 * Copyright (c) 2016-2017, fastquery.org and/or its affiliates. All rights reserved.
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
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.fastquery.core.Id;
import org.fastquery.core.Param;
import org.fastquery.core.Placeholder;
import org.fastquery.core.Query;
import org.fastquery.core.QueryByNamed;
import org.fastquery.core.Repository;
import org.fastquery.core.RepositoryException;
import org.fastquery.mapper.QueryPool;
import org.fastquery.page.PageIndex;
import org.fastquery.page.PageSize;
import org.fastquery.struct.ParamMap;
import org.fastquery.where.Condition;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class TypeUtil implements Opcodes{

	private static final Logger LOG = LoggerFactory.getLogger(TypeUtil.class);
	
	private TypeUtil(){}
	
	/**
	 * 根据基本类型标识符取出类型信息<br>
	 * [0]: 基本类型所对应的包转类型; [1]:解包方法 [3]:默认构造方法 [4]:asm操作码.
	 * 
	 * @param d 允许的字符串"I","Z","B","C","S","D","F","J" <br>
	 * "I" int  Type.getType(int.class).getDescriptor() <br> 
	 * "S" short <br>
	 * "J" long <br>
	 * "D" double <br>
	 * "F" float <br>
	 * "Z" boolean <br>
	 * "B" byte <br>
	 * "C" char <br>
	 * @return 类型信息
	 */
	public static Object[] getTypeInfo(String d){
		Object[] strs = new Object[4];
	     if ("I".equals(d)) {
	    	 strs[0] = "java/lang/Integer"; 
	    	 strs[1] = "intValue"; // 用于解包的方法
	    	 strs[2] = "()I";     // 默认构造方法
	    	 strs[3] = IRETURN;   // 返回类型
	    	 return strs;
         } else if ("Z".equals(d)) {
	    	 strs[0] = "java/lang/Boolean";
	    	 strs[1] = "booleanValue";
	    	 strs[2] = "()Z";
	    	 strs[3] = IRETURN;
	    	 return strs;
         } else if ("B".equals(d)) {
	    	 strs[0] = "java/lang/Byte";
	    	 strs[1] = "byteValue";
	    	 strs[2] = "()B";
	    	 strs[3] = IRETURN;
	    	 return strs;
         } else if ("C".equals(d)) {
	    	 strs[0] = "java/lang/Character";
	    	 strs[1] = "charValue";
	    	 strs[2] = "()C";
	    	 strs[3] = IRETURN;
	    	 return strs;
         } else if ("S".equals(d)) {
	    	 strs[0] = "java/lang/Short";
	    	 strs[1] = "shortValue";
	    	 strs[2] = "()S";
	    	 strs[3] = IRETURN;
	    	 return strs;
         } else if ("D".equals(d)) {
	    	 strs[0] = "java/lang/Double";
	    	 strs[1] = "doubleValue";
	    	 strs[2] = "()D";
	    	 strs[3] = DRETURN;
	    	 return strs;
         } else if ("F".equals(d)) {
	    	 strs[0] = "java/lang/Float";
	    	 strs[1] = "floatValue";
	    	 strs[2] = "()F";
	    	 strs[3] = FRETURN;
	    	 return strs;
         } else  if ("J".equals(d)){
	    	 strs[0] = "java/lang/Long";
	    	 strs[1] = "longValue";
	    	 strs[2] = "()J";
	    	 strs[3] = LRETURN;
	    	 return strs;
         } else {
        	 return null;
         }
	}
	
	
	/**
	 * 从 clazz类中搜索具备有methodDescriptor属性,并且name为methodName的Method
	 * @param clazz 类
	 * @param methodName 方法名称
	 * @param methodDescriptor asm方法描述
	 * @return 匹配的方法, 如果没有匹配到抛出异常 {@link RuntimeException}
	 */
	public static Method getMethod(Class<?> clazz,String methodName,String methodDescriptor){
		Method[] methods = clazz.getMethods();
		for (Method method : methods) {
			if (method.getName().equals(methodName) && methodDescriptor.equals(Type.getType(method).getDescriptor())) {
				return method;
			}
		}
		throw new RepositoryException("致命错误,没有找到方法!");
	}
	
	
	//给定一个"正则匹配"匹配在另一个字符串,把匹配上的字符串存入一个数组里. 这样一来即可以用,又可以统计出现次数!
	public static List<String> matches(String str,String regex) {
		List<String> empty = new ArrayList<>();
		if(str==null) {
			return empty;
		}
		return matcheAll(regex, str, empty);
	}
	
	
	// 集合中没有重复
	public static Set<String> matchesNotrepeat(String str,String regex) {
		Set<String> empty = new HashSet<>();
		if(str==null) {
			return empty;
		}
		return matcheAll(regex, str, empty);
	}
	
	
	@SuppressWarnings("unchecked")
	public static <E> E matcheAll(String regex,String str,Collection<String> collection){
		//将给定的正则表达式编译到模式中。
	    Pattern p = Pattern.compile(regex);
	    //创建匹配给定输入与此模式的匹配器。
	    Matcher m = p.matcher(str);
	    String val;    
	    //尝试查找与该模式匹配的输入序列的下一个子序列。
	    while (m.find()){
	      //返回由以前匹配操作所匹配的输入子序列。
	      val = m.group();
	      collection.add(val);
	    }
	    return  (E) collection;
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
	 * @param sql sql语句
	 * @return sql
	 */
	public static int[] getSQLParameter(String sql) {
		List<String> subs = matches(sql, "\\?\\d+");
		int len = subs.size();
		int[] ints = new int[len];
		for (int i = 0; i < len; i++) {
			ints[i] = Integer.parseInt(subs.get(i).replace("?",""));
		}
		return ints;
	}
	
	/**
	 * 为描述方便: 假设返回的值是 arrs
	 * 
	 * arrs[0]: 返回sql中的"?"需要变化的地方.例如:若返回Map&lt;Integer, Integer&gt; -&gt; {1=3,5=2} <br>
     * 表示sql中的第1个(从0开始计数)"?"号,应该调整3个"?"号,问号彼此用","号隔开 <br>
     * 完成上次替换之后的新sql中的第5个(从0开始计数)"?"号,应该调整2个"?"号,问号彼此用","号隔开 <br>
     * 强调: 完成上次替换之后的新sql,这句话要理解! <br>
     * 
	 * arrs[1]: 返回sql参数的值.例如: 若返回List-&gt;{1,3.3,"aa"} <br>
	 * 表示: sql中的第1个参数的值对应1 <br>
     * 表示: sql中的第2个参数的值对应3.3 <br>
     * 表示: sql中的第3个参数的值对应"aa" <br>
	 * 
	 * @param indexMap 标记着,sql中的参数,对应着哪个方法参数 <br>
	 * 例如: int[4]={2,2,3,1,4} <br>
	 * 表示: sql中的第1个参数 对应方法的第2个参数 <br>
	 * sql中的第2个参数 对应方法的第2个参数 <br>
	 * sql中的第3个参数 对应方法的第3个参数 <br>
	 * sql中的第4个参数 对应方法的第1个参数 <br>
	 * sql中的第5个参数 对应方法的第4个参数 <br>
	 * 
	 * 请特别注意: sql参数与方法参数的对应,并不代表就是值的对应. <br>
	 * 
	 * @param args 方法参数的具体值,如: args[0] 表示第一个方法参数的值
	 * 
	 * @return sql处理信号集
	 */
	public static ParamMap getParamMap(int[] indexMap,Object[] args) {
		Map<Integer, Integer> rps = new HashMap<>();
		List<Object> objs = new ArrayList<>();
		int increment = 0;
		for (int i = 0; i < indexMap.length; i++) {
			// 取出sql参数所对应的方法参数
			Object mp = args[indexMap[i]-1]; // 这个值有可能是null
			if(mp==null) {
				objs.add(mp);
				continue;
			}
			Class<?> mpClazz = mp.getClass();
			int count;
			if(mp instanceof Iterable) {
				@SuppressWarnings("unchecked")
				Iterable<Object> iterable =  (Iterable<Object>) mp;
				Iterator<Object> iterator =  iterable.iterator();
				count = 0;
				while (iterator.hasNext()) {
					++count;
					objs.add(iterator.next());
				}
				if(count!=0) {
					rps.put(i + increment, count); // 第i个"?"需要替换成count个"?"
					increment += count-1;	// 增量个数(除开本身一个)
				}
			} else if(mpClazz.isArray()) {
				// asList 是一个可变变量
				List<Object> arrs = toList(mp);
				arrs.forEach(objs::add);
				count = arrs.size();
				if(count!=0) {
					rps.put(i + increment, count);
					increment += count-1;	
				}
			} else {
				objs.add(mp);
			}
		}
		
		return new ParamMap(rps, objs);
		
	}
	
	public static String overChar(int overlap) {
		if(overlap<1) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < overlap; i++) {
			sb.append("?,");
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}
	/**
	 * 将第index个'?'换成overlap个'?','?'号之间用逗号(,)隔开
	 * @param str 被处理的字符串
	 * @param index 索引
	 * @param overlap 个数
	 * @return 处理后的字符串
	 */
	public static String replace(String str,int index,int overlap) {
		if(str == null || index < 0 || overlap < 1) 
		   return "";
		StringBuilder sb = new StringBuilder(str);
		int arisen = 0; // 记录"?"是第几次出现
		int len = sb.length();
		for (int i = 0; i < len; i++) { // 在这里用len 而不是 sb.length(),那是因为sb一但被改变了,这个for循环就会结束,因此用len是没问题的
			if(sb.charAt(i) == '?' && index == arisen++) {
				sb.replace(i, i + 1, overChar(overlap));
				break;
			}
		}
		return sb.toString();
	}
	
	/**
	 * 判断在str字符串中,在不区分大小写的前提下是否包含有给定的word
	 * @param str 目标字符串
	 * @param word 待差找的单词
	 * @return 存在:true,反之,false
	 */
	public static boolean containsIgnoreCase(String str,String word) {
	
	    //将给定的正则表达式编译到模式中。
		// 在不区分大小写的前提下,匹配是否包含有单词 word
		// 注意:单词的分隔不仅是空格,例如: hello{good<yes(ok)jetty 123abc  包含有单词 hello good yes ok 等等. (已验证)
	    Pattern p = Pattern.compile("(?i)\\b"+word+"\\b");
	    
	    //创建匹配给定输入与此模式的匹配器。
	    Matcher m = p.matcher(str);
	    
	    //尝试查找与该模式匹配的输入序列的下一个子序列。
	    while (m.find()){
	      //m.group() 表示返回由以前匹配操作所匹配的输入子序列。
	      if(m.group().equalsIgnoreCase(word)) {
	    	  return true;
	      }
	    }
	    
	    return false;
	}
	
	/**
	 * 检查对象是否存在默认构造函数
	 * @param clazz 待检查的class
	 * @return 有:true, 无:false
	 * @throws SecurityException 安全异常
	 */
	public static boolean hasDefaultConstructor(Class<?> clazz) {
		if(clazz == null) {
			return false;
		}
	        try {
				clazz.getConstructor();
			}catch (Exception e) {
				LOG.debug(clazz + ": 该类没有一个默认的构造方法.");
				return false;
			}
	    return true;
	}
	
	/**
	 * 从给定的参数列表中,搜寻出是第几个参数带有指定的注解.注意:从0开始计数
	 * @param clazz 需要查询的注解
	 * @param parameters 被查的参数列表
	 * @return 第几个
	 */
	public static int findAnnotationIndex(Class<? extends Annotation> clazz,Parameter[] parameters){
		for (int i = 0; i < parameters.length; i++) {
			if(parameters[i].getAnnotation(clazz)!=null){
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * 查询标识有指定注解的参数
	 * @param clazz 待差找的类型
	 * @param parameters 参数类型集
	 * @return 参数类型
	 */
	public static Parameter findParameter(Class<? extends Annotation> clazz,Parameter[] parameters){
		for (Parameter parameter : parameters) {
			if(parameter.getAnnotation(clazz)!=null){
				return parameter;
			}
		}
		return null;
	}
	
	/**
	 * parameters 中是否存在 clazz,存在就返回true,反之返回false
	 * @param clazz 待差找的类型
	 * @param parameters 参数类型
	 * @return 有返回true,无:false
	 */
	public static boolean hasType(Class<?> clazz,Parameter[] parameters){
		for (Parameter parameter : parameters) {
			if(parameter.getType() == clazz) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 统计一个注解在参数列表中出现的次数
	 * @param clazz 注解的class
	 * @param parameters 参数类型
	 * @return 出现次数
	 */
	public static int countRepeated(Class<? extends Annotation> clazz,Parameter[] parameters) {
		int count = 0;
		for (Parameter parameter : parameters) {
			if(parameter.getAnnotation(clazz) != null){
				count += 1;
			}
		}
		return count;
	}
	
	/**
	 * 从给定的参数列表中,搜寻出是第几个参数带有@Id注解.注意:从0开始计数
	 * @param parameters 参数类型
	 * @return 若返回 -1 表示没有找到
	 */
	public static int findId(Parameter[] parameters) {
		return findAnnotationIndex(Id.class, parameters);
	}

	/**
	 * 处理 @Param 模板参数
	 * 
	 * @param method 方法
	 * @param args 给这个方法传递的参数值
	 * @param sql sql语句
	 * @return sql
	 */
	private static String paramFilter(Method method, Object[] args, String sql) {
		String s = sql;
		// 替换@Param
		Annotation[][] annotations = method.getParameterAnnotations();
		QueryByNamed queryByNamed = method.getAnnotation(QueryByNamed.class);
		int len = annotations.length;
		for (int i = 0; i < len; i++) {
			Annotation[] anns = annotations[i];
			for (Annotation ann : anns) {
				if(ann.annotationType() == Param.class) {
					Param param = (Param) ann;
					Object objx = args[i];
					objx = BeanUtil.parseList(objx);
					// 将 ":xx" 格式的 替换成 "?num"
					// 替换时必须加单词分界符(\\b),举例说明: sql中同时存在":ABCD",":A", 不加单词分界符,":A"替换成"?num"后,会使":ABCD"变成":?numBCD"
					s = s.replaceAll("\\:"+param.value()+"\\b", "?"+(i+1));
					// 这里的replaceAll的先后顺序很重要
					// '{' 是正则语法的关键字,必须转义
					if( queryByNamed == null ) {
						String replacement = objx!=null?objx.toString():Matcher.quoteReplacement(param.defaultVal());
						s = s.replaceAll("\\$\\{"+param.value()+"\\}", replacement);
						s = s.replaceAll("\\$"+param.value()+"\\b", replacement);	
					}
				}
			}
		}
		// 替换@Param End
		return s;
	}
	
	/**
	 * 处理 @Param 模板参数 仅仅只处理把 ":name" 替换成 "?数字" 
	 * @param method 方法
	 * @param args 参数值
	 * @param sql sql语句
	 * @return sql
	 */
	public static String paramNameFilter(Method method, Object[] args, String sql) {
		String s = sql;
		// 替换@Param
		Annotation[][] annotations = method.getParameterAnnotations();
		int len = annotations.length;
		for (int i = 0; i < len; i++) {
			Annotation[] anns = annotations[i];
			for (Annotation ann : anns) {
				if(ann.annotationType() == Param.class) {
					Param param = (Param) ann;
					// Pattern.quote(":"+param.value())
					s = s.replaceAll("\\:"+param.value()+"\\b", "?"+(i+1));
				}
			}
		}
		// 替换@Param End
		return s;
	}
	
	/**
	 * 裁决是否忽略指定的条件,返回true表示要把这个条件忽略掉
	 * @param condition 条件
	 * @return y:true/n:false
	 */
	private static boolean ignoreCondition(Condition condition,Object arg) {
		/*
		if(arg == null) {
			return true;
		}
		
		String[] allows = condition.allow();
		if(allows.length != 0) { // 表明,允许的范围并不是全部,而是有所限定
			boolean a = false;
			for (String allow : allows) {
				if(Pattern.matches(allow, arg.toString())) {
					a = true;
					break;
				}
			}
			if(!a){ // 明确指定了限定允许,而传递的参数从allows中却没有找到一个能匹配上的,那么必然忽略.
				return true;
			}
		}
		
		if(condition.ignoreEmpty() && "".equals(arg.toString())) {
			return true;
		}
		
		String[] ignores = condition.ignore();
		for (String ignore : ignores) {
			if( Pattern.matches(ignore,arg.toString())){
				return true;
			}
		}
		
		return false;
		*/
		
		String[] allows = condition.allow();
		
		if(allows.length != 0) { // 表明,允许的范围并不是全部,而是有所限定
			if(arg==null) { // 范围有明确指定,还传递null,那么必然忽略
				return true;
			}
			// 判断传递的值是否是允许的
			boolean a = false;
			for (String allow : allows) {
				// 因为注解的特性 allows的集合中的成员永远不可能出现null
				if(Pattern.matches(allow, arg.toString())) {
					a = true;
					break;
				}
			}
			// 判断传递的值是否是允许的 End
			if(!a){ // 传递的值不在允许范围之内,那么必然忽略条件
				return true;
			}
		}
		
		if(condition.ignoreNull() && arg==null) { // 允许null就忽略条件,正好arg==null
			return true;
		}
		
		if(arg==null) { // arg == null 而且 ignoreNull是false,那么表明arg即使是null也不忽略
			return false;
		}
		
		if(condition.ignoreEmpty() && "".equals(arg.toString())) {
			return true;
		}
		
		String[] ignores = condition.ignore();
		for (String ignore : ignores) {
			if( Pattern.matches(ignore,arg.toString())){
				return true;
			}
		}
		
		return false;
		
	}
	
	/**
	 * 考虑条件是否参与运算问题.
	 * @param method 方法
	 * @param args 参数集
	 * @return where部分sql
	 */
	public static String getWhereSQL(Method method, Object[] args) {
		StringBuilder sb = new StringBuilder();
		// 追加条件
		Condition[] conditions = method.getAnnotationsByType(Condition.class);
		o: for (int i = 0; i < conditions.length; i++) {
			String value = conditions[i].value();
			value = paramFilter(method, args, value);
			// value 属性中包含的参数,必须去重
			Set<String> pars = TypeUtil.matchesNotrepeat(value, "\\?\\d+");
			for (String par : pars) {
				int index = Integer.parseInt(par.replace("?", "")); // 计数是1开始的
				if (ignoreCondition(conditions[i], args[index - 1])) {
					continue o; // 跳出最外层的当次循环,不进行条件追加
				} else if(args[index - 1] == null) {
					// 如果传递null 还要求参与运算.
					// sql中null无法跟比较运算符(如 =, <, 或者 <>),一起运算,必须使用 is null 和 is not null 操作符. 
					value = value.replaceAll("\\s+", " "); // 把多个空白换成一个空格
					value = value.replaceAll("=\\?", "= ?"); // 将"=?" 替换成 "= ?"  
					value = value.replaceAll(" = \\?" + index," is null");
					value = value.replaceAll(" <> \\?" + index," is not null");
					value = value.replaceAll(" != \\?" + index," is not null");
				}
			}

			int sblen = sb.length();
			// sb的长度是0 或者 最后一个字符就是空格
			if( sblen ==0 || (sblen>=1 && sb.charAt(sblen-1) != ' ')   ) {
				sb.append(' ');	
			}
			if (sb.length() == 1 && i != 0) { // 条件成立,表示这个SQL条件的前面还不存在条件,// 那么第一个条件的链接符,必须去掉. (where后面不能直接跟运算符号)
				sb.append(removePart(value));
			} else {
				sb.append(value);
			}
		}
		// 追加条件 End
		if (!"".equals(sb.toString())) { // 特别注意: 此处不能写成 !sb.equals("")
			sb.insert(0, "where");
		}
		return sb.toString();
	}
	

	/**
	 * 获取完整的SQL语句
	 * @param method 方法
	 * @param queries 注解集
	 * @param args 参数集
	 * @return sql集
	 */
	public static List<String> getQuerySQL(Method method,Query[] queries,Object[] args) {
		List<String> sqls = new ArrayList<>();
		
		// 如果是QueryByNamed
		if(method.getAnnotation(QueryByNamed.class)!=null){
			String s = QueryPool.render(true);
			s = paramFilter(method, args, s);
			sqls.add(s);
			return sqls;
		}
		
		for (Query query : queries) {
			String sql = query.value();
			
			sql = paramFilter(method, args, sql);
			
			sqls.add(sql.replaceFirst(Placeholder.WHERE_REG, Matcher.quoteReplacement(getWhereSQL(method, args))));
		}
		return sqls;
	}

	public static String getCountQuerySQL(Method method, String sql, Object[] args) {
		String csql = sql.replaceFirst(Placeholder.WHERE_REG, Matcher.quoteReplacement(getWhereSQL(method, args)));
		LOG.info("求和:" + csql);
		return csql;
	}
	
	/**
	 * 过滤java语法中的注释
	 * @param str 等待过滤的字符串
	 * @return 处理之后的字符串
	 */
	public static String filterComments(String str){
		// 过滤 // 
		String s = str.replaceAll("//(.)+\\n", "");
		s = s.replaceAll("//(.)?\\n", "");
		// 过滤多行注释
		s = s.replaceAll("\\/\\*[\\s\\S]*?\\*\\/", "");
		return s;
	}
	
	/**
	 * 过滤SQL语法中的注释
	 * @param str 等待过滤的字符串
	 * @return 处理之后的字符串
	 */
	public static String filterSQLComments(String str){
		// 过滤 // 
		String s = str.replaceAll("--(.)+\\n", "");
		s = s.replaceAll("--(.)?\\n", "");
		// 过滤多行注释
		s = s.replaceAll("\\/\\*[\\s\\S]*?\\*\\/", "");
		return s;
	}
	
	/**
	 * 判断类型是否是Map&lt;String,Object&gt;或Map&lt;String,String&gt;
	 * @param type 类型
	 * @return y:true/n:false
	 */
	public static boolean isMapSO(java.lang.reflect.Type type){
		if(type==null){
			return false;
		}
		return "java.util.Map<java.lang.String, java.lang.Object>".equals(type.toString()) || "java.util.Map<java.lang.String, java.lang.String>".equals(type.toString());
	}
	/**
	 * 判断是否是List&lt;Map&lt;String,Object&gt;&gt;或List&lt;Map&lt;String,String&gt;&gt;
	 * @param type 类型
	 * @return y:true/n:false
	 */
	public static boolean isListMapSO(java.lang.reflect.Type type) {
		if(type==null){
			return false;
		}
		return "java.util.List<java.util.Map<java.lang.String, java.lang.Object>>".equals(type.toString()) || "java.util.List<java.util.Map<java.lang.String, java.lang.String>>".equals(type.toString());
	}
	
	// 假设: 有两个类,其class分别为c1和c2. c1的直接父类的范型为X
	//  设: X=<T>
	//  若: (c2就是T 或者 c2是T的子类) 并且T是Repository的子类或者就是Repository. 并且X限制只有一个成员.("<>"中是有可能有多个参数的)
	//  则: 返回true,反之返回false.
	public static boolean compareType(Class<?> c1,Class<?> c2){
		java.lang.reflect.Type type = c1.getGenericSuperclass(); // 获取c1的直接父类的范型
		// 如果这个type的实例就是ParameterizedType的子类或就是ParameterizedType
		if(ParameterizedType.class.isAssignableFrom(type.getClass())) { // 判断是否是范型
			ParameterizedType parameterizedType = (ParameterizedType) type; // 如果是范型就转换
			java.lang.reflect.Type[] tys = parameterizedType.getActualTypeArguments(); // 范型中有多个类型. 换言只尖括号"<>"中有多个类型

			if(tys.length>1) {
				//当前: X的范型参数个数已大于1
				return false;
			}
			
			java.lang.reflect.Type ty = tys[0];
			
			if(Class.class.isAssignableFrom(ty.getClass())) { // 如果当前类型是Class的子类或者就是Class
				Class<?> t = (Class<?>) ty; 
				if(Repository.class.isAssignableFrom(t)) { // 如果t是Repository的子类或者t就是Repository
					if(!t.isAssignableFrom(c2)) { 
						//当前: c2不是t的子类且不就是t.
						return false;
					}
				} else {
					// 当前: T 不是Repository,且不是Repository的子类
					return false;
				}
			} else {
				// 当前: T 不是Class,且不是Class的子类
				return false;
			}
			
		} else {
			//当前: c1的直接父类不是范型
			return false;
		}
		
		
		// 如果以上都不满足
		return true;
	}
	
	public static <B> List<B> listMap2ListBean(List<Map<String, Object>> maps,Class<B> b){
		List<B> bs = new ArrayList<>();
		maps.forEach( map -> bs.add( JSON.toJavaObject(new JSONObject(map), b) ) );
		return bs;
	}
	
	public static List<String> tokenizeToStringArray(String str, String delimiters) {
		if (str == null) {
			return new ArrayList<>();
		}
		
		StringTokenizer st = new StringTokenizer(str, delimiters);
		List<String> tokens = new ArrayList<>();
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
				token = token.trim();
			if (token.length() > 0) {
				tokens.add(token);
			}
		}
		return tokens;
	}
	
	/**
	 * 查找出标识有注解的参数的具体值,没有找到返回null
	 * @param clazz 注解class
	 * @param parameters 方法的参数类型列表
	 * @param args 该方的具体参数
	 * @return 值
	 */
	public static Object findAnnotationParameterVal(Class<? extends Annotation> clazz,Parameter[] parameters,Object...args){
		for (int i = 0; i < parameters.length; i++) {
			if(parameters[i].getAnnotation(clazz) != null){
				return args[i];
			}
		}
		return null;
	}
	
	/**
	 * 标识有PageIndex注解的参数的具体的实参.
	 * @param parameters 类型集合
	 * @param args 实参
	 * @return 位置索引
	 */
	public static int findPageIndex(Parameter[] parameters,Object...args){
		Object obj = findAnnotationParameterVal(PageIndex.class, parameters, args);
		return obj !=null ? (int)obj : -1;
	}

	/**
	 * 标识有PageSize注解的参数的具体的实参.
	 * @param parameters 参数类型
	 * @param args 参数值
	 * @return 位置索引
	 */
	public static int findPageSize(Parameter[] parameters,Object...args){
		Object obj = findAnnotationParameterVal(PageSize.class, parameters, args);
		return obj !=null ? (int)obj : -1;
	}
	
	/**
	 * 去除 str 的首尾空白, 如果还存在空白, 就把第1个空白前面的部分删除掉(同时也删除第一个空白). 返回删除之后所留下的字符串, 注意返回之前都会trim<br>
	 * 特别注意: 空白: 包行tab或空格... <br>
	 * 例如: str = "a b c" 则返回 "b c" <br>
	 * 如果传递null, 则返回null, 如果没有什么可删除的,则原样返回.
	 * @param str 待处理字符串
	 * @return 处理之后的字符串
	 */
	public static String removePart(String str){
		if(str==null) {
			return null;
		}
		// 为了方便处理先把首尾空白去掉,把省下的空白换成空格
		StringBuilder sb = new StringBuilder(str.trim().replaceAll("\\s+", " "));
		int i = sb.indexOf(" "); // 查找第一个空格
		if(i != -1){ // 如果还存在空格, 就把第1个空格前面的部分删除掉
			sb.delete(0, i+1);
		} 
		
		return sb.toString().trim();
	}
	
	/**
	 * 如果在str中,"where"的下一个单词如果是"or"或者"and",那么就删除(忽略大小写)
	 * @param str 待处理的字符串
	 * @return 处理之后的字符串
	 */
	public static String parWhere(String str) { // 不可能传递null进来
		
		// <where> 不存在大小写问题,因为在初始化阶段已经严格要求<where>,</where> 只能是小写
		String[] wheres = StringUtils.substringsBetween(str, "<where>", "</where>");
		if(wheres==null) {
			return str;
		}
		
		String s = str;
		for (String where : wheres) {
			
			// sorce 不会受 where 的变化的变化
			String sorce = where; // 把值copy一份
			
			where = where.trim().replaceFirst("(?i)^where\\b","");
			// 如果第一个单词是"or"或者and,则去掉
			where = where.trim().replaceFirst("(?i)^or\\b", "");
			where = where.trim().replaceFirst("(?i)^and\\b", "");
			where = where.trim();
			
			// 注意: 这里用quote是因为 sorce 很可能会包含有正则符号
			if("".equals(where)){
				s = s.replaceFirst(Pattern.quote("<where>"+sorce+"</where>"),"");
			} else {
				s = s.replaceFirst(Pattern.quote("<where>"+sorce+"</where>"),Matcher.quoteReplacement("where " + where));
			}
		}
		return s;
	}

	/**
	 * 先去除首尾空被,获取第一个空白前面的字符串
	 * @param str 待处理的字符串
	 * @return 处理之后的字符串
	 */
	public static String getFirstWord(String str){
		if(str==null) {
			return null;
		}
		String word = str.trim().replaceAll("\\s+", " ");
		int index = word.indexOf(' ');
		if(index==-1) {
			return str;
		}
		return word.substring(0, index);
	}
	
	/**
	 * 判断传入的ct是否是String,Byte,Short,Integer,Long,Float,Double,Character,Boolean其中的一个.如果是,返回true
	 * @param ct 类型
	 * @return 是:true,否:false
	 */
	public static boolean isWarrp(java.lang.reflect.Type ct) {
		if(ct == null){
			return false;
		}
		return ct == String.class || ct == Byte.class || ct == Short.class || ct == Integer.class || ct == Long.class || ct == Float.class
				|| ct == Double.class || ct == Character.class || ct == Boolean.class;
	}
	
	/**
	 * 获取返回值map泛型的value的类型
	 * @param method 方法
	 * @return clazz
	 */
	public static Class<?> mapValueTyep(Method method) {
		ParameterizedType type = (ParameterizedType) method.getGenericReturnType();
		return (Class<?>) type.getActualTypeArguments()[1];
	}
	
	/**
	 * 获取返回值listmap泛型的value的类型
	 * @param method 方法
	 * @return clazz
	 */
	public static Class<?> listMapValueTyep(Method method) {
		ParameterizedType type = (ParameterizedType) method.getGenericReturnType();
		type = (ParameterizedType) type.getActualTypeArguments()[0];
		return (Class<?>) type.getActualTypeArguments()[1];
	}
	
	/**
	 * 将一个数组类型转换成Object[]. 注意: 如果传递的不是一个数组,那么抛出{@link ClassCastException}异常
	 * @param array 数组
	 * @return 数组
	 */
	public static List<Object> toList(Object array) {
		 Objects.requireNonNull(array);
		if(array.getClass().isArray()) {
			int len = Array.getLength(array);
			List<Object> objs = new ArrayList<>();
			for (int i = 0; i < len; i++) {
				objs.add(Array.get(array, i));
			}
			return objs;
		} else {
			throw new ClassCastException("你传递的不是一个数组");
		}
	}
}








