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

package org.fastquery.mapper.filter.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.fastquery.mapper.filter.Filter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * query 节点语法约束
 * @author xixifeng (fastquery@126.com)
 */
public class StructureFilter implements Filter {

	// <query> 要么直接是文本内容,要么query下面存在Element,那么value节点必须要存在
	// query 下面的value,countQuery parts均不能重复
	@Override
	public Element doFilter(String xmlName, Element element) {
		StringBuilder sb = new StringBuilder(xmlName);
		sb.append(", <query id=\"");
		sb.append(element.getAttribute("id"));
		sb.append("\">");
		List<String> elementNames = new ArrayList<>(); // 把query的子ELEMENT_NODE的name,全部存储起来
		NodeList partNodes = element.getChildNodes();
		for (int j = 0; j < partNodes.getLength(); j++) {
			Node partNode = partNodes.item(j);
			short nodeType = partNode.getNodeType();
			if (nodeType == Document.ELEMENT_NODE) { 
				elementNames.add(partNode.getNodeName());
			}
		}
		
		// 统计value节点的个数
		if(Collections.frequency(elementNames, "value")>1) {
			this.abortWith(sb.toString()+" 该节点下面所包裹的value节点最多只能出现一次");
		}
		
		if(Collections.frequency(elementNames, "countQuery")>1) {
			this.abortWith(sb.toString()+" 该节点下面所包裹的countQuery节点最多只能出现一次");
		}
		
		if(Collections.frequency(elementNames, "parts")>1) {
			this.abortWith(sb.toString()+" 该节点下面所包裹的parts节点最多只能出现一次");
		}
		
		if(!elementNames.isEmpty() && !elementNames.contains("value")) { // 如果不为空,且不包含value
			this.abortWith(sb.toString()+" 该节点下面所包裹的要么全部是文本内容,要么就必须存在value节点");
		}
		return element;
	}

}
