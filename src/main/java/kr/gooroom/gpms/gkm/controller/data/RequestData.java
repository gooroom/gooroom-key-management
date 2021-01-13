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

import java.math.BigInteger;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;

public class RequestData {
	private ASN1ObjectIdentifier HashAlgorithmOID;
	private byte[] IssuerNameHash;
	private byte[] IssuerKeyHash;
	private BigInteger SerialNumber;

	public RequestData() {

	}

	public RequestData(ASN1ObjectIdentifier hashAlgoOID, byte[] issuerNameHash, byte[] issuerKeyHash,
			BigInteger serialNumber) {

		this.HashAlgorithmOID = hashAlgoOID;
		this.IssuerNameHash = issuerNameHash;
		this.IssuerKeyHash = issuerKeyHash;
		this.SerialNumber = serialNumber;

	}

	public ASN1ObjectIdentifier getHashAlgorithmOID() {
		return HashAlgorithmOID;
	}

	public void setHashAlgorithmOID(ASN1ObjectIdentifier hashAlgorithmOID) {
		HashAlgorithmOID = hashAlgorithmOID;
	}

	public byte[] getIssuerNameHash() {
		return IssuerNameHash;
	}

	public void setIssuerNameHash(byte[] issuerNameHash) {
		IssuerNameHash = issuerNameHash;
	}

	public byte[] getIssuerKeyHash() {
		return IssuerKeyHash;
	}

	public void setIssuerKeyHash(byte[] issuerKeyHash) {
		IssuerKeyHash = issuerKeyHash;
	}

	public BigInteger getSerialNumber() {
		return SerialNumber;
	}

	public void setSerialNumber(BigInteger serialNumber) {
		SerialNumber = serialNumber;
	}
}
