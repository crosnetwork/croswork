package com.cros.block.api.impl;

import static java.util.Arrays.asList;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import net.sf.json.JSONObject;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Utils;
import org.bitcoinj.params.MainNetParams;
import org.bson.Document;
import org.springframework.stereotype.Service;

import com.cros.block.api.ICrosMsgApi;
import com.cros.block.api.IWeibeiMsgApi;
import com.cros.block.api.coprs.CoprApiService;
import com.cros.block.api.coprs.CrosApiService;
import com.cros.block.util.EncryptionUtil;
import com.cros.block.util.TripleDES;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * @ClassName: CrosMsgApiImpl
 * @Packagecom.cros.block.api.impl
 * @Description:TODO ADD FUNCTION
 * @date:2018-04-26
 * @author yeran@ascore.com.cn
 * @version
 */
@Service
public class CrosMsgApiImpl implements ICrosMsgApi {

	private Log logger = LogFactory.getLog(CrosMsgApiImpl.class);

	@Resource(name = "crosApiService")
	private CrosApiService crosApiService;

	
	
	@POST
	@Path("/newtx")
	public String crosRegisterData(String params) {
		System.out.println("newtx");
		logger.info("cros记录 开始");
		Map<String, Object> result = new HashMap<String, Object>();
		// TODO Auto-generated method stub
		if (StringUtils.isBlank(params)) {
			result.put("status", 401);
			result.put("err_msg", "参数为空");
			return JSON.toJSONString(result);
		}
		try {
			// 解码
			/*byte[] asBytes = Base64.decodeBase64(params);
			String str = new String(asBytes, "utf-8");*/
			JSONObject json = JSONObject.fromObject(params);

			/* 校验厂家是否正确 */
			/*String agentId = json.getString("agent_id");
			if (!StringUtils.equals("banquanjia", agentId)) {
				result.put("status", 402);
				result.put("err_msg", "厂家不正确");
				return JSON.toJSONString(result);
			}*/
			/* 校验加密是否正确 */
			String sign = json.getString("sign");
			json.remove("sign");
			String sign1 = EncryptionUtil.encrytion(json.toString());
			if (!StringUtils.equals(sign1, sign)) {
				logger.info(json.toString());
				result.put("status", 403);
				result.put("err_msg", "sign验证错误");
				return JSON.toJSONString(result);
			}
			// 解析参数
			String blockChose = json.has("block_chose") == true ? json
					.getString("block_chose") : "";// 链标识位
			String agentUserId = json.has("agent_user_id") == true ? json
					.getString("agent_user_id") : "";// 用户唯一ID
			String event = json.has("event") == true ? json
					.getString("event") : "";
					/*
					  事件分类：
					  
					  发行token/1
					  资产映射/2
					 token交易/3
					   转账/4
					   智能合约模板公开/5
					   执行智能合约模板/6
					   众包工作流模板公开/7
					   众包工作流模板发布/8
					   众包工作流承接/9
					   众包工作流启动/10
					   自定义工作流模板公开/11
					   自定义工作流模板启动/12
					   商品交易回执/13
					 一键建链/14
					 */
			String eventStatus = json.has("event_status") == true ? json
				    .getString("event_status") : "";// 事件状态：success/fail	
			String payCros = json.has("pay_cros") == true ? json
					.getString("pay_cros") : "";// 事件状态：success/fail	
			
					
					String userPublicKey = "";
					String userPrivateKey = "";
					String userWalletAddress = "";
					DBCursor userInfo = crosApiService.queryForUid(agentUserId,
							"Wallet"+(blockChose.equals("0") ? "" : blockChose));
					try {
						if (userInfo.hasNext()) {
							logger.info(" : old user========================================");
							DBObject userObj = userInfo.next();
							userPublicKey = userObj.get("userPublicKey").toString();
							userPrivateKey = userObj.get("userPrivateKey").toString();
							userWalletAddress = userObj.get("userWalletAddress")
									.toString();
						} else {
							logger.info(json.toString());
							result.put("status", 403);
							result.put("err_msg", "当前账户没有钱包地址");
							return JSON.toJSONString(result);
						/*	logger.info(" : new user========================================");
							ECKey eckey = new ECKey();
							userPublicKey = eckey.getPublicKeyAsHex();
							userPrivateKey = eckey.getPrivateKeyAsHex();
							userWalletAddress = eckey.toAddress(MainNetParams.get())
									.toBase58();
							DBObject obj = new BasicDBObject();
							obj.put("userPublicKey", TripleDES.encrypt(userPublicKey.getBytes()).toString());
							obj.put("userPrivateKey",TripleDES.encrypt(userPrivateKey.getBytes()).toString());
							obj.put("userWalletAddress", userWalletAddress);
							obj.put("agentUserId", agentUserId);
							crosApiService.save(obj, "Wallet");*/
						}
					} finally {
						userInfo.close();
					}
					logger.info("Public key : " + TripleDES.encrypt(userPublicKey.getBytes()));
					logger.info("Private key : " + TripleDES.encrypt(userPrivateKey.getBytes()));
					logger.info("Weibei wallet address : " + userWalletAddress);
					logger.info("\n\n");

					// Weibei system keys (TODO: get value from Wallet collection)
					String systemPublicKey = "0275ef688470a1ed81c5b6dc6c9410c8e48f3605eeb06a170419a99bdb0ed6549d";
					String systemPrivateKey = "e9016ee5acc7952af2a99cd8971bd0197499270c626230319917a98657679ef0";
					ECKey systemECKey = ECKey.fromPrivateAndPrecalculatedPublic(
							Utils.HEX.decode(systemPrivateKey),
							Utils.HEX.decode(systemPublicKey));
					String systemWalletAddress = systemECKey.toAddress(
							MainNetParams.get()).toBase58();
					logger.info("System wallet address : " + systemWalletAddress); // Should
																					// return
																					// '1KctVnjncFK8QsoJHjx6q73VXVxaqc76Q5'
					
			Document doc = new Document("ver", 1);
			if("1".equals(event)){
				doc = createDoc1(doc, json, userWalletAddress, blockChose, agentUserId, event, eventStatus, payCros);
			}else if("2".equals(event)){
				doc = createDoc2(doc, json, userWalletAddress, blockChose, agentUserId, event, eventStatus, payCros);
			}else if("3".equals(event)){
				doc = createDoc3(doc, json, userWalletAddress, blockChose, agentUserId, event, eventStatus, payCros);
			}else if("4".equals(event)){
				doc = createDoc4(doc, json, userWalletAddress, blockChose, agentUserId, event, eventStatus, payCros);
			}else if("5".equals(event)){
				doc = createDoc5(doc, json, userWalletAddress, blockChose, agentUserId, event, eventStatus, payCros);
			}else if("6".equals(event)){
				doc = createDoc6(doc, json, userWalletAddress, blockChose, agentUserId, event, eventStatus, payCros);
			}else if("7".equals(event)){
				doc = createDoc7(doc, json, userWalletAddress, blockChose, agentUserId, event, eventStatus, payCros);
			}else if("8".equals(event)){
				doc = createDoc8(doc, json, userWalletAddress, blockChose, agentUserId, event, eventStatus, payCros);
			}else if("9".equals(event)){
				doc = createDoc9(doc, json, userWalletAddress, blockChose, agentUserId, event, eventStatus, payCros);
			}else if("10".equals(event)){
				doc = createDoc10(doc, json, userWalletAddress, blockChose, agentUserId, event, eventStatus, payCros);
			}else if("11".equals(event)){
				doc = createDoc11(doc, json, userWalletAddress, blockChose, agentUserId, event, eventStatus, payCros);
			}else if("12".equals(event)){
				doc = createDoc12(doc, json, userWalletAddress, blockChose, agentUserId, event, eventStatus, payCros);
			}else if("13".equals(event)){
				doc = createDoc13(doc, json, userWalletAddress, blockChose, agentUserId, event, eventStatus, payCros);
			}else if("14".equals(event)){
				if(!"0".equals(blockChose)){
					logger.info(json.toString());
					result.put("status", 403);
					result.put("err_msg", "唯有公链账户可一键建链");
					return JSON.toJSONString(result);
				}
				doc = createDoc14(doc, json, userWalletAddress, blockChose, agentUserId, event, eventStatus, payCros);
			}		    
				    
				    
			logger.info("Raw document - " + doc.toJson());
//			doc.append("status", "pending");
			// Hash raw document twice, then sign the hash to get signature.
			String docHash = Utils.HEX.encode(Sha256Hash.hashTwice(doc.toJson()
					.getBytes()));
			String signature2 = systemECKey.signMessage(docHash);
			logger.info("Signature - " + signature2);
			doc.append("scriptSig", signature2 + " " + systemPublicKey);
			logger.info("Document with signature- " + doc.toJson());
			// Generate hash256 hex and add to document
			String docHash2 = Utils.HEX.encode(Sha256Hash.hashTwice(doc
					.toJson().getBytes()));
			doc.append("tx_id", docHash2);
			logger.info("Document with hash - " + doc.toJson());
			if (verifyTx(doc)) {
				doc.append("status", "pending");
				doc.append("tx_id", docHash2);
				doc.append("scriptSig", signature2 + " " + systemPublicKey);
				crosApiService.save(doc, "TxPool"+(blockChose.equals("0") ? "" : blockChose));
				result.put("status", 200);
				result.put("tx_id", docHash2);
				result.put("wallet_id", userWalletAddress);
				result.put("tx_time", doc.get("tx_time"));
			} else {
				result.put("status", 405);
				result.put("err_msg", "系统故障");
			}	    
				    
		} catch (Exception ex) {
			String msg = "登记信息发生异常:" + ex.getMessage();
			logger.error(msg, ex);
			result.put("status", 405);
			result.put("err_msg", "系统故障:"+msg);
		}
		logger.info("登记信息结束");
		return JSON.toJSONString(result);

	}
	
