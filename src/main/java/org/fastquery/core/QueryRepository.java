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

package org.fastquery.core;

import java.util.List;
import java.util.Map;

import org.fastquery.page.Page;
import org.fastquery.page.Pageable;
import org.fastquery.sql.NativeSpec;

import com.alibaba.fastjson.JSONArray;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public interface QueryRepository extends Repository{

	/**
	 * 构建语句查找
	 * @param spec 构建器
	 * @return
	 */
	@Id(MethodId.QUERY0)
	JSONArray find2JSON(NativeSpec spec);
	
	/**
	 * 构建语句查找
	 * @param spec 构建器
	 * @param dataSourceName 数据源名称
	 * @return
	 */
	@Id(MethodId.QUERY0)
	JSONArray find2JSON(NativeSpec spec,@Source String dataSourceName);
	
	/**
	 * 构建语句查找
	 * @param spec 构建器
	 * @return
	 */
	@Id(MethodId.QUERY0)
	List<Map<String, Object>> find2ListMap(NativeSpec spec);
	
	/**
	 * 查询返回list map格式
	 * @param spec
	 * @param dataSourceName 指定数据源名
	 * @return
	 */
	@Id(MethodId.QUERY0)
	List<Map<String, Object>> find2ListMap(NativeSpec spec,@Source String dataSourceName);
	
	/**
	 * 查询返回list bean格式
	 * @param spec
	 * @param beanType
	 * @return
	 */
	@Id(MethodId.QUERY0)
	<T> List<T> find2Bean(NativeSpec spec,@BeanType Class<?> beanType);
	
	/**
	 * 查询返回list bean格式
	 * @param spec
	 * @param beanType
	 * @param dataSourceName
	 * @return
	 */
	@Id(MethodId.QUERY0)
	<T> List<T> find2Bean(NativeSpec spec,@BeanType Class<?> beanType,@Source String dataSourceName);
	
	/**
	 * 执行SQL文件
	 * @param sqlName 基准目录下的SQL文件名称 注意: 基准目录在fastquery.json里配置
	 * @param output 指定执行SQL后的输出,放在哪个文件里. 注意: 会在基准目录里寻找output
	 * @param dataSourceName 数据源的名称
	 * @return 执行所影响的行数
	 */
	@Id(MethodId.QUERY3)
	void executeBatch(String sqlName,String output,@Source String dataSourceName);
	/**
	 * 执行SQL文件
	 * @param sqlName 基准目录下的SQL文件名称 注意: 基准目录在fastquery.json里配置
	 * @param output 指定执行SQL后的输出将放在哪个文件里. 注意: 会在基准目录里寻找output
	 */
	@Id(MethodId.QUERY3)
	void executeBatch(String sqlName,String output);
	
	/**
	 * 动态构建分页
	 * @param spec 构建器
	 * @param pageable 分页基本属性. 不能为null.
	 * @param countField 求和字段, 若为null,默认值是"id"
	 * @param countsql 自定义求和sql,注意:若传递null,表示采用系统推导出来的求和语句
	 * @param closeCount 关闭count.若为true:表示在分页中不统计总行数.那么分页对象中的totalElements的值为-1L,totalPages为-1.其他属性都有效并且真实.
	 * @param dataSourceName 数据源的名称,传递null,表示采用默认方式适配数据源
	 * @return
	 */
	@Id(MethodId.QUERY7)
	Page<Map<String, Object>> find(NativeSpec spec, Pageable pageable,String countField,String countsql,boolean closeCount,@Source String dataSourceName);

	/**
	 * 动态构建分页
	 * @param spec 构建器
	 * @param pageable 分页基本属性. 不能为null.
	 * @param countField 求和字段, 若为null,默认值是"id"
	 * @param countsql 自定义求和sql,注意:若传递null,表示采用系统推导出来的求和语句
	 * @param closeCount 关闭count.若为true:表示在分页中不统计总行数.那么分页对象中的totalElements的值为-1L,totalPages为-1.其他属性都有效并且真实.
	 * @return
	 */
	@Id(MethodId.QUERY7)
	Page<Map<String, Object>> find(NativeSpec spec, Pageable pageable,String countField,String countsql,boolean closeCount);

	// 如下两个还没有测试待续...
	@Id(MethodId.QUERY7)
	<T> Page<T> find(NativeSpec spec, Pageable pageable,String countField,String countsql,boolean closeCount,@Source String dataSourceName,@BeanType Class<?> beanType);
	@Id(MethodId.QUERY7)
	<T> Page<T> find(NativeSpec spec, Pageable pageable,String countField,String countsql,boolean closeCount,@BeanType Class<?> beanType);
}










