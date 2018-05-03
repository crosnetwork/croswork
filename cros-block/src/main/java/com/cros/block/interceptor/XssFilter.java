/**
 * Project Name:trader-eval
 * File Name:XssFilter.java
 * Package Name:com.trader.eval.interceptor
 * Date:2016年12月9日下午4:24:33
 * Copyright (c) 2016, hokuny@foxmail.com All Rights Reserved.
 *
*/

package com.cros.block.interceptor;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.cros.block.interceptor.XssHttpServletRequestWrapper;

/**
 * ClassName:XssFilter <br/>
 * Function: CSRF、XSS和SQL注入攻击
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2016年12月9日 下午4:24:33 <br/>
 * @author   hokuny@foxmail.com
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
public class XssFilter implements Filter {
	FilterConfig filterConfig = null;
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		System.out.println("init");
		this.filterConfig = filterConfig;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		 chain.doFilter(new XssHttpServletRequestWrapper((HttpServletRequest)request), response);
	}

	@Override
	public void destroy() {
		 this.filterConfig = null;
	}

}

