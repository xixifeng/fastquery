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

package org.fastquery.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.fastquery.asm.AsmRepository;
import org.fastquery.core.FQueryResourceImpl;
import org.fastquery.core.GenerateRepository;
import org.fastquery.core.Repository;
import org.fastquery.core.Resource;
import org.fastquery.dsm.FastQueryJson;
import org.fastquery.mapper.QueryPool;
import org.fastquery.util.ClassUtil;
import org.fastquery.util.LoadPrperties;

/**
 * 这个类在osgi环境中不能调用
 * 
 * @author xixifeng (fastquery@126.com)
 */
class GenerateRepositoryImpl implements GenerateRepository {

	private static final Logger LOG = LoggerFactory.getLogger(GenerateRepositoryImpl.class);

	private FqClassLoader classLoader;
	
	GenerateRepositoryImpl(FqClassLoader classLoader) {
		this.classLoader = classLoader;
		LOG.debug("GenerateRepositoryImpl 已实例化.");
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Repository> Class<? extends T> generate(Class<T> repositoryClazz) {
		String name = repositoryClazz.getName() + SUFFIX;

		byte[] bytes = AsmRepository.generateBytes(repositoryClazz);

		/*
		// 把生成的文件存储起来
		try (java.io.FileOutputStream fos = new java.io.FileOutputStream("/data/tmp/" + name + ".class")) {
			fos.write(bytes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 把生成的文件存储起来 end
		 */
		return (Class<? extends T>) classLoader.defineClassByName(name, bytes, 0, bytes.length);
	}
	
	void persistent() {

		Resource resource = new FQueryResourceImpl(classLoader);

		// 1). 装载配置文件
		Set<FastQueryJson> fqPropertie = LoadPrperties.load(resource);
		
		List<Class<Repository>> clses = new ArrayList<>();

		// 3). 批量生成 Repository 的实现类
		Set<String> basePackages;
		for (FastQueryJson fQueryPropertie : fqPropertie) {
			basePackages = fQueryPropertie.getBasePackages();
			for (String basePackage : basePackages) {
				List<Class<Repository>> classes = ClassUtil.getClasses(basePackage, classLoader);
				clses.addAll(classes);
				// classes.forEach(this::generate)
				for (Class<Repository> rcls : classes) {
					generate(rcls); // 生成
					QueryPool.put(rcls.getName(), resource);
				}
			}
		}

		// 3). 生成 Repository之后的检测
		AsmRepository.after(clses);
	}
}
