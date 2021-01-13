/*
 * Copyright 2015-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kr.gooroom.gpms.account.service;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Session java bean
 * 
 * @author HNC
 * @version 1.0
 * @since 1.8
 */

@SuppressWarnings("serial")
public class SessionVO implements Serializable {

	String userId;
	String userName;
	String displayName;
	String email;
	String password;
	long quota = 0L;
	String gpkiAuthValue;
	String deptCd;
	String headDeptCd;
	Timestamp loginDate;
	String tokenId;
	long rootFolderId = 0L;
	long trashFolderId = 0L;
	long revisionFolderId = 0L;

	public String getAdminPermission() {
		return adminPermission;
	}

	public void setAdminPermission(String adminPermission) {
		this.adminPermission = adminPermission;
	}

	String adminPermission;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public long getQuota() {
		return quota;
	}

	public void setQuota(long quota) {
		this.quota = quota;
	}

	public String getGpkiAuthValue() {
		return gpkiAuthValue;
	}

	public void setGpkiAuthValue(String gpkiAuthValue) {
		this.gpkiAuthValue = gpkiAuthValue;
	}

	public String getDeptCd() {
		return deptCd;
	}

	public void setDeptCd(String deptCd) {
		this.deptCd = deptCd;
	}

	public String getHeadDeptCd() {
		return headDeptCd;
	}

	public void setHeadDeptCd(String headDeptCd) {
		this.headDeptCd = headDeptCd;
	}

	public Timestamp getLoginDate() {
		return loginDate;
	}

	public void setLoginDate(Timestamp loginDate) {
		this.loginDate = loginDate;
	}

	public String getTokenId() {
		return tokenId;
	}

	public void setTokenId(String tokenId) {
		this.tokenId = tokenId;
	}

	public long getRootFolderId() {
		return rootFolderId;
	}

	public void setRootFolderId(long rootFolderId) {
		this.rootFolderId = rootFolderId;
	}

	public long getTrashFolderId() {
		return trashFolderId;
	}

	public void setTrashFolderId(long trashFolderId) {
		this.trashFolderId = trashFolderId;
	}

	public long getRevisionFolderId() {
		return revisionFolderId;
	}

	public void setRevisionFolderId(long revisionFolderId) {
		this.revisionFolderId = revisionFolderId;
	}

}
