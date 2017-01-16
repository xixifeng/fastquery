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

package org.fastquery.util;

import java.util.ArrayList;
import java.util.List;

import org.fastquery.core.RepositoryException;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class FastQueryJSONObject {
	private static JSONObject jsonObject;
	
	private FastQueryJSONObject(){}
	
	static void  setJsonObject(JSONObject o){// 只允许对jsonObject赋值一次
		if(jsonObject==null) {
			jsonObject = o;	
		}
	}
	
	/**
	 * 获取基准路径
	 * @return basedir
	 */
	public static String getBasedir(){
		return jsonObject.getString("basedir");
	}
	
	/**
	 * @return velocity配置文件的路径 
	 */
	public static String getVelocity(){
		return jsonObject.getString("velocity");
	}
	
	public static boolean getDebug(){
		return (boolean)jsonObject.getOrDefault("debug", false);
	}
	
	public static List<String> getQueries(){
		List<String> strs = new ArrayList<>();
		JSONArray jsonArray = jsonObject.getJSONArray("queries");
		if(jsonArray==null){
			return strs;
		}
		jsonArray.forEach(s -> strs.add(s.toString()));
		return strs;
	}
	
	static void check(){
		// 1). queries属性要么不配置,要么配置正确
		List<String> strs = getQueries();
		for (String str : strs) {
			if( "".equals(str)) {
				continue;
			}
			if(str.charAt(0)=='/') {
				throw new RepositoryException(String.format("fastquery.json-> queries配置错误,\"%s\"的开头不应该有\"/\"", str));
			}	
			if(str.charAt(str.length()-1)!='/'){
				throw new RepositoryException(String.format("fastquery.json-> queries配置错误,\"%s\"的末尾必须加\"/\"", str));
			}
		}
	}
}
