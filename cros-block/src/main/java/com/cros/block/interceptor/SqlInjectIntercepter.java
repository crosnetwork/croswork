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
 * @ClassName: SqlInjectIntercepter
 * @Package com.trader.eval.interceptor
 * @Description:TODO ADD FUNCTION
 * @date: 2016年8月15日 下午1:19:15
 * @author hokuny@foxmail.com
 * @version 
 */
public class SqlInjectIntercepter implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
		if(request!=null && "POST".equals(request.getMethod().toUpperCase(Locale.ENGLISH))){
			return true;
		}
		if(request!=null){
			Enumeration<String> names = request.getParameterNames();
			if(names!=null){
				while (names.hasMoreElements()) {
					String name = names.nextElement();
					String[] values = request.getParameterValues(name);
					for (int i = 0; i < values.length; i++) {
						
						if(isSqlInject(values[i])){
							System.out.println("==="+values[i]);
							response.setContentType("text/html;charset=utf-8");
							PrintWriter out =response.getWriter();
							out.flush();
							out.println("<script>");
							out.println("alert('请不要尝试sql注入"+values[i]+"关键字');");
							out.println("history.back();");
							out.println("</script>");
							return false;
						}
					}
				}
			}
		}
		return true;
	}
	
	public boolean isSqlInject(String value){
		if(StringUtils.isNotBlank(value)){
			String inj_str = "'|*|--|+";
			String[] inj_stra=inj_str.split("\\|");
			for (int i=0 ; i < inj_stra.length ; i++ )
			{
				if (value.indexOf(inj_stra[i])>=0)
				{
					System.out.println(value+"==="+inj_stra[i]);
					return true;
				}
			}
			return false;
		}
		return false;
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
