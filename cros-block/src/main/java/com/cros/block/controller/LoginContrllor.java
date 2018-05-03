package com.cros.block.controller;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.params.MainNetParams;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cros.block.api.dao.mongodb.MongoDBBaseDao;
import com.cros.block.common.GetSession;
import com.cros.block.model.User;
import com.cros.block.model.Wallet;
import com.cros.block.util.CreatReportUtil;
import com.cros.block.util.MD5Util;
import com.cros.block.util.MD5Utils;
import com.cros.block.util.ResultData;
import com.cros.block.util.StringRandom;
import com.cros.block.util.ValidCodeUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * Created by ASUS on 2018/4/9.
 */
@Controller
@RequestMapping("/auth")
public class LoginContrllor {
	
	@Resource(name = "mongoDBBaseDao")
	MongoDBBaseDao mongoDBBaseDao;

    private final String VALID_CODE="login.valid.code";

    private Log logger = LogFactory.getLog(LoginContrllor.class);
    
	@RequestMapping("/login")
	public String toUserPage(){
		return "login";
	}
    
    
    /**
     *
     * login
     *
     * @Title login
     * @retrun String
     */
    @RequestMapping(value = "/personLogin")
    @ResponseBody
    public Object login(Model model, HttpServletRequest request,HttpServletResponse response) throws Exception {
        String userName =  request.getParameter("userName");
        String passWord = request.getParameter("passWord");
        String checkCode =  request.getParameter("checkCode");
        String flag = request.getParameter("flag");
    	Object validCode =request.getSession().getAttribute(VALID_CODE);
        if(passWord!=null && !passWord.equals("null") && !passWord.equals("")){
            passWord = MD5Utils.MD5Encrypt(passWord);
        }
        if(StringUtils.isEmpty(userName) || StringUtils.isEmpty(passWord)){
            return ResultData.buildFailureResult("请输入用户名或密码！");
        }

        if(StringUtils.isEmpty(checkCode)){
            return ResultData.buildFailureResult("请输入验证码！");
        }
        if(!checkCode.equals(validCode.toString())){
            return ResultData.buildFailureResult("验证码有误！");
        }
        // 防止sql注入
        if (userName.indexOf("'") >= 0 || passWord.indexOf("'") >= 0
                || userName.indexOf("\"") >= 0 || passWord.indexOf("\"") >= 0
                || userName.indexOf(" ") >= 0 || passWord.indexOf(" ") >= 0) {
            return ResultData.buildFailureResult("用户名或密码输入错误！");
        }
        HttpSession session = request.getSession();

        DBCursor users= mongoDBBaseDao.queryByUsername("User", userName);
        if(users.hasNext()){
        	DBObject user = users.next();
           if (Integer.parseInt(user.get("status").toString())==0) {
        	   return ResultData.buildFailureResult("账号已被禁止登录!");
           }
           /* 验证账号密码 */
   		   if (!user.get("username").equals(userName)||!user.get("password").equals(passWord)) {
       			return ResultData.buildFailureResult("用户名或密码输入错误!");
		   }else {
				// 类似单点登录，拦截器做登录校验，session过期自动销毁
	            ServletContext context = session.getServletContext();
	            context.setAttribute(user.get("_id").toString(), session);
	            session.setAttribute("loginUser", user);
	            logger.info(user.get("username")+"成功登录，ip:"+CreatReportUtil.getIpAddr(request));
			}
        }else {
        	return ResultData.buildFailureResult("用户名或密码输入错误!");
		}
    	
        if(!StringUtils.isEmpty(flag)){
            if (flag.equals("1")) {
                StringBuffer codeValue = new StringBuffer();
                codeValue.append(flag);
                codeValue.append("-");
                codeValue.append(userName);
                codeValue.append("-");
                codeValue.append("-");
                codeValue.append("-");
                Cookie cookie = new Cookie("cookie_user",codeValue.toString());
                cookie.setMaxAge(60 * 60 * 24 * 30); // cookie 保存30天
                cookie.setPath("/");
                response.addCookie(cookie);
            }
        }else {
            Cookie cookie = new Cookie("cookie_user", null + "-" + userName
                    + "-" +null);
            cookie.setMaxAge(60 * 60 * 24 * 30); // cookie 保存30天
            cookie.setPath("/");
            response.addCookie(cookie);
        }
        
        return ResultData.buildSuccessResult("登录成功!");
    }
 