	@POST
	@Path("/gettx")
	public String crosQueryData(String params) {
		// TODO Auto-generated method stub
		System.out.println("gettx");
		logger.info("登记信息 查询");
		Map<String, Object> result = new HashMap<String, Object>();
		// TODO Auto-generated method stub
		if (StringUtils.isBlank(params)) {
			result.put("status", 401);
			result.put("err_msg", "参数为空");
			return JSON.toJSONString(result);
		}
		try {
			// 解码
			byte[] asBytes = Base64.decodeBase64(params);
			String str = new String(asBytes, "utf-8");
			logger.info("=====" + str); // 输出为: some string
			JSONObject json = (JSONObject) JSONObject.fromObject(str);
		
			/* 校验加密是否正确 */
			String sign = json.getString("sign");
			json.remove("sign");
			String sign1 = EncryptionUtil.encrytion(json.toString());
			if (!StringUtils.equals(sign1, sign)) {
				result.put("status", 403);
				result.put("err_msg", "sign验证错误");
				return JSON.toJSONString(result);
			}
			String agentUserId = json.has("agent_user_id") == true ? json
					.getString("agent_user_id") : null;// 用户唯一ID
			String productHash = json.has("product_hash") == true ? json
					.getString("product_hash") : null; // 作品hash(md5)
			String[] productHashs;
			if (productHash != null) {
				productHashs = productHash.split(",");
			} else {
				productHashs = null;
			}
			String agent_idempotent = json.has("agent_idempotent") == true ? json
					.getString("agent_idempotent") : null; // 中国版权中心DCI码
			String txId = json.has("tx_id") == true ? json.getString("tx_id")
					: null; // 区块交易记录号
			int pageNumber = json.has("pageNumber") == true ? json
					.getInt("pageNumber") : 1; // 中国版权中心DCI码
			int pageSize = json.has("pageSize") == true ? json
					.getInt("pageSize") : 10; // 区块交易记录号

			DBCursor dbs = crosApiService.query("TxPool", agentUserId,
					agent_idempotent, txId, productHashs, pageNumber, pageSize);
			try {
				JSONArray jsonArray = new JSONArray();
				if (dbs != null && dbs.size() > 0) {
					result.put("status", 200);
					while (dbs.hasNext()) {
						DBObject document = dbs.next();
						Map map = document.toMap();
						JSONArray jArray = JSONArray.parseArray(map.get("out")
								.toString());
						System.out.println(jArray.getJSONObject(0).get("data"));
						String datajson = jArray.getJSONObject(0).get("data")
								.toString();
						JSONObject jsonObject = JSONObject.fromObject(datajson);
						jsonObject.put("tx_id", document.get("tx_id"));
						jsonObject.put("tx_time", document.get("tx_time"));
						jsonObject.remove("creative_self");
						jsonObject.remove("creative_process");
						jsonObject.remove("creative_purpose");
						jsonArray.add(jsonObject);
					}
					result.put("data", jsonArray);
				} else {
					result.put("status", 200);
					result.put("data", jsonArray);
				}
			} finally {
				dbs.close();
			}
		} catch (Exception ex) {
			String msg = "登记信息发生异常:" + ex.getMessage();
			logger.error(msg, ex);
			result.put("status", 405);
			result.put("err_msg", "系统故障");
		}
		logger.info("登记信息结束");
		return JSON.toJSONString(result);
	}

