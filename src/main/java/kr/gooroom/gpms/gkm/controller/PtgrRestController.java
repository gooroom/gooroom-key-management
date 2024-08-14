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

import kr.gooroom.gpms.common.GPMSConstants;
import kr.gooroom.gpms.common.service.ResultVO;
import kr.gooroom.gpms.common.service.StatusVO;
import kr.gooroom.gpms.gkm.controller.data.PtgrCertData;
import kr.gooroom.gpms.gkm.controller.data.PtgrNonceData;
import kr.gooroom.gpms.gkm.service.PtgrNonceService;
import kr.gooroom.gpms.gkm.service.PtgrNonceVO;
import kr.gooroom.gpms.gkm.utils.CertificateUtils;
import kr.gooroom.gpms.gkm.utils.CertificateVO;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.crypto.Cipher;
import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping(value = "/v1/portable")
public class PtgrRestController {

    private static final Logger logger = LoggerFactory.getLogger(PtgrRestController.class);

    @Resource(name="ptgrCertService")
    private PtgrNonceService ptgrNonceService;

    @RequestMapping(value = "/cert", method = { RequestMethod.GET})
    public ResultVO createCert (@RequestParam(value = "cn", required = true) String cn,
                                @RequestParam(value = "pw", required = true) String pw,
                                HttpServletRequest req,
                                HttpServletResponse res, ModelMap model) throws Exception {

        Security.addProvider(new BouncyCastleProvider());

        ResultVO resultVO = new ResultVO();

        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.YEAR, 1);
            Date yearFromNow = cal.getTime();

            CertificateUtils utils = new CertificateUtils();
            CertificateVO certVo = utils.createGcspCertificate(cn, yearFromNow, new BigInteger(64, new SecureRandom()), pw);

