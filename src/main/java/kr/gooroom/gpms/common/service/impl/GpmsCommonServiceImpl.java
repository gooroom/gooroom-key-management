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

package kr.gooroom.gpms.common.service.impl;

import java.util.HashMap;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import kr.gooroom.gpms.common.GPMSConstants;
import kr.gooroom.gpms.common.service.CertForServerVO;
import kr.gooroom.gpms.common.service.GpmsCommonService;
import kr.gooroom.gpms.common.service.ResultVO;
import kr.gooroom.gpms.common.service.ServerAddrInfoVO;
import kr.gooroom.gpms.common.service.ServerVersionVO;
import kr.gooroom.gpms.common.service.StatusVO;
import kr.gooroom.gpms.common.utils.MessageSourceHelper;
import kr.gooroom.gpms.gkm.utils.CertificateUtils;

@Service("gpmsCommonService")
public class GpmsCommonServiceImpl implements GpmsCommonService {

	private static final Logger logger = LoggerFactory.getLogger(GpmsCommonServiceImpl.class);

	@Resource(name = "gpmsCommonDAO")
	private GpmsCommonDAO gpmsCommonDAO;

	/**
	 * 서버 정보(ip, url) 조회
	 * 
	 * @param
	 * @return ResultVO
	 * @throws Exception
	 */
	@Override
	public ResultVO getGpmsServersInfo() throws Exception {

		ResultVO resultVO = new ResultVO();

		try {

			ServerAddrInfoVO re = gpmsCommonDAO.getGpmsServersInfo();

			if (re != null) {

				ServerAddrInfoVO[] row = new ServerAddrInfoVO[1];
				row[0] = re;
				resultVO.setData(row);
				resultVO.setStatus(new StatusVO(GPMSConstants.MSG_SUCCESS, GPMSConstants.CODE_SELECT,
						MessageSourceHelper.getMessage("system.common.selectdata")));

			} else {

				Object[] o = new Object[0];
				resultVO.setData(o);
				resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_SELECTERROR,
						MessageSourceHelper.getMessage("system.common.noselectdata")));
			}

		} catch (Exception ex) {
			logger.error("error in getGpmsServersInfo : {}, {}, {}", GPMSConstants.CODE_SYSERROR,
					MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR), ex.toString());
			if (resultVO != null) {
				resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_SYSERROR,
						MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR)));
			}
		}

		return resultVO;
	}

	/**
	 * 서버 인증서 정보(gkm, glm, grm) 조회
	 * 
	 * @param
	 * @return ResultVO
	 * @throws Exception
	 */
	@Override
	public ResultVO getGpmsServersCertificate() throws Exception {

		ResultVO resultVO = new ResultVO();
		CertificateUtils utils = new CertificateUtils();

		try {

			CertForServerVO certificateVO = utils.getServerCertificate();

			if (certificateVO != null) {

				Object[] objects = { certificateVO };
				resultVO.setData(objects);
				resultVO.setStatus(new StatusVO(GPMSConstants.MSG_SUCCESS, GPMSConstants.CODE_SELECT,
						MessageSourceHelper.getMessage("system.common.selectdata")));

			} else {

				Object[] o = new Object[0];
				resultVO.setData(o);
				resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_SELECTERROR,
						MessageSourceHelper.getMessage("system.common.noselectdata")));
			}

		} catch (Exception ex) {
			logger.error("error in getGpmsServersCertificate : {}, {}, {}", GPMSConstants.CODE_SYSERROR,
					MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR), ex.toString());
			if (resultVO != null) {
				resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_SYSERROR,
						MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR)));
			}
		}

		return resultVO;
	}

	/**
	 * 서버 버전 조회
	 * 
	 * @param
	 * @return ResultVO
	 * @throws Exception
	 */
	@Override
	public ResultVO getGpmsServerVersion() throws Exception {

		ResultVO resultVO = new ResultVO();

		try {

			ServerVersionVO re = gpmsCommonDAO.selectSiteVersion();
			if (re != null) {
				ServerVersionVO[] row = new ServerVersionVO[1];
				row[0] = re;
				resultVO.setData(row);
				resultVO.setStatus(new StatusVO(GPMSConstants.MSG_SUCCESS, GPMSConstants.CODE_SELECT,
						MessageSourceHelper.getMessage("system.common.selectdata")));
			} else {

				Object[] o = new Object[0];
				resultVO.setData(o);
				resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_SELECTERROR,
						MessageSourceHelper.getMessage("system.common.noselectdata")));
			}

		} catch (Exception ex) {
			logger.error("error in getGpmsServerVersion : {}, {}, {}", GPMSConstants.CODE_SYSERROR,
					MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR), ex.toString());
			if (resultVO != null) {
				resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_SYSERROR,
						MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR)));
			}
		}

		return resultVO;
	}

}
