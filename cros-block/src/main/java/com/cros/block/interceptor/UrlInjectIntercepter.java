package com.cros.block.interceptor;

import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * 
 * @ClassName: UrlInjectIntercepter
 * @Package com.trader.eval.interceptor
 * @Description:TODO ADD FUNCTION
 * @date: 2016年12月15日 上午12:01:24
 * @author yeran
 * @version
 */
public class UrlInjectIntercepter implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
		StringBuffer path = request.getRequestURL();
		if(path.toString().contains(".php")||path.toString().contains(".asp")||path.toString().contains(".aspx")){
			return false;
		}else{
			return true;
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