	private boolean verifyTx(Document document) {
		String txHash = document.getString("tx_id");
		String scriptSig = document.getString("scriptSig");
		String[] tokens = scriptSig.split(" ");
		String signature = tokens[0];
		String publicKey = tokens[1];
		document.remove("_id");
		document.remove("tx_id");
		logger.info("Document minus hash and id - " + document.toJson());
		String docHash = Utils.HEX.encode(Sha256Hash.hashTwice(document
				.toJson().getBytes()));
		logger.info("Is hash the same = " + docHash.equals(txHash));
		document.remove("scriptSig");
		logger.info("Raw document - " + document.toJson());
		try {
			String docHash2 = Utils.HEX.encode(Sha256Hash.hashTwice(document
					.toJson().getBytes()));
			ECKey eckey3 = ECKey.fromPublicOnly(Utils.HEX.decode(publicKey));
			eckey3.verifyMessage(docHash2, signature);
			logger.info("Signature verified.");
			return true;
		} catch (java.security.SignatureException sigExe) {
			logger.info("Signature verification failed.");
			return false;
		}
	}
	
	/**
	 * 发行token
	 * @param doc
	 * @param json
	 * @param userWalletAddress
	 * @param blockChose
	 * @param agentUserId
	 * @param event
	 * @param eventStatus
	 * @param payCros
	 * @return doc
	 * @see 发行token
	 */
	public Document createDoc1(Document doc,JSONObject json,String userWalletAddress ,String blockChose ,String agentUserId , String event ,String eventStatus ,String payCros){
		Calendar cal = Calendar.getInstance();
		String tokenAddress = json.has("token_address") == true ? json
			    .getString("token_address") : "";// token合约地址
		    String tokenName = json.has("token_name") == true ? json
				    .getString("token_name") : "";// token名称
	   	    String tokenFrom = json.has("token_from") == true ? json
	   			    .getString("token_from") : "";// token发行人
	   		String totalAssetsMapping =json.has("total_assets_mapping") == true ? json.getString("total_assets_mapping") : "";//token初始资产映射
	   		String totalNum =json.has("total_num") == true ? json.getString("total_num") : "";//token发行总量
	   		String unitPrice =json.has("unit_price") == true ? json.getString("unit_price") : "";//token发行单价
	// Create a document
		 doc.append("vin_size", 1)
			.append("vout_size", 1)
			.append("tx_time", cal.getTimeInMillis())
			.append("in",
					asList(new Document().append("prev_out",
							"00000000000000000000000000000000").append(
							"prev_n", -1)))
			.append("out",
					asList(new Document()
							.append("value", 1)
							.append("scriptPubKey",
									"OP_DUP OP_HASH160 "
											+ userWalletAddress
											+ " OP_EQUALVERIFY OP_CHECKSIG")
							.append("data",
									new Document("agent_user_id",
											agentUserId)
											.append("block_chose", blockChose)
											.append("agent_user_id",
													agentUserId)
											.append("event",
													event)
											.append("event_status",
													eventStatus)
											.append("pay_cros",
													payCros)
											.append("token_address",
													tokenAddress)
											.append("token_name",
													tokenName)
											.append("token_from",
													tokenFrom)
											.append("total_assets_mapping", totalAssetsMapping)
											.append("total_num",
													totalNum)
											.append("unit_price",
													unitPrice))));
		 return doc;
	}
	
