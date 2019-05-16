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

package org.fastquery.bean;

import org.fastquery.core.Id;

/**
 * 
 * @author mei.sir@aliyun.cn
 */
public class City {

	@Id
	private Integer id;
	private Integer code;
	private String cityAbb;
	private String cityName;
		
	public City() {
	}
	public City(Integer id, Integer code, String cityAbb, String cityName) {
		this.id = id;
		this.code = code;
		this.cityAbb = cityAbb;
		this.cityName = cityName;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getCode() {
		return code;
	}
	public void setCode(Integer code) {
		this.code = code;
	}
	public String getCityAbb() {
		return cityAbb;
	}
	public void setCityAbb(String cityAbb) {
		this.cityAbb = cityAbb;
	}
	public String getCityName() {
		return cityName;
	}
	public void setCityName(String cityName) {
		this.cityName = cityName;
	}
	@Override
	public String toString() {
		return "City [id=" + id + ", code=" + code + ", cityAbb=" + cityAbb + ", cityName=" + cityName + "]";
	}
}
