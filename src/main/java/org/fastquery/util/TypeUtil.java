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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.fastquery.core.Id;
import org.fastquery.core.Param;
import org.fastquery.core.Placeholder;
import org.fastquery.core.Query;
import org.fastquery.core.Repository;
import org.fastquery.core.RepositoryException;
import org.fastquery.core.Source;
import org.fastquery.page.PageIndex;
import org.fastquery.page.PageSize;
import org.fastquery.where.Condition;
import org.fastquery.where.Operator;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class TypeUtil implements Opcodes{

	private static final Logger LOG = Logger.getLogger(TypeUtil.class);
	
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
	 * @param clazz 
	 * @param methodName 
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
		return matcheAll(regex, str, new ArrayList<>());
	}
	
	
	// 集合中没有重复
	public static Set<String> matchesNotrepeat(String str,String regex) {
		return matcheAll(regex, str, new HashSet<>());
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
	 * sql参数也称之为是sql占位符叫法不一,不要扣字眼. <br>
	 * 例如: int[4]={2,2,3,1,4} <br>
	 * 表示: sql中的第1个参数 对应方法的第2个参数 <br>
	 * sql中的第2个参数 对应方法的第2个参数 <br>
	 * sql中的第3个参数 对应方法的第3个参数 <br>
	 * sql中的第4个参数 对应方法的第1个参数 <br>
	 * sql中的第5个参数 对应方法的第4个参数 <br>
	 * @param sql
	 * @return
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
	 * 判断在str字符串中,在不区分大小写的前提下是否包含有给定的word
	 * @param str 
	 * @param word 单词
	 * @return
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
	 * @param clazz
	 * @return
	 * @throws SecurityException
	 */
	public static boolean hasDefaultConstructor(Class<?> clazz) {
		if(clazz == null) {
			return false;
		}
	        try {
				clazz.getConstructor();
			}catch (Exception e) {
				LOG.trace(e);
				return false;
			}
	    return true;
	}
	
	/**
	 * 从给定的参数列表中,搜寻出是第几个参数带有指定的注解.注意:从0开始计数
	 * @param annotation 需要查询的注解
	 * @param parameters 被查的参数列表
	 * @return
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
	 * @param clazz
	 * @param parameters
	 * @return
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
	 * @param clazz
	 * @param parameters
	 * @return
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
	 * @param clazz
	 * @param parameters
	 * @return
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
	 * @param parameters
	 * @return 若返回 -1 表示没有找到
	 */
	public static int findId(Parameter[] parameters) {
		return findAnnotationIndex(Id.class, parameters);
	}
	
	public static int findSource(Parameter[] parameters) {
		return findAnnotationIndex(Source.class, parameters);
	}
	
	/**
	 * 获取完整的SQL语句,考虑条件是否参与运算问题.
	 * @param method
	 * @param query
	 * @param args
	 * @return
	 */
	public static List<String> getQuerySQL(Method method,Query[] queries,Object[] args){
		List<String> sqls = new ArrayList<>();
		for (Query query : queries) {
			String sql = query.value();
			
			// 替换@Param
			Annotation[][] annotations = method.getParameterAnnotations();
			int len = annotations.length;
			for (int i = 0; i < len; i++) {
				Annotation[] anns = annotations[i];
				for (Annotation ann : anns) {
					if(ann.annotationType() == Param.class) {
						Param param = (Param) ann;
						Object objx = args[i];
						// '{' 是正则语法的关键字,必须转义
						sql = sql.replaceAll("\\{"+param.value()+"\\}", objx!=null?objx.toString():"");
					}
				}
			}
			// 替换@Param End
			
			StringBuilder sb = new StringBuilder();
			// 追加条件
			Condition[] conditions = method.getAnnotationsByType(Condition.class);
			o:for (int i = 0; i < conditions.length; i++) {
				
				if(conditions[i].ignoreNull()){ // 忽略null,那么就要看参数是否传递null啦
					// r 属性中包含的参数,必须去重
					Set<String> pars = TypeUtil.matchesNotrepeat(conditions[i].r(), "\\?\\d+");
					if(!pars.isEmpty()){ // 表明这个条件有"?"占位符号
						for (String par : pars) {
							int index = Integer.parseInt(par.replace("?", "")); // 计数是1开始的
							if(args[index-1] == null){ //如果传递了参数null, 那么这个条件就不加入
								continue o; // 跳出最外层的当次循环
							}
						}
					}
				}
				sb.append(' ');
				if(sb.length() > 1){ // 第一个条件不能加上条件连接符,请特别注意:此处的条件不能用如果(i!=0),因为上面会中途跳出循环.
					sb.append(conditions[i].c().getVal());	
				}
				sb.append(' ');	
				sb.append(conditions[i].l());
				sb.append(' ');
				Operator[] operators = conditions[i].o();
				for (Operator operator : operators) {
					sb.append(operator.getVal());
					sb.append(' ');
				}
				sb.append(conditions[i].r());
			}
			// 追加条件 End
			if(!"".equals(sb.toString())){ // 特别注意: 此处不能写成 !sb.equals("")
				sb.replace(0, 1, "where");
			}
			sqls.add(sql.replaceFirst(Placeholder.WHERE_REG, sb.toString()));
		}
		return sqls;
	}
	
	
	public static String getCountQuerySQL(Method method,String sql,Object[] args){
			StringBuilder sb = new StringBuilder();
			// 追加条件
			Condition[] conditions = method.getAnnotationsByType(Condition.class);
			o:for (int i = 0; i < conditions.length; i++) {
				
				if(conditions[i].ignoreNull()){ // 忽略null,那么就要看参数是否传递null啦
					// r 属性中包含的参数,必须去重
					Set<String> pars = TypeUtil.matchesNotrepeat(conditions[i].r(), "\\?\\d+");
					if(!pars.isEmpty()){ // 表明这个条件有"?"占位符号
						for (String par : pars) {
							int index = Integer.parseInt(par.replace("?", "")); // 计数是1开始的
							if(args[index-1] == null){ //如果传递了参数null, 那么这个条件就不加入
								continue o; // 跳出最外层的当次循环
							}
						}
					}
				}
				
				sb.append(' ');
				if(sb.length() > 1){ // 第一个条件不能加上条件连接符,请特别注意:此处的条件不能用如果(i!=0),因为上面会中途跳出循环.
					sb.append(conditions[i].c().getVal());	
				}
				sb.append(' ');	
				sb.append(conditions[i].l());
				sb.append(' ');
				Operator[] operators = conditions[i].o();
				for (Operator operator : operators) {
					sb.append(operator.getVal());
					sb.append(' ');
				}
				sb.append(conditions[i].r());
			}
			// 追加条件 End
			if(!"".equals(sb.toString())){ // 特别注意: 此处不能写成 !sb.equals("")
				sb.replace(0, 1, "where");
			}
			
		return sql.replaceFirst(Placeholder.WHERE_REG, sb.toString());
	}
	
	
	/**
	 * 过滤java语法中的注释
	 * @param str 等待过滤的字符串
	 * @return
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
	 * @return
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
	 * 判断类型是否是Map<String,Object>
	 * @param type
	 * @return
	 */
	public static boolean isMapSO(java.lang.reflect.Type type){
		if(type==null){
			return false;
		}
		return "java.util.Map<java.lang.String, java.lang.Object>".equals(type.toString());
	}
	/**
	 * 判断是否是List<Map<String,Object>>
	 * @param type
	 * @return
	 */
	public static boolean isListMapSO(java.lang.reflect.Type type) {
		if(type==null){
			return false;
		}
		return "java.util.List<java.util.Map<java.lang.String, java.lang.Object>>".equals(type.toString());
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
	 * @param parameters 方法的参数类型列表
	 * @param args 该方的具体参数
	 * @return
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
	 * 标识有Source注解的参数的具体的实参.
	 * @param parameters 类型集合
	 * @param args 实参
	 * @return
	 */
	public static String findSource(Parameter[] parameters,Object...args){
		Object obj = findAnnotationParameterVal(Source.class, parameters, args);
		return obj !=null ? obj.toString() : null;
	}
	
	/**
	 * 标识有PageIndex注解的参数的具体的实参.
	 * @param parameters 类型集合
	 * @param args 实参
	 * @return
	 */
	public static int findPageIndex(Parameter[] parameters,Object...args){
		Object obj = findAnnotationParameterVal(PageIndex.class, parameters, args);
		return obj !=null ? (int)obj : null;
	}
	
	/**
	 * 标识有PageSize注解的参数的具体的实参.
	 * @param parameters 类型集合
	 * @param args 实参
	 * @return
	 */
	public static int findPageSize(Parameter[] parameters,Object...args){
		Object obj = findAnnotationParameterVal(PageSize.class, parameters, args);
		return obj !=null ? (int)obj : null;
	}
	
	
}








