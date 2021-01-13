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

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;

import kr.gooroom.gpms.common.GPMSConstants;
import kr.gooroom.gpms.common.service.CertForClientVO;
import kr.gooroom.gpms.common.service.dao.SqlSessionMetaDAO;

@Repository("certificateDAO")
public class CertificateDAO extends SqlSessionMetaDAO {

	/**
	 * 인증서 정보 등록
	 * 
	 * @param CertRequestVO
	 * @return long result
	 * @throws Exception
	 */
	public long createClientCertificate(CertForClientVO vo) throws SQLException {

		long re1 = sqlSessionMeta.insert("insertCertificateInfo", vo);
		sqlSessionMeta.insert("insertCertificateInfoExt", vo);
		sqlSessionMeta.insert("insertCertificateInfoHist", vo);

		return re1;

	}

	/**
	 * 인증서 정보 수정(업데이트) - 인증서 변경
	 * 
	 * @param CertRequestVO
	 * @return long result
	 * @throws Exception
	 */
	public long updateClientCertificate(CertForClientVO vo) throws SQLException {
		long re1 = sqlSessionMeta.insert("updateCertificateInfo", vo);
		sqlSessionMeta.insert("insertCertificateInfoHist", vo);
		return re1;
	}

	/**
	 * 인증서 정보 수정(업데이트) - 인증서와 이름 변경
	 * 
	 * @param CertRequestVO
	 * @return long result
	 * @throws Exception
	 */
	public long updateClientCertificateAndName(CertForClientVO vo) throws SQLException {
		long re1 = sqlSessionMeta.insert("updateCertificateAndNameInfo", vo);
		sqlSessionMeta.insert("insertCertificateInfoHist", vo);
		return re1;
	}

	/**
	 * 인증서 CN 중복 검사 - select count
	 * 
	 * @param CertRequestVO
	 * @return boolean result
	 * @throws Exception
	 */
	public boolean isValidClientName(String cn) throws SQLException {

		boolean isValid = false;
		int re = -1;

		try {
			re = ((Integer) sqlSessionMeta.selectOne("selectClientNameCount", cn)).intValue();
			if (re > 0) {
				isValid = false;
			} else {
				isValid = true;
			}
		} catch (Exception ex) {
			re = -1;
			isValid = false;
		}

		return isValid;
	}

	/**
	 * 인증서 CN 존재여부 검사 - select count
	 * 
	 * @param CertRequestVO
	 * @return boolean result
	 * @throws Exception
	 */
	public boolean isExistClientName(String cn) throws SQLException {

		boolean isValid = false;
		int re = -1;

		try {
			re = ((Integer) sqlSessionMeta.selectOne("selectClientNameCount", cn)).intValue();
			if (re > 0) {
				isValid = true;
			} else {
				isValid = false;
			}
		} catch (Exception ex) {
			re = -1;
			isValid = false;
		}

		return isValid;
	}

	/**
	 * 단말기 번호 를 위한 시쿼스 조회
	 * 
	 * @param CertRequestVO
	 * @return long result
	 * @throws Exception
	 */
	public int selectNextClientNo() throws SQLException {

		int re = -1;

		try {
			re = ((Integer) sqlSessionMeta.selectOne("selectNextClinetNo")).intValue();
		} catch (Exception ex) {
			re = -1;
		}

		return re;
	}

	/**
	 * verify that the certificate is revoked.
	 * 
	 * @param serialNo
	 * @return boolean result
	 * @throws Exception
	 */
	public boolean isRevoked(String serialNo) throws SQLException {

		boolean revoked = false;
		int re = -1;

		try {

			Map<String, Object> param = new HashMap<String, Object>();
			param.put("serialNo", serialNo);
			param.put("status", GPMSConstants.STS_REVOKED);

			re = ((Integer) sqlSessionMeta.selectOne("selectRevokedClientBySerialNo", param)).intValue();
			if (re > 0) {
				revoked = false;
			} else {
				revoked = true;
			}
		} catch (Exception ex) {
			re = -1;
			revoked = true;
		}

		return revoked;
	}

}
