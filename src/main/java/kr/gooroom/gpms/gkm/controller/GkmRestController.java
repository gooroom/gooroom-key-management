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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kr.gooroom.gpms.common.GPMSConstants;
import kr.gooroom.gpms.common.service.GpmsCommonService;
import kr.gooroom.gpms.common.service.ResultVO;
import kr.gooroom.gpms.common.service.StatusVO;
import kr.gooroom.gpms.common.utils.MessageSourceHelper;
import kr.gooroom.gpms.gkm.service.CertRequestVO;
import kr.gooroom.gpms.gkm.service.CertificateService;
import kr.gooroom.gpms.gkm.utils.CertificateUtils;
import kr.gooroom.gpms.valid.service.ClientRegKeyVO;
import kr.gooroom.gpms.valid.service.ClientValidService;

@RestController
public class GkmRestController {

	private static final Logger logger = LoggerFactory.getLogger(GkmRestController.class);

	@Resource(name = "certificateService")
	private CertificateService certificateService;

	@Resource(name = "gpmsCommonService")
	private GpmsCommonService gpmsCommonService;

	@Resource(name = "adminUserService")
	private ClientValidService clientValidService;

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
	}

	CertificateUtils utils = new CertificateUtils();
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	/**
	 * 서버 인증서 요청
	 * 
	 * @return ResultVO
	 * @throws Exception
	 */
	@GetMapping("/v1/cert")
	public ResultVO getServerCert() {

		ResultVO resultVO = new ResultVO();

		try {

			resultVO = gpmsCommonService.getGpmsServersCertificate();

		} catch (Exception ex) {

			resultVO = null;
			logger.error("error in getServerCert : {}, {}, {}", GPMSConstants.CODE_SYSERROR,
					MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR), ex.toString());
		}

		return resultVO;
	}

	/**
	 * 서버 버전 요청
	 * 
	 * @return ResultVO
	 * @throws Exception
	 */
	@GetMapping("/v2/version")
	public ResultVO getServerVersion() {

		ResultVO resultVO = new ResultVO();

		try {

			resultVO = gpmsCommonService.getGpmsServerVersion();

		} catch (Exception ex) {

			resultVO = null;
			logger.error("error in getServerVersion : {}, {}, {}", GPMSConstants.CODE_SYSERROR,
					MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR), ex.toString());
		}

		return resultVO;
	}

	/**
	 * 단말 등록 요청 (CSR + 어드민 계정)
	 * 
	 * @param CertRequestVO paramVO
	 * @return ResultVO
	 * @throws Exception
	 */
	@PostMapping("/v1/client/register")
	public ResultVO getCertificate(@RequestParam(value = "csr", required = true, defaultValue = "") String csr,
			@RequestParam(value = "valid_date", required = false, defaultValue = "") String valid_date,
			@RequestParam(value = "ou", required = false, defaultValue = "") String ou,
			@RequestParam(value = "comment", required = false, defaultValue = "") String comment,
			@RequestParam(value = "cn", required = true, defaultValue = "") String cn,
			@RequestParam(value = "ipv4", required = true, defaultValue = "") String ipv4,
            @RequestParam(value = "ipv6", required = true, defaultValue = "") String ipv6,
			@RequestParam(value = "user_id", required = true, defaultValue = "") String user_id,
			@RequestParam(value = "user_pw", required = true, defaultValue = "") String user_pw) {

		ResultVO resultVO = new ResultVO();

		// 파라미터로 받은 CSR 확인
		if (csr == null || "".equals(csr)) {
			return new ResultVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_NODATA,
					MessageSourceHelper.getMessage("register.result.csrerror"));
		} else if (user_id == null || "".equals(user_id)) {
			return new ResultVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_NODATA,
					MessageSourceHelper.getMessage("register.result.noadminid"));
		} else if (user_pw == null || "".equals(user_pw)) {
			return new ResultVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_NODATA,
					MessageSourceHelper.getMessage("register.result.wrongpassword"));
		} else {

			// 관리자 계정 검사
			try {
				ResultVO authRe = clientValidService.getAdminUserAuthAndInfo(user_id, user_pw);
				if ("success".equalsIgnoreCase(authRe.getStatus().getResult())) {
					CertRequestVO vo = new CertRequestVO();
					vo.setCsr(csr);

					Calendar validTo = Calendar.getInstance();
					if (valid_date != null && valid_date.length() > 0) {
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
						validTo.setTime(sdf.parse(valid_date));
						validTo.set(Calendar.HOUR_OF_DAY, 23);
						validTo.set(Calendar.MINUTE, 59);
						validTo.set(Calendar.SECOND, 59);
					} else {
						// 10년 으로 설정 (10년후 12월 31일 23시 59분 59초
						validTo.add(Calendar.YEAR, 10);
						validTo.set(Calendar.MONTH, 11);
						validTo.set(Calendar.DATE, 31);
						validTo.set(Calendar.HOUR_OF_DAY, 23);
						validTo.set(Calendar.MINUTE, 59);
						validTo.set(Calendar.SECOND, 59);
					}
					vo.setExpireDate(validTo.getTime());
					vo.setOu(ou);
					vo.setComment(comment);
					vo.setCn(cn);
					vo.setName(cn);
					vo.setIpv4(ipv4);
					vo.setIpv6(ipv6);
					vo.setAdminUserId(user_id);

					// String clientPem = "";
					try {

						resultVO = certificateService.createCertificateFromCSR(vo);

					} catch (Exception ex) {
						// System.out.println("\nex : " + ex + "\n");

						logger.error("error in getCertificate(createCertificate) : {}, {}, {}",
								GPMSConstants.CODE_SYSERROR, MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR),
								ex.toString());
						if (resultVO != null) {
							resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_SYSERROR,
									MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR)));
						}
					}
				} else {
					resultVO = authRe;
				}

			} catch (Exception ex) {

				logger.error("error in getCertificate : {}, {}, {}", GPMSConstants.CODE_SYSERROR,
						MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR), ex.toString());
				if (resultVO != null) {
					resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_SYSERROR,
							MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR)));
				}
			}

			return resultVO;
		}
	}

	/**
	 * GPMS 서버정보제공
	 * 
	 * @param CertRequestVO paramVO
	 * @return ResultVO
	 * @throws Exception
	 */
	@GetMapping("/v1/gpms")
	public ResultVO getGpmsInfo() {

		ResultVO resultVO = new ResultVO();
		try {
			resultVO = gpmsCommonService.getGpmsServersInfo();
		} catch (Exception ex) {

			logger.error("error in getGpmsInfo : {}, {}, {}", GPMSConstants.CODE_SYSERROR,
					MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR), ex.toString());
			if (resultVO != null) {
				resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_SYSERROR,
						MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR)));
			}

		}

		return resultVO;
	}

	private String convertFormatIpv6(String rowIp) throws Exception {
		try {
			String[] parts = new String[8];
			int contractionIdx = rowIp.indexOf("::");
			if(contractionIdx == -1) {  //없어
				parts = rowIp.split(":");
			} else {
				String firstStr=rowIp.substring(0,contractionIdx);
				String secondStr=rowIp.substring(contractionIdx+2);
				if(firstStr.length() == 0) { //맨앞
					String[] innerParts = secondStr.split(":");
					for(int i = 0; i <= 8 - innerParts.length; i++) {
						parts[i] = "0000";
					}
					System.arraycopy(innerParts, 0, parts, 8-innerParts.length, innerParts.length);
				} else if(secondStr.length() == 0) { //맨뒤
					String[] innerParts = firstStr.split(":");
					System.arraycopy(innerParts, 0, parts, 0, innerParts.length);
					for(int i = innerParts.length; i < 8; i++) {
						parts[i] = "0000";
					}
				} else {
					String[] front = firstStr.split(":");
					String[] back = secondStr.split(":");
					System.arraycopy(front, 0, parts, 0, front.length);
					for(int i=front.length;i<=8-front.length-back.length;i++) {
						parts[i] = "0000";
					}
					System.arraycopy(back, 0, parts, front.length+(8-front.length-back.length), back.length );
				}
			}
			for(int i = 0; i < parts.length; i++) {
				if("*".equals(parts[i])) {
					parts[i] = "-1";
					break;
				} else {
					parts[i] = String.valueOf(Long.parseLong(parts[i], 16));
				}
			}

			return String.join("", parts);
		} catch (Exception ex) {
			throw ex;
		}
	}

	private int convertFormat(String v) throws Exception {
		try {
			if ("*".equals(v)) {
				return -1;
			} else {
				int iv = Integer.parseInt(v);
				return iv;
			}
		} catch (Exception ex) {
			throw ex;
		}
	}

	private boolean checkIpv6Range(String ipRules, String clientIp) {
		try {
			String[] ips = null;
			if (ipRules.indexOf(",") > -1) {
				ips = ipRules.split(",");
			} else {
				ips = new String[1];
				ips[0] = ipRules;
			}

			// target ip
			String clientIpStr = convertFormatIpv6(clientIp);
			for (int i = 0; i < ips.length; i++) {

				if (ips[i].indexOf("-") > -1) {
					// from, to = ip range
					int fromCompare = clientIpStr
							.compareTo(convertFormatIpv6(ips[i].substring(0, ips[i].indexOf("-"))));
					int toCompare = clientIpStr
							.compareTo(convertFormatIpv6(ips[i].substring(ips[i].indexOf("-") + 1)));

					if (fromCompare == 0 || toCompare == 0 || (fromCompare > 0 && toCompare < 0)) {
						return true;
					}
				} else {
					String ipRuleStr = convertFormatIpv6(ips[i]);

					// check '*' -> '-01'
					if (ipRuleStr.indexOf("-1") > -1) {
						if (clientIpStr.startsWith(ipRuleStr.substring(0, ipRuleStr.indexOf("-1")))) {
							return true;
						}
					} else {
						if (clientIpStr.equals(ipRuleStr)) {
							return true;
						}
					}
				}
			}
			return false;
		} catch (Exception ex){
			return  false;
		}
	}

	private boolean checkIpRange(String ipRules, String clientIp) {

		try {
			String[] ips = null;
			if (ipRules.indexOf(",") > -1) {
				ips = ipRules.split(",");
			} else {
				ips = new String[1];
				ips[0] = ipRules;
			}

			//ipv6
			Pattern pattern = Pattern.compile("^(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]).){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]).){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))$");
			if (pattern.matcher(clientIp).matches() == true) {
				return checkIpv6Range(ipRules, clientIp);
			}

			// target ip
			String clientIpStr = Arrays.stream(clientIp.split("\\.")).map(s -> {
				try {
					return String.format("%03d", convertFormat(s.trim()));
				} catch (Exception e) {
					return "ERROR";
				}
			}).collect(Collectors.joining(""));

			for (int i = 0; i < ips.length; i++) {

				if (ips[i].indexOf("-") > -1) {
					// from, to = ip range
					int fromCompare = clientIpStr
							.compareTo(Arrays.stream(ips[i].substring(0, ips[i].indexOf("-")).split("\\.")).map(s -> {
								try {
									return String.format("%03d", convertFormat(s.trim()));
								} catch (Exception e) {
									return "ERROR";
								}
							}).collect(Collectors.joining("")));
					int toCompare = clientIpStr
							.compareTo(Arrays.stream(ips[i].substring(ips[i].indexOf("-") + 1).split("\\.")).map(s -> {
								try {
									return String.format("%03d", convertFormat(s.trim()));
								} catch (Exception e) {
									return "ERROR";
								}
							}).collect(Collectors.joining("")));

					if (fromCompare == 0 || toCompare == 0 || (fromCompare > 0 && toCompare < 0)) {
						return true;
					}
				} else {
					String ipRuleStr = Arrays.stream(ips[i].split("\\.")).map(s -> {
						try {
							return String.format("%03d", convertFormat(s.trim()));
						} catch (Exception e) {
							return "ERROR";
						}
					}).collect(Collectors.joining(""));

					// check '*' -> '-01'
					if (ipRuleStr.indexOf("-01") > -1) {
						if (clientIpStr.startsWith(ipRuleStr.substring(0, ipRuleStr.indexOf("-01")))) {
							return true;
						}
					} else {
						if (clientIpStr.equals(ipRuleStr)) {
							return true;
						}
					}
				}
			}
			return false;
		} catch (Exception ex) {
			return false;
		}
	}

	private Calendar getValidDate(String valid_date) {

		Calendar validTo = null;

		try {

			validTo = Calendar.getInstance();

			if (valid_date != null && valid_date.length() > 0) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				validTo.setTime(sdf.parse(valid_date));
				validTo.set(Calendar.HOUR_OF_DAY, 23);
				validTo.set(Calendar.MINUTE, 59);
				validTo.set(Calendar.SECOND, 59);
			} else {
				// 10년 으로 설정 (10년후 12월 31일 23시 59분 59초
				validTo.add(Calendar.YEAR, 10);
				validTo.set(Calendar.MONTH, 11);
				validTo.set(Calendar.DATE, 31);
				validTo.set(Calendar.HOUR_OF_DAY, 23);
				validTo.set(Calendar.MINUTE, 59);
				validTo.set(Calendar.SECOND, 59);
			}

		} catch (ParseException pex) {
			// 10년 으로 설정 (10년후 12월 31일 23시 59분 59초
			validTo.add(Calendar.YEAR, 10);
			validTo.set(Calendar.MONTH, 11);
			validTo.set(Calendar.DATE, 31);
			validTo.set(Calendar.HOUR_OF_DAY, 23);
			validTo.set(Calendar.MINUTE, 59);
			validTo.set(Calendar.SECOND, 59);
		}

		return (validTo == null) ? Calendar.getInstance() : validTo;
	}

	/**
	 * 단말 등록 요청 (CSR + 어드민 계정)
	 * 
	 * @param CertRequestVO paramVO
	 * @return ResultVO
	 * @throws Exception
	 */
	@PostMapping("/v1/client/register/idpw/create")
	public ResultVO registerByAdminAccount(@RequestParam(value = "csr", required = true, defaultValue = "") String csr,
			@RequestParam(value = "valid_date", required = false, defaultValue = "") String valid_date,
			@RequestParam(value = "ou", required = false, defaultValue = "") String ou,
			@RequestParam(value = "comment", required = false, defaultValue = "") String comment,
			@RequestParam(value = "cn", required = true, defaultValue = "") String cn,
			@RequestParam(value = "name", required = false, defaultValue = "") String clientName,
			@RequestParam(value = "ipv4", required = true, defaultValue = "") String ipv4,
            @RequestParam(value = "ipv6", required = true, defaultValue = "") String ipv6,
			@RequestParam(value = "user_id", required = true, defaultValue = "") String user_id,
			@RequestParam(value = "user_pw", required = true, defaultValue = "") String user_pw) {

		ResultVO resultVO = new ResultVO();

		// 파라미터 정보의 공백 검사
		if (csr == null || "".equals(csr)) {
			return new ResultVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_NODATA,
					MessageSourceHelper.getMessage("register.result.csrerror"));
		} else if (user_id == null || "".equals(user_id)) {
			return new ResultVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_NODATA,
					MessageSourceHelper.getMessage("register.result.noadminid"));
		} else if (user_pw == null || "".equals(user_pw)) {
			return new ResultVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_NODATA,
					MessageSourceHelper.getMessage("register.result.wrongpassword"));
		} else {

			// 관리자 계정 검사
			try {

				ResultVO authRe = clientValidService.getAdminUserAuthAndInfo(user_id, user_pw);
				if ("success".equalsIgnoreCase(authRe.getStatus().getResult())) {
					CertRequestVO vo = new CertRequestVO();
					vo.setCsr(csr);
					vo.setExpireDate(getValidDate(valid_date).getTime());
					vo.setOu(ou);
					vo.setComment(comment);
					vo.setCn(cn);
					vo.setName(("".equals(clientName)) ? cn : clientName);
					vo.setIpv4(ipv4);
					vo.setIpv6(ipv6);
					vo.setAdminUserId(user_id);

					// String clientPem = "";
					try {
						resultVO = certificateService.createCertificateFromCSR(vo);
					} catch (Exception ex) {
						// System.out.println("\nex : " + ex + "\n");
						logger.error("error in registerByAdminAccount(createCertificate) : {}, {}, {}",
								GPMSConstants.CODE_SYSERROR, MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR),
								ex.toString());
						if (resultVO != null) {
							resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_SYSERROR,
									MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR)));
						}
					}
				} else {
					resultVO = authRe;
				}
			} catch (Exception ex) {
				logger.error("error in registerByAdminAccount : {}, {}, {}", GPMSConstants.CODE_SYSERROR,
						MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR), ex.toString());
				if (resultVO != null) {
					resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_SYSERROR,
							MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR)));
				}
			}

			return resultVO;
		}
	}

	/**
	 * 단말 갱신 요청 (CSR + 어드민 계정)
	 * 
	 * @param CertRequestVO paramVO
	 * @return ResultVO
	 * @throws Exception
	 */
	@PostMapping("/v1/client/register/idpw/update")
	public ResultVO updateByAdminAccount(@RequestParam(value = "csr", required = true, defaultValue = "") String csr,
			@RequestParam(value = "valid_date", required = false, defaultValue = "") String valid_date,
			@RequestParam(value = "ou", required = false, defaultValue = "") String ou,
			@RequestParam(value = "comment", required = false, defaultValue = "") String comment,
			@RequestParam(value = "cn", required = true, defaultValue = "") String cn,
			@RequestParam(value = "name", required = false, defaultValue = "") String clientName,
			@RequestParam(value = "ipv4", required = true, defaultValue = "") String ipv4,
            @RequestParam(value = "ipv6", required = true, defaultValue = "") String ipv6,
			@RequestParam(value = "user_id", required = true, defaultValue = "") String user_id,
			@RequestParam(value = "user_pw", required = true, defaultValue = "") String user_pw) {

		ResultVO resultVO = new ResultVO();

		// 파라미터 정보의 공백 검사
		if (csr == null || "".equals(csr)) {
			return new ResultVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_NODATA,
					MessageSourceHelper.getMessage("register.result.csrerror"));
		} else if (user_id == null || "".equals(user_id)) {
			return new ResultVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_NODATA,
					MessageSourceHelper.getMessage("register.result.noadminid"));
		} else if (user_pw == null || "".equals(user_pw)) {
			return new ResultVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_NODATA,
					MessageSourceHelper.getMessage("register.result.wrongpassword"));
		} else {

			// 관리자 계정 검사
			try {

				ResultVO authRe = clientValidService.getAdminUserAuthAndInfo(user_id, user_pw);
				if ("success".equalsIgnoreCase(authRe.getStatus().getResult())) {
					CertRequestVO vo = new CertRequestVO();
					vo.setCsr(csr);
					vo.setExpireDate(getValidDate(valid_date).getTime());
					vo.setOu(ou);
					vo.setComment(comment);
					vo.setCn(cn);
					vo.setName(("".equals(clientName)) ? cn : clientName);
					vo.setIpv4(ipv4);
					vo.setIpv6(ipv6);
					vo.setAdminUserId(user_id);

					// String clientPem = "";
					try {
						resultVO = certificateService.updateCertificateFromCSR(vo);
					} catch (Exception ex) {
						logger.error("error in updateByAdminAccount(createCertificate) : {}, {}, {}",
								GPMSConstants.CODE_SYSERROR, MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR),
								ex.toString());
						if (resultVO != null) {
							resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_SYSERROR,
									MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR)));
						}
					}
				} else {
					resultVO = authRe;
				}
			} catch (Exception ex) {
				logger.error("error in updateByAdminAccount : {}, {}, {}", GPMSConstants.CODE_SYSERROR,
						MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR), ex.toString());
				if (resultVO != null) {
					resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_SYSERROR,
							MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR)));
				}
			}

			return resultVO;
		}
	}

	/**
	 * 단말 등록 요청 (CSR + 어드민 계정)
	 * 
	 * @param CertRequestVO paramVO
	 * @return ResultVO
	 * @throws Exception
	 */
	@PostMapping("/v1/client/register/idpw/create_or_update")
	public ResultVO recreateByAdminAccount(@RequestParam(value = "csr", required = true, defaultValue = "") String csr,
			@RequestParam(value = "valid_date", required = false, defaultValue = "") String valid_date,
			@RequestParam(value = "ou", required = false, defaultValue = "") String ou,
			@RequestParam(value = "comment", required = false, defaultValue = "") String comment,
			@RequestParam(value = "cn", required = true, defaultValue = "") String cn,
			@RequestParam(value = "name", required = false, defaultValue = "") String clientName,
			@RequestParam(value = "ipv4", required = true, defaultValue = "") String ipv4,
            @RequestParam(value = "ipv6", required = true, defaultValue = "") String ipv6,
			@RequestParam(value = "user_id", required = true, defaultValue = "") String user_id,
			@RequestParam(value = "user_pw", required = true, defaultValue = "") String user_pw) {

		ResultVO resultVO = new ResultVO();

		// 파라미터 정보의 공백 검사
		if (csr == null || "".equals(csr)) {
			return new ResultVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_NODATA,
					MessageSourceHelper.getMessage("register.result.csrerror"));
		} else if (user_id == null || "".equals(user_id)) {
			return new ResultVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_NODATA,
					MessageSourceHelper.getMessage("register.result.noadminid"));
		} else if (user_pw == null || "".equals(user_pw)) {
			return new ResultVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_NODATA,
					MessageSourceHelper.getMessage("register.result.wrongpassword"));
		} else {

			// 관리자 계정 검사
			try {

				ResultVO authRe = clientValidService.getAdminUserAuthAndInfo(user_id, user_pw);
				if ("success".equalsIgnoreCase(authRe.getStatus().getResult())) {
					CertRequestVO vo = new CertRequestVO();
					vo.setCsr(csr);
					vo.setExpireDate(getValidDate(valid_date).getTime());
					vo.setOu(ou);
					vo.setComment(comment);
					vo.setCn(cn);
					vo.setName(("".equals(clientName)) ? cn : clientName);
					vo.setIpv4(ipv4);
					vo.setIpv6(ipv6);
					vo.setAdminUserId(user_id);

					// String clientPem = "";
					try {
						resultVO = certificateService.reCreateCertificateFromCSR(vo);
					} catch (Exception ex) {
						// System.out.println("\nex : " + ex + "\n");
						logger.error("error in recreateByAdminAccount(createCertificate) : {}, {}, {}",
								GPMSConstants.CODE_SYSERROR, MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR),
								ex.toString());
						if (resultVO != null) {
							resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_SYSERROR,
									MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR)));
						}
					}
				} else {
					resultVO = authRe;
				}
			} catch (Exception ex) {
				logger.error("error in recreateByAdminAccount : {}, {}, {}", GPMSConstants.CODE_SYSERROR,
						MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR), ex.toString());
				if (resultVO != null) {
					resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_SYSERROR,
							MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR)));
				}
			}

			return resultVO;
		}
	}

	/**
	 * 단말 등록 요청 (CSR + 등록키)
	 * 
	 * @param CertRequestVO paramVO
	 * @return ResultVO
	 * @throws Exception
	 */
	@PostMapping("/v1/client/register/regkey/create")
	public ResultVO registerByRegKey(HttpServletRequest request,
			@RequestParam(value = "csr", required = true, defaultValue = "") String csr,
			@RequestParam(value = "valid_date", required = false, defaultValue = "") String valid_date,
			@RequestParam(value = "ou", required = false, defaultValue = "") String ou,
			@RequestParam(value = "comment", required = false, defaultValue = "") String comment,
			@RequestParam(value = "cn", required = true, defaultValue = "") String cn,
			@RequestParam(value = "ipv4", required = true, defaultValue = "") String ipv4,
            @RequestParam(value = "ipv6", required = true, defaultValue = "") String ipv6,
			@RequestParam(value = "name", required = false, defaultValue = "") String clientName,
			@RequestParam(value = "regkey", required = true, defaultValue = "") String registKey) {

		ResultVO resultVO = new ResultVO();
		String myIp = request.getRemoteAddr();

		logger.info("MyIp : " + myIp);

		// 파라미터로 받은 CSR 확인
		if (csr == null || "".equals(csr)) {
			return new ResultVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_NODATA,
					MessageSourceHelper.getMessage("register.result.csrerror"));
		} else if (registKey == null || "".equals(registKey)) {
			return new ResultVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_NODATA,
					MessageSourceHelper.getMessage("register.result.noregistkey"));
		} else {

			try {
				ResultVO registrationDataRe = clientValidService.getRegistInfoForKey(registKey);
				if ("success".equalsIgnoreCase(registrationDataRe.getStatus().getResult())
						&& registrationDataRe.getData() != null && registrationDataRe.getData().length > 0) {

					ClientRegKeyVO registrationDataVO = (ClientRegKeyVO) registrationDataRe.getData()[0];

					// check valid date
					if (registrationDataVO.getValidDate().after(Calendar.getInstance().getTime())) {
						// check ip arrange
						if (checkIpRange(registrationDataVO.getIpRange(), myIp)) {

							// create client : sign client certificate
							CertRequestVO vo = new CertRequestVO();
							vo.setCsr(csr);
							if (valid_date != null && valid_date.length() > 0) {
								vo.setExpireDate(getValidDate(valid_date).getTime());
							} else if (registrationDataVO.getExpireDate() != null) {
								vo.setExpireDate(
										getValidDate(sdf.format(registrationDataVO.getExpireDate())).getTime());
							}
							vo.setOu(ou);
							vo.setComment(comment);
							vo.setCn(cn);
							vo.setName(("".equals(clientName)) ? cn : clientName);
							vo.setIpv4(ipv4);
							vo.setIpv6(ipv6);
							vo.setAdminUserId(GPMSConstants.CODE_REGKEY_ADMIN);

							// String clientPem = "";
							try {
								resultVO = certificateService.createCertificateFromCSR(vo);
							} catch (Exception ex) {
								// System.out.println("\nex : " + ex + "\n");
								logger.error("error in registerByRegKey(createCertificate) : {}, {}, {}",
										GPMSConstants.CODE_SYSERROR,
										MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR), ex.toString());
								if (resultVO != null) {
									resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_SYSERROR,
											MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR)));
								}
							}
						} else {
							// invalid ip range.
							resultVO.setStatus(
									new StatusVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_REGIST_ERROR_IPRANGE,
											MessageSourceHelper.getMessage(GPMSConstants.MSG_REGIST_ERROR_IPRANGE)));
						}
					} else {
						// invalid date.
						resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_REGIST_ERROR_DATE,
								MessageSourceHelper.getMessage(GPMSConstants.MSG_REGIST_ERROR_DATE)));
					}
				} else {
					// invalid client registration key information.
					resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_REGIST_ERROR_REGKEY,
							MessageSourceHelper.getMessage(GPMSConstants.MSG_REGIST_ERROR_REGKEY)));
				}

			} catch (Exception ex) {

				logger.error("error in registerByRegKey : {}, {}, {}", GPMSConstants.CODE_SYSERROR,
						MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR), ex.toString());
				if (resultVO != null) {
					resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_SYSERROR,
							MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR)));
				}
			}

			return resultVO;
		}
	}

	/**
	 * 단말 갱신 요청 (CSR + 등록키)
	 * 
	 * @param CertRequestVO paramVO
	 * @return ResultVO
	 * @throws Exception
	 */
	@PostMapping("/v1/client/register/regkey/update")
	public ResultVO updateByRegKey(HttpServletRequest request,
			@RequestParam(value = "csr", required = true, defaultValue = "") String csr,
			@RequestParam(value = "valid_date", required = false, defaultValue = "") String valid_date,
			@RequestParam(value = "ou", required = false, defaultValue = "") String ou,
			@RequestParam(value = "comment", required = false, defaultValue = "") String comment,
			@RequestParam(value = "name", required = false, defaultValue = "") String clientName,
			@RequestParam(value = "cn", required = true, defaultValue = "") String cn,
			@RequestParam(value = "ipv4", required = true, defaultValue = "") String ipv4,
            @RequestParam(value = "ipv6", required = true, defaultValue = "") String ipv6,
			@RequestParam(value = "regkey", required = true, defaultValue = "") String registKey) {

		ResultVO resultVO = new ResultVO();
		String myIp = request.getRemoteAddr();

		logger.info("MyIp : " + myIp);

		// 파라미터로 받은 CSR 확인
		if (csr == null || "".equals(csr)) {
			return new ResultVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_NODATA,
					MessageSourceHelper.getMessage("register.result.csrerror"));
		} else if (registKey == null || "".equals(registKey)) {
			return new ResultVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_NODATA,
					MessageSourceHelper.getMessage("register.result.noregistkey"));
		} else {

			try {
				ResultVO registrationDataRe = clientValidService.getRegistInfoForKey(registKey);
				if ("success".equalsIgnoreCase(registrationDataRe.getStatus().getResult())
						&& registrationDataRe.getData() != null && registrationDataRe.getData().length > 0) {
					ClientRegKeyVO registrationDataVO = (ClientRegKeyVO) registrationDataRe.getData()[0];

					// check valid date
					if (registrationDataVO.getValidDate().after(Calendar.getInstance().getTime())) {
						// check ip arrange
						if (checkIpRange(registrationDataVO.getIpRange(), myIp)) {

							// create client : sign client certificate
							CertRequestVO vo = new CertRequestVO();
							vo.setCsr(csr);
							if (valid_date != null && valid_date.length() > 0) {
								vo.setExpireDate(getValidDate(valid_date).getTime());
							} else if (registrationDataVO.getExpireDate() != null) {
								vo.setExpireDate(
										getValidDate(sdf.format(registrationDataVO.getExpireDate())).getTime());
							}
							vo.setOu(ou);
							vo.setComment(comment);
							vo.setCn(cn);
							vo.setName(("".equals(clientName)) ? cn : clientName);
							vo.setIpv4(ipv4);
							vo.setIpv6(ipv6);
							vo.setAdminUserId(GPMSConstants.CODE_REGKEY_ADMIN);

							// String clientPem = "";
							try {
								resultVO = certificateService.updateCertificateFromCSR(vo);
							} catch (Exception ex) {
								// System.out.println("\nex : " + ex + "\n");
								logger.error("error in updateByRegKey(createCertificate) : {}, {}, {}",
										GPMSConstants.CODE_SYSERROR,
										MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR), ex.toString());
								if (resultVO != null) {
									resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_SYSERROR,
											MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR)));
								}
							}
						} else {
							// invalid ip range.
							resultVO.setStatus(
									new StatusVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_REGIST_ERROR_IPRANGE,
											MessageSourceHelper.getMessage(GPMSConstants.MSG_REGIST_ERROR_IPRANGE)));
						}
					} else {
						// invalid date.
						resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_REGIST_ERROR_DATE,
								MessageSourceHelper.getMessage(GPMSConstants.MSG_REGIST_ERROR_DATE)));
					}
				} else {
					// invalid client registration key information.
					resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_REGIST_ERROR_REGKEY,
							MessageSourceHelper.getMessage(GPMSConstants.MSG_REGIST_ERROR_REGKEY)));
				}

			} catch (Exception ex) {

				logger.error("error in updateByRegKey : {}, {}, {}", GPMSConstants.CODE_SYSERROR,
						MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR), ex.toString());
				if (resultVO != null) {
					resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_SYSERROR,
							MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR)));
				}
			}

			return resultVO;
		}
	}

	/**
	 * 단말 등록 요청 (CSR + 등록키)
	 * 
	 * @param CertRequestVO paramVO
	 * @return ResultVO
	 * @throws Exception
	 */
	@PostMapping("/v1/client/register/regkey/create_or_update")
	public ResultVO recreateByRegKey(HttpServletRequest request,
			@RequestParam(value = "csr", required = true, defaultValue = "") String csr,
			@RequestParam(value = "valid_date", required = false, defaultValue = "") String valid_date,
			@RequestParam(value = "ou", required = false, defaultValue = "") String ou,
			@RequestParam(value = "comment", required = false, defaultValue = "") String comment,
			@RequestParam(value = "cn", required = true, defaultValue = "") String cn,
			@RequestParam(value = "ipv4", required = true, defaultValue = "") String ipv4,
            @RequestParam(value = "ipv6", required = true, defaultValue = "") String ipv6,
			@RequestParam(value = "name", required = false, defaultValue = "") String clientName,
			@RequestParam(value = "regkey", required = true, defaultValue = "") String registKey) {

		ResultVO resultVO = new ResultVO();
		String myIp = request.getRemoteAddr();

		logger.info("MyIp : " + myIp);

		// 파라미터로 받은 CSR 확인
		if (csr == null || "".equals(csr)) {
			return new ResultVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_NODATA,
					MessageSourceHelper.getMessage("register.result.csrerror"));
		} else if (registKey == null || "".equals(registKey)) {
			return new ResultVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_NODATA,
					MessageSourceHelper.getMessage("register.result.noregistkey"));
		} else {

			try {
				ResultVO registrationDataRe = clientValidService.getRegistInfoForKey(registKey);
				if ("success".equalsIgnoreCase(registrationDataRe.getStatus().getResult())
						&& registrationDataRe.getData() != null && registrationDataRe.getData().length > 0) {

					ClientRegKeyVO registrationDataVO = (ClientRegKeyVO) registrationDataRe.getData()[0];

					// check valid date
					if (registrationDataVO.getValidDate().after(Calendar.getInstance().getTime())) {
						// check ip arrange
						if (checkIpRange(registrationDataVO.getIpRange(), myIp)) {

							// create client : sign client certificate
							CertRequestVO vo = new CertRequestVO();
							vo.setCsr(csr);
							if (valid_date != null && valid_date.length() > 0) {
								vo.setExpireDate(getValidDate(valid_date).getTime());
							} else if (registrationDataVO.getExpireDate() != null) {
								vo.setExpireDate(
										getValidDate(sdf.format(registrationDataVO.getExpireDate())).getTime());
							}
							vo.setOu(ou);
							vo.setComment(comment);
							vo.setCn(cn);
							vo.setName(("".equals(clientName)) ? cn : clientName);
							vo.setIpv4(ipv4);
							vo.setIpv6(ipv6);
							vo.setAdminUserId(GPMSConstants.CODE_REGKEY_ADMIN);

							// String clientPem = "";
							try {
								resultVO = certificateService.reCreateCertificateFromCSR(vo);
							} catch (Exception ex) {
								// System.out.println("\nex : " + ex + "\n");
								logger.error("error in recreateByRegKey(createCertificate) : {}, {}, {}",
										GPMSConstants.CODE_SYSERROR,
										MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR), ex.toString());
								if (resultVO != null) {
									resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_SYSERROR,
											MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR)));
								}
							}
						} else {
							// invalid ip range.
							resultVO.setStatus(
									new StatusVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_REGIST_ERROR_IPRANGE,
											MessageSourceHelper.getMessage(GPMSConstants.MSG_REGIST_ERROR_IPRANGE)));
						}
					} else {
						// invalid date.
						resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_REGIST_ERROR_DATE,
								MessageSourceHelper.getMessage(GPMSConstants.MSG_REGIST_ERROR_DATE)));
					}
				} else {
					// invalid client registration key information.
					resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_REGIST_ERROR_REGKEY,
							MessageSourceHelper.getMessage(GPMSConstants.MSG_REGIST_ERROR_REGKEY)));
				}

			} catch (Exception ex) {

				logger.error("error in recreateByRegKey : {}, {}, {}", GPMSConstants.CODE_SYSERROR,
						MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR), ex.toString());
				if (resultVO != null) {
					resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_SYSERROR,
							MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR)));
				}
			}

			return resultVO;
		}
	}

}
