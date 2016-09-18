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

package org.fastquery.mapper;

import java.io.InputStream;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.fastquery.core.Param;
import org.fastquery.core.QueryByNamed;
import org.fastquery.core.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class QueryPool {
	
	private static final Logger LOG = Logger.getLogger(QueryPool.class);
	
	private static Resource resource;
	
	private QueryPool(){}

	private static Map<String, Set<QueryMapper>> mapQueryMapper = new HashMap<>();

	/**
	 * 将*.querys.xml -> QueryMapper 并检测配置文件的正确性
	 * 
	 * @param className
	 * @param resource
	 * @return
	 */
	public static Set<QueryMapper> xml2QueryMapper(String className,Resource resource){
		String xmlName = className + ".queries.xml";
		Set<QueryMapper> queryMappers = new HashSet<>();
		// 判断xmlName有没有存在
		if(!resource.exist(xmlName)){
			LOG.debug("没有找到文件:" + xmlName);
			return queryMappers;
		}
		
		try( InputStream inputStream = resource.getResourceAsStream(xmlName) ) {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(inputStream);
			Element element = document.getDocumentElement();
			NodeList nodeList = element.getChildNodes();
			int len = nodeList.getLength();
			for (int i = 0; i < len; i++) {
				Node node = nodeList.item(i);
				if("query".equals(node.getNodeName())) {
					QueryMapper queryMapper = new QueryMapper(node.getAttributes().getNamedItem("id").getNodeValue(),  node.getTextContent().replaceAll("\\s+", " ").trim());
					// id 或 template 有重复,说明配置文件是错误的
					notDuplicate(queryMappers, queryMapper,xmlName);
					queryMappers.add(queryMapper);
				}
			}
		} catch (Exception e) {
			LOG.error(e);
		}
		return queryMappers;
	}

	/**
	 * 根据 className -> query配置文件 -> 获取模板,然后存储到QueryPool里.<br>
	 * 注意: 这个方法开销较大,生产环境中最好做到项目初始时执行一次,不要执行多遍.
	 * 
	 * @param className
	 * @param resource
	 */
	public static void put(String className,Resource resource){
		QueryPool.resource = resource;
		Set<QueryMapper> queryMappers = xml2QueryMapper(className, resource);
		queryMappers.forEach(queryMapper -> addTemplate(className, queryMapper));
	}
	
	/**
	 * 重新读取模板放入池中,显然在调试模式下很有用
	 * 
	 * @param className
	 * @param resource
	 */
	public static void reset(String className){
		mapQueryMapper.clear();
		Set<QueryMapper> queryMappers = xml2QueryMapper(className, resource);
		queryMappers.forEach(queryMapper -> addTemplate(className, queryMapper));
	}

	/**
	 * 渲染模板,如果没有找到模板返回null
	 * @param className
	 * @param id
	 * @param map
	 * @return
	 */
	public static String render(String className,String id,Map<String, Object> map){
		String tpl = getTemplate(className, id);
		if(tpl==null || "".equals(tpl)){
			return null;
		}
		
		VelocityContext context = new VelocityContext();
		if(map != null) {
			// 往上下文设置值
			map.forEach((k,v)->context.put(k, v));	
		}		
		
		// 输出流
		StringWriter writer = new StringWriter();
		
	    // 转换输出
		Velocity.evaluate(context, writer, className+'.'+id, tpl);
		
		return writer.toString();
	}
	
	public static String render(String className,Method method,Object...args){
		String id = method.getAnnotation(QueryByNamed.class).value();
		String tpl = getTemplate(className, id);
		if(tpl==null || "".equals(tpl)){
			return null;
		}
		
		VelocityContext context = new VelocityContext();
		// 替换@Param
		Annotation[][] annotations = method.getParameterAnnotations();
		int len = annotations.length;
		for (int i = 0; i < len; i++) {
			Annotation[] anns = annotations[i];
			for (Annotation ann : anns) {
				if(ann.annotationType() == Param.class) {
					Param param = (Param) ann;
					context.put(param.value(), args[i]);
				}
			}
		}
		// 替换@Param End
		
		// 输出流
		StringWriter writer = new StringWriter();
		
	    // 转换输出
		Velocity.evaluate(context, writer, className+'.'+id, tpl);
		
		return writer.toString();
	}

	/**
	 * 往QueryPool里添加一个模板,参数值全部都不能为null,否则对外抛出IllegalArgumentException.
	 * 
	 * @param className
	 * @param id
	 * @param template
	 */
	private static synchronized  void addTemplate(String className,QueryMapper queryMapper){
		Set<QueryMapper> queryMappers = getQueryMappers(className);
		queryMappers.add(queryMapper);
	}
	
	/**
	 * 根据类名称(包含包地址)和id查询出模板,没有找到返回null
	 * 
	 * @param className
	 * @param id
	 * @return
	 */
	private static String getTemplate(String className,String id) {
		if(className == null || id == null) {
			return null;
		}
		Set<QueryMapper> queryMappers = mapQueryMapper.get(className);
		if(queryMappers == null){
			LOG.warn(String.format("从%s.queries.xml中没有id为%s的模板,将返回null", className,id));
			return null;
		}
		for (QueryMapper queryMapper : queryMappers) {
			if(queryMapper.getId().equals(id)){
				return queryMapper.getTemplate();
			}
		}
		LOG.warn(String.format("从%s.queries.xml中没有id为%s的模板", className,id));
		return null;
	}
	
	// 在解析xml时校验是否用重复值,有的话表明违规
	private static void notDuplicate(Set<QueryMapper> queryMappers, QueryMapper queryMapper,String xmlName) {
		String id = queryMapper.getId();
		String template = queryMapper.getTemplate();
		
		if(id ==null || "".equals(id.trim())) {
			throw new ExceptionInInitializerError(xmlName + " 里面的id不能为\"\",且不能为null!");
		}
		
		if(template ==null || "".equals(template.trim())){
			throw new ExceptionInInitializerError(xmlName + " 配置的模板不能为\"\",且不能为null!");
		}
		
		for (QueryMapper qm : queryMappers) {
			if(qm.getId().equals(id)){
				throw new ExceptionInInitializerError(xmlName + " 里面的id不能重复!");
			}
			if(qm.getTemplate().equals(template)){
				throw new ExceptionInInitializerError(xmlName + " 配置的模板不能重复!");
			}
		}
	}
	
	/**
	 * 根据className 获取它对应的QueryMapper集合,如果没有的话new一个QueryMapper集合存储起来,然后返回这个空集合,供外界修改
	 * @param className
	 * @return
	 */
	private static Set<QueryMapper> getQueryMappers(String className){
		Set<QueryMapper> queryMappers  = mapQueryMapper.get(className);
		if(queryMappers==null){
			queryMappers =  new HashSet<>();
			mapQueryMapper.put(className, queryMappers);
		}
		return queryMappers;
	}
}