            Map<String, Object> resultData = new HashMap<String, Object>();
            resultData.put("cert", certVo.getCertificatePem());
            resultData.put("private", certVo.getPrivateKeyPem());
            Object[] objects = { resultData };
            resultVO.setData(objects);
        }
        catch (Exception e) {
            resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL,  GPMSConstants.RSP_CODE_PTGR_FAIL_NONCE, e.getMessage()));
            return resultVO;
        }

        resultVO.setStatus(new StatusVO(GPMSConstants.MSG_SUCCESS,  GPMSConstants.RSP_CODE_PTGR_SUCCESS, ""));

        return resultVO;
    }

    @RequestMapping(value = "/certfile", method = { RequestMethod.GET})
    public @ResponseBody ResponseEntity<FileSystemResource>  getCreateCert (@RequestParam(value = "cn", required = true) String cn,
                                                                            @RequestParam(value = "pw", required = true) String pw,
                                                                            HttpServletRequest req,
                                                                            HttpServletResponse res, ModelMap model) throws Exception {

        Security.addProvider(new BouncyCastleProvider());

        CertificateVO certVo = null;
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.DATE, 10);
            Date yearFromNow = cal.getTime();

            CertificateUtils utils = new CertificateUtils();
            String hashPw = utils.sha256Encrypt(pw);
            String hashPw1 = utils.sha256Encrypt(cn + hashPw);
            String password = utils.sha256Encrypt(hashPw1);

            certVo = utils.createGcspCertificate(cn, yearFromNow, new BigInteger(64, new SecureRandom()), password);
       }
        catch (Exception e) {
            return null;
        }

        exportCertFileZip(certVo, GPMSConstants.PORTABLE_SERVER_CERTFILE );

        HttpHeaders header = new HttpHeaders();
        header.set("Content-Type", "application/zip");
        header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=cert.zip");
        return new ResponseEntity<FileSystemResource>(new FileSystemResource(GPMSConstants.PORTABLE_SERVER_CERTFILE), header, HttpStatus.OK);
    }

    @RequestMapping(value = "/nonce", method = { RequestMethod.POST })
    public ResultVO getNonce (@RequestBody PtgrNonceData cnData, HttpServletRequest req,
                              HttpServletResponse res, ModelMap model) throws Exception {

        ResultVO resultVO = new ResultVO();
        Map<String, Object> resultData = new HashMap<String, Object>();

        try {
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            String val = new BigInteger(60, random).toString(32);
            PtgrNonceVO vo = new PtgrNonceVO();
            vo.setCn(cnData.getCn());
            vo.setNonce(val);
            ptgrNonceService.insertCentNonce(vo);
            resultData.put("nonce",  val);
            Object[] o = {resultData};
            resultVO.setData(o);
            logger.debug("nonce : " +  val);
        }
        catch (Exception e) {
            resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL,  GPMSConstants.RSP_CODE_PTGR_FAIL_NONCE, e.getMessage()));
            return resultVO;
        }

        resultVO.setStatus(new StatusVO(GPMSConstants.MSG_SUCCESS,  GPMSConstants.RSP_CODE_PTGR_SUCCESS, ""));
        return resultVO;
    }

    @PostMapping(value = "/cert")
    public ResultVO verifyCert (@RequestBody PtgrCertData certVO) throws  Exception {

        Security.addProvider(new BouncyCastleProvider());

        ResultVO resultVO = new ResultVO();

        logger.debug("Nonce [" + certVO.getSignedNonce() +"]");
        logger.debug("Cert[" + certVO.getCert() +"]");
        if (certVO.getCert().isEmpty()) {
            resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL,  GPMSConstants.RSP_CODE_PTGR_FAIL_CERT, "Parameter Error"));
            return resultVO;
        }

        String strCert = certVO.getCert();
        byte[] decodeCert = Base64.getDecoder().decode(strCert);
        strCert = new String(decodeCert);

        Object readObject = null;
        X509CertificateHolder holder = null;

        try {
            InputStream stream = new ByteArrayInputStream(strCert.getBytes("UTF-8"));
            PEMParser pemParser = new PEMParser(new InputStreamReader(stream));
            readObject = pemParser.readObject();
            if (!(readObject instanceof X509CertificateHolder))  {
                stream.close();
                pemParser.close();
                resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL,  GPMSConstants.RSP_CODE_PTGR_FAIL_CERT, "Certificate parser error"));
                logger.debug("Certificate parser error");
                return resultVO;
            }
            holder = (X509CertificateHolder) readObject;
            stream.close();
            pemParser.close();
        }
        catch (Exception e) {
            logger.debug(e.getMessage());
            resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL,  GPMSConstants.RSP_CODE_PTGR_FAIL_CERT, e.getMessage()));
            return resultVO;
        }

        X509Certificate cert = null;

        //1.인증서 유호셩 검사
        //유효기간
        logger.debug("verify server");
        try {
            cert = new JcaX509CertificateConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME).getCertificate(holder);
            cert.checkValidity(new Date());
        }
        catch(CertificateExpiredException e){   // 유효기간이 지난 경우 에러메시지
            resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL,  GPMSConstants.RSP_CODE_PTGR_FAIL_CERT_EXPIRED, e.getMessage()));
            logger.debug(e.getMessage());
            return resultVO;
        }
        catch(CertificateNotYetValidException e){  // 유효기간이 아직 시작되지 않은 경우 에러메시지
            resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL,  GPMSConstants.RSP_CODE_PTGR_FAIL_CERT_EXPIRED, e.getMessage()));
            logger.debug(e.getMessage());
            return resultVO;
        }
        catch (Exception e) {
            resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL,  GPMSConstants.RSP_CODE_PTGR_FAIL_CERT, e.getMessage()));
            logger.debug(e.getMessage());
            return resultVO;
        }
        //서버키 확인
        CertificateUtils utils = new CertificateUtils();
        if (!utils.verifyServerCertificate(holder)) {
            resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL,  GPMSConstants.RSP_CODE_PTGR_FAIL_CERT, "Certificate Validation Error"));
            logger.debug("Certificate Validation Error");
            return resultVO;
        }
        else {
            logger.debug("Certificate Validation Success");
        }

        //2.CN값 추출
        String strCertCN = "";
        try {
            X500Name x500name = holder.getSubject();
            RDN cn = x500name.getRDNs(BCStyle.CN)[0];
            strCertCN = IETFUtils.valueToString(cn.getFirst().getValue());
            strCertCN = strCertCN.replaceAll("\\\\", "");
        } catch (Exception e) {
            resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL,  GPMSConstants.RSP_CODE_PTGR_FAIL_CERT, e.getMessage()));
            return resultVO;
        }
        logger.debug("CN : " + strCertCN);

        //3.DB에서 Nonce값 추출
        PtgrNonceVO nonceVO = ptgrNonceService.selectCertNonceByCN(strCertCN);
        if (nonceVO == null || nonceVO.getNonce().isEmpty()) {
            resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL, GPMSConstants.RSP_CODE_PTGR_FAIL_CERT_SIGN_NONCE, "Nonce value database error"));
            return resultVO;
        }
        logger.debug("DB Nonce : [" + nonceVO.getNonce() + "]");
        //시간 체크
        String strDate = nonceVO.getRegDt();
        SimpleDateFormat sfo = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date nonceDate = sfo.parse(strDate);

        Calendar cal = Calendar.getInstance();
        cal.setTime(nonceDate);
        cal.add(Calendar.MINUTE, 5);
        Date baseDate = sfo.parse(sfo.format(cal.getTime()));

        Date nowDate = new Date();
        logger.debug("DB Nonce Time: " + baseDate.toString());
        logger.debug("Now Time: " + nowDate.toString());

        if (baseDate.before(nowDate)) {
            logger.debug("Nonce value past its expiration date");
            resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL, GPMSConstants.RSP_CODE_PTGR_FAIL_CERT_EXPIRED, "Nonce value past its expiration date"));
            return resultVO;
        }

        //4.Nonce 값 비교
        String decryptNonce = "";
        try {
            PublicKey userPubKey = cert.getPublicKey();
            byte[] byteEncrypted = Base64.getDecoder().decode(certVO.getSignedNonce());
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, userPubKey);
            byte[] bytePlain = cipher.doFinal(byteEncrypted);
            decryptNonce = new String(bytePlain, "utf-8").trim();
        }
        catch (Exception e) {
            logger.debug(e.getMessage());
            resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL, GPMSConstants.RSP_CODE_PTGR_FAIL_CERT_SIGN_NONCE, e.getMessage()));
            return resultVO;
        }
        logger.debug("DecryptNonce : [" + decryptNonce + "]");

        if (!nonceVO.getNonce().equals(decryptNonce)) {
            logger.debug("Nonce values do not match");
            resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL, GPMSConstants.RSP_CODE_PTGR_FAIL_CERT_SIGN_NONCE, "Nonce values do not match"));
            return resultVO;
        }

        Map<String, Object> resultData = new HashMap<String, Object>();
        resultData.put("userId", strCertCN);
        Object[] o = {resultData};
        resultVO.setData(o);
        resultVO.setStatus(new StatusVO(GPMSConstants.MSG_SUCCESS,  GPMSConstants.RSP_CODE_PTGR_SUCCESS, ""));
        return resultVO;
    }

    public void exportCertFileZip (CertificateVO certVO, String zipFile) throws Exception
    {
        ZipOutputStream zos = new ZipOutputStream( new FileOutputStream(zipFile));
        ZipEntry zipCert = new ZipEntry("cert.pem");
        zos.putNextEntry( zipCert);
        zos.write(certVO.getCertificatePem().getBytes(StandardCharsets.UTF_8));
        ZipEntry zipPrivateKey = new ZipEntry("private.key");
        zos.putNextEntry( zipPrivateKey);
        zos.write(certVO.getPrivateKeyPem().getBytes(StandardCharsets.UTF_8));

        ZipEntry zipPubKey = new ZipEntry("root.pem");
        zos.putNextEntry(zipPubKey);
        FileInputStream is = new FileInputStream(GPMSConstants.ROOT_CERTPATH + "/" + GPMSConstants.ROOT_CERTFILENAME);
        zos.write(is.readAllBytes());

        zos.closeEntry();
        zos.close();
    }
}