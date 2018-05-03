/**
 * Project Name:risk-control
 * File Name:SystemSessionInterceptor.java
 * Package Name:com.usolv.risk.interceptor
 * Date:2016年8月8日下午2:25:40
 * Copyright (c) 2016, hokuny@foxmail.com All Rights Reserved.
 *
 */

package com.cros.block.interceptor;

import java.io.File;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.alibaba.fastjson.JSON;
import com.cros.block.common.GetSession;
import com.cros.block.common.MySessionContext;
import com.cros.block.util.ResultData;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * ClassName:SystemSessionInterceptor <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason: TODO ADD REASON. <br/>
 * Date: 2016年8月8日 下午2:25:40 <br/>
 * 
 * @author hokuny@foxmail.com
 * @version
 * @since JDK 1.6
 * @see
 */
public class SystemSessionInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
//		InetAddress ia=null;
//		ia=ia.getLocalHost();
//        String localip=ia.getHostAddress();
		HttpSession session = GetSession.getSession(request);
		//session中获取用户名信息
        Object obj = session.getAttribute("loginUser");
        Object obj1 = session.getAttribute("loginMember");
        if (obj!=null || obj1!=null) {
            return true;
        }else{
        	System.out.println("getContextPath:"+request.getContextPath());
//        	response.sendRedirect("http://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath());
			Map mapResult = new HashMap<>();
			PrintWriter out = response.getWriter();
			mapResult.put("code", -1);
			mapResult.put("msg", "未取得登录权限");
			String jsonReturn = JSON.toJSONString(mapResult);
			response.setCharacterEncoding("UTF-8"); // 设置编码格式
			response.setContentType("application/json"); // 设置数据格式
			try {
				out.write(jsonReturn);
				out.flush();
			} finally {
				if (null != out) {
					out.close();
				}
			}
            return false;
        }
	}

	@Override
	public void postHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {

		// TODO Auto-generated method stub

	}

	@Override
	public void afterCompletion(HttpServletRequest request,
			HttpServletResponse response, Object handler, Exception ex)
			throws Exception {

		// TODO Auto-generated method stub

	}

}
