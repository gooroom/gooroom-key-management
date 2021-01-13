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

package kr.gooroom.gpms.valid.service;

import kr.gooroom.gpms.common.service.ResultVO;

public interface ClientValidService {

	/**
	 * get administrator user information data by user id and password.
	 * 
	 * @param adminId string user id
	 * @param adminPw string user password
	 * @return ResultVO result data bean
	 * @throws Exception
	 */
	ResultVO getAdminUserAuthAndInfo(String adminId, String adminPw) throws Exception;

	/**
	 * get registration properties by regist-key.
	 * 
	 * @param registKey string regist-key
	 * @return ResultVO result data bean
	 * @throws Exception
	 */
	ResultVO getRegistInfoForKey(String registKey) throws Exception;
}
