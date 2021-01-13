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

package kr.gooroom.gpms.gkm.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ocsp.ResponderID;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import org.bouncycastle.cert.ocsp.BasicOCSPRespBuilder;
import org.bouncycastle.cert.ocsp.CertificateStatus;
import org.bouncycastle.cert.ocsp.OCSPException;
import org.bouncycastle.cert.ocsp.OCSPReq;
import org.bouncycastle.cert.ocsp.OCSPResp;
import org.bouncycastle.cert.ocsp.OCSPRespBuilder;
import org.bouncycastle.cert.ocsp.RespID;
import org.bouncycastle.cert.ocsp.RevokedStatus;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.gooroom.gpms.common.service.StatusVO;
import kr.gooroom.gpms.gkm.controller.data.OCSPCertificate;
import kr.gooroom.gpms.gkm.controller.factory.OCSPCertificateFactory;
import kr.gooroom.gpms.gkm.service.CertificateService;

@RestController
public class GkmOCSPResponer {

	@Resource(name = "certificateService")
	private CertificateService certificateService;

	private final static ASN1ObjectIdentifier ID_ASN1 = new ASN1ObjectIdentifier("1.3.14.3.2.26");
	private final static ASN1ObjectIdentifier ID_NONCE = new ASN1ObjectIdentifier("1.3.6.1.5.5.7.48.1.2");

	@PostMapping("/ocsp")
	public void checkOCSP(HttpServletRequest req, HttpServletResponse res, ModelMap model) {

		try {

			OCSPReq ocspreq = getOcspRequest(req);

			// boolean isGood = setRequestData(req, requestDataList, ocspreq);
			boolean isGood = setRequestData(ocspreq);

			OCSPCertificate ocspCertificate = OCSPCertificateFactory.getOCSPCertificate();

			OCSPResp ocspresp = getSignedOcspResponse(ocspreq, ocspCertificate, isGood);
			setOcspResponse(res, ocspresp);

			// System.out.println("[ checkOCSP - e n d ] - (" +
			// Calendar.getInstance().getTimeInMillis() + ") - " + req);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void setOcspResponse(HttpServletResponse resp, OCSPResp ocspresp) throws IOException {

		byte[] respBytes = ocspresp.getEncoded();
		resp.setContentType("application/ocsp-response");
		resp.setContentLength(respBytes.length);
		resp.getOutputStream().write(respBytes);
	}

	private OCSPResp getSignedOcspResponse(OCSPReq ocspreq, OCSPCertificate ocspCertificate, boolean isGood) {
		int responseCode = OCSPRespBuilder.INTERNAL_ERROR;
		ResponderID respondID = new ResponderID(ocspCertificate.getCertificateChain()[0].getSubject());
		RespID respID = new RespID(respondID);
		BasicOCSPRespBuilder bOCSPbuilder = new BasicOCSPRespBuilder(respID);

		// Date dateRevoke = new Date();
		// bOCSPbuilder.addResponse(ocspreq.getRequestList()[0].getCertID(), new
		// org.bouncycastle.cert.ocsp.UnknownStatus());
		bOCSPbuilder.addResponse(ocspreq.getRequestList()[0].getCertID(), getStatus(isGood));

		Extension ext = ocspreq.getExtension(ID_NONCE);
		bOCSPbuilder.setResponseExtensions(new Extensions(new Extension[] { ext }));
		BasicOCSPResp basicResponse = null;
		Date myDate = new Date(1000000);
		OCSPResp ocspResp = null;
		try {
			// Signature
			basicResponse = bOCSPbuilder.build(ocspCertificate.getSigner(), ocspCertificate.getCertificateChain(),
					myDate);
			responseCode = OCSPRespBuilder.SUCCESSFUL;
			ocspResp = new OCSPRespBuilder().build(responseCode, basicResponse);
		} catch (OCSPException e) {
			// System.out.println(e.getMessage() + " : " + e);
			e.printStackTrace();
		}
		return ocspResp;
	}

	private CertificateStatus getStatus(boolean isGood) {
		if (isGood) {
			return CertificateStatus.GOOD;
		} else {
			Date revocationDate = new Date();
			revocationDate.setTime(revocationDate.getTime() - TimeUnit.DAYS.toMillis(100));
			return new RevokedStatus(revocationDate, 2);
		}
	}

	// private boolean setRequestData(HttpServletRequest req, List<RequestData>
	// reqData, OCSPReq ocspreq) throws IOException {
	private boolean setRequestData(OCSPReq ocspreq) throws IOException {

		boolean result = false;

		org.bouncycastle.cert.ocsp.Req[] requestList = ocspreq.getRequestList();
		if (requestList.length <= 0) {
			System.out.println("No OCSP requests found");
			// ??
		}

		for (int i = 0; i < requestList.length; i++) {

			BigInteger certSerialNo = requestList[i].getCertID().getSerialNumber();
			ASN1ObjectIdentifier algID = requestList[i].getCertID().getHashAlgOID();
			if (ID_ASN1.equals(algID)) {
				// tmpReq.setHashAlgorithmOID(algID);
				// ??
			} else {
				throw new IllegalArgumentException();
			}

			// USE DATABASE
			StatusVO statusVO = new StatusVO();
			try {
				statusVO = certificateService.isRevoked(String.valueOf(certSerialNo));
				if (statusVO != null) {
					if ("true".equals(statusVO.getMessage())) {
						// REVOKED
						result = false;
					} else {
						// NOT REVOKED
						result = true;
					}
				} else {
					// QUERY ERROR
					result = false;
				}
			} catch (Exception ex) {
				// QUERY ERROR
				result = false;
				ex.printStackTrace();
			}
		}

		return result;
	}

	private OCSPReq getOcspRequest(HttpServletRequest req) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		// System.out.println("Recuperation de la requete");
		IOUtils.copy(req.getInputStream(), baos);
		// System.out.println("Verification de la requete recue");
		byte[] reqBytes = checkByteArray(baos);
		// System.out.println("Recuperation des data de la requete");
		OCSPReq ocspreq = new OCSPReq(reqBytes);
		return ocspreq;
	}

	private byte[] checkByteArray(ByteArrayOutputStream baos) {
		byte[] reqBytes = baos.toByteArray();
		if ((reqBytes == null) || (reqBytes.length == 0)) {
			// System.out.println("No Request bytes");
			throw new IllegalArgumentException("No request bytes");
		}
		return reqBytes;
	}
}
