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

import kr.gooroom.gpms.gkm.service.PtgrNonceService;
import kr.gooroom.gpms.gkm.service.PtgrNonceVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service("ptgrCertService")
public class PtgrNonceServiceImpl implements PtgrNonceService {

	@Resource(name = "ptgrNonceDAO")
	private PtgrNonceDAO ptgrNonceDAO;

	@Override
	public PtgrNonceVO selectCertNonceByCN(String cn) throws Exception {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("cn", cn);
		return ptgrNonceDAO.selectCertNonce(paramMap);
	}

	@Override
	public int insertCentNonce(PtgrNonceVO ptgrNonceVO) throws Exception {
		return ptgrNonceDAO.insertCertNonce(ptgrNonceVO);
	}
}
