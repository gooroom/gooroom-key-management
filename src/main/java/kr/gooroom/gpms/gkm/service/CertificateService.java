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

package kr.gooroom.gpms.gkm.service;

import kr.gooroom.gpms.common.service.ResultVO;
import kr.gooroom.gpms.common.service.StatusVO;

public interface CertificateService {

	/**
	 * create certificate with sign from csr
	 * 
	 * @param CertRequestVO
	 * @return ResultVO
	 * @throws Exception
	 */
	ResultVO createCertificateFromCSR(CertRequestVO vo) throws Exception;

	/**
	 * update certificate with sign from csr
	 * 
	 * @param CertRequestVO
	 * @return ResultVO
	 * @throws Exception
	 */
	ResultVO updateCertificateFromCSR(CertRequestVO vo) throws Exception;

	/**
	 * re-create certificate with sign from csr
	 * 
	 * @param CertRequestVO
	 * @return ResultVO
	 * @throws Exception
	 */
	ResultVO reCreateCertificateFromCSR(CertRequestVO vo) throws Exception;

	/**
	 * verify that the certificate is revoked.
	 * 
	 * @param serialNo
	 * @return StatusVO
	 * @throws Exception
	 */
	StatusVO isRevoked(String serialNo) throws Exception;

}