	/**
	 * 资产映射
	 * @param doc
	 * @param json
	 * @param userWalletAddress
	 * @param blockChose
	 * @param agentUserId
	 * @param event
	 * @param eventStatus
	 * @param payCros
	 * @return doc
	 * @see 资产映射
	 */
	public Document createDoc2(Document doc,JSONObject json,String userWalletAddress ,String blockChose ,String agentUserId , String event ,String eventStatus ,String payCros){
		Calendar cal = Calendar.getInstance();
		
	   		String assetOwnAddress =json.has("asset_own_address") == true ? json.getString("asset_own_address") : "";//资产所有人地址
	   		String assetsMapping =json.has("assets_mapping") == true ? json.getString("assets_mapping") : "";//资产映射
	// Create a document
		 doc.append("vin_size", 1)
			.append("vout_size", 1)
			.append("tx_time", cal.getTimeInMillis())
			.append("in",
					asList(new Document().append("prev_out",
							"00000000000000000000000000000000").append(
							"prev_n", -1)))
			.append("out",
					asList(new Document()
							.append("value", 1)
							.append("scriptPubKey",
									"OP_DUP OP_HASH160 "
											+ userWalletAddress
											+ " OP_EQUALVERIFY OP_CHECKSIG")
							.append("data",
									new Document("agent_user_id",
											agentUserId)
											.append("block_chose", blockChose)
											.append("agent_user_id",
													agentUserId)
											.append("event",
													event)
											.append("event_status",
													eventStatus)
											.append("pay_cros",
													payCros)
											.append("assetOwn_address",
													assetOwnAddress)
											.append("assets_mapping",
													asList(assetsMapping)))));
		 return doc;
	}
	
	/**
	 * Token交易
	 * @param doc
	 * @param json
	 * @param userWalletAddress
	 * @param blockChose
	 * @param agentUserId
	 * @param event
	 * @param eventStatus
	 * @param payCros
	 * @return doc
	 * @see Token交易
	 */
	public Document createDoc3(Document doc,JSONObject json,String userWalletAddress ,String blockChose ,String agentUserId , String event ,String eventStatus ,String payCros){
		Calendar cal = Calendar.getInstance();
		
	   		String tradingOrder =json.has("trading_order") == true ? json.getString("trading_order") : "";//交易订单
	   		String tokenAddress =json.has("token_address") == true ? json.getString("token_address") : "";//token合约地址
	   		String tokenName =json.has("token_name") == true ? json.getString("token_name") : "";//token名称
	   		String tokenFrom =json.has("token_from") == true ? json.getString("token_from") : "";//token交易卖方
	   		String tokenTo =json.has("token_to") == true ? json.getString("token_to") : "";//token交易买方
	   		String tradeTotal =json.has("trade_total") == true ? json.getString("trade_total") : "";//交易总金额
	   		String tradeNum =json.has("trade_num") == true ? json.getString("trade_num") : "";//交易数量
	   		String tradeUnit =json.has("trade_unit") == true ? json.getString("trade_unit") : "";//交易单价
	   		
	// Create a document
		 doc.append("vin_size", 1)
			.append("vout_size", 1)
			.append("tx_time", cal.getTimeInMillis())
			.append("in",
					asList(new Document().append("prev_out",
							"00000000000000000000000000000000").append(
							"prev_n", -1)))
			.append("out",
					asList(new Document()
							.append("value", 1)
							.append("scriptPubKey",
									"OP_DUP OP_HASH160 "
											+ userWalletAddress
											+ " OP_EQUALVERIFY OP_CHECKSIG")
							.append("data",
									new Document("agent_user_id",
											agentUserId)
											.append("block_chose", blockChose)
											.append("agent_user_id",
													agentUserId)
											.append("event",
													event)
											.append("event_status",
													eventStatus)
											.append("pay_cros",
													payCros)
											.append("trading_order",
													tradingOrder)
											.append("token_address",
													tokenAddress)
											.append("token_name",
													tokenName)
											.append("token_from",
													tokenFrom)
											.append("token_to",
													tokenTo)
											.append("trade_total",
													tradeTotal)
											.append("trade_num",
													tradeNum)
											.append("trade_unit",
													tradeUnit))));
		 return doc;
	}
	
	/**
	 * 转账
	 * @param doc
	 * @param json
	 * @param userWalletAddress
	 * @param blockChose
	 * @param agentUserId
	 * @param event
	 * @param eventStatus
	 * @param payCros
	 * @return doc
	 * @see 转账
	 */
	public Document createDoc4(Document doc,JSONObject json,String userWalletAddress ,String blockChose ,String agentUserId , String event ,String eventStatus ,String payCros){
		Calendar cal = Calendar.getInstance();
		
	   		String tokenAddress =json.has("token_address") == true ? json.getString("token_address") : "";//token合约地址
	   		String tokenName =json.has("token_name") == true ? json.getString("token_name") : "";//token名称
	   		String tokenFrom =json.has("token_from") == true ? json.getString("token_from") : "";//token发送方
	   		String tokenTo =json.has("token_to") == true ? json.getString("token_to") : "";//token接受方
	   		String sendNum =json.has("send_num") == true ? json.getString("send_num") : "";//发送数量
	   		
	// Create a document
		 doc.append("vin_size", 1)
			.append("vout_size", 1)
			.append("tx_time", cal.getTimeInMillis())
			.append("in",
					asList(new Document().append("prev_out",
							"00000000000000000000000000000000").append(
							"prev_n", -1)))
			.append("out",
					asList(new Document()
							.append("value", 1)
							.append("scriptPubKey",
									"OP_DUP OP_HASH160 "
											+ userWalletAddress
											+ " OP_EQUALVERIFY OP_CHECKSIG")
							.append("data",
									new Document("agent_user_id",
											agentUserId)
											.append("block_chose", blockChose)
											.append("agent_user_id",
													agentUserId)
											.append("event",
													event)
											.append("event_status",
													eventStatus)
											.append("pay_cros",
													payCros)
											.append("token_address",
													tokenAddress)
											.append("token_name",
													tokenName)
											.append("token_from",
													tokenFrom)
											.append("token_to",
													tokenTo)
											.append("send_num",
													sendNum))));
		 return doc;
	}

