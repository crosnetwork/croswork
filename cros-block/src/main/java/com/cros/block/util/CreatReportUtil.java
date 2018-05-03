package com.cros.block.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.spi.LoggerFactory;

import com.cros.block.controller.LoginContrllor;
import com.mongodb.diagnostics.logging.Logger;



/**
 * @ClassName: CreatReportUtil
 * @Package com.cros.util
 * @Description:TODO ADD FUNCTION
 * @date: 2016年8月16日 上午11:23:49
 * @author weixicai
 * @version 
 */
public class CreatReportUtil {
	private static Log logger = LogFactory.getLog(CreatReportUtil.class);
	public static String getIpAddr(HttpServletRequest request) {
		String ipAddress = request.getHeader("x-forwarded-for");
		if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getHeader("Proxy-Client-IP");
		}
		if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getRemoteAddr();
			if (ipAddress.equals("127.0.0.1") || ipAddress.equals("0:0:0:0:0:0:0:1")) {
				InetAddress inet = null;
				try {
					inet = InetAddress.getLocalHost();
				} catch (UnknownHostException e) {
					logger.error("异常信息：" + e.getMessage());
				}
				if(inet!=null){
					ipAddress = inet.getHostAddress();
				}
			}
		}
		// 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
		if (!ipAddress.isEmpty() && ipAddress.length() > 15) { // "***.***.***.***".length()
															// = 15
			if (ipAddress.contains(",")) {
				ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
			}
		}
		return ipAddress;
	}
}
