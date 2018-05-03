package com.cros.block.interceptor;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import com.cros.block.model.User;

/**
 * @ClassName: DxSessionListener
 * @Package com.trader.eval.interceptor
 * @Description:TODO ADD FUNCTION
 * @date: 2016年8月15日 下午1:43:34
 * @author hokuny@foxmail.com
 * @version 
 */
public class DxSessionListener implements HttpSessionListener {
	
	public void sessionCreated(HttpSessionEvent event) {
		
	}

	public void sessionDestroyed(HttpSessionEvent event) {
		HttpSession session = event.getSession();
		ServletContext context = session.getServletContext();
		User user = (User)session.getAttribute("loginUser");
		if(user!=null){
			context.removeAttribute(user.get_id().toString());
		}
	}

}
