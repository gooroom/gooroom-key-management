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

package kr.gooroom.gpms.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class GPMSConstants {

	private static final Properties prop = new Properties();
	private static final String GOOROOM_PROPERTIES = "/properties/gooroomapi.properties";

	static {
		InputStream is = GPMSConstants.class.getClassLoader().getResourceAsStream(GOOROOM_PROPERTIES);
		try {
			prop.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static final String SITE_NAME = prop.getProperty("gooroom.site.name");

	public static final String MSG_SUCCESS = "success";
	public static final String MSG_FAIL = "fail";

	public static final String ROOT_CERTPATH = prop.getProperty("gooroom.root.certificate.path");
	public static final String ROOT_CERTFILENAME = prop.getProperty("gooroom.root.certificate.filename");
	public static final String ROOT_KEYPATH = prop.getProperty("gooroom.root.privatekey.path");
	public static final String ROOT_KEYFILENAME = prop.getProperty("gooroom.root.privatekey.filename");

	public static final String CA_CLIENT_CRL_FILE = "client_crl_list.crl";
	public static final String CA_CRL_SERVICE_PATH = prop.getProperty("gooroom.crl.path");
	public static final String TEMP_CRL_PATH = prop.getProperty("gooroom.crl.temp.path");

	// Client Status Code
	public static final String STS_INITIALIZE = "STAT001";
	public static final String STS_USABLE = "STAT010";
	public static final String STS_REVOKED = "STAT021";
	public static final String STS_EXPIRE = "STAT022";

	public static final String GKM_SERVER_CERTFILE = prop.getProperty("gooroom.gkmserver.certificate.file");
	public static final String GLM_SERVER_CERTFILE = prop.getProperty("gooroom.glmserver.certificate.file");
	public static final String GRM_SERVER_CERTFILE = prop.getProperty("gooroom.grmserver.certificate.file");

	public static final String CODE_SYSERROR = "ERR9999";
	public static final String MSG_SYSERROR = "system.common.error";
	public static final String CODE_NODATA = "GRSM0004";

	public static final String CODE_SELECT = "GRSM0010";
	public static final String CODE_SELECTERROR = "GRSM3010";
	public static final String CODE_INSERT = "GRSM0011";
	public static final String CODE_INSERTERROR = "GRSM3011";
	public static final String CODE_DELETE = "GRSM0012";
	public static final String CODE_DELETEERROR = "GRSM3012";
	public static final String CODE_UPDATE = "GRSM0013";
	public static final String CODE_UPDATEERROR = "GRSM3013";

	public static final String MSG_REGIST_ERROR_IPRANGE = "reg.error.iprange";
	public static final String CODE_REGIST_ERROR_IPRANGE = "IP_RANGE_ERROR";
	public static final String MSG_REGIST_ERROR_DATE = "reg.error.invaliddate";
	public static final String CODE_REGIST_ERROR_DATE = "INVALID_DATE_FOR_REG-KEY";
	public static final String MSG_REGIST_ERROR_REGKEY = "reg.error.invalidkey";
	public static final String CODE_REGIST_ERROR_REGKEY = "INVALID_REG-KEY";

	public static final String CODE_CHANGE_TYPE_CREATE = "CREATE";
	public static final String CODE_CHANGE_TYPE_UPDATE = "UPDATE";

	public static final String CODE_REGKEY_ADMIN = "REGKEY";

	public static final String CTRL_CLIENT_GROUP_DEFAULT = "CGRPDEFAULT";

	//Portable
	public static final String RSP_CODE_PTGR_SUCCESS = "PCERT000";
	public static final String RSP_CODE_PTGR_FAIL_NONCE = "PCERT001";
	public static final String RSP_CODE_PTGR_FAIL_CERT = "PCERT002";
	public static final String RSP_CODE_PTGR_FAIL_CERT_EXPIRED  = "PCERT003";
	public static final String RSP_CODE_PTGR_FAIL_CERT_SIGN_NONCE = "PCERT004";

	public static final String PORTABLE_SERVER_CERTFILE = prop.getProperty("gooroom.portable.certificate.file");
}