	/**
	 * 智能合约模板公开
	 * @param doc
	 * @param json
	 * @param userWalletAddress
	 * @param blockChose
	 * @param agentUserId
	 * @param event
	 * @param eventStatus
	 * @param payCros
	 * @return doc
	 * @see 智能合约模板公开
	 */
	public Document createDoc5(Document doc,JSONObject json,String userWalletAddress ,String blockChose ,String agentUserId , String event ,String eventStatus ,String payCros){
		Calendar cal = Calendar.getInstance();
		
	   		String contractAddress =json.has("contract_address") == true ? json.getString("contract_address") : "";//合约地址
	   		String contractName =json.has("contract_name") == true ? json.getString("contract_name") : "";//合约名称
	   		String contractFrom =json.has("contract_from") == true ? json.getString("contract_from") : "";//合约创建者
	   		String crosUnitIncome =json.has("cros_unit_income") == true ? json.getString("cros_unit_income") : "";//cros收益
	   		
	// Create a document
		 doc.append("vin_size", 1)
			.append("vout_size", 1)
			.append("tx_time", cal.getTimeInMillis())
			.append("in",
					asList(new Document().append("prev_out",
							"00000000000000000000000000000000").append(
							"prev_n", -1)))
			.append("out",
					asList(new Document()
							.append("value", 1)
							.append("scriptPubKey",
									"OP_DUP OP_HASH160 "
											+ userWalletAddress
											+ " OP_EQUALVERIFY OP_CHECKSIG")
							.append("data",
									new Document("agent_user_id",
											agentUserId)
											.append("block_chose", blockChose)
											.append("agent_user_id",
													agentUserId)
											.append("event",
													event)
											.append("event_status",
													eventStatus)
											.append("pay_cros",
													payCros)
											.append("contract_address",
													contractAddress)
											.append("contract_name",
													contractName)
											.append("contract_from",
													contractFrom)
											.append("cros_unit_income",
													crosUnitIncome))));
		 return doc;
	}
	
	/**
	 * 智能合约模板执行
	 * @param doc
	 * @param json
	 * @param userWalletAddress
	 * @param blockChose
	 * @param agentUserId
	 * @param event
	 * @param eventStatus
	 * @param payCros
	 * @return doc
	 * @see 智能合约模板执行
	 */
	public Document createDoc6(Document doc,JSONObject json,String userWalletAddress ,String blockChose ,String agentUserId , String event ,String eventStatus ,String payCros){
		Calendar cal = Calendar.getInstance();
		
	   		String contractAddress =json.has("contract_address") == true ? json.getString("contract_address") : "";//合约模板地址
	   		String contractName =json.has("contract_name") == true ? json.getString("contract_name") : "";//合约模板名称
	   		String contractStartFrom =json.has("contract_start_from") == true ? json.getString("contract_start_from") : "";//合约模板执行人
	   		
	// Create a document
		 doc.append("vin_size", 1)
			.append("vout_size", 1)
			.append("tx_time", cal.getTimeInMillis())
			.append("in",
					asList(new Document().append("prev_out",
							"00000000000000000000000000000000").append(
							"prev_n", -1)))
			.append("out",
					asList(new Document()
							.append("value", 1)
							.append("scriptPubKey",
									"OP_DUP OP_HASH160 "
											+ userWalletAddress
											+ " OP_EQUALVERIFY OP_CHECKSIG")
							.append("data",
									new Document("agent_user_id",
											agentUserId)
											.append("block_chose", blockChose)
											.append("agent_user_id",
													agentUserId)
											.append("event",
													event)
											.append("event_status",
													eventStatus)
											.append("pay_cros",
													payCros)
											.append("contract_address",
													contractAddress)
											.append("contract_name",
													contractName)
											.append("contract_start_from",
													contractStartFrom))));
		 return doc;
	}
	/**
	 * 众包工作流模板公开
	 * @param doc
	 * @param json
	 * @param userWalletAddress
	 * @param blockChose
	 * @param agentUserId
	 * @param event
	 * @param eventStatus
	 * @param payCros
	 * @return doc
	 * @see 众包工作流模板公开
	 */
	public Document createDoc7(Document doc,JSONObject json,String userWalletAddress ,String blockChose ,String agentUserId , String event ,String eventStatus ,String payCros){
		Calendar cal = Calendar.getInstance();
		
	   		String crowdsourcingAddress =json.has("crowdsourcing_address") == true ? json.getString("crowdsourcing_address") : "";//众包工作流模板地址
	   		String crowdsourcingName =json.has("crowdsourcing_name") == true ? json.getString("crowdsourcing_name") : "";//众包工作流模板名称
	   		String crowdsourcingFrom =json.has("crowdsourcing_from") == true ? json.getString("crowdsourcing_from") : "";//众包工作流模板创建者
	   		String crosUnitIncome =json.has("cros_unit_income") == true ? json.getString("cros_unit_income") : "";//cros收益

	// Create a document
		 doc.append("vin_size", 1)
			.append("vout_size", 1)
			.append("tx_time", cal.getTimeInMillis())
			.append("in",
					asList(new Document().append("prev_out",
							"00000000000000000000000000000000").append(
							"prev_n", -1)))
			.append("out",
					asList(new Document()
							.append("value", 1)
							.append("scriptPubKey",
									"OP_DUP OP_HASH160 "
											+ userWalletAddress
											+ " OP_EQUALVERIFY OP_CHECKSIG")
							.append("data",
									new Document("agent_user_id",
											agentUserId)
											.append("block_chose", blockChose)
											.append("agent_user_id",
													agentUserId)
											.append("event",
													event)
											.append("event_status",
													eventStatus)
											.append("pay_cros",
													payCros)
											.append("crowdsourcing_address",
													crowdsourcingAddress)
											.append("crowdsourcing_name",
													crowdsourcingName)
											.append("crowdsourcing_from",
													crowdsourcingFrom)
											.append("cros_unit_income",
													crosUnitIncome))));
		 return doc;
	}
	
