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

import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import kr.gooroom.gpms.common.GPMSConstants;
import kr.gooroom.gpms.common.service.ClientGroupIpInfoVO;
import kr.gooroom.gpms.common.service.ServerAddrInfoVO;
import kr.gooroom.gpms.common.service.ServerVersionVO;
import kr.gooroom.gpms.common.service.dao.SqlSessionMetaDAO;
import kr.gooroom.gpms.common.utils.MessageSourceHelper;

@Repository("gpmsCommonDAO")
public class GpmsCommonDAO extends SqlSessionMetaDAO {

	private static final Logger logger = LoggerFactory.getLogger(GpmsCommonDAO.class);

	/**
	 * 서버 정보(ip, url) 조회
	 * 
	 * @param
	 * @return ServerAddrInfoVO
	 * @throws SQLException
	 */
	public ServerAddrInfoVO getGpmsServersInfo() throws SQLException {
		ServerAddrInfoVO re = null;
		try {
			re = sqlSessionMeta.selectOne("selectServerAddrInfo");
		} catch (Exception ex) {
			re = null;
			logger.error("error in getGpmsServersInfo : {}, {}, {}", GPMSConstants.CODE_SYSERROR,
					MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR), ex.toString());
		}
		return re;
	}

	/**
	 * 서버 버전 조회
	 * 
	 * @param
	 * @return ServerVersion
	 * @throws SQLException
	 */
	public ServerVersionVO selectSiteVersion() throws SQLException {
		ServerVersionVO re = null;
		try {
			re = sqlSessionMeta.selectOne("selectSiteVersion", GPMSConstants.SITE_NAME);
		} catch (Exception ex) {
			re = null;
			logger.error("error in selectSiteVersion : {}, {}, {}", GPMSConstants.CODE_SYSERROR,
					MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR), ex.toString());
		}
		return re;
	}

	/**
	 * 단말그룹 자동등록 IP 정보 조회
	 * 
	 * @param
	 * @return ServerVersion
	 * @throws SQLException
	 */
	public List<ClientGroupIpInfoVO> selectClientGroupIpInfo() throws SQLException {
		List<ClientGroupIpInfoVO> re = null;
		try {
			re = sqlSessionMeta.selectList("selectClientGroupIpInfo");
		} catch (Exception ex) {
			re = null;
			logger.error("error in selectClientGroupIpInfo : {}, {}, {}", GPMSConstants.CODE_SYSERROR,
					MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR), ex.toString());
		}
		return re;
	}

}
