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

package kr.gooroom.gpms.gkm.controller.data;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.operator.ContentSigner;

public class OCSPCertificate {
	private X509CertificateHolder[] certificateChain;

	private ContentSigner signer;

	public X509CertificateHolder[] getCertificateChain() {
		return certificateChain;
	}

	public void setCertificateChain(X509CertificateHolder[] certificateChain) {
		this.certificateChain = certificateChain;
	}

	public ContentSigner getSigner() {
		return signer;
	}

	public void setSigner(ContentSigner signer) {
		this.signer = signer;
	}
}
