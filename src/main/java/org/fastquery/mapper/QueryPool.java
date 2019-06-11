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

package org.fastquery.mapper;

import java.io.InputStream;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.fastquery.core.MethodInfo;
import org.fastquery.core.Param;
import org.fastquery.core.QueryByNamed;
import org.fastquery.core.QueryContext;
import org.fastquery.core.RepositoryException;
import org.fastquery.core.Resource;
import org.fastquery.util.FastQueryJSONObject;
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

	private static final Logger LOG = LoggerFactory.getLogger(QueryPool.class);

	private static Resource resource;

	private static Map<String, Set<QueryMapper>> mapQueryMapper = new HashMap<>();

	private static Map<String, String> countQueryMap = new HashMap<>();

	private QueryPool() {
	}

	private static void putCountQuery(String key, String value) {
		if (value == null || key == null) {
			return;
		}
		countQueryMap.put(key, value);
	}

	/**
	 * 获取求和模板 key: "类的完整名称.id值"
	 * 
	 * @param key
	 * @return
	 */
	static String getCountQuery(String key) {
		return countQueryMap.get(key);
	}

	private static String findTplXml(String className, Resource resource) {
		// 搜索顺序,先从classpath的根目录开始搜寻,再从fastquery.json的queries所指定的目录里查找
		// 一旦找到,就不往下找了,立马返回.

		List<String> pers = FastQueryJSONObject.getQueries();
		pers.add("");
		for (String per : pers) {
			String perxml = new StringBuilder().append(per).append(className).append(".queries.xml").toString();
			if (resource.exist(perxml)) {
				return perxml;
			}
		}

		return null;
	}

	/**
	 * 将*.querys.xml -> QueryMapper 并检测配置文件的正确性
	 * 
	 * @param className
	 * @param resource
	 * @return
	 */
	private static Set<QueryMapper> xml2QueryMapper(String className, Resource resource) {
		// 用来存储全局parts
		Map<String, String> gparts = new HashMap<>();
		Set<QueryMapper> queryMappers = new HashSet<>();
		String xmlName = findTplXml(className, resource);
		// 判断xmlName有没有存在
		if (xmlName == null) {
			return queryMappers;
		}

		try (InputStream inputStream = resource.getResourceAsStream(xmlName)) {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(inputStream);
			Element element = document.getDocumentElement();
			NodeList nodeList = element.getChildNodes();
			int len = nodeList.getLength();

			// 全局 parts
			for (int i = 0; i < len; i++) {
				Node node = nodeList.item(i);
				if (isElementNode(node,"parts")) {
					Allocation.center(xmlName, (Element) node);
					NodeList partNodes = node.getChildNodes();
					for (int j = 0; j < partNodes.getLength(); j++) {
						Node partNode = partNodes.item(j);
						if (isElementNode(partNode,"part")) {
							gparts.put(((Element) partNode).getAttribute("name"), partNode.getTextContent());
						}
					}
				}
			}
			// 全局 parts End

			for (int i = 0; i < len; i++) {
				Node node = nodeList.item(i);
				if (isElementNode(node, "query")) {
					Allocation.center(xmlName, (Element) node);
					element = (Element) node;
					String id = element.getAttribute("id");
					String postion = String.format("错误位置:%s  --> <%s id=\"%s\"", xmlName, element.getNodeName(), id);
					String template = fuseValTpl(postion, gparts, element, "value", true);
					String countQuery = fuseValTpl(postion, gparts, element, "countQuery", false);
					// countQuery 单独存储起来 (className + "." + id)可以确保唯一值,
					putCountQuery(className + "." + id, countQuery); // 该方法接受到null值后,会视而不见
					LOG.debug("id={} , template={}", id, template);
					QueryMapper queryMapper = new QueryMapper(id, template);
					// 边解析,边做合法校验
					legalCheck(queryMappers, queryMapper, postion);
					legalCheck(countQuery, postion);
					queryMappers.add(queryMapper);
				}
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			throw new RepositoryException(e.getMessage(), e);
		}
		return queryMappers;
	}

	// 判断是否是<nodeName> ELEMENT_NODE 节点
	private static boolean isElementNode(Node node,String nodeName) {
		return node.getNodeType() == Document.ELEMENT_NODE && nodeName.equals(node.getNodeName());
	}

	/**
	 * 融合模板 融合 <query>节点下的<XXX>和<parts> 然后返回字符串
	 * 
	 * @param gparts 全局parts
	 * @param element
	 * @param xxx
	 * @param defaultText 如果没有找到<XXX>节点元素是否直接取<query>下的textContent, true表示是.
	 * @return 注意: 如果既没有<XXX>又不允许defaultText为true, 那么就返回null.
	 */
	private static String fuseValTpl(String postion, Map<String, String> gparts, Element element, String xxx, boolean defaultText) {
		// 判断这个节点是否有子节点 value
		Element ele = getChildElement(element, xxx);
		String template = null;
		if (ele != null) {
			template = ele.getTextContent();
			// 看<value> 节点是否有兄弟节点<parts>
			Element parts = getChildElement(element, "parts");
			if (parts != null) { // 存在parts节点
				template = fuseStr(postion, template, parts);
			}
		} else if (defaultText) {
			template = element.getTextContent();
		}

		if (template == null) {
			return null;
		}

		// 融合全局part
		Set<Entry<String, String>> entries = gparts.entrySet();
		for (Entry<String, String> entry : entries) {
			// #{#"+entry.getKey()+"}这个值包含有正则关键符号,因此用quote
			template = template.replaceAll(Pattern.quote("#{#" + entry.getKey() + "}"), Matcher.quoteReplacement(entry.getValue()));
		}
		// 融合全局part End
		return template;
	}

	// template 和 parts 融合
	private static String fuseStr(String postion, String template, Element parts) throws ExceptionInInitializerError {
		NodeList ps = parts.getChildNodes();
		for (int j = 0; j < ps.getLength(); j++) {
			if (isElementNode(ps.item(j), "part")) {
				Element p = (Element) ps.item(j);
				String name = p.getAttribute("name");
				if ("".equals(name)) {
					throw new ExceptionInInitializerError(String.format("%s> 下面的 part 节点没有设置name属性", postion));
				} else {
					// p.getTextContent() 里面很可能包含有$ 或 \
					// 如果不用Matcher.quoteReplacement进行处理,那么$表示反向引用,就会报错的
					// 这个name在初始化阶段就被限定只能是字母和数字,应此不存在包含有正则符号
					template = template.replaceAll("\\#\\{\\#" + name + "\\}", Matcher.quoteReplacement(p.getTextContent()));
				}
			}
		}
		return template;
	}

	/**
	 * 从给定节点中查询名称为nodeName的子元素. 没有找到返回null
	 * 
	 * @param node
	 * @param nodeName
	 * @return
	 */
	private static Element getChildElement(Node node, String nodeName) {
		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node n = nodeList.item(i);
			if (isElementNode(n, nodeName)) {
				return (Element) n;
			}
		}
		return null;
	}

	/**
	 * 根据 className -&gt; query配置文件 -&gt; 获取模板,然后存储到QueryPool里.<br>
	 * 注意: 这个方法开销较大,生产环境中最好做到项目初始时执行一次,不要执行多遍.
	 * 
	 * @param className class名称
	 * @param resource 资源
	 */
	public static void put(String className, Resource resource) {
		QueryPool.resource = resource;
		Set<QueryMapper> queryMappers = xml2QueryMapper(className, resource);
		queryMappers.forEach(queryMapper -> addTemplate(className, queryMapper));
	}

	/**
	 * 重新读取模板放入池中,显然在调试模式下很有用
	 * 
	 * @param className class名称
	 */
	public static void reset(String className) {
		mapQueryMapper.clear();
		countQueryMap.clear();
		Set<QueryMapper> queryMappers = xml2QueryMapper(className, resource);
		queryMappers.forEach(queryMapper -> addTemplate(className, queryMapper));
	}

	/**
	 * 渲染模板,该方法永远不会返回null或空,因为在初始化时就做了检测
	 * 
	 * @param tpl 模板
	 * @param logTag 日志标识
	 * @param map 键值
	 * @return 渲染之后的字符串
	 */
	private static String render(String tpl, String logTag, Map<String, Object> map) {
		// 不用判断map是否为空,这个方法没有公开,在作用域
		VelocityContext context = new VelocityContext();
		// 往上下文设置值
		map.forEach(context::put);
		
		// 把当前repository的Method放入模板上下文里
		context.put("_method", QueryContext.getMethodInfo());

		// 输出流
		StringWriter writer = new StringWriter();

		// 转换输出
		Velocity.evaluate(context, writer, logTag, tpl);

		return writer.toString();
	}

	// 该方法永远不会返回null或空,因为在初始化时就做了检测
	public static String render(boolean isQuery) {
		String className = QueryContext.getIclass().getName();
		MethodInfo method = QueryContext.getMethodInfo();
		Object[] args = QueryContext.getArgs();
		QueryByNamed qbn = method.getQueryByNamed();
		String id = qbn.value();
		if ("".equals(id)) {
			id = method.getName();
		}
		LOG.info("已获得模板:{}", id);
		String tpl;
		if (isQuery) {
			// getTemplate 永远不会为null,在初始化时已经做了检测
			tpl = getTemplate(className, id);
		} else {
			tpl = getCountQuery(className + '.' + id);
		}

		if (!qbn.render()) {
			return tpl;
		}

		// 处理@Param
		Map<String, Object> map = new HashMap<>();
		Annotation[][] annotations = method.getParameterAnnotations();
		int len = annotations.length;
		for (int i = 0; i < len; i++) {
			Annotation[] anns = annotations[i];
			for (Annotation ann : anns) {
				if (ann.annotationType() == Param.class) {
					Param param = (Param) ann;
					map.put(param.value(), args[i]);
				}
			}
		}
		// 处理@Param End

		String logTag = new StringBuilder(className).append('.').append(id).toString();

		String str = render(tpl, logTag, map).trim().replaceAll("\\s+", " ");

		return TypeUtil.parWhere(str);
	}

	/**
	 * 往QueryPool里添加一个模板,参数值全部都不能为null,否则对外抛出IllegalArgumentException.
	 * 
	 * @param className
	 * @param id
	 * @param template
	 */
	private static synchronized void addTemplate(String className, QueryMapper queryMapper) {
		Set<QueryMapper> queryMappers = getQueryMappers(className);
		queryMappers.add(queryMapper);
	}

	/**
	 * 根据类名称(包含包地址)和id查询出模板 该方法永远不会返回null或"",因为在初始化时做了与处理,如果为null或"",初始化都通过不了.
	 * 
	 * @param className
	 * @param id
	 * @return
	 */
	private static String getTemplate(String className, String id) {
		// 特别注意: 是否有模板,已经在初始化时做了严格校验,在此处就不用判断是否为null了
		Set<QueryMapper> queryMappers = mapQueryMapper.get(className);
		for (QueryMapper queryMapper : queryMappers) {
			if (queryMapper.getId().equals(id)) {
				return queryMapper.getTemplate();
			}
		}
		return null;
	}

	// 在解析xml时,合法性检查
	private static void legalCheck(Set<QueryMapper> queryMappers, QueryMapper queryMapper, String postion) {
		String id = queryMapper.getId();
		String template = queryMapper.getTemplate();

		notNullAndEmpty(id, postion + ">,这个query里面的id属性值不能为\"\".");
		notNullAndEmpty(template, postion + ">, 这个query所包裹的模板内容不能为空白.");

		for (QueryMapper qm : queryMappers) {
			if (qm.getId().equals(id)) {
				throw new ExceptionInInitializerError(postion + "> 这个query的id值已经存在了,请不要重复.");
			}
		}
		notSemicolons(template, postion + "> 这个query所包裹的模板内容不能出现\";\"号,并不是说不能往里面传递值包含有\";\"的参数");
	}

	private static void legalCheck(String countQuery, String postion) {
		notSemicolons(countQuery, postion + "> 下面的<countQuery>所包裹的模板内容不能出现\";\"号,并不是说不能往里面传递值包含有\";\"的参数");
	}

	// 禁止";"号
	private static void notSemicolons(String query, String postion) {
		if (query != null && query.indexOf(';') != -1) {
			throw new ExceptionInInitializerError(postion);
		}
	}

	private static void notNullAndEmpty(String str, String msg) {
		if ("".equals(str.trim())) {
			throw new ExceptionInInitializerError(msg);
		}
	}

	// 根据className 获取它对应的QueryMapper集合,如果没有的话new一个QueryMapper集合存储起来,然后返回这个空集合,供外界修改
	private static Set<QueryMapper> getQueryMappers(String className) {
		// 根据className(K) 从 mapQueryMapper(Map) 中取V,V不为null直接返回.如果V是null,那么就根据k创建一个V存入mapQueryMapper中[k -> new HashSet<>()],并返回新V.
		// 这个函数封装得有点高级,看看源码就清楚了
		return mapQueryMapper.computeIfAbsent(className, k -> new HashSet<>());
	}

	static Map<String, Set<QueryMapper>> getMapQueryMapper() {
		return mapQueryMapper;
	}

	static Map<String, String> getCountQueryMap() {
		return countQueryMap;
	}
}
