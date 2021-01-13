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
public class CertForServerVO implements Serializable {

	private String gkmCertificate;
	private String glmCertificate;
	private String grmCertificate;

	public String getGkmCertificate() {
		return gkmCertificate;
	}

	public void setGkmCertificate(String gkmCertificate) {
		this.gkmCertificate = gkmCertificate;
	}

	public String getGlmCertificate() {
		return glmCertificate;
	}

	public void setGlmCertificate(String glmCertificate) {
		this.glmCertificate = glmCertificate;
	}

	public String getGrmCertificate() {
		return grmCertificate;
	}

	public void setGrmCertificate(String grmCertificate) {
		this.grmCertificate = grmCertificate;
	}

}
