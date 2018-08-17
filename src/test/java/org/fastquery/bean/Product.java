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

/**
 * 
 * @author mei.sir@aliyun.cn
 */
public class Product {
	
 	private Integer pid;
	private Integer lid;
	private String pname;
	private String description;
	
	public Product() {
		System.out.println("Product...........");
	}
	
	public Product(Integer pid, Integer lid, String pname) {
		super();
		this.pid = pid;
		this.lid = lid;
		this.pname = pname;
	}
	
	public Integer getPid() {
		return pid;
	}
	public Integer getLid() {
		return lid;
	}
	public String getPname() {
		return pname;
	}
	public String getDescription() {
		return description;
	}
	public void setPid(Integer pid) {
		this.pid = pid;
	}
	public void setLid(Integer lid) {
		this.lid = lid;
	}
	public void setPname(String pname) {
		this.pname = pname;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
}
