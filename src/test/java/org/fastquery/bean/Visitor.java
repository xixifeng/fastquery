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
import org.fastquery.core.Transient;

/**
 * 
 * 普通实体
 * 
 */
public class Visitor {

	@Id
	private Long id;
	private Long punitId = 0L;
	private String vname;
	private String idCard;
	private String mobile;
	private String email;
	private Byte gender = (byte) 0;
	private String toAddr;
	private String remark;
	private Long lastDate;
	private Long createDate;
	private String iden;
	private Integer dId;

	public Visitor() {
	}

	public Visitor(Long punitId, String vname, String idCard, String mobile, String email, Byte gender, String toAddr, String remark, Long lastDate,
			String iden, Integer dId) {
		this.punitId = punitId;
		this.vname = vname;
		this.idCard = idCard;
		this.mobile = mobile;
		this.email = email;
		this.gender = gender;
		this.toAddr = toAddr;
		this.remark = remark;
		this.lastDate = lastDate;
		this.iden = iden;
		this.dId = dId;
	}

	public Long getId() {
		return id;
	}

	public Long getPunitId() {
		return punitId;
	}

	public String getVname() {
		return vname;
	}

	public String getIdCard() {
		return idCard;
	}

	public String getMobile() {
		return mobile;
	}

	public String getEmail() {
		return email;
	}

	public Byte getGender() {
		return gender;
	}

	public String getToAddr() {
		return toAddr;
	}

	public String getRemark() {
		return remark;
	}

	public Long getLastDate() {
		return lastDate;
	}

	public Long getCreateDate() {
		return createDate;
	}

	public String getIden() {
		return iden;
	}

	public void setIden(String iden) {
		this.iden = iden;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setPunitId(Long punitId) {
		this.punitId = punitId;
	}

	public void setVname(String vname) {
		this.vname = vname;
	}

	public void setIdCard(String idCard) {
		this.idCard = idCard;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setGender(Byte gender) {
		this.gender = gender;
	}

	public void setToAddr(String toAddr) {
		this.toAddr = toAddr;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public void setLastDate(Long lastDate) {
		this.lastDate = lastDate;
	}

	public void setCreateDate(Long createDate) {
		this.createDate = createDate;
	}

	public Integer getdId() {
		return dId;
	}

	public void setdId(Integer dId) {
		this.dId = dId;
	}

	@Transient
	private String description;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
