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

package org.fastquery.core;

/**
 * 占位符/正则 常量
 * @author xixifeng (fastquery@126.com)
 */
public interface Placeholder {
	
	String TABLE="#{#table}";
	String TABLE_REG="\\#\\{\\#table\\}";

	String ID="#{#id}";
	String ID_REG="\\#\\{\\#id\\}";
	
	String WHERE="#{#where}";
	String WHERE_REG="\\#\\{\\#where\\}";
	
	String LIMIT="#{#limit}";
	String LIMIT_RGE="\\#\\{\\#limit\\}";
	
	// 匹配 (?4,?5,?6)的正则(允许有首尾空格)
	String INV_REG = "\\s*\\(\\s*\\?\\d+\\s*,\\s*\\?\\d+\\s*,\\s*\\?\\d+\\s*\\)\\s*"; 
	
	// 不区分大小写匹配格式 "?8 and ?9"
	String ANDV_REG = "(?i)\\s*\\?\\d+\\s+and\\s+\\?\\d+\\s*";
	
	String SP1_REG = "\\?\\d+";
	
	// 匹配格式 "?2"(允许首尾空格)
	String SP2_REG = "\\s*\\?\\d+\\s*";
	
}
