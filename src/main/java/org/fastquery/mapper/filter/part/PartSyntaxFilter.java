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

package org.fastquery.mapper.filter.part;

import org.fastquery.mapper.filter.Filter;
import org.w3c.dom.Element;

/**
 * 校验 part 节点的语法
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class PartSyntaxFilter implements Filter {

	@Override
	public Element doFilter(String xmlName, Element element) {
		// 找出part的爷爷
		Element pe = (Element) element.getParentNode().getParentNode();
		String ps;
		if("query".equals(pe.getNodeName())) {
			ps = "<query id=\""+pe.getAttribute("id")+"\">里面的parts";
		} else {
			ps = "<queries>下面的全局parts";
		}
		
		// 1). part节点必须有name属性
		String name = element.getAttribute("name");
		if("".equals(name)) {
			this.abortWith("解析"+xmlName + "错误,原因:没有给part节点设置name属性.大概位置:它被"+ps+"包裹着");
		}
		
		return element;
	}

}
