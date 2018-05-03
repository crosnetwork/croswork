package com.cros.block.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cros.block.api.ICrosMsgApi;
import com.cros.block.api.coprs.CrosApiService;
import com.cros.block.api.dao.mongodb.MongoDBBaseDao;
import com.cros.block.common.GetSession;
import com.cros.block.model.BlockInfo;
import com.cros.block.model.User;
import com.cros.block.util.EncryptionUtil;
import com.cros.block.util.ResultData;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import net.sf.json.JSONObject;

@Controller
@RequestMapping("/build")
public class BuildChainController {

	@Resource(name = "mongoDBBaseDao")
	MongoDBBaseDao mongoDBBaseDao;

	@Autowired
	private ICrosMsgApi crosMsgApi;
	
	@SuppressWarnings("unchecked")
	@RequestMapping("/getHash")
	@ResponseBody
	public Object getHash(String block_name,String block_description,HttpServletRequest request){
	/*	Document doc,JSONObject json,String userWalletAddress ,String blockChose ,
		String agentUserId , String event ,String eventStatus ,String payCros*/
		HttpSession session = GetSession.getSession(request);
	    User user = (User) session.getAttribute("loginUser");
		String userId = user.get_id().toString();
		DBCursor wallet = mongoDBBaseDao.queryForAgentUserId("Wallet", userId);
		String userWalletAddress = "";
		if (wallet.hasNext()) {
			DBObject dbObject = wallet.next();
			userWalletAddress = dbObject.get("userWalletAddress").toString();
		}
		String blockChose = "0";
		String agentUserId = userId.toString();
		String event = "14";
		String eventStatus = "success";
		String payCros = "30";
		Map map = new HashMap<>();
		map.put("block_name", block_name);
		map.put("block_description",block_description);
		map.put("userWalletAddress", userWalletAddress);
		map.put("block_chose", blockChose);
		map.put("agent_user_id", agentUserId);
		map.put("event", event);
		map.put("event_status", eventStatus);
		map.put("pay_cros", payCros);
		JSONObject json = JSONObject.fromObject(map);
		String sign = EncryptionUtil.encrytion(json.toString());
		json.put("sign", sign);
		//上链
		String data = crosMsgApi.crosRegisterData(json.toString());
		System.out.println(data);
		JSONObject jsonObject = JSONObject.fromObject(data);
		String txHash = jsonObject.get("tx_id").toString();
		//建立侧链的记录文档
		BlockInfo blockInfo = new BlockInfo();
		blockInfo.setAgentUserId(userId);
		blockInfo.setBlock_address(userWalletAddress);
		blockInfo.setBlock_name(block_name);
		blockInfo.setBlock_description(block_description);
		blockInfo.setCreate_time(new Date());
		blockInfo.setTxHash(txHash);
		mongoDBBaseDao.add(blockInfo, "BlockInformation");
		String blockInfoId = blockInfo.get_id().toString();
		//创立四个与主链相同功能结构的侧链文档
		mongoDBBaseDao.createCollection("Blockchain"+blockInfoId);
		mongoDBBaseDao.createCollection("TxPool"+blockInfoId);
		mongoDBBaseDao.createCollection("Wallet"+blockInfoId);
		//返回页面结果
		Map resultMap = new HashMap<>();
		resultMap.put("txHash", txHash);
		resultMap.put("block_name", block_name);
		resultMap.put("block_description",block_description);
		return ResultData.buildSuccessResult("返回信息", resultMap);
	} 
	
	@RequestMapping("/chain")
	@ResponseBody
	public Object buildChain(){
		return null;
	}
}