	/**
	 * 众包工作流模板发布
	 * @param doc
	 * @param json
	 * @param userWalletAddress
	 * @param blockChose
	 * @param agentUserId
	 * @param event
	 * @param eventStatus
	 * @param payCros
	 * @return doc
	 * @see 众包工作流模板发布
	 */
	public Document createDoc8(Document doc,JSONObject json,String userWalletAddress ,String blockChose ,String agentUserId , String event ,String eventStatus ,String payCros){
		Calendar cal = Calendar.getInstance();
		
	   		String crowdsourcingAddress =json.has("crowdsourcing_address") == true ? json.getString("crowdsourcing_address") : "";//众包工作流模板地址
	   		String crowdsourcingName =json.has("crowdsourcing_name") == true ? json.getString("crowdsourcing_name") : "";//众包工作流模板名称
	   		String crowdsourcingPublishFrom =json.has("crowdsourcing_publish_from") == true ? json.getString("crowdsourcing_publish_from") : "";//众包工作流模板发布
	   		
	// Create a document
		 doc.append("vin_size", 1)
			.append("vout_size", 1)
			.append("tx_time", cal.getTimeInMillis())
			.append("in",
					asList(new Document().append("prev_out",
							"00000000000000000000000000000000").append(
							"prev_n", -1)))
			.append("out",
					asList(new Document()
							.append("value", 1)
							.append("scriptPubKey",
									"OP_DUP OP_HASH160 "
											+ userWalletAddress
											+ " OP_EQUALVERIFY OP_CHECKSIG")
							.append("data",
									new Document("agent_user_id",
											agentUserId)
											.append("block_chose", blockChose)
											.append("agent_user_id",
													agentUserId)
											.append("event",
													event)
											.append("event_status",
													eventStatus)
											.append("pay_cros",
													payCros)
											.append("crowdsourcing_address",
													crowdsourcingAddress)
											.append("crowdsourcing_name",
													crowdsourcingName)
											.append("crowdsourcing_publish_from",
													crowdsourcingPublishFrom))));
		 return doc;
	}

	/**
	 * 众包工作流模板承接
	 * @param doc
	 * @param json
	 * @param userWalletAddress
	 * @param blockChose
	 * @param agentUserId
	 * @param event
	 * @param eventStatus
	 * @param payCros
	 * @return doc
	 * @see 众包工作流模板承接
	 */
	public Document createDoc9(Document doc,JSONObject json,String userWalletAddress ,String blockChose ,String agentUserId , String event ,String eventStatus ,String payCros){
		Calendar cal = Calendar.getInstance();
		
	   		String crowdsourcingRunAddress =json.has("crowdsourcing_run_address") == true ? json.getString("crowdsourcing_run_address") : "";//众包工作流模板地址
	   		String crowdsourcingRunName =json.has("crowdsourcing_run_name") == true ? json.getString("crowdsourcing_run_name") : "";//众包工作流模板名称
	   		String crowdsourcingRunPublishFrom =json.has("crowdsourcing_run_publish_from") == true ? json.getString("crowdsourcing_run_publish_from") : "";//众包工作流模板发布
	   		String crowdsourcingRunTo =json.has("crowdsourcing_run_to") == true ? json.getString("crowdsourcing_run_to") : "";//众包工作流模板发布
	   		
	// Create a document
		 doc.append("vin_size", 1)
			.append("vout_size", 1)
			.append("tx_time", cal.getTimeInMillis())
			.append("in",
					asList(new Document().append("prev_out",
							"00000000000000000000000000000000").append(
							"prev_n", -1)))
			.append("out",
					asList(new Document()
							.append("value", 1)
							.append("scriptPubKey",
									"OP_DUP OP_HASH160 "
											+ userWalletAddress
											+ " OP_EQUALVERIFY OP_CHECKSIG")
							.append("data",
									new Document("agent_user_id",
											agentUserId)
											.append("block_chose", blockChose)
											.append("agent_user_id",
													agentUserId)
											.append("event",
													event)
											.append("event_status",
													eventStatus)
											.append("pay_cros",
													payCros)
											.append("crowdsourcing_run_address",
													crowdsourcingRunAddress)
											.append("crowdsourcing_run_name",
													crowdsourcingRunName)
											.append("crowdsourcing_run_publish_from",
													crowdsourcingRunPublishFrom)
											.append("crowdsourcing_run_to",
													crowdsourcingRunTo))));
		 return doc;
	}
	
