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

package kr.gooroom.gpms.gkm.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.UUID;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AccessDescription;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSAlgorithm;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.PKCS8Generator;
import org.bouncycastle.operator.*;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.jcajce.JcePKCSPBEOutputEncryptorBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.util.io.pem.PemWriter;

import kr.gooroom.gpms.common.GPMSConstants;
import kr.gooroom.gpms.common.service.CertForServerVO;

public class CertificateUtils {

	private static final Properties prop = new Properties();
	private static final String GOOROOM_PROPERTIES = "/properties/gooroomapi.properties";

	static {
		InputStream is = GPMSConstants.class.getClassLoader().getResourceAsStream(GOOROOM_PROPERTIES);
		try {
			prop.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static enum RevocationReason {
		unspecified, keyCompromise, caCompromise, affiliationChanged, superseded, cessationOfOperation, certificateHold,
		unused, removeFromCRL, privilegeWithdrawn, ACompromise;

		public static RevocationReason[] reasons = { unspecified, keyCompromise, caCompromise, affiliationChanged,
				superseded, cessationOfOperation, privilegeWithdrawn };

		@Override
		public String toString() {
			return name() + " (" + ordinal() + ")";
		}
	}

	private String CA_OCSP_ENDPOINT_URL = prop.getProperty("gooroom.ocsp.url");

	public CertificateVO createGcspCertificate (String cn, Date validToDate, BigInteger newSerialNo, String pw) throws Exception {

		CertificateVO vo = new CertificateVO();

		// 루트 인증서 로드
		CertificateFactory fact = CertificateFactory.getInstance("X.509");
		FileInputStream is = new FileInputStream(GPMSConstants.ROOT_CERTPATH + "/" + GPMSConstants.ROOT_CERTFILENAME);
		X509Certificate rootCert = (X509Certificate) fact.generateCertificate(is);
		is.close();

		// 루트 개인키 로드
		File privKeyFile = new File(GPMSConstants.ROOT_KEYPATH + "/" + GPMSConstants.ROOT_KEYFILENAME);
		// PemReader pemReader = new PemReader(new FileReader(privKeyFile));
		PemReader pemReader = new PemReader(new InputStreamReader(new FileInputStream(privKeyFile), "UTF-8"));
		PemObject pemObject = pemReader.readPemObject();
		pemReader.close();
		byte[] privKeyBytes = pemObject.getContent();
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(privKeyBytes);
		PrivateKey rootPriKey = keyFactory.generatePrivate(ks);

		/*
		 * *********************************************************************
		 * *******************
		 */
		KeyPairGenerator kpGen = KeyPairGenerator.getInstance("RSA");
		kpGen.initialize(2048, new SecureRandom());
		KeyPair pair = kpGen.generateKeyPair();
		PublicKey pubKey = pair.getPublic();

		// 1.
		X500Name issuer = new X500Name(rootCert.getSubjectX500Principal().getName());
		// 2.
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		Date notBefore = cal.getTime();
		// 3.
		Locale dateLocale = new Locale.Builder().setLanguage("ko").setRegion("KO").build();
		// 4.
		X500Name subject = new X500Name("cn=" + cn);
		// 5.
		SubjectPublicKeyInfo publicKeyInfo = SubjectPublicKeyInfo.getInstance(pubKey.getEncoded());

		X509v3CertificateBuilder builder = new X509v3CertificateBuilder(issuer, newSerialNo, notBefore, validToDate,
				dateLocale, subject, publicKeyInfo);

		// builder 에 전자 서명
		X509CertificateHolder holder = builder.build(createSigner(rootPriKey));
		// 인증서 생성
		X509Certificate cert = new JcaX509CertificateConverter().getCertificate(holder);

		vo.setCertificate(cert);
		vo.setPrivateKey(rootPriKey);

		PemObject certPemObject = new PemObject("CERTIFICATE", cert.getEncoded());
		ByteArrayOutputStream certBs = new ByteArrayOutputStream();
		PemWriter certPemWriter = new PemWriter(new OutputStreamWriter(certBs, "UTF-8"));
		certBs.close();
		try {
			certPemWriter.writeObject(certPemObject);
		} finally {
			certPemWriter.close();
		}
		vo.setCertificatePem(certBs.toString("UTF-8"));

		PemObject priPemObject;
		if (pw.isEmpty())
		{
			priPemObject = new PemObject("RSA PRIVATE KEY", pair.getPrivate().getEncoded());
		} else {
			// AES256 개인키 암호화
			OutputEncryptor encryptor = new JcePKCSPBEOutputEncryptorBuilder(CMSAlgorithm.AES256_CBC)
					.setProvider("BC").build(pw.toCharArray());
			PKCS8Generator encryt = new PKCS8Generator(PrivateKeyInfo.getInstance(pair.getPrivate().getEncoded()), encryptor);
			priPemObject = encryt.generate();
		}
		ByteArrayOutputStream priBs = new ByteArrayOutputStream();
		PemWriter priPemWriter = new PemWriter(new OutputStreamWriter(priBs, "UTF-8"));
		priBs.close();
		try {
			priPemWriter.writeObject(priPemObject);
		} finally {
			priPemWriter.close();
		}
		vo.setPrivateKeyPem(priBs.toString("UTF-8"));

		return vo;
	}

	public X509Certificate signCSR(Reader pemcsr, Date validToDate, BigInteger newSerialNo) throws Exception {

		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

		// System.out.println("=====================================================");

		// 루트 인증서 로드
		CertificateFactory fact = CertificateFactory.getInstance("X.509");
		FileInputStream is = new FileInputStream(GPMSConstants.ROOT_CERTPATH + "/" + GPMSConstants.ROOT_CERTFILENAME);
		X509Certificate serverCert = (X509Certificate) fact.generateCertificate(is);
		is.close();

		// 서버 개인키 로드
		File privKeyFile = new File(GPMSConstants.ROOT_KEYPATH + "/" + GPMSConstants.ROOT_KEYFILENAME);
		// PemReader pemReader = new PemReader(new FileReader(privKeyFile));
		PemReader pemReader = new PemReader(new InputStreamReader(new FileInputStream(privKeyFile), "UTF-8"));
		PemObject pemObject = pemReader.readPemObject();
		pemReader.close();

		byte[] privKeyBytes = pemObject.getContent();
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(privKeyBytes);
		PrivateKey serverPriKey = keyFactory.generatePrivate(ks);

		try (PEMParser reader = new PEMParser(pemcsr)) {

			PKCS10CertificationRequest csr = (PKCS10CertificationRequest) reader.readObject();

			// 승인자
			X500Name issuer = new X500Name(serverCert.getSubjectX500Principal().getName());

			// 인증서 유효기간 시작일
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, -7);
			Date from = cal.getTime();

			X509v3CertificateBuilder builder = new X509v3CertificateBuilder(issuer, newSerialNo, from, validToDate,
					csr.getSubject(), csr.getSubjectPublicKeyInfo());

			// Add the OCSP endpoint
			AccessDescription ocsp = new AccessDescription(AccessDescription.id_ad_ocsp,
					new GeneralName(GeneralName.uniformResourceIdentifier, CA_OCSP_ENDPOINT_URL));
			ASN1EncodableVector authInfoAccessASN = new ASN1EncodableVector();
			authInfoAccessASN.add(ocsp);
			builder.addExtension(Extension.authorityInfoAccess, false, new DERSequence(authInfoAccessASN));

			// ContentSigner signer = createSigner(serverPriKey);
			X509CertificateHolder holder = builder.build(createSigner(serverPriKey));
			X509Certificate cert = new JcaX509CertificateConverter().getCertificate(holder);
			return cert;

		} catch (CertificateException | CertIOException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private ContentSigner createSigner(PrivateKey privateKey) {

		try {

			AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find("SHA256withRSA");
			AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);

			return new BcRSAContentSignerBuilder(sigAlgId, digAlgId)
					.build(PrivateKeyFactory.createKey(privateKey.getEncoded()));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Could not create content signer.", e);
		}

	}


	public boolean verifyServerCertificate(X509CertificateHolder holder) throws Exception {

		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		// 루트 인증서 로드
		CertificateFactory fact = CertificateFactory.getInstance("X.509");
		FileInputStream is = new FileInputStream(GPMSConstants.ROOT_CERTPATH + "/" + GPMSConstants.ROOT_CERTFILENAME);
		X509Certificate rootCert = (X509Certificate) fact.generateCertificate(is);
		is.close();

		PublicKey rootPubKey = rootCert.getPublicKey();

		ContentVerifierProvider contentVerifierProvider = new JcaContentVerifierProviderBuilder()
				.setProvider("BC").build(rootPubKey);

		return holder.isSignatureValid(contentVerifierProvider);
	}

	public BigInteger getBigIntegerFromUuid(UUID randomUUID) {
		ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
		bb.putLong(randomUUID.getMostSignificantBits());
		bb.putLong(randomUUID.getLeastSignificantBits());
		return new BigInteger(bb.array());
	}

	public int getDayCountToDate(String toDate) {

		int re = -1;
		try {
			Calendar cal = Calendar.getInstance();
			Date beginDate = cal.getTime();
			Date endDate = new SimpleDateFormat("yyyy-MM-dd").parse(toDate);

			// 시간차이를 시간,분,초를 곱한 값으로 나누면 하루 단위가 나옴
			long diff = endDate.getTime() - beginDate.getTime();
			float diffDays = (float) diff / (float) (24 * 60 * 60 * 1000);

			re = (int) Math.ceil(diffDays);

		} catch (Exception ex) {

			re = -1;
			ex.printStackTrace();
		}

		return re;
	}

	public CertForServerVO getServerCertificate() {

		CertForServerVO certificateVO = new CertForServerVO();

		// GKM Certificate read
		try {

			byte[] inKmFile = FileUtils.readContent(new File(GPMSConstants.GKM_SERVER_CERTFILE));
			certificateVO.setGkmCertificate(new String(inKmFile, "UTF-8"));

			byte[] inLmFile = FileUtils.readContent(new File(GPMSConstants.GLM_SERVER_CERTFILE));
			certificateVO.setGlmCertificate(new String(inLmFile, "UTF-8"));

			byte[] inRmFile = FileUtils.readContent(new File(GPMSConstants.GRM_SERVER_CERTFILE));
			certificateVO.setGrmCertificate(new String(inRmFile, "UTF-8"));

		} catch (Exception ex) {

			certificateVO = null;
			ex.printStackTrace();
		}

		return certificateVO;
	}

}
