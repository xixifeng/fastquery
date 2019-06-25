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

package org.fastquery.core;

/**
 * 占位符/正则 常量
 * 
 * @author xixifeng (fastquery@126.com)
 */
public final class Placeholder {

	private Placeholder() {
		throw new IllegalStateException("Placeholder is Utility class");
	}

	/**
	 * 生成db类的后缀名称
	 */
	public static final String DB_SUF = "a$";
	/**
	 * 生成rest类的后缀名称
	 */
	public static final String REST_SUF = "b$";

	public static final String TABLE = "#{#table}";
	static final String TABLE_REG = "\\#\\{\\#table\\}";

	public static final String ID = "#{#id}";
	static final String ID_REG = "\\#\\{\\#id\\}";

	public static final String SETS_REG = "\\#\\{\\#sets\\}";
	public static final String WHERE_REG = "\\#\\{\\#where\\}";

	public static final String LIMIT = "#{#limit}";
	public static final String LIMIT_RGE = "\\#\\{\\#limit\\}";

	public static final String SP1_REG = "\\?\\d+";

	/**
	 * 搜索出"?"后面的数字
	 */
	public static final String SEARCH_NUM = "(?<=\\?)\\d+";

	/**
	 * 匹配冒号表达式
	 */
	public static final String COLON_REG = ":+[A-Za-z0-9]+";

	/**
	 * 匹配EL表达式,可能包含有_ . ( ) 如: 在模板中调用 ${_method.getString()} 
	 */
	public static final String EL_REG = "\\$\\{?[A-Za-z0-9_\\.()]+\\}?";

	/**
	 * 匹配EL表达式或匹配冒号表达式
	 */
	public static final String EL_OR_COLON = EL_REG + "|" + COLON_REG;

	/**
	 * 匹配微笑表达式
	 */
	public static final String SMILE = "`-[^`]*\\?[^`]*-`";

	public static final String SMILE_BIG = "`-[^`]*[^`]*-`";

	public static final String PERCENT = "%+";

}
