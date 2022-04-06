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

import kr.gooroom.gpms.common.service.dao.SqlSessionMetaDAO;
import kr.gooroom.gpms.gkm.service.PtgrNonceVO;
import org.springframework.stereotype.Repository;

import java.util.Map;

/**
 *  휴대형구름 인증관련 DAO
 */

@Repository("ptgrNonceDAO")
public class PtgrNonceDAO extends SqlSessionMetaDAO {

    /**
     * 인증서 난수값 업데이트
     * @param ptgrNonceVO
     * @return
     * @throws Exception
     */
    public int insertCertNonce (PtgrNonceVO ptgrNonceVO) throws Exception {
	return sqlSessionMeta.insert("ptgrNonceDAO.insertCertNonce", ptgrNonceVO);
    }

    /**
     * 인증서 난수값 조회
     * 
     * @param paramMap
     * @return
     * @throws Exception
     */
    public PtgrNonceVO selectCertNonce (Map<?,?> paramMap) throws Exception {
	return sqlSessionMeta.selectOne("ptgrNonceDAO.selectCertNonce", paramMap);
    }
}
