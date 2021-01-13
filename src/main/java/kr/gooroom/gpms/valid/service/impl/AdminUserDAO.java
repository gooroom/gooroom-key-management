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

import java.sql.SQLException;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import kr.gooroom.gpms.common.GPMSConstants;
import kr.gooroom.gpms.common.service.dao.SqlSessionMetaDAO;
import kr.gooroom.gpms.common.utils.MessageSourceHelper;
import kr.gooroom.gpms.valid.service.AdminUserVO;

/**
 * data access object class for administrator user management process.
 * 
 * @author HNC
 * @version 1.0
 * @since 1.8
 */

@Repository("adminUserDAO")
public class AdminUserDAO extends SqlSessionMetaDAO {

	private static final Logger logger = LoggerFactory.getLogger(AdminUserDAO.class);

	/**
	 * response administrator user information by user id and password for
	 * authority.
	 * 
	 * @param adminId string administrator user id
	 * @param adminPw string administrator user password
	 * @return AdminUserVO List
	 * @throws SQLException
	 */
	public AdminUserVO selectAdminUserAuthAndInfo(String adminId, String adminPw) throws SQLException {

		AdminUserVO re = null;
		try {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("adminId", adminId);
			map.put("adminPw", adminPw);

			re = sqlSessionMeta.selectOne("selectAdminUserAuthAndInfo", map);

		} catch (Exception ex) {
			logger.error("error in selectAdminUserAuthAndInfo : {}, {}, {}", GPMSConstants.CODE_SYSERROR,
					MessageSourceHelper.getMessage(GPMSConstants.MSG_SYSERROR), ex.toString());
			re = null;
		}

		return re;
	}

}
