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
import java.util.Date;

@SuppressWarnings("serial")
public class ServerAddrInfoVO implements Serializable {

	private String gkmIp;
	private String gkmUrl;

	private String glmIp;
	private String glmUrl;

	private String grmIp;
	private String grmUrl;

	private String gpmsIp;
	private String gpmsUrl;

	private Date modifyDate;

	public Date getModifyDate() {
		return modifyDate;
	}

	public void setModifyDate(Date modifyDate) {
		this.modifyDate = modifyDate;
	}

	public String getGkmIp() {
		return gkmIp;
	}

	public void setGkmIp(String gkmIp) {
		this.gkmIp = gkmIp;
	}

	public String getGkmUrl() {
		return gkmUrl;
	}

	public void setGkmUrl(String gkmUrl) {
		this.gkmUrl = gkmUrl;
	}

	public String getGlmIp() {
		return glmIp;
	}

	public void setGlmIp(String glmIp) {
		this.glmIp = glmIp;
	}

	public String getGlmUrl() {
		return glmUrl;
	}

	public void setGlmUrl(String glmUrl) {
		this.glmUrl = glmUrl;
	}

	public String getGrmIp() {
		return grmIp;
	}

	public void setGrmIp(String grmIp) {
		this.grmIp = grmIp;
	}

	public String getGrmUrl() {
		return grmUrl;
	}

	public void setGrmUrl(String grmUrl) {
		this.grmUrl = grmUrl;
	}

	public String getGpmsIp() {
		return gpmsIp;
	}

	public void setGpmsIp(String gpmsIp) {
		this.gpmsIp = gpmsIp;
	}

	public String getGpmsUrl() {
		return gpmsUrl;
	}

	public void setGpmsUrl(String gpmsUrl) {
		this.gpmsUrl = gpmsUrl;
	}

}
