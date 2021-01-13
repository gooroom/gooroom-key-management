package kr.gooroom.gpms.common.utils;

import java.math.BigInteger;
import java.net.InetAddress;
import java.util.List;
import java.util.regex.Pattern;

import kr.gooroom.gpms.common.service.ClientGroupIpInfoVO;
import org.bouncycastle.util.IPAddress;
import org.springframework.security.web.util.matcher.IpAddressMatcher;

public class AutoGroupSelector {

	private static String regexIPv4 = "^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$";
	private static String regexIPv6 = "^(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]).){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]).){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))$";
	private static String regexIPv4andIPv6 = "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)|(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]).){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]).){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))$";

	private List<ClientGroupIpInfoVO> data = null;

	public AutoGroupSelector(List<ClientGroupIpInfoVO> data) {
		this.data = data;
	}

	private String checkIpVersion(String ipStr) {

		String ipVersion = "";
		Pattern pattern;
		pattern = Pattern.compile(regexIPv4andIPv6);
		if (ipStr == null || pattern.matcher(ipStr).matches() == false) {
			System.out.println("유효하지 않은 IP 주소입니다.");

		} else {
			// IPv4
			pattern = Pattern.compile(regexIPv4);
			if (pattern.matcher(ipStr).matches() == true) {
				ipVersion = "IPv4";
			}

			// IPv6
			pattern = Pattern.compile(regexIPv6);
			if (pattern.matcher(ipStr).matches() == true) {
				ipVersion = "IPv6";
			}
		}

		return ipVersion;
	}

	private String makeIpString(String rowIp) {
		if (rowIp != null) {
			if("IPv4".equals(checkIpVersion(rowIp))) {
				String[] parts = rowIp.split("\\.");
				if (parts.length == 4) {
					for (int i = 0; i < parts.length; i++) {
						if("*".equals(parts[i])) {
							parts[i] = "";
							break;
						} else {
							parts[i] = ("000" + parts[i]).substring(parts[i].length());
						}
					}
					return String.join("", parts);
				}
			} else if("IPv6".equals(checkIpVersion(rowIp))) {

				String[] parts = new String[8];
				int contractionIdx = rowIp.indexOf("::");
				if(contractionIdx == -1) {  //없어
					parts = rowIp.split(":");
				} else {
					String firstStr=rowIp.substring(0,contractionIdx);
					String secondStr=rowIp.substring(contractionIdx+2);
					if(firstStr.length() == 0) { //맨앞
						String[] innerParts = secondStr.split(":");
						for(int i = 0; i <= 8 - innerParts.length; i++) {
							parts[i] = "0000";
						}
						System.arraycopy(innerParts, 0, parts, 8-innerParts.length, innerParts.length);
					} else if(secondStr.length() == 0) { //맨뒤
						String[] innerParts = firstStr.split(":");
						System.arraycopy(innerParts, 0, parts, 0, innerParts.length);
						for(int i = innerParts.length; i < 8; i++) {
							parts[i] = "0000";
						}
					} else {
						String[] front = firstStr.split(":");
						String[] back = secondStr.split(":");
						System.arraycopy(front, 0, parts, 0, front.length);
						for(int i=front.length;i<=8-front.length-back.length;i++) {
							parts[i] = "0000";
						}
						System.arraycopy(back, 0, parts, front.length+(8-front.length-back.length), back.length );
					}
				}
				for(int i = 0; i < parts.length; i++) {
					if("*".equals(parts[i])) {
						parts[i] = "";
						break;
					} else {
//						parts[i] = String.valueOf(Long.parseLong(parts[i], 16));
						parts[i] = ("0000" + parts[i]).substring(parts[i].length());
					}
				}

				return String.join("", parts);
			}
		}
		return "";
	}

	public String getClientGroupId(String ipStr) {
		String ip = makeIpString(ipStr);
		if (data != null && data.size() > 0 && (ip.length() == 12 || ip.length() == 32)) {
			String re = "";
			for (ClientGroupIpInfoVO vo : data) {
				re = "";
				if (vo.getRegClientIp() != null) {
					if (vo.getRegClientIp().indexOf("~") > 0) {
						String[] st = vo.getRegClientIp().split("~");
						if (st.length > 1) {
							if("IPv4".equals(checkIpVersion(ipStr))) {
								try {
									long from = Long.parseLong(makeIpString(st[0].trim()));
									long to = Long.parseLong(makeIpString(st[1].trim()));
									if (from <= Long.parseLong(ip) && to >= Long.parseLong(ip)) {
										return vo.getGrpId();
									}
								} catch (Exception eex) {
								}
							} else if("IPv6".equals(checkIpVersion(ipStr))) {
								try {
									BigInteger from = new BigInteger(makeIpString(st[0].trim()));
									BigInteger to = new BigInteger(makeIpString(st[1].trim()));
									BigInteger target = new BigInteger(ip);
									if(target.compareTo(from) == 0 || target.compareTo(to) == 0 ||
											(target.compareTo(from) == 1 && target.compareTo(to) == -1)) {
										return vo.getGrpId();
									}
								} catch (Exception eex) {
								}
							}
						}
					} else if (vo.getRegClientIp().indexOf("-") > 0) {
						String[] st = vo.getRegClientIp().split("-");
						if (st.length > 1) {
							if("IPv4".equals(checkIpVersion(ipStr))) {
								try {
									long from = Long.parseLong(makeIpString(st[0].trim()));
									long to = Long.parseLong(makeIpString(st[1].trim()));
									if (from <= Long.parseLong(ip) && to >= Long.parseLong(ip)) {
										return vo.getGrpId();
									}
								} catch (Exception eex) {
								}
							} else if("IPv6".equals(checkIpVersion(ipStr))) {
								try {
									BigInteger from = new BigInteger(makeIpString(st[0].trim()));
									BigInteger to = new BigInteger(makeIpString(st[1].trim()));
									BigInteger target = new BigInteger(ip);
									if(target.compareTo(from) == 0 || target.compareTo(to) == 0 ||
											(target.compareTo(from) == 1 && target.compareTo(to) == -1)) {
										return vo.getGrpId();
									}
								} catch (Exception eex) {
								}
							}
						}
					} else if (vo.getRegClientIp().indexOf("*") > 0) {
						String headIpStr = makeIpString(vo.getRegClientIp().trim());
						if(ip.startsWith(headIpStr)) {
							return vo.getGrpId();
						}
					} else {
						String headIpStr = makeIpString(vo.getRegClientIp().trim());
						if(ip.equals(headIpStr)) {
							return vo.getGrpId();
						}
					}
				}
			}

			return re;
		} else {
			return "";
		}
	}

}