	/**
	 * 众包工作流模板启动
	 * @param doc
	 * @param json
	 * @param userWalletAddress
	 * @param blockChose
	 * @param agentUserId
	 * @param event
	 * @param eventStatus
	 * @param payCros
	 * @return doc
	 * @see 众包工作流模板启动
	 */
	public Document createDoc10(Document doc,JSONObject json,String userWalletAddress ,String blockChose ,String agentUserId , String event ,String eventStatus ,String payCros){
		Calendar cal = Calendar.getInstance();
		
	   		String crowdsourcingRunAddress =json.has("crowdsourcing_run_address") == true ? json.getString("crowdsourcing_run_address") : "";//众包工作流模板地址
	   		String crowdsourcingRunName =json.has("crowdsourcing_run_name") == true ? json.getString("crowdsourcing_run_name") : "";//众包工作流模板名称
	   		String crowdsourcingRunStartFrom =json.has("crowdsourcing_run_start_from") == true ? json.getString("crowdsourcing_run_start_from") : "";//众包工作流模板发布
	   		
	// Create a document
		 doc.append("vin_size", 1)
			.append("vout_size", 1)
			.append("tx_time", cal.getTimeInMillis())
			.append("in",
					asList(new Document().append("prev_out",
							"00000000000000000000000000000000").append(
							"prev_n", -1)))
			.append("out",
					asList(new Document()
							.append("value", 1)
							.append("scriptPubKey",
									"OP_DUP OP_HASH160 "
											+ userWalletAddress
											+ " OP_EQUALVERIFY OP_CHECKSIG")
							.append("data",
									new Document("agent_user_id",
											agentUserId)
											.append("block_chose", blockChose)
											.append("agent_user_id",
													agentUserId)
											.append("event",
													event)
											.append("event_status",
													eventStatus)
											.append("pay_cros",
													payCros)
											.append("crowdsourcing_run_address",
													crowdsourcingRunAddress)
											.append("crowdsourcing_run_name",
													crowdsourcingRunName)
											.append("crowdsourcing_run_start_from",
													crowdsourcingRunStartFrom))));
		 return doc;
	}
	
	/**
	 * 自定义工作流模板公开
	 * @param doc
	 * @param json
	 * @param userWalletAddress
	 * @param blockChose
	 * @param agentUserId
	 * @param event
	 * @param eventStatus
	 * @param payCros
	 * @return doc
	 * @see 自定义工作流模板公开
	 */
	public Document createDoc11(Document doc,JSONObject json,String userWalletAddress ,String blockChose ,String agentUserId , String event ,String eventStatus ,String payCros){
		Calendar cal = Calendar.getInstance();
		
	   		String customWorkflowAddress =json.has("custom_workflow_address") == true ? json.getString("custom_workflow_address") : "";//自定义工作流模板地址
	   		String customWorkflowName =json.has("custom_workflow_name") == true ? json.getString("custom_workflow_name") : "";//自定义工作流模板名称
	   		String customWorkflowFrom =json.has("custom_workflow_from") == true ? json.getString("custom_workflow_from") : "";//自定义工作流模板创建者

	   		String crosUnitIncome =json.has("cros_unit_income") == true ? json.getString("cros_unit_income") : "";//cros收益布
	   		
	// Create a document
		 doc.append("vin_size", 1)
			.append("vout_size", 1)
			.append("tx_time", cal.getTimeInMillis())
			.append("in",
					asList(new Document().append("prev_out",
							"00000000000000000000000000000000").append(
							"prev_n", -1)))
			.append("out",
					asList(new Document()
							.append("value", 1)
							.append("scriptPubKey",
									"OP_DUP OP_HASH160 "
											+ userWalletAddress
											+ " OP_EQUALVERIFY OP_CHECKSIG")
							.append("data",
									new Document("agent_user_id",
											agentUserId)
											.append("block_chose", blockChose)
											.append("agent_user_id",
													agentUserId)
											.append("event",
													event)
											.append("event_status",
													eventStatus)
											.append("pay_cros",
													payCros)
											.append("custom_workflow_address",
													customWorkflowAddress)
											.append("custom_workflow_name",
													customWorkflowName)
											.append("custom_workflow_from",
													customWorkflowFrom)
											.append("cros_unit_income",
													crosUnitIncome))));
		 return doc;
	}
	
