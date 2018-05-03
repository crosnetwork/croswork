package com.cros.block.exception;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;



/**
 * @ClassName: RiskExceptionHandler
 * @Package com.trader.eval.exception
 * @Description:TODO ADD FUNCTION
 * @date: 2016年8月15日 下午1:35:54
 * @author hokuny@foxmail.com
 * @version 
 */
public class RiskExceptionHandler implements HandlerExceptionResolver {
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler,  
            Exception ex) {  
    	ex.printStackTrace();
        Map<String, Object> model = new HashMap<String, Object>();  
        model.put("ex", ex);  

        String view = "error";
        
        if(ex instanceof DaoException) {
        	model.put("message","访问数据库出错!");
            return new ModelAndView(view, model);  
        }
        if(ex instanceof SecurityException) {
        	model.put("message","无权访问!");
            return new ModelAndView(view, model);  
        }
        if(ex instanceof ArgumentException) {
        	model.put("message","参数错误！");
            return new ModelAndView(view, model);  
        }
        if(ex instanceof TimeoutException) {
        	model.put("message","访问超时");
            return new ModelAndView(view, model);  
        }
        if(ex instanceof RiskException) {
        	model.put("message","系统错误！");
            return new ModelAndView(view, model);  
        }
    	model.put("message","系统错误！");
        return new ModelAndView(view, model);  
    }  
    
    public String getAction(HttpServletRequest request) {
		String contextPath = request.getContextPath();
		String action=request.getRequestURI().replaceAll("/\\d+\\w*", "").replace(contextPath + "/", "");
		if(!action.equals("")){
			action=action.charAt(0)=='/'?action.substring(1):action;
		}
		return action;
	}
}
