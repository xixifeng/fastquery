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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.fastquery.core.RepositoryException;
import org.fastquery.core.Resource;
import org.fastquery.dsm.FastQueryJson;
import org.fastquery.dsm.JdbcConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class PropertiesUtil {
	
	private static final Logger LOG = Logger.getLogger(PropertiesUtil.class);
	
	private PropertiesUtil(){}
	
	/**
	 * 解析 c3p0-config.xml
	 * 返回null 表示解析失败了或是待解析的文件不存在.
	 * @return
	 */
	public static JSONObject getC3p0Configs(InputStream inputStream){

		if(inputStream == null) {
			return null;
		}
		
		JSONObject jdbcConfigs = new JSONObject();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		Document document = null;
		try {
			builder = factory.newDocumentBuilder();
			document = builder.parse(inputStream);
			
			
			Element element = document.getDocumentElement();

			NodeList jdbcConfigNodes = element.getElementsByTagName("named-config");
			Element jdbcConfigElement = null;
			NodeList childNodes = null;
			String key = null;
			String val = null;
			String named = null;
			JSONObject jsonObject = null;
			Node node = null;
			for (int i = 0; i < jdbcConfigNodes.getLength(); i++) {
				jdbcConfigElement = (Element) jdbcConfigNodes.item(i);
				named = jdbcConfigElement.getAttribute("name");
				childNodes = jdbcConfigElement.getChildNodes();
				jsonObject = new JSONObject();
				for (int j = 0; j < childNodes.getLength(); j++) {
					node = childNodes.item(j);
					if (node.getNodeType() == Node.ELEMENT_NODE) {
						jdbcConfigElement = (Element) node;
						key = jdbcConfigElement.getAttribute("name");
						val = jdbcConfigElement.getTextContent();
						if(key == null || "".equals(key)) {
							throw new RepositoryException("c3p0-config.xml 中的property其name属性不能是空字符且不能为null");
						}
						if(val == null || "".equals(val)) {
							throw new RepositoryException("c3p0-config.xml 中的property其值不能是空字符且不能为null");
						}
						jsonObject.put(key, val);
					}
				}
				jdbcConfigs.put(named, jsonObject);
			}

		} catch (Exception e) {
			throw new RepositoryException(e.getMessage(),e);
		} finally {
				try {
					inputStream.close();
				} catch (IOException e) {
					LOG.error(e.getMessage(),e);
				}
		}

		return jdbcConfigs;
	}
	
	/**
	 * 解析 jdbc-config.xml
	 * 返回null 表示解析失败了或是待解析的文件不存在.
	 * @return
	 */
	public static Map<String,JdbcConfig> getJdbcConfigs(InputStream inputStream) {

		if(inputStream == null) {
			return null;
		}
		Map<String,JdbcConfig> jdbcConfigs = new HashMap<>();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		Document document = null;
		try {
			builder = factory.newDocumentBuilder();
			document = builder.parse(inputStream);
			

			Element element = document.getDocumentElement();

			NodeList jdbcConfigNodes = element.getElementsByTagName("named-config");
			Element jdbcConfigElement = null;
			JdbcConfig jdbcConfig = null;
			NodeList childNodes = null;
			String key = null;
			String val = null;
			for (int i = 0; i < jdbcConfigNodes.getLength(); i++) {
				jdbcConfigElement = (Element) jdbcConfigNodes.item(i);
				jdbcConfig = new JdbcConfig();
				String named = jdbcConfigElement.getAttribute("name");
				jdbcConfig.setNamed(named);
				childNodes = jdbcConfigElement.getChildNodes();
				for (int j = 0; j < childNodes.getLength(); j++) {
					Node node = childNodes.item(j);
					if (node.getNodeType() == Node.ELEMENT_NODE) {
						jdbcConfigElement = (Element) node;
						key = jdbcConfigElement.getAttribute("name");
						val = jdbcConfigElement.getTextContent();
						if(key == null || "".equals(key)) {
							throw new RepositoryException("jdbc-config.xml 中的property其name属性不能是空字符且不能为null");
						}
						if(val == null || "".equals(val)) {
							throw new RepositoryException("jdbc-config.xml 中的property其值不能是空字符且不能为null");
						}
						if ("databaseName".equals(key)) {
							jdbcConfig.setDatabaseName(val);
						} else if ("password".equals(key)) {
							jdbcConfig.setPassword(val);
						} else if ("portNumber".equals(key)) {
							jdbcConfig.setPortNumber(Integer.parseInt(val));
						} else if ("serverName".equals(key)) {
							jdbcConfig.setServerName(val);
						} else if ("user".equals(key)) {
							jdbcConfig.setUser(val);
						} else if ("url".equals(key)) {
							jdbcConfig.setUrl(val);
						} else if ("driverClass".equals(key)) {
							jdbcConfig.setDriverClass(val);
						} else {
							throw new RepositoryException("jdbc-config.xml 中不支持该属性值: name=" + key);
						}
					}
				}
				jdbcConfigs.put(named, jdbcConfig);
			}

		} catch (Exception e) {
			throw new RepositoryException(e.getMessage(),e);
		} finally {
				try {
					inputStream.close();
				} catch (IOException e) {
					LOG.error(e.getMessage(),e);
				}
		}
		
		return jdbcConfigs;
	}
	
	
	
	/**
	 * 
	 * @param fqueryjson
	 * @param jdbcConfig
	 * @param fqueryResource
	 * @return
	 */
	public static Set<FastQueryJson> getFQueryProperties(InputStream fqueryjson,Resource fqueryResource){
				
		Set<FastQueryJson> fqs = null; 
		
		if(fqueryjson==null) {
			throw new RepositoryException("没有找到fastquery.json .");
		}
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		int b = 0;
		String fqueryJson=null;
		try {
			while( (b=fqueryjson.read()) != -1){
				byteArrayOutputStream.write(b);
			}
			fqueryJson = byteArrayOutputStream.toString();
			fqueryJson = TypeUtil.filterComments(fqueryJson);
		} catch (IOException e) {
			throw new RepositoryException(e.getMessage(),e);
		} finally {
			try {
				byteArrayOutputStream.close();
			} catch (IOException e) {
				LOG.error(e.getMessage(),e);
			} finally {
				try {
					fqueryjson.close(); // 在此不用判断是否为null, 如果它为null 早就返回了
				} catch (IOException e) {
					LOG.error(e.getMessage(),e);
				}
			}
		}
		
		if(fqueryJson==null) { // 表明没有fastquery.json 配置文件,直接报错
			throw new RepositoryException("没有找到fastquery.json .");
		}
		
		fqs = new HashSet<>();
		JSONObject json = JSONObject.parseObject(fqueryJson);
		FastQueryJSONObject.setJsonObject(json);
		FastQueryJson[] fqProperties = JSON.toJavaObject(json.getJSONArray("scope"), FastQueryJson[].class);
		String config = null;
		String dataSourceName = null;
		Set<String> basePackages = null;
		List<String> dataSourceNames = new ArrayList<>(); // 用于存储所有的数据源名称,在fastquery.json文件里禁止dataSourceName重复出现
		List<String> bpNames = new ArrayList<>(); // 用于存储所有的basePackage,在fastquery.json文件里禁止basePackage重复出现
		for (FastQueryJson fQueryPropertie : fqProperties) {
			
			// 顺便校验配置
			config = fQueryPropertie.getConfig();
			dataSourceName = fQueryPropertie.getDataSourceName();
			basePackages = fQueryPropertie.getBasePackages(); 
			if(config == null || "".equals(config)) {
				throw new RepositoryException("fastquery.json 中的config属性配置错误,提示,不能是空字符且不能为null");
			}
			if("".equals(dataSourceName)){
				throw new RepositoryException("fastquery.json 中的dataSourceName配置错误,提示,不能是空字符且不能为null");
			}
			if(basePackages==null || basePackages.isEmpty()) {
				throw new RepositoryException("fastquery.json 中的basePackage配置错误,提示basePackages不能配置成空");
			}
			for (String basePackage : basePackages) {
				if(basePackage==null || "".equals(basePackage)){
					throw new RepositoryException("fastquery.json 中的basePackage配置错误,提示,不能是空字符且不能为null");
				}
				bpNames.add(basePackage); // 把所有的basePackage收集在一个集合里,方便校验是否有重复
			}
			
			switch (config) {
			case "c3p0":
				// 校验是否存在 c3p0-config.xml 文件
				if(!fqueryResource.exist("c3p0-config.xml")) {
					throw new RepositoryException("fastquery.json 配置文件中, config设置了c3p0,因此依赖c3p0-config.xml配置文件,可是没有找到.");
				}
				// 校验指定的数据源名称是否正确
				if(dataSourceName!=null && !getC3p0Configs(fqueryResource.getResourceAsStream("c3p0-config.xml")).containsKey(dataSourceName)) {
					throw new RepositoryException("fastquery.json 配置文件中, 指定了数据源为"+dataSourceName+",而在c3p0-config.xml中,找不到对该数据源的配置.");
				}
				break;
			case "jdbc":
				// 校验是否存在 jdbc-config.xml 文件
				if(!fqueryResource.exist("jdbc-config.xml")){
					throw new RepositoryException("fastquery.json 配置文件中, config设置了jdbc,因此依赖jdbc-config.xml配置文件,可是没有找到.");
				}
				// getJdbcConfigs 里面有对流进行关闭
				if(!getJdbcConfigs(fqueryResource.getResourceAsStream("jdbc-config.xml")).containsKey(dataSourceName)){
					throw new RepositoryException("fastquery.json 配置文件中, 指定了数据源为"+dataSourceName+",而在jdbc-config.xml中,找不到对该数据源的配置.");
				}
				// 校验指定的数据源名称是否正确
				break;
			default:
				throw new RepositoryException("fastquery.json 配置文件中, config设置了"+config+",不支持该属性值");
			}
			
			// 校验数据源的名称是否配置正确
			
			
			// 检验 end
			
			// 收集数据 用做校验
			if(dataSourceName==null){
				dataSourceNames.add(dataSourceName);	
			}
			// 收集数据 用做校验 End
			
			
			fqs.add(fQueryPropertie);
		}
	
		// 校验 fastquery.json
		for (int i = 0; i < dataSourceNames.size(); i++) {
			if(Collections.frequency(dataSourceNames, dataSourceNames.get(i))>1) {
				throw new RepositoryException("fastquery.json 配置文件中 \"dataSourceName\"=\""+dataSourceNames.get(i) + "\" 不能重复出现.");
			}
		}
		for (int j = 0; j < bpNames.size(); j++) {
			if( Collections.frequency(bpNames, bpNames.get(j)) >1) {
				throw new RepositoryException("fastquery.json 配置文件中, basePackages中的元素\""+bpNames.get(j)+"\"不能重复出现.");
			}
		}
		// 校验 fastquery.json End
				
		// 还有其他校验... 待续 可以扩展
		
		return fqs;
	}	
}
