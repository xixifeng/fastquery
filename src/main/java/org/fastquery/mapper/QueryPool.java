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
import java.util.regex.Matcher;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.fastquery.core.Param;
import org.fastquery.core.QueryByNamed;
import org.fastquery.core.Resource;
import org.fastquery.util.TypeUtil;
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

	private static Map<String, Set<QueryMapper>> mapQueryMapper = new HashMap<>();
	
	private static Map<String, String> countQueryMap = new HashMap<>();
	
	private QueryPool(){}
	
	private static void putCountQuery(String key,String value){
		if(value == null || key == null) {
			return ;
		}
		countQueryMap.put(key, value);
	}
	
	/**
	 * 获取求和模板 key: "类的完整名称.id值"
	 * @param key
	 * @return
	 */
	public static String getCountQuery(String key){
		return countQueryMap.get(key);
	}

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
				if(node.getNodeType() == Document.ELEMENT_NODE && "query".equals(node.getNodeName())) {
					element = (Element) node;
					String id = element.getAttribute("id");
					String template = fuseValTpl(element, "value",true);
					String countQuery = fuseValTpl(element, "countQuery",false);
					
					//  在存储template, 和 countQuery 之前 需要做数据库过滤
					//  待续...
					//  在存储template, 和 countQuery 之前 需要做数据库过滤 end
					
					// countQuery 单独存储起来 (className + "." + id)可以确保唯一值,
					putCountQuery(className + "." + id,countQuery); // 该方法接受到null值后,会视而不见
					LOG.debug(String.format("id=%s , template=%s", id,template));
					QueryMapper queryMapper = new QueryMapper(id, template);
					// 边解析,边做合法校验
					legalCheck(queryMappers, queryMapper,xmlName);
					queryMappers.add(queryMapper);
				}
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(),e);
		}
		return queryMappers;
	}

	/**
	 * 融合模板 融合 <query>节点下的<XXX>和<parts> 然后返回字符串
	 * @param element <query>节点元素
	 * @param ele <XXX>节点元素
	 * @param defaultText 如果没有找到<XXX>节点元素是否直接取<query>下的textContent, true表示是.
	 * @return 注意: 如果既没有<XXX>又不允许defaultText为true, 那么就返回null.
	 */
	private static String fuseValTpl(Element element, String xxx,boolean defaultText) {
		// 判断这个节点是否有子节点 value 
		Element ele = getChildElement(element, xxx);
		String template = null;
		if(ele !=null) {
			template = ele.getTextContent();
			// 看<value> 节点是否有兄弟节点<parts>
			Element parts = getChildElement(element, "parts");
			if(parts == null){ // 若没有parts节点
				return template;
			}
			
			NodeList ps = parts.getChildNodes();
			for (int j = 0; j < ps.getLength(); j++) {
				if(ps.item(j).getNodeType() == Element.ELEMENT_NODE) {
					Element p = (Element) ps.item(j);
					String name = p.getAttribute("name");
					// p.getTextContent() 里面很可能包含有$ 或 \ 如果不用Matcher.quoteReplacement进行处理,那么$表示反向引用,就会报错的
					template = template.replaceAll("\\#\\{\\#"+name+"\\}",Matcher.quoteReplacement(p.getTextContent()));
				}
			}
			
		} else if(defaultText) {
			template = element.getTextContent();
		}
		return template;
	}

	/**
	 * 从给定节点中查询名称为nodeName的子元素. 没有找到返回null
	 * @param node
	 * @param nodeName
	 * @return
	 */
	private static Element getChildElement(Node node,String nodeName) {
		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node n = nodeList.item(i); 
			if( n.getNodeType() == Element.ELEMENT_NODE && n.getNodeName().equals(nodeName) ) {
				return (Element) n;
			}
		}
		return null;
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
		
		return writer.toString().replaceAll("\\s+", " ").trim();
	}
	
	public static String render(String className,Method method,boolean isQuery,Object...args){
		String id = method.getAnnotation(QueryByNamed.class).value();
		
		String tpl;
		if(isQuery){
			tpl = getTemplate(className, id);	
		} else {
			tpl = getCountQuery(className+'.'+id);
		}
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
		
		String str = writer.toString().trim().replaceAll("\\s+", " ");
		
		return TypeUtil.parWhere(str);
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
	
	// 在解析xml时,合法性检查
	private static void legalCheck(Set<QueryMapper> queryMappers, QueryMapper queryMapper,String xmlName) {
		String id = queryMapper.getId();
		String template = queryMapper.getTemplate();
		
		notNullAndEmpty(id,xmlName + " 里面的id不能为\"\",且不能为null!");
		notNullAndEmpty(template,xmlName + " 配置的模板不能为\"\",且不能为null!");
		
		for (QueryMapper qm : queryMappers) {
			if(qm.getId().equals(id)){
				throw new ExceptionInInitializerError(xmlName + " 里面的id不能重复!");
			}
			if(qm.getTemplate().equals(template)){
				throw new ExceptionInInitializerError(xmlName + " 配置的模板不能重复!");
			}
		}
		
	}
	
	private static void notNullAndEmpty(String str,String msg){
		if(str == null || "".equals(str.trim())) {
			throw new ExceptionInInitializerError(msg);
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
