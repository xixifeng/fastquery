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

package org.fastquery.asm;

import java.lang.reflect.Method;

import org.fastquery.analysis.AnnotationSynxFilter;
import org.fastquery.analysis.ArgsFilter;
import org.fastquery.analysis.ConditionParameterFilter;
import org.fastquery.analysis.InterceptorFilter;
import org.fastquery.analysis.MarkFilter;
import org.fastquery.analysis.MethodAnnotationFilter;
import org.fastquery.analysis.MethodFilterChain;
import org.fastquery.analysis.ModifyingDependencyFilter;
import org.fastquery.analysis.ModifyingReturnTypeFilter;
import org.fastquery.analysis.MuestionFilter;
import org.fastquery.analysis.NotAllowedRepeat;
import org.fastquery.analysis.OutFilter;
import org.fastquery.analysis.PageFilter;
import org.fastquery.analysis.PageableFilter;
import org.fastquery.analysis.ParameterFilter;
import org.fastquery.analysis.QueriesFileFilter;
import org.fastquery.analysis.QueryReturnTypeFilter;
import org.fastquery.analysis.ReturnTypeFilter;
import org.fastquery.analysis.SQLFilter;
import org.fastquery.analysis.SetFilter;
import org.fastquery.analysis.SharpFilter;
import org.fastquery.analysis.SourceFilter;
import org.fastquery.analysis.TplPageFilter;
import org.fastquery.core.Modifying;
import org.fastquery.core.Query;
import org.fastquery.core.QueryByNamed;
import org.fastquery.core.QueryRepository;
import org.fastquery.core.Repository;
import org.fastquery.core.RepositoryException;

/**
 * 生成 Repository的实现扩展, 在生成的时候,额外要做的事情(extend)
 * 
 * @author xixifeng (fastquery@126.com)
 */
// 该方法只为AsmRepository 服务
class GenerateExtends {

	private GenerateExtends() {
	}

	/**
	 * 在生成Repository实现类之前,做安全检测
	 * 
	 * @param repositoryClazz
	 */
	static void safeCheck(Class<? extends Repository> repositoryClazz) {

		MethodFilterChain filterChain;

		Modifying modifying;
		Query[] querys;
		QueryByNamed queryByNamed;

		Method[] methods = repositoryClazz.getMethods();
		for (Method method : methods) {

			Class<?> declaringClass = method.getDeclaringClass();

			// 规范接口中的方法不参与校验
			if (declaringClass == QueryRepository.class || declaringClass == Repository.class) {
				continue;
			}

			modifying = method.getAnnotation(Modifying.class);
			querys = method.getAnnotationsByType(Query.class);
			queryByNamed = method.getAnnotation(QueryByNamed.class);

			// filter/modifying 拦截标注有@Query @modifying的方法,并且是QueryRepository的实现方法
			// filter/mquery 拦截既没有标注@Query,又没有标注@modifying的方法, 并且是QueryRepository的实现方法
			// filter/query 拦截既标注有@Query,没有标注@modifying的方法, 并且是QueryRepository的实现方法
			// filter/querya 拦截QueryRepository的实现方法

			// 拦截全局的方法
			filterChain = new MethodFilterChain();
			filterChain.addFilter(new ReturnTypeFilter());
			filterChain.addFilter(new InterceptorFilter()); // @Before,@After拦截器安全校验
			filterChain.addFilter(new PageableFilter());
			filterChain.addFilter(new SharpFilter()); // #{#表达式} 合法检测


			if (QueryRepository.class.isAssignableFrom(repositoryClazz)) { // 若:QueryRepository 是
																			// repositoryClazz的父类

				// filter/querya
				filterChain.addFilter(new ModifyingDependencyFilter());
				filterChain.addFilter(new MethodAnnotationFilter());
				filterChain.addFilter(new SQLFilter());
				filterChain.addFilter(new ConditionParameterFilter());
				filterChain.addFilter(new SourceFilter());
				filterChain.addFilter(new OutFilter());
				filterChain.addFilter(new MarkFilter());
				filterChain.addFilter(new MuestionFilter());
				filterChain.addFilter(new SetFilter());
				

				// filter/modifying
				if (modifying != null && querys.length > 0) {
					filterChain.addFilter(new AnnotationSynxFilter());
					filterChain.addFilter(new ArgsFilter());
					filterChain.addFilter(new ModifyingReturnTypeFilter());
				}

				// filter/query
				if (querys.length > 0 && modifying == null) {
					filterChain.addFilter(new QueryReturnTypeFilter());
					filterChain.addFilter(new ParameterFilter());
					filterChain.addFilter(new NotAllowedRepeat());
					filterChain.addFilter(new PageFilter());
				}

				// @QueryByNamed && @modifying
				if (modifying != null && queryByNamed != null) {

				}
				// @QueryByNamed, !@modifying
				if (queryByNamed != null && modifying == null) {
					filterChain.addFilter(new TplPageFilter());
					filterChain.addFilter(new QueriesFileFilter());
				}

				// filter/mquery
				if (querys.length == 0 && modifying == null) {
					// 有待扩展...
				}

			} else {
				throw new RepositoryException(repositoryClazz + "不能解析");
			}

			// 把责任链条连接起来
			// 多根链接衔接完毕之后就执行过滤
			filterChain.doFilter(method);

		}
	}
}
