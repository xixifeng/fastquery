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

package org.fastquery.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.fastquery.core.RepositoryException;
import org.fastquery.core.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * 解析XML
 * @author mei.sir@aliyun.cn
 */
public class XMLParse {

	private XMLParse() {
	}
	
	
	private static interface Function<M,E,R> {
		R appley(M m,E e);
	}
	
	static boolean exists(Resource resource, String resourceName, String dataSourceName,String tagName) {
		return toWho(resource, resourceName, dataSourceName,tagName, (map,ele) -> (map!=null && !map.isEmpty()) || ele != null);
	}
	
	public static Map<Object, Object> toMap(Resource resource, String resourceName, String dataSourceName,String tagName) {
		return toWho(resource, resourceName, dataSourceName,tagName,(m,unitElement) -> {
			if(m!=null && !m.isEmpty()) {
				return m;
			}
			
			String key = null;
			String val = null;
			Node node = null;
			
			Map<Object, Object> map = new HashMap<>();			
			NodeList childNodes = unitElement.getChildNodes();
			
			for (int j = 0; j < childNodes.getLength(); j++) {
				node = childNodes.item(j);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					unitElement = (Element) node;
					key = unitElement.getAttribute("name");
					val = unitElement.getTextContent();
					if(val==null || "".equals(val)) {
						val = unitElement.getAttribute("value");
					}
					check(key, val); // 存之前检测
					map.put(key, val);
				}
			}
			return map;
		});
	}

	private static void check(String key, String val) {
		if ("".equals(key)) {
			throw new RepositoryException("druid.xml 中的property其name属性不能是空字符且不能为null");
		}
		if ("".equals(val)) {
			throw new RepositoryException("druid.xml 中的property其值不能是空字符且不能为null");
		}
	}
	
	private static <R> R toWho(Resource resource, String resourceName, String dataSourceName,String tagName, XMLParse.Function<Map<Object, Object>, Element, R> fun) {
		
		// 首先判断是否有 ${nodeName}.properties
		String properName = new StringBuilder(dataSourceName).append('.').append("properties").toString();
		if (resource.exist(properName)) {
			try (InputStream inputStream = resource.getResourceAsStream(properName)) {
				Map<Object, Object> properties = new Properties();
				((Properties) properties).load(inputStream);
				return fun.appley(properties, null);
			} catch (IOException e) {
				throw new ExceptionInInitializerError(e);
			}

		} else if (resource.exist(resourceName)) {
			try (InputStream inputStream = resource.getResourceAsStream(resourceName)) {

				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = null;
				Document document = null;

				builder = factory.newDocumentBuilder();
				document = builder.parse(inputStream);

				Element element = document.getDocumentElement();

				NodeList nodes = element.getElementsByTagName(tagName);
				Element unitElement = null;
				String named = null;
				for (int i = 0; i < nodes.getLength(); i++) {
					unitElement = (Element) nodes.item(i);
					named = unitElement.getAttribute("name");
					if (dataSourceName.equals(named)) {
						return fun.appley(null, unitElement);
					}
				}

				throw new ExceptionInInitializerError(resourceName + " 中的没有找到节点:" + dataSourceName);

			} catch (IOException | ParserConfigurationException | SAXException e) {
				throw new ExceptionInInitializerError(e);
			}

		} else {
			throw new ExceptionInInitializerError(dataSourceName + ".properties 或 " + resourceName + " 没有找到.");
		}
	}

}



