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

package org.fastquery.bean;

import org.fastquery.core.Id;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public class PManager {

	@Id
	private Long pmuid; // 物管员帐号ID
	private Long punitId; // 物业单位ID
	private String mobile; // 唯一约束，登录帐号
	private String password;// 密码
	private Byte isActive = 0;// 是否激活 0未激活，1激活
	private Byte isdm = 0; // 是否是设备管理员（0否，1是）可管理门禁，蓝牙设备
	private Byte isReg = 0; // 是否可进行人员登记(0:否,1:是，默认:0) 可操作people
	private Byte pmRole = 0; // 物管员角色 (0:其他,1:物业主任,2:保安经理,3:保安队长,4:保安,88:维修人员/技工)
	private String realName; // 真实姓名
	private Byte gender = 0; // 性别（0:保密,1:男,2:女）
	private String head; // 头像
	private Byte isOnline = 0; // 是否在线 (0:否,1:是，默认:0)
	private String hxuser; // 环信帐号（用于门禁呼叫）
	private String hxpass; // 环信密码（用于门禁呼叫）
	private String hxRoomId; // 环信聊天房间Id（用于对讲广播）
	private Long createUid = 0L;// 默认:0 创建人UID
	private Long lastUpdateUid;// 默认:0 最后修改人UID --云平台

	public PManager() {
	}

	public PManager(Long punitId, String mobile, String password, Byte isdm, Byte isReg,
			Byte pmRole, String realName, Byte gender) {
		this.punitId = punitId;
		this.mobile = mobile;
		this.password = password;
		this.isdm = isdm;
		this.isReg = isReg;
		this.pmRole = pmRole;
		this.realName = realName;
		this.gender = gender;
	}

	public Long getPmuid() {
		return pmuid;
	}

	public Long getPunitId() {
		return punitId;
	}

	public String getMobile() {
		return mobile;
	}

	public String getPassword() {
		return password;
	}

	public Byte getIsActive() {
		return isActive;
	}

	public Byte getIsdm() {
		return isdm;
	}

	public Byte getIsReg() {
		return isReg;
	}

	public Byte getPmRole() {
		return pmRole;
	}

	public String getRealName() {
		return realName;
	}

	public Byte getGender() {
		return gender;
	}

	public String getHead() {
		return head;
	}

	public Byte getIsOnline() {
		return isOnline;
	}

	public String getHxuser() {
		return hxuser;
	}

	public String getHxpass() {
		return hxpass;
	}

	public String getHxRoomId() {
		return hxRoomId;
	}
	
	public Long getCreateUid() {
		return createUid;
	}

	public Long getLastUpdateUid() {
		return lastUpdateUid;
	}

	public void setPmuid(Long pmuid) {
		this.pmuid = pmuid;
	}

	public void setPunitId(Long punitId) {
		this.punitId = punitId;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setIsActive(Byte isActive) {
		this.isActive = isActive;
	}

	public void setIsdm(Byte isdm) {
		this.isdm = isdm;
	}

	public void setIsReg(Byte isReg) {
		this.isReg = isReg;
	}

	public void setPmRole(Byte pmRole) {
		this.pmRole = pmRole;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public void setGender(Byte gender) {
		this.gender = gender;
	}

	public void setHead(String head) {
		this.head = head;
	}

	public void setIsOnline(Byte isOnline) {
		this.isOnline = isOnline;
	}

	public void setHxuser(String hxuser) {
		this.hxuser = hxuser;
	}

	public void setHxpass(String hxpass) {
		this.hxpass = hxpass;
	}

	public void setHxRoomId(String hxRoomId) {
		this.hxRoomId = hxRoomId;
	}

	public void setCreateUid(Long createUid) {
		this.createUid = createUid;
	}

	public void setLastUpdateUid(Long lastUpdateUid) {
		this.lastUpdateUid = lastUpdateUid;
	}
}
