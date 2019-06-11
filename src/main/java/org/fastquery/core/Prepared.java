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

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class Prepared { // NO_UCD

	private static final Logger LOG = LoggerFactory.getLogger(Prepared.class);

	private Prepared() {
	}

	/**
	 * 执行方法
	 * 
	 * @param methodInfo 当前接口方法
	 * @param args 方法参数 注意: 此处参数列表的成员,永远都是包装类型(已经验证)
	 * @param target 目标 Repository
	 * @return 执行之后的值
	 */
	public static Object excute(MethodInfo methodInfo,Object[] args, QueryRepository target) { // NO_UCD
		long start = System.currentTimeMillis();
		Method method = methodInfo.getMethod();
		try {
			Class<? extends QueryRepository> iclazz = target.getInterfaceClass();

			// 如果是调试模式
			if (FastQueryJSONObject.getDebug()) {
				QueryPool.reset(iclazz.getName());
			}
			
			// QueryContext 生命开始
			QueryContext.start(iclazz, methodInfo, args);

			// 在businessProcess的先后加拦截器 ==================
			// 注入BeforeFilter
			Object object = FilterChainHandler.bindBeforeFilterChain(iclazz, target, method, args);
			if (object != void.class) {
				return object;
			}

			LOG.info("准备执行方法:{}", method);
			object = businessProcess();

			// 注入AfterFilter
			object = FilterChainHandler.bindAfterFilterChain(iclazz, target, method, args, object); // 注意,这个方法的method,必须是原始的!!!
			// 在businessProcess的先后加拦截器 ================== End

			return object;
		} catch (Exception e) {
			LOG.error("执行出错: " + method.getName(), e);
			StringBuilder sb = new StringBuilder();
			String msg = e.getMessage();
			if (msg != null) {
				sb.append(msg);
			}

			sb.append('\n');
			sb.append("发生方法:" + QueryContext.getMethodInfo());
			sb.append('\n');
			sb.append("执行过的sql:");

			List<String> sqls = QueryContext.getSqls();
			sqls.forEach(sql -> {
				sb.append(sql);
				sb.append('\n');
			});
			LOG.error(sb.toString());
			throw new RepositoryException(e);
		} finally {
			// QueryContext 生命终止
			try {
				QueryContext.clear();
			} catch (SQLException e) {
				LOG.error("数据库连接无法释放", e);
			}
			
			long slowQueryTime = FastQueryJSONObject.getSlowQueryTime();
			if (slowQueryTime > 0) {
				long end = System.currentTimeMillis();
				long x = end - start;
				if (x >= slowQueryTime) {
					LOG.warn("slowQueryTime: 执行{} 耗时: {} 毫秒", method, x);
				}
			}
		}
	}

	private static Object businessProcess() {
		MethodInfo methodInfo = QueryContext.getMethodInfo();
		Class<?> returnType = QueryContext.getReturnType();
		Query[] querys = methodInfo.getQueries();
		Modifying modifying = methodInfo.getModifying();
		QueryByNamed queryById = methodInfo.getQueryByNamed();

		boolean hasVehicle = querys.length > 0 || queryById != null; // 有SQL模板吗?
		if (hasVehicle) {
			if (returnType == Page.class) { // 分页
				return QueryProcess.getInstance().queryPage(queryById);
			} else if (modifying != null) { // 改
				return QueryProcess.getInstance().modifying();
			} else { // 查
				return QueryProcess.getInstance().query();
			}
		} else {
			Id id = methodInfo.getId();
			if (id != null) { // 有@id
				return QueryProcess.getInstance().methodQuery(id);
			} else { // 无@id
				return QueryProcess.getInstance().methodQuery();
			}
		}
	}
}
