package com.cros.block.controller;

import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.params.MainNetParams;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.cros.block.api.dao.mongodb.MongoDBBaseDao;
import com.cros.block.common.GetSession;
import com.cros.block.model.User;
import com.cros.block.model.Wallet;
import com.cros.block.util.MD5Utils;
import com.cros.block.util.ResultData;
import com.cros.block.util.StringRandom;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

@Controller
@RequestMapping("/test")
public class TestController {

	@Resource(name = "mongoDBBaseDao")
	MongoDBBaseDao mongoDBBaseDao;
	  /**
     * 注册
     * @param request
     */
    @RequestMapping(value="/register",method= RequestMethod.POST)
    @ResponseBody
    public Object register(HttpServletRequest request,HttpServletResponse response,String username,
                           String password,String checkCode) throws Exception {
        HttpSession session = GetSession.getSession(request);
        if (mongoDBBaseDao.queryByUsername("User01", username).hasNext()){
        	return ResultData.buildFailureResult("用户名已存在");
        }else{
//        	 BasicDBObject user = new BasicDBObject();
             StringRandom random = new StringRandom();
             password = MD5Utils.MD5Encrypt(password.trim());
//             user.put("status", 1);
//             user.put("username", username.trim());
//             user.put("password", password);
//             user.put("dateCreate", new Date());
//             user.put("deleteFlag", 1);
//             //生成私钥
             String secrityKey = random.getStringRandom(64);
             //随机生成地址
             String stringRandom = random.getStringRandom(40);
             stringRandom = "0x"+stringRandom;
//             user.put("blockAddress", stringRandom);
//             user.put("blockKey", secrityKey);
             User user = new User();
             user.setStatus(1);
             user.setUsername(username.trim());
             user.setPassword(password);
             user.setDateCreate(new Date());
             user.setDeleteFlag(1);
             mongoDBBaseDao.add(user,"User");
             System.out.println(user.get_id());
             DBObject dbObject = mongoDBBaseDao.queryForId("User",user.get_id());
             System.out.println(dbObject);
             System.out.println(dbObject.get("_id"));
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
             return ResultData.buildSuccessResult("注册成功！");
        }
    }
}
