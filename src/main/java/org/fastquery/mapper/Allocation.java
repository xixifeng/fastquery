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

import org.fastquery.mapper.filter.FilterChain;
import org.fastquery.mapper.filter.part.PartSyntaxFilter;
import org.fastquery.mapper.filter.query.StructureFilter;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 对filter进行分类
 * 
 * @author xixifeng (fastquery@126.com)
 */
class Allocation {

	private Allocation() {
	}

	// 校验SQL模板, 该校验仅仅作用于初始化阶段(因此不用考虑性能问题)
	// 该校验可以减少运行期错误
	private static void doCountQuery(String xmlName, Element element) {
		FilterChain countFilterChain = new FilterChain();

		countFilterChain.doFilter(xmlName, element);
	}

	private static void doPart(String xmlName, Element element) {
		FilterChain partFilterChain = new FilterChain();
		partFilterChain.addFilter(new PartSyntaxFilter());
		partFilterChain.doFilter(xmlName, element);
	}

	private static void doQuery(String xmlName, Element element) {
		FilterChain queryFilterChain = new FilterChain();
		// <query> 结构语法检测
		queryFilterChain.addFilter(new StructureFilter());
		queryFilterChain.doFilter(xmlName, element);
	}

	private static void doValue(String xmlName, Element element) {
		FilterChain valueFilterChain = new FilterChain();

		valueFilterChain.doFilter(xmlName, element);
	}
	// 校验SQL模板 End

	static void center(String xmlName, Element element) {

		String nodeName = element.getNodeName();
		if ("parts".equals(nodeName)) {
			eachParts(xmlName, element);
		} else if ("query".equals(nodeName)) {
			doQuery(xmlName, element);
			NodeList partNodes = element.getChildNodes();
			for (int j = 0; j < partNodes.getLength(); j++) {
				Node partNode = partNodes.item(j);
				short nodeType = partNode.getNodeType();
				final short elementNode = Node.ELEMENT_NODE;
				final String partNodeName = partNode.getNodeName();
				if (nodeType == elementNode) {
					switch (partNodeName) {
					case "parts":
						eachParts(xmlName, (Element) partNode);
						break;
					case "value":
						doValue(xmlName, element);
						break;
					case "countQuery":
						doCountQuery(xmlName, element);
						break;
					default:
						break;
					}
				}
			}
		}
	}

	private static void eachParts(String xmlName, Element element) {
		NodeList partNodes = element.getChildNodes();
		for (int j = 0; j < partNodes.getLength(); j++) {
			Node partNode = partNodes.item(j);
			if (partNode.getNodeType() == Node.ELEMENT_NODE && "part".equals(partNode.getNodeName())) {
				doPart(xmlName, (Element) partNode);
			}
		}
	}
}
