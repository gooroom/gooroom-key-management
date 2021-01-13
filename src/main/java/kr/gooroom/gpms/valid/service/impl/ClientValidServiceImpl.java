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

package kr.gooroom.gpms.valid.service.impl;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import kr.gooroom.gpms.common.GPMSConstants;
import kr.gooroom.gpms.common.service.ResultVO;
import kr.gooroom.gpms.common.service.StatusVO;
import kr.gooroom.gpms.common.utils.MessageSourceHelper;
import kr.gooroom.gpms.valid.service.AdminUserVO;
import kr.gooroom.gpms.valid.service.ClientRegKeyVO;
import kr.gooroom.gpms.valid.service.ClientValidService;

/**
 * @Class Name : AdminUserServiceImpl.java
 * @Description :
 * @Modification Information
 *
 * @author
 * @since 2017-06-05
 * @version 1.0
 * @see
 * 
 * 		Copyright (C) All right reserved.
 */

@Service("adminUserService")
public class ClientValidServiceImpl implements ClientValidService {

	private static final Logger logger = LoggerFactory.getLogger(ClientValidServiceImpl.class);

	@Resource(name = "adminUserDAO")
	private AdminUserDAO adminUserDao;

	@Resource(name = "registrationDataDAO")
	private ClientRegKeyDAO registrationDataDao;

	/**
	 * get administrator user information data by user id and password.
	 * 
	 * @param adminId string user id
	 * @param adminPw string user password
	 * @return ResultVO result data bean
	 * @throws Exception
	 */
	@Override
	public ResultVO getAdminUserAuthAndInfo(String adminId, String adminPw) throws Exception {

		ResultVO resultVO = new ResultVO();

		try {

			AdminUserVO re = adminUserDao.selectAdminUserAuthAndInfo(adminId, adminPw);

			if (re != null) {

				AdminUserVO[] row = new AdminUserVO[1];
				row[0] = re;
				resultVO.setData(row);
				resultVO.setStatus(new StatusVO(GPMSConstants.MSG_SUCCESS, GPMSConstants.CODE_SELECT,
						MessageSourceHelper.getMessage("system.common.selectdata")));

			} else {

				Object[] o = new Object[0];
				resultVO.setData(o);
				resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_SELECTERROR,
						MessageSourceHelper.getMessage("system.common.adminerror")));
			}

		} catch (Exception ex) {
			logger.error("error in getAdminUserAuthAndInfo : {}, {}, {}", GPMSConstants.CODE_SYSERROR,
					MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR), ex.toString());
			if (resultVO != null) {
				resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_SYSERROR,
						MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR)));
			}
		}

		return resultVO;
	}

	/**
	 * get registration properties by regist-key.
	 * 
	 * @param registKey string regist-key
	 * @return ResultVO result data bean
	 * @throws Exception
	 */
	@Override
	public ResultVO getRegistInfoForKey(String registKey) throws Exception {

		ResultVO resultVO = new ResultVO();

		try {

			ClientRegKeyVO re = registrationDataDao.selectRegistInfo(registKey);

			if (re != null) {

				ClientRegKeyVO[] row = new ClientRegKeyVO[1];
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
			logger.error("error in getAdminUserAuthAndInfo : {}, {}, {}", GPMSConstants.CODE_SYSERROR,
					MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR), ex.toString());
			if (resultVO != null) {
				resultVO.setStatus(new StatusVO(GPMSConstants.MSG_FAIL, GPMSConstants.CODE_SYSERROR,
						MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR)));
			}
		}

		return resultVO;
	}

}
