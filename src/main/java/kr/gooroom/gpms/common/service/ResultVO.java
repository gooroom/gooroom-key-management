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

import java.io.Serializable;

@SuppressWarnings("serial")
public class ResultVO implements Serializable {

	private StatusVO status;
	private Object[] data;

	public ResultVO() {
		status = null;
		data = null;
	}

	public ResultVO(String result, String resultcode, String errorMessage) {
		status = new StatusVO(result, resultcode, errorMessage);
		data = null;
	}

	public void setResultInfo(StatusVO status, Object[] data) {
		this.status = status;
		this.data = data;
	}

	public StatusVO getStatus() {
		return status;
	}

	public void setStatus(StatusVO status) {
		this.status = status;
	}

	public Object[] getData() {
		return data;
	}

	public void setData(Object[] data) {
		this.data = data;
	}

}
