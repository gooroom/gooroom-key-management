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

package kr.gooroom.gpms.gkm.controller.factory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyFactory;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import kr.gooroom.gpms.common.GPMSConstants;
import kr.gooroom.gpms.gkm.controller.data.OCSPCertificate;

public class OCSPCertificateFactory {

	private static final String OCSP_CERTIFICATE_PK_PATH = GPMSConstants.ROOT_KEYPATH + "/"
			+ GPMSConstants.ROOT_KEYFILENAME;
	private static final String OCSP_CERTIFICATE_PATH = GPMSConstants.ROOT_CERTPATH + "/"
			+ GPMSConstants.ROOT_CERTFILENAME;

	public static OCSPCertificate getOCSPCertificate()
			throws FileNotFoundException, IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		OCSPCertificate retour = new OCSPCertificate();
		X509Certificate caOcsp;
		X509CertificateHolder[] caOcspHolder = new X509CertificateHolder[1];
		ContentSigner ocspSignKey = null;
		try {
			caOcsp = readCertificate(OCSP_CERTIFICATE_PATH);
			final byte[] encoded = caOcsp.getEncoded();
			caOcspHolder[0] = new X509CertificateHolder(encoded);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// Get responder's private key
		PrivateKey caOcspKey = readPrivateKey(OCSP_CERTIFICATE_PK_PATH);
		JcaContentSignerBuilder jca = new JcaContentSignerBuilder("SHA256WITHRSA");
		try {
			ocspSignKey = jca.build(caOcspKey);
		} catch (OperatorCreationException e) {
			e.printStackTrace();
		}
		retour.setCertificateChain(caOcspHolder);
		retour.setSigner(ocspSignKey);
		return retour;
	}

	private static PrivateKey readPrivateKey(String path)
			throws FileNotFoundException, IOException, NoSuchAlgorithmException, InvalidKeySpecException {

		File privKeyFile = new File(path);
		// PemReader pemReader = new PemReader(new FileReader(privKeyFile));
		PemReader pemReader = new PemReader(new InputStreamReader(new FileInputStream(privKeyFile), "UTF-8"));
		PemObject pemObject = pemReader.readPemObject();
		pemReader.close();
		byte[] privKeyBytes = pemObject.getContent();
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(privKeyBytes);
		PrivateKey rootPriKey = keyFactory.generatePrivate(ks);
		return rootPriKey;
	}

	private static X509Certificate readCertificate(String keyFileClassPath) throws IOException, KeyStoreException,
			NoSuchProviderException, NoSuchAlgorithmException, CertificateException {
		InputStream fis = null;
		ByteArrayInputStream bais = null;
		try {

			// 루트 인증서 로드
			CertificateFactory fact = CertificateFactory.getInstance("X.509");
			FileInputStream is = new FileInputStream(keyFileClassPath);
			X509Certificate rootCert = (X509Certificate) fact.generateCertificate(is);
			return rootCert;

		} finally {
			IOUtils.closeQuietly(fis);
			IOUtils.closeQuietly(bais);
		}
	}
}
