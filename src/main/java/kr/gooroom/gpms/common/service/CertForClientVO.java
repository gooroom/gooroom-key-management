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

package kr.gooroom.gpms.common.service;

import java.io.Serializable;

@SuppressWarnings("serial")
public class CertForClientVO implements Serializable {

	private String certInfo;
	private String clientCN;
	private String clientName;
	private String clientOU;

	private String defaultClientGroupId;

	private String expireDate;
	private String expireDateSlim;

	private String clientStatus;
	private String regUserId;
	private String comment;
	private String serialNo;

	private String chgTp;

	public String getCertInfo() {
		return certInfo;
	}

	public void setCertInfo(String certInfo) {
		this.certInfo = certInfo;
	}

	public String getClientCN() {
		return clientCN;
	}

	public void setClientCN(String clientCN) {
		this.clientCN = clientCN;
	}

	public String getClientOU() {
		return clientOU;
	}

	public void setClientOU(String clientOU) {
		this.clientOU = clientOU;
	}

	public String getExpireDate() {
		return expireDate;
	}

	public void setExpireDate(String expireDate) {
		this.expireDate = expireDate;
		if (expireDate != null && expireDate.indexOf("-") > -1) {
			this.expireDateSlim = expireDate.replaceAll("-", "");
		}
	}

	public String getExpireDateSlim() {
		return expireDateSlim;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public String getClientStatus() {
		return clientStatus;
	}

	public void setClientStatus(String clientStatus) {
		this.clientStatus = clientStatus;
	}

	public String getRegUserId() {
		return regUserId;
	}

	public void setRegUserId(String regUserId) {
		this.regUserId = regUserId;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getChgTp() {
		return chgTp;
	}

	public void setChgTp(String chgTp) {
		this.chgTp = chgTp;
	}

	public String getSerialNo() {
		return serialNo;
	}

	public void setSerialNo(String serialNo) {
		this.serialNo = serialNo;
	}

	public String getDefaultClientGroupId() {
		return defaultClientGroupId;
	}

	public void setDefaultClientGroupId(String defaultClientGroupId) {
		this.defaultClientGroupId = defaultClientGroupId;
	}

	public void setExpireDateSlim(String expireDateSlim) {
		this.expireDateSlim = expireDateSlim;
	}

}
