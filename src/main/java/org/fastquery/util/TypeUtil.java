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

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fastquery.core.Id;
import org.fastquery.core.Placeholder;
import org.fastquery.core.Query;
import org.fastquery.where.Condition;
import org.fastquery.where.Operator;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class TypeUtil implements Opcodes{

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
	     if (d.equals("I")) {
	    	 strs[0] = "java/lang/Integer"; 
	    	 strs[1] = "intValue"; // 用于解包的方法
	    	 strs[2] = "()I";     // 默认构造方法
	    	 strs[3] = IRETURN;   // 返回类型
	    	 return strs;
         } else if (d.equals("Z")) {
	    	 strs[0] = "java/lang/Boolean";
	    	 strs[1] = "booleanValue";
	    	 strs[2] = "()Z";
	    	 strs[3] = IRETURN;
	    	 return strs;
         } else if (d.equals("B")) {
	    	 strs[0] = "java/lang/Byte";
	    	 strs[1] = "byteValue";
	    	 strs[2] = "()B";
	    	 strs[3] = IRETURN;
	    	 return strs;
         } else if (d.equals("C")) {
	    	 strs[0] = "java/lang/Character";
	    	 strs[1] = "charValue";
	    	 strs[2] = "()C";
	    	 strs[3] = IRETURN;
	    	 return strs;
         } else if (d.equals("S")) {
	    	 strs[0] = "java/lang/Short";
	    	 strs[1] = "shortValue";
	    	 strs[2] = "()S";
	    	 strs[3] = IRETURN;
	    	 return strs;
         } else if (d.equals("D")) {
	    	 strs[0] = "java/lang/Double";
	    	 strs[1] = "doubleValue";
	    	 strs[2] = "()D";
	    	 strs[3] = DRETURN;
	    	 return strs;
         } else if (d.equals("F")) {
	    	 strs[0] = "java/lang/Float";
	    	 strs[1] = "floatValue";
	    	 strs[2] = "()F";
	    	 strs[3] = FRETURN;
	    	 return strs;
         } else  if (d.equals("J")){
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
		throw new RuntimeException("致命错误,没有找到方法!");
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
	    String val = null;    
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
			ints[i] = Integer.valueOf(subs.get(i).replace("?","")).intValue();
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
		
		/*
		历史写法保留
		if(sql==null || keyword==null) {
			throw new IllegalArgumentException("参数不能为null");
		}
		String custom = ":?#-"; 
		
		sql = sql.trim();
		
		// 将sql中的 "update " 不区分大小写替换成"?#-"
		sql = sql.replaceFirst("(?i)^"+keyword+" ", custom);
		
		// 将sql中的 " update " 不区分大小写全部替换成"?#-"
		sql = sql.replaceAll("(?i) "+keyword+" ", custom);
		
		return sql.contains(custom);
		*/
		
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
			} catch (NoSuchMethodException e) {
				return false;
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    return true;
	}
	
	
	/**
	 * 从给定的参数列表中,搜寻出是第几个参数带有@Id注解.注意:从0开始计数
	 * @param parameters
	 * @return 若返回 -1 表示没有找到
	 */
	public static int findId(Parameter[] parameters) {
		for (int i = 0; i < parameters.length; i++) {
			if(parameters[i].getAnnotation(Id.class)!=null){
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * 获取完整的SQL语句,考虑条件是否参与运算问题.
	 * @param method
	 * @param query
	 * @param args
	 * @return
	 */
	public static String getQuerySQL(Method method,Query query,Object[] args){
		String sql = query.value();
		StringBuilder sb = new StringBuilder();
		// 追加条件
		Condition[] conditions = method.getAnnotationsByType(Condition.class);
		o:for (int i = 0; i < conditions.length; i++) {
			
			if(conditions[i].ignoreNull()){ // 忽略null,那么就要看参数是否传递null啦
				// r 属性中包含的参数,必须去重
				Set<String> pars = TypeUtil.matchesNotrepeat(conditions[i].r(), "\\?\\d+");
				if(pars.size()!=0){ // 表明这个条件有"?"占位符号
					for (String par : pars) {
						int index = Integer.valueOf(par.replace("?", "")); // 计数是1开始的
						if(args[index-1] == null){ //如果传递了参数null, 那么这个条件就不加入
							continue o; // 跳出最外层的当次循环
						}
					}
				}
			}
			sb.append(' ');
			if(sb.length() > 1){ // 第一个条件不能加上条件连接符,请特别注意:此处的条件不能用if(i!=0),因为上面会中途跳出循环.
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
		if(!sb.toString().equals("")){ // 特别注意: 此处不能写成 !sb.equals("")
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
		str = str.replaceAll("//(.)+\\n", "");
		// 过滤多行注释
		str = str.replaceAll("\\/\\*[\\s\\S]*?\\*\\/", "");
		return str;
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
		
		if(type.toString().equals("java.util.Map<java.lang.String, java.lang.Object>")){
			return true;
		} else {
			return false;
		}
		/*if(ParameterizedType.class.isAssignableFrom(type.getClass())){
			ParameterizedType parameterizedType = (ParameterizedType) type;
			java.lang.reflect.Type[] types = parameterizedType.getActualTypeArguments(); // 获取<>中的参数类型
			if ((parameterizedType.getRawType() == Map.class) && (types[0] == String.class)
					&& (types[1] == Object.class)){
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}*/
	}
	
	public static boolean isListMapSO(java.lang.reflect.Type type) {
		if(type==null){
			return false;
		}
		if(type.toString().equals("java.util.List<java.util.Map<java.lang.String, java.lang.Object>>")) {
			return true;
		} else {
			return false;
		}
	}
	
}








