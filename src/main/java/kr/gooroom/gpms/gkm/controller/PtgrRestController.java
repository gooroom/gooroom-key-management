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
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.crypto.Cipher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

        ResultVO resultVO = new ResultVO();
        resultVO.setStatus(new StatusVO(GPMSConstants.MSG_SUCCESS,  GPMSConstants.RSP_CODE_PTGR_SUCCESS, ""));

        return resultVO;
    }

    @RequestMapping(value = "/certfile", method = { RequestMethod.GET})
    public @ResponseBody ResponseEntity<FileSystemResource>  getCreateCert (@RequestParam(value = "cn", required = true) String cn,
                                                                            @RequestParam(value = "pw", required = true) String pw,
                                                                            HttpServletRequest req,
                                                                            HttpServletResponse res, ModelMap model) throws Exception {

        return null;
    }

    @RequestMapping(value = "/nonce", method = { RequestMethod.POST })
    public ResultVO getNonce (@RequestBody PtgrNonceData cnData, HttpServletRequest req,
                              HttpServletResponse res, ModelMap model) throws Exception {

        ResultVO resultVO = new ResultVO();
        resultVO.setStatus(new StatusVO(GPMSConstants.MSG_SUCCESS,  GPMSConstants.RSP_CODE_PTGR_SUCCESS, ""));
        return resultVO;
    }

    @PostMapping(value = "/cert")
    public ResultVO verifyCert (@RequestBody PtgrCertData certVO) throws  Exception {

        Security.addProvider(new BouncyCastleProvider());

        ResultVO resultVO = new ResultVO();
        resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL,  GPMSConstants.RSP_CODE_PTGR_FAIL_CERT, "Parameter Error"));

        return resultVO;
    }

    public void exportCertFileZip (CertificateVO certVO, String zipFile) throws Exception
    {
    }
}
