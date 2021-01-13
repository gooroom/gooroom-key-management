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

package kr.gooroom.gpms.common.service;

/**
 * Common service class.
 * 
 * @author HNC
 * @version 1.0
 * @since 1.8
 */

public interface GpmsCommonService {

	/**
	 * 서버 정보(ip, url) 조회
	 * 
	 * @param
	 * @return ResultVO
	 * @throws Exception
	 */
	ResultVO getGpmsServersInfo() throws Exception;

	/**
	 * 서버 인증서 정보(gkm, glm, grm) 조회
	 * 
	 * @param
	 * @return ResultVO
	 * @throws Exception
	 */
	ResultVO getGpmsServersCertificate() throws Exception;

	/**
	 * 서버 버전 조회
	 * 
	 * @param
	 * @return ResultVO
	 * @throws Exception
	 */
	ResultVO getGpmsServerVersion() throws Exception;

}
