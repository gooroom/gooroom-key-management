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

package kr.gooroom.gpms.gkm.service.impl;

/**
 * client certificate management service implements class
 * 
 * @author HNC
 * @version 1.0
 * @since 1.8
 */

import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import kr.gooroom.gpms.common.GPMSConstants;
import kr.gooroom.gpms.common.service.CertForClientVO;
import kr.gooroom.gpms.common.service.ClientGroupIpInfoVO;
import kr.gooroom.gpms.common.service.ResultVO;
import kr.gooroom.gpms.common.service.StatusVO;
import kr.gooroom.gpms.common.service.impl.GpmsCommonDAO;
import kr.gooroom.gpms.common.utils.AutoGroupSelector;
import kr.gooroom.gpms.common.utils.MessageSourceHelper;
import kr.gooroom.gpms.gkm.service.CertRequestVO;
import kr.gooroom.gpms.gkm.service.CertificateService;
import kr.gooroom.gpms.gkm.utils.CertificateUtils;

@Service("certificateService")
public class CertificateServiceImpl implements CertificateService {

	private static final Logger logger = LoggerFactory.getLogger(CertificateServiceImpl.class);

	@Resource(name = "certificateDAO")
	private CertificateDAO certificateDao;

	@Resource(name = "gpmsCommonDAO")
	private GpmsCommonDAO gpmsCommonDAO;

	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	/**
	 * 인증 요청서에 사인하여 인증서 생성 - 데이터 생성
	 * 
	 * @param CertRequestVO
	 * @return ResultVO
	 * @throws Exception
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	synchronized public ResultVO createCertificateFromCSR(CertRequestVO vo) throws Exception {

		ResultVO resultVO = new ResultVO();
		String certPem = "";

		CertificateUtils utils = new CertificateUtils();

		// @@@ CN 중복 검사 필요 - 등록시 중복은 오류
		boolean isExist = certificateDao.isExistClientName(vo.getCn());
		if (isExist) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_INSERTERROR,
					MessageSourceHelper.getMessage("client.result.duplicate"))	);
			return resultVO;
		}

		// @@@ 고유 번호 생성 필요 (Serial no)
		UUID uuid = UUID.nameUUIDFromBytes(vo.getCn().getBytes("UTF-8"));
		BigInteger newSerialNo = utils.getBigIntegerFromUuid(uuid);

		try {

			X509Certificate clientCert = utils.signCSR(new StringReader(vo.getCsr()), vo.getExpireDate(), newSerialNo);
			
			if(clientCert != null) {
				// PEM 형식으로 변경
				PemObject pemObject = new PemObject("CERTIFICATE", clientCert.getEncoded());
				StringWriter sw = new StringWriter();
				PemWriter pemWriter = new PemWriter(sw);
				try {
					pemWriter.writeObject(pemObject);
				} finally {
					pemWriter.close();
				}
				certPem = sw.toString();
				
				if (certPem != null && !"".equals(certPem) && certPem.length() > 0) {
					// 단말그룹의 자동등록 IP 조사
					List<ClientGroupIpInfoVO> ipRe = gpmsCommonDAO.selectClientGroupIpInfo();
					AutoGroupSelector ags = new AutoGroupSelector(ipRe);
					String defaultGrpId = "";
					if(!vo.getIpv4().equals("")) {
						defaultGrpId = ags.getClientGroupId(vo.getIpv4());
					}
					if(!vo.getIpv6().equals("")) {
						defaultGrpId = ags.getClientGroupId(vo.getIpv6());
					}


					// 인증서 오브젝트 생성
					CertForClientVO certificateVO = new CertForClientVO();

					certificateVO.setClientCN(vo.getCn());
					certificateVO.setClientName(vo.getName());
					certificateVO.setClientOU(vo.getOu());
					certificateVO.setCertInfo(certPem);
					
					if(defaultGrpId != null && !"".equals(defaultGrpId)) {
						certificateVO.setDefaultClientGroupId(defaultGrpId);
					} else {
						certificateVO.setDefaultClientGroupId(GPMSConstants.CTRL_CLIENT_GROUP_DEFAULT);
					}

					certificateVO.setExpireDate(sdf.format(vo.getExpireDate()));
					certificateVO.setClientStatus(GPMSConstants.STS_USABLE);
					certificateVO.setRegUserId(vo.getAdminUserId());
					certificateVO.setComment(vo.getComment());
					certificateVO.setChgTp(GPMSConstants.CODE_CHANGE_TYPE_CREATE);
					certificateVO.setSerialNo(String.valueOf(newSerialNo));

					// 테이블에 저장
					long re = certificateDao.createClientCertificate(certificateVO);
					if (re > -1) {
						Object[] objects = { certificateVO };
						resultVO.setData(objects);
						resultVO.setStatus(new StatusVO(GPMSConstants.MSG_SUCCESS, GPMSConstants.CODE_INSERT,
								MessageSourceHelper.getMessage("client.result.creatcertificate")));
					} else {
						resultVO.setData(null);
						resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_SYSERROR,
								MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR)));
					}

				} else {
					resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_INSERTERROR,
							MessageSourceHelper.getMessage("client.result.signerror2")));
				}
				
			} else {
				resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_INSERTERROR,
						MessageSourceHelper.getMessage("client.result.signerror1")));
			}


		} catch (SQLException sqlEx) {

			logger.error("error in createCertificateFromCSR : {}, {}, {}", GPMSConstants.CODE_SYSERROR,
					MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR), sqlEx.toString());
			if (resultVO != null) {
				resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_SYSERROR,
						MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR)));
			}
			throw sqlEx;

		} catch (Exception ex) {

			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			logger.error("error in createCertificateFromCSR : {}, {}, {}", GPMSConstants.CODE_SYSERROR,
					MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR), ex.toString());
			if (resultVO != null) {
				resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_SYSERROR,
						MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR)));
			}
		}

		return resultVO;
	}

	/**
	 * 인증 요청서에 사인하여 인증서 갱신 - 데이터 갱신
	 * 
	 * @param CertRequestVO
	 * @return ResultVO
	 * @throws Exception
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	synchronized public ResultVO updateCertificateFromCSR(CertRequestVO vo) throws Exception {

		ResultVO resultVO = new ResultVO();
		String certPem = "";

		CertificateUtils utils = new CertificateUtils();

		// @@@ CN 중복 검사 필요 - 중복은 갱신, 중복되지 않으면 오류!
		boolean isExist = certificateDao.isExistClientName(vo.getCn());
		if (!isExist) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_INSERTERROR,
					MessageSourceHelper.getMessage("client.result.noduplicate")));
			return resultVO;
		}

		// @@@ 고유 번호 생성 필요 (Serial no)
		UUID uuid = UUID.nameUUIDFromBytes(vo.getCn().getBytes("UTF-8"));
		BigInteger newSerialNo = utils.getBigIntegerFromUuid(uuid);

		// below code is not use.
		int nextNo = certificateDao.selectNextClientNo();
		if (nextNo <= 0) {

			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_INSERTERROR,
					MessageSourceHelper.getMessage("client.result.createrror")));
			return resultVO;
		}

		try {

			X509Certificate clientCert = utils.signCSR(new StringReader(vo.getCsr()), vo.getExpireDate(), newSerialNo);

			// PEM 형식으로 변경
			PemObject pemObject = new PemObject("CERTIFICATE", clientCert.getEncoded());
			StringWriter sw = new StringWriter();
			PemWriter pemWriter = new PemWriter(sw);
			try {
				pemWriter.writeObject(pemObject);
			} finally {
				pemWriter.close();
			}

			certPem = sw.toString();

			if (certPem != null && !"".equals(certPem) && certPem.length() > 0) {

				resultVO.setStatus(new StatusVO(GPMSConstants.MSG_SUCCESS, GPMSConstants.CODE_INSERT,
						MessageSourceHelper.getMessage("client.result.creatcertificate")));

				// 인증서 오브젝트 생성
				CertForClientVO certificateVO = new CertForClientVO();

				certificateVO.setClientCN(vo.getCn());
				certificateVO.setClientName(vo.getName());
				certificateVO.setCertInfo(certPem);
				
				if (isExist) {
					// 단말그룹의 자동등록 IP 조사
					List<ClientGroupIpInfoVO> ipRe = gpmsCommonDAO.selectClientGroupIpInfo();
					AutoGroupSelector ags = new AutoGroupSelector(ipRe);
					String defaultGrpId = "";
					if(!vo.getIpv4().equals("")) {
						defaultGrpId = ags.getClientGroupId(vo.getIpv4());
					}
					if(!vo.getIpv6().equals("")) {
						defaultGrpId = ags.getClientGroupId(vo.getIpv6());
					}


					if(defaultGrpId != null && !"".equals(defaultGrpId)) {
						certificateVO.setDefaultClientGroupId(defaultGrpId);
					} else {
						certificateVO.setDefaultClientGroupId(GPMSConstants.CTRL_CLIENT_GROUP_DEFAULT);
					}
				}

				certificateVO.setExpireDate(sdf.format(vo.getExpireDate()));
				certificateVO.setClientStatus(GPMSConstants.STS_USABLE);
				certificateVO.setRegUserId(vo.getAdminUserId());
				certificateVO.setComment(vo.getComment());
				certificateVO.setChgTp(GPMSConstants.CODE_CHANGE_TYPE_UPDATE);
				certificateVO.setSerialNo(String.valueOf(newSerialNo));

				// 테이블에 저장
				long re = certificateDao.updateClientCertificate(certificateVO);
				if (re > -1) {
					Object[] objects = { certificateVO };
					resultVO.setData(objects);
					resultVO.setStatus(new StatusVO(GPMSConstants.MSG_SUCCESS, GPMSConstants.CODE_UPDATE,
							MessageSourceHelper.getMessage("client.result.creatcertificate")));
				} else {
					resultVO.setData(null);
					resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_SYSERROR,
							MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR)));
				}
			} else {
				resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_INSERTERROR,
						MessageSourceHelper.getMessage("client.result.signerror")));
			}

		} catch (SQLException sqlEx) {

			logger.error("error in updateCertificateFromCSR : {}, {}, {}", GPMSConstants.CODE_SYSERROR,
					MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR), sqlEx.toString());
			if (resultVO != null) {
				resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_SYSERROR,
						MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR)));
			}
			throw sqlEx;

		} catch (Exception ex) {

			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			logger.error("error in updateCertificateFromCSR : {}, {}, {}", GPMSConstants.CODE_SYSERROR,
					MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR), ex.toString());
			if (resultVO != null) {
				resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_SYSERROR,
						MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR)));
			}
		}

		return resultVO;
	}

	/**
	 * 인증 요청서에 사인하여 인증서 생성 - 데이터 생성
	 * 
	 * @param CertRequestVO
	 * @return ResultVO
	 * @throws Exception
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	synchronized public ResultVO reCreateCertificateFromCSR(CertRequestVO vo) throws Exception {

		ResultVO resultVO = new ResultVO();
		String certPem = "";

		CertificateUtils utils = new CertificateUtils();

		// @@@ CN 중복 검사 필요 - 등록시 중복은 오류
		boolean isExist = certificateDao.isExistClientName(vo.getCn());

		// @@@ 고유 번호 생성 필요 (Serial no)
		UUID uuid = UUID.nameUUIDFromBytes(vo.getCn().getBytes("UTF-8"));
		BigInteger newSerialNo = utils.getBigIntegerFromUuid(uuid);

		try {

			X509Certificate clientCert = utils.signCSR(new StringReader(vo.getCsr()), vo.getExpireDate(), newSerialNo);

			// PEM 형식으로 변경
			PemObject pemObject = new PemObject("CERTIFICATE", clientCert.getEncoded());
			StringWriter sw = new StringWriter();
			PemWriter pemWriter = new PemWriter(sw);
			try {
				pemWriter.writeObject(pemObject);
			} finally {
				pemWriter.close();
			}

			certPem = sw.toString();

			if (certPem != null && !"".equals(certPem) && certPem.length() > 0) {

				// 인증서 오브젝트 생성
				CertForClientVO certificateVO = new CertForClientVO();

				certificateVO.setClientCN(vo.getCn());
				certificateVO.setClientName(vo.getName());
				certificateVO.setClientOU(vo.getOu());
				certificateVO.setCertInfo(certPem);
				
				if (isExist) {
					// 단말그룹의 자동등록 IP 조사
					List<ClientGroupIpInfoVO> ipRe = gpmsCommonDAO.selectClientGroupIpInfo();
					AutoGroupSelector ags = new AutoGroupSelector(ipRe);
					String defaultGrpId = "";
					if(!vo.getIpv4().equals("")) {
						defaultGrpId = ags.getClientGroupId(vo.getIpv4());
					}
					if(!vo.getIpv6().equals("")) {
						defaultGrpId = ags.getClientGroupId(vo.getIpv6());
					}

					if(defaultGrpId != null && !"".equals(defaultGrpId)) {
						certificateVO.setDefaultClientGroupId(defaultGrpId);
					} else {
						certificateVO.setDefaultClientGroupId(GPMSConstants.CTRL_CLIENT_GROUP_DEFAULT);
					}
				} else {
					certificateVO.setDefaultClientGroupId(GPMSConstants.CTRL_CLIENT_GROUP_DEFAULT);
				}

				certificateVO.setExpireDate(sdf.format(vo.getExpireDate()));
				certificateVO.setClientStatus(GPMSConstants.STS_USABLE);
				certificateVO.setRegUserId(vo.getAdminUserId());
				certificateVO.setComment(vo.getComment());
				certificateVO.setSerialNo(String.valueOf(newSerialNo));

				// 테이블에 저장
				long re = -1;
				if (isExist) {
					certificateVO.setChgTp(GPMSConstants.CODE_CHANGE_TYPE_UPDATE);
					re = certificateDao.updateClientCertificateAndName(certificateVO);
				} else {
					certificateVO.setChgTp(GPMSConstants.CODE_CHANGE_TYPE_CREATE);
					re = certificateDao.createClientCertificate(certificateVO);
				}
				if (re > -1) {
					Object[] objects = { certificateVO };
					resultVO.setData(objects);
					resultVO.setStatus(new StatusVO(GPMSConstants.MSG_SUCCESS, GPMSConstants.CODE_INSERT,
							MessageSourceHelper.getMessage("client.result.creatcertificate")));
				} else {
					resultVO.setData(null);
					resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_SYSERROR,
							MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR)));
				}

			} else {
				resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_INSERTERROR,
						MessageSourceHelper.getMessage("client.result.signerror")));
			}

		} catch (SQLException sqlEx) {

			logger.error("error in createCertificateFromCSR : {}, {}, {}", GPMSConstants.CODE_SYSERROR,
					MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR), sqlEx.toString());
			if (resultVO != null) {
				resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_SYSERROR,
						MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR)));
			}
			throw sqlEx;

		} catch (Exception ex) {

			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			logger.error("error in createCertificateFromCSR : {}, {}, {}", GPMSConstants.CODE_SYSERROR,
					MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR), ex.toString());
			if (resultVO != null) {
				resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_SYSERROR,
						MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR)));
			}
		}

		return resultVO;
	}

	/**
	 * verify that the certificate is revoked.
	 * 
	 * @param serialNo String
	 * @return StatusVO result status
	 * @throws Exception
	 */
	@Override
	public StatusVO isRevoked(String serialNo) throws Exception {

		// System.out.println("[ isRevoked ] - " + serialNo + " (" +
		// Calendar.getInstance().getTimeInMillis() + ")");

		StatusVO statusVO = new StatusVO();

		try {

			boolean isValid = certificateDao.isRevoked(serialNo);

			if (isValid) {
				statusVO.setResultInfo(GPMSConstants.MSG_SUCCESS, GPMSConstants.CODE_UPDATE, "true");
			} else {
				statusVO.setResultInfo(GPMSConstants.MSG_SUCCESS, GPMSConstants.CODE_UPDATE, "false");
			}
		} catch (SQLException sqlEx) {
			logger.error("error in isRevoked : {}, {}, {}", GPMSConstants.CODE_SYSERROR,
					MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR), sqlEx.toString());
			if (statusVO != null) {
				statusVO.setResultInfo(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_SYSERROR,
						MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR));
			}
			throw sqlEx;

		} catch (Exception ex) {
			logger.error("error in isRevoked : {}, {}, {}", GPMSConstants.CODE_SYSERROR,
					MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR), ex.toString());
			if (statusVO != null) {
				statusVO.setResultInfo(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_SYSERROR,
						MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR));
			}
		}

		return statusVO;
	}

}
