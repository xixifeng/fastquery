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

package org.fastquery.core;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.fastquery.filter.FilterChainHandler;
import org.fastquery.mapper.QueryPool;
import org.fastquery.page.Page;
import org.fastquery.util.FastQueryJSONObject;
import org.fastquery.util.TypeUtil;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class Prepared {
	
	private static final Logger LOG = LoggerFactory.getLogger(Prepared.class);
	
	private Prepared(){}
  
	/**
	 * 执行方法
	 * @param methodName 方法名称
	 * @param methodDescriptor 方法完整描述(asm)
	 * @param args 方法参数 注意: 此处参数列表的成员,永远都是包装类型(已经验证)
	 * @param target 目标 Repository
	 * @return 执行之后的值
	 */
	public static Object excute(String methodName,String methodDescriptor,Object[] args,Repository target) {
		try {
			@SuppressWarnings("unchecked") // 是动态生成的实例,因此它的接口可以很明确就是一个
			Class<? extends Repository> iclazz = (Class<? extends Repository>) target.getClass().getInterfaces()[0];
			Method method = TypeUtil.getMethod(iclazz, methodName,methodDescriptor);
			
	        // 如果是调试模式
	        if(FastQueryJSONObject.getDebug()){
	        	QueryPool.reset(iclazz.getName());
	        }
	        
	        // QueryContext 生命开始
	        QueryContext.start(iclazz, method, args);
	        
			// 在businessProcess的先后加拦截器 ==================
			// 注入BeforeFilter
	        Object object = FilterChainHandler.bindBeforeFilterChain(iclazz,target,method,args);
	        if(object!=void.class){
	                return object;
	        }

	        LOG.info("准备执行方法:" + method);
	        // 取出当前线程中method和args(BeforeFilter 有可能中途修换其他method, 因为过滤器有个功能this.change(..,...) )
	        object = businessProcess();

	        // 注入AfterFilter
	        object = FilterChainHandler.bindAfterFilterChain(iclazz,target,method,args,object); // 注意,这个方法的method,必须是原始的!!!
	        // 在businessProcess的先后加拦截器 ================== End
	        
	        return object;	
		} catch (Exception e) {
						
			StringBuilder sb = new StringBuilder();
			String msg = e.getMessage();
			if(msg!=null) {
				sb.append(msg);
			}
			
			sb.append('\n');
			sb.append("发生方法:" + QueryContext.getMethod());
			sb.append('\n');
			sb.append("执行过的sql:");
			
			List<String> sqls = QueryContext.getSqls();
			sqls.forEach(sql -> {
				sb.append(sql);
				sb.append('\n');
			});
			LOG.error(sb.toString(),e);
			throw new RepositoryException(e);
		} finally {
	        // QueryContext 生命终止
	        try {
	        	QueryContext.clear();
			} catch (SQLException e) {
				LOG.error("数据库连接无法释放",e);
			}
		}
	}
	
	private static Object businessProcess() {
		Method method = QueryContext.getMethod();
		Class<?> returnType = QueryContext.getReturnType();
		// 目前只有一种可能:Query Interface
		// 在这里是一个分水岭
		if(QueryRepository.class.isAssignableFrom(QueryContext.getIclass())){ // 判断iclazz 是否就是QueryRepository.class,或是其子类
			// QueryRepository 中的方法可分成4类
			// 1. 同时包含有@Query和@Modify
			// 2. 只包含@Query
			// 3. 只包含@Modify 这是不允许的, 该检测已放在生成类之前做了.
			// 4. 没有Query,也没有@Modify
			Query[] querys = method.getAnnotationsByType(Query.class);
			Modifying modifying = method.getAnnotation(Modifying.class);
			QueryByNamed queryById = method.getAnnotation(QueryByNamed.class);
			if( (querys.length>0 || queryById!=null) && modifying !=null) {  // ->进入Modify
				return QueryProcess.getInstance().modifying();
			} else if(querys.length>0  || queryById!=null) {
				if(returnType == Page.class && queryById != null) {  // ->进入QueryByNamed Page
					return QueryProcess.getInstance().queryByNamedPage();
				}
				
				
				if(returnType == Page.class) {  // ->进入Page
					return QueryProcess.getInstance().queryPage();
				}
				
				// -> 进入query
				return QueryProcess.getInstance().query();
			} 
			 else {
				 // 分两种 是否由@Id
				 Id id = method.getAnnotation(Id.class);
				 if(id!=null) {
					 return QueryProcess.getInstance().methodQuery(id);
				 } else {
					 return QueryProcess.getInstance().methodQuery();
				 }
			}
			
		} else {
			throw new RepositoryException("不能识别的Repository");
		}
	}
}