	/**
	 * 自定义工作流模板启动
	 * @param doc
	 * @param json
	 * @param userWalletAddress
	 * @param blockChose
	 * @param agentUserId
	 * @param event
	 * @param eventStatus
	 * @param payCros
	 * @return doc
	 * @see 自定义工作流模板启动
	 */
	public Document createDoc12(Document doc,JSONObject json,String userWalletAddress ,String blockChose ,String agentUserId , String event ,String eventStatus ,String payCros){
		Calendar cal = Calendar.getInstance();
		
	   		String customWorkflowRunAddress =json.has("custom_workflow_run_address") == true ? json.getString("custom_workflow_run_address") : "";//自定义工作流地址
	   		String customWorkflowRunName =json.has("custom_workflow_run_name") == true ? json.getString("custom_workflow_run_name") : "";//自定义工作流名称
	   		String customWorkflowRunStartFrom =json.has("custom_workflow_run_start_from") == true ? json.getString("custom_workflow_run_start_from") : "";//自定义工作流启动者
	   		
	// Create a document
		 doc.append("vin_size", 1)
			.append("vout_size", 1)
			.append("tx_time", cal.getTimeInMillis())
			.append("in",
					asList(new Document().append("prev_out",
							"00000000000000000000000000000000").append(
							"prev_n", -1)))
			.append("out",
					asList(new Document()
							.append("value", 1)
							.append("scriptPubKey",
									"OP_DUP OP_HASH160 "
											+ userWalletAddress
											+ " OP_EQUALVERIFY OP_CHECKSIG")
							.append("data",
									new Document("agent_user_id",
											agentUserId)
											.append("block_chose", blockChose)
											.append("agent_user_id",
													agentUserId)
											.append("event",
													event)
											.append("event_status",
													eventStatus)
											.append("pay_cros",
													payCros)
											.append("custom_workflow_run_address",
													customWorkflowRunAddress)
											.append("custom_workflow_run_name",
													customWorkflowRunName)
											.append("custom_workflow_run_start_from",
													customWorkflowRunStartFrom))));
		 return doc;
	}
	
	/**
	 * 商品交易回执
	 * @param doc
	 * @param json
	 * @param userWalletAddress
	 * @param blockChose
	 * @param agentUserId
	 * @param event
	 * @param eventStatus
	 * @param payCros
	 * @return doc
	 * @see 商品交易回执
	 */
	public Document createDoc13(Document doc,JSONObject json,String userWalletAddress ,String blockChose ,String agentUserId , String event ,String eventStatus ,String payCros){
		Calendar cal = Calendar.getInstance();
		
	   		String goodsOrder =json.has("goods_order") == true ? json.getString("goods_order") : "";//商品订单
	   		String goodsName =json.has("goods_name") == true ? json.getString("goods_name") : "";//商品名称
	   		String goodsUnit =json.has("goods_unit") == true ? json.getString("goods_unit") : "";//商品单价
	   		String goodsNum =json.has("goods_num") == true ? json.getString("goods_num") : "";//商品数量
	   		String goodsTotalAmt =json.has("goods_total_amt") == true ? json.getString("goods_total_amt") : "";//商品总额
	   		String goodsOrderTime =json.has("goods_order_time") == true ? json.getString("goods_order_time") : "";//商品订单时间
	   		String goodsOrderFrom =json.has("goods_order_from") == true ? json.getString("goods_order_from") : "";//商品购买方
	   		
	// Create a document
		 doc.append("vin_size", 1)
			.append("vout_size", 1)
			.append("tx_time", cal.getTimeInMillis())
			.append("in",
					asList(new Document().append("prev_out",
							"00000000000000000000000000000000").append(
							"prev_n", -1)))
			.append("out",
					asList(new Document()
							.append("value", 1)
							.append("scriptPubKey",
									"OP_DUP OP_HASH160 "
											+ userWalletAddress
											+ " OP_EQUALVERIFY OP_CHECKSIG")
							.append("data",
									new Document("agent_user_id",
											agentUserId)
											.append("block_chose", blockChose)
											.append("agent_user_id",
													agentUserId)
											.append("event",
													event)
											.append("event_status",
													eventStatus)
											.append("pay_cros",
													payCros)
											.append("goods_order",
													goodsOrder)
											.append("goods_name",
													goodsName)
											.append("goods_unit",
													goodsUnit)
											.append("goods_num",
													goodsNum)
											.append("goods_total_amt",
													goodsTotalAmt)
											.append("goods_order_time",
													goodsOrderTime)
											.append("goods_order_from",
													goodsOrderFrom))));
		 return doc;
	}
	
	/**
	 * 一键建链
	 * @param doc
	 * @param json
	 * @param userWalletAddress
	 * @param blockChose
	 * @param agentUserId
	 * @param event
	 * @param eventStatus
	 * @param payCros
	 * @return doc
	 * @see 一键建链
	 */
	public Document createDoc14(Document doc,JSONObject json,String userWalletAddress ,String blockChose ,String agentUserId , String event ,String eventStatus ,String payCros){
		Calendar cal = Calendar.getInstance();
		
	   		String createBlockAddress = userWalletAddress;//建链人公链所在地址
	   		String blockName =json.has("block_name") == true ? json.getString("block_name") : "";//侧链名称
	   		String blockDescription =json.has("block_description") == true ? json.getString("block_description") : "";//侧链描述
	   		
	// Create a document
		 doc.append("vin_size", 1)
			.append("vout_size", 1)
			.append("tx_time", cal.getTimeInMillis())
			.append("in",
					asList(new Document().append("prev_out",
							"00000000000000000000000000000000").append(
							"prev_n", -1)))
			.append("out",
					asList(new Document()
							.append("value", 1)
							.append("scriptPubKey",
									"OP_DUP OP_HASH160 "
											+ userWalletAddress
											+ " OP_EQUALVERIFY OP_CHECKSIG")
							.append("data",
									new Document("agent_user_id",
											agentUserId)
											.append("block_chose", blockChose)
											.append("agent_user_id",
													agentUserId)
											.append("event",
													event)
											.append("event_status",
													eventStatus)
											.append("pay_cros",
													payCros)
											.append("create_block_address",
													createBlockAddress)
											.append("block_name",
													blockName)
											.append("block_description",
													blockDescription))));
		 return doc;
	}
}