    @RequestMapping("/index")
    @ResponseBody
    public Object index(HttpServletRequest request){
        HttpSession session = GetSession.getSession(request);
        User user =(User)session.getAttribute("loginUser");
        HashMap<String, Object> map = new HashMap<>();
        
        return ResultData.buildSuccessResult("首页返回参数",map);
    }
    
    /**
     * 注册
     * @param request
     * @return
     */
    @RequestMapping(value="/register",method= RequestMethod.POST)
    @ResponseBody
    public Object register(HttpServletRequest request) throws Exception {
    	String userName = request.getParameter("userName");
    	String password = request.getParameter("passWord");
    	String checkCode = request.getParameter("checkCode");
        if (StringUtils.isEmpty(userName)||StringUtils.isEmpty(password)){
            return ResultData.buildFailureResult("请填写有效信息");
        }
        HttpSession session = GetSession.getSession(request);
        String validCode = session.getAttribute(VALID_CODE).toString();
        if (StringUtils.isEmpty(validCode) || !validCode.equals(checkCode)) {
            //返回登录注册页面
            return ResultData.buildFailureResult("验证码有误！");
        }
        if (mongoDBBaseDao.queryByUsername("User", userName).hasNext()){
            return ResultData.buildFailureResult("用户名已存在");
        }
        User user = new User();
        StringRandom random = new StringRandom();
        password = MD5Utils.MD5Encrypt(password.trim());
        user.setStatus(1);
        user.setUsername(userName);
        user.setPassword(password);
        user.setDateCreate(new Date());
        user.setDeleteFlag(1);
        mongoDBBaseDao.add(user,"User");
        Wallet wallet = new Wallet();
        ECKey eckey = new ECKey();
		String userPublicKey = eckey.getPublicKeyAsHex();
		String userPrivateKey = eckey.getPrivateKeyAsHex();
		String userWalletAddress = eckey.toAddress(MainNetParams.get()).toBase58();
		wallet.setAgentUserId(user.get_id().toString());
		wallet.setUserPrivateKey(userPrivateKey);
		wallet.setUserPublicKey(userPublicKey);
		wallet.setUserWalletAddress(userWalletAddress);
		mongoDBBaseDao.add(wallet, "Wallet");
        logger.info(userName+"注册成功,ip:"+CreatReportUtil.getIpAddr(request));
        return ResultData.buildSuccessResult("注册成功，返回登录页面");
    }

    @RequestMapping("validcode")
    @ResponseBody
    public Object validcode(HttpServletRequest request,String code){
        HttpSession session = GetSession.getSession(request);
        String validCode = session.getAttribute(VALID_CODE).toString();
        if (StringUtils.isEmpty(validCode) || !validCode.equals(code)) {
            //返回登录注册页面
            return ResultData.buildFailureResult("验证码有误！");
        }
        return ResultData.buildSuccessResult("验证通过！");
    }



    /**
     *
     * logout:(退出)
     *
     * @Title logout
     * @retrun String
     */
    @RequestMapping(value = "/logOut")
    @ResponseBody
    public Object logout(HttpServletRequest request) throws Exception {
        HttpSession session = GetSession.getSession(request);
        session.invalidate();
        return ResultData.buildSuccessResult("退出成功，返回登陆页面");
    }



	@RequestMapping(value="code")
	public void code(HttpServletRequest request,HttpServletResponse response) throws Exception {
		ValidCodeUtil.generate(VALID_CODE, request, response);
		// 返回到界面
	}
	
	@RequestMapping("/updatePwd")
	@ResponseBody
	public Object updatePwd(HttpServletRequest request,String passWord) throws Exception{

		HttpSession session = GetSession.getSession(request);
		DBObject user = (DBObject) session.getAttribute("loginUser");
		String username = (String) user.get("username");
        String password = ServletRequestUtils.getStringParameter(request,"passWord");
        if (!StringUtils.isEmpty(username)&&!StringUtils.isEmpty(password)) {
            password = MD5Utils.MD5Encrypt(password);
            if (!user.get("password").equals(password)) {
            	int i = mongoDBBaseDao.updatePwd("User", username, password);
                if (i==1){
                    return ResultData.buildSuccessResult("密码修改成功");
                }else {
                    return ResultData.buildFailureResult("密码修改失败");
                }
            }else {
                return ResultData.buildFailureResult("原密码与新密码相同");
            }
        }else {
            return ResultData.buildFailureResult("密码修改失败");
        }
    
	}
}
