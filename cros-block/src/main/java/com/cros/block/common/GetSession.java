package com.cros.block.common;

import org.springframework.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Created by ASUS on 2018/4/14.
 */
public class GetSession {

    public static HttpSession getSession(HttpServletRequest request){
        String sessionId ="";
        HttpSession session = null;
        Cookie[] cookies = request.getCookies();
        if (!StringUtils.isEmpty(cookies)){
            for (Cookie cookie:cookies) {
                if ("JSESSIONID".equals(cookie.getName())){
                    sessionId=cookie.getValue();
                }
                MySessionContext myc= MySessionContext.getInstance();
                session = myc.getSession(sessionId);
            }
        }
        if (StringUtils.isEmpty(session)){
            session=request.getSession();
        }
        return  session;
    }
}
