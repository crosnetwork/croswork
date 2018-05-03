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
import com.cros.block.api.IWeibeiMsgApi;
import com.cros.block.api.coprs.CoprApiService;
import com.cros.block.util.EncryptionUtil;
import com.cros.block.util.TripleDES;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * @ClassName: WeibeiMsgApiImpl
 * @Package org.weibei.blockchain.api.impl
 * @Description:TODO ADD FUNCTION
 * @date: 2016年12月7日 上午12:31:10
 * @author hokuny@foxmail.com
 * @version
 */
public class WeibeiMsgApiImpl implements IWeibeiMsgApi {

	private Log logger = LogFactory.getLog(WeibeiMsgApiImpl.class);

	@Resource(name = "coprApiService")
	private CoprApiService coprApiService;

	@POST
	@Path("/newtx")
	public String coprRegisterData(String params) {
		System.out.println("newtx");
		logger.info("版权家登记信息 开始");
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
			JSONObject json = (JSONObject) JSONObject.fromObject(str);

			/* 校验厂家是否正确 */
			String agentId = json.getString("agent_id");
			if (!StringUtils.equals("banquanjia", agentId)) {
				result.put("status", 402);
				result.put("err_msg", "厂家不正确");
				return JSON.toJSONString(result);
			}
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
			String agentUserId = json.has("agent_user_id") == true ? json
					.getString("agent_user_id") : "";// 用户唯一ID
			String registerType = json.has("register_type") == true ? json
					.getString("register_type") : ""; // 登记分类
			String productLabel = json.has("product_label") == true ? json
					.getString("product_label") : ""; // 作品分类标签
			String productName = json.has("product_name") == true ? json
					.getString("product_name") : ""; // 作品名
			String agentIdempotent = json.has("agent_idempotent") == true ? json
					.getString("agent_idempotent") : ""; // 厂家内部记录标识
			String productHash = json.has("product_hash") == true ? json
					.getString("product_hash") : ""; // 作品hash(md5)
			String[] productHashs = productHash.split(",");
			String productType = json.has("product_type") == true ? json
					.getString("product_type") : ""; // 作品类型
			String dci = json.has("dci") == true ? json.getString("dci") : ""; // 中国版权中心DCI码
			String creativeDate = json.has("creative_date") == true ? json
					.getString("creative_date") : "";// 创作日期
			String creativeAddress = json.has("creative_address") == true ? json
					.getString("creative_address") : ""; // 创作地点
			String publishStatus = json.has("publish_status") == true ? json
					.getString("publish_status") : ""; // 发表状态
			String publishDate = json.has("publish_date") == true ? json
					.getString("publish_date") : ""; // 发表时间
			String publishAddress = json.has("publish_address") == true ? json
					.getString("publish_address") : "";// 创作日期
			String creativePurpose = json.has("creative_purpose") == true ? json
					.getString("creative_purpose") : ""; // 创作地点
			String creativeProcess = json.has("creative_process") == true ? json
					.getString("creative_process") : ""; // 发表状态
			String creativeSelf = json.has("creative_self") == true ? json
					.getString("creative_self") : ""; // 发表时间
			String productAbstract = json.has("product_abstract") == true ? json
					.getString("product_abstract") : "";//作品摘要

			String userPublicKey = "";
			String userPrivateKey = "";
			String userWalletAddress = "";
			DBCursor userInfo = coprApiService.queryForUid(agentUserId,
					"Wallet");
			try {
				if (userInfo.hasNext()) {
					logger.info(" : old user========================================");
					DBObject userObj = userInfo.next();
					userPublicKey = userObj.get("userPublicKey").toString();
					userPrivateKey = userObj.get("userPrivateKey").toString();
					userWalletAddress = userObj.get("userWalletAddress")
							.toString();
				} else {
					logger.info(" : new user========================================");
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
					coprApiService.save(obj, "Wallet");
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
			Calendar cal = Calendar.getInstance();
			// Create a document
			Document doc = new Document("ver", 1)
					.append("vin_size", 1)
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
													.append("agent_id", agentId)
													.append("register_type",
															registerType)
													.append("product_label",
															productLabel)
													.append("product_name",
															productName)
													.append("agent_idempotent",
															agentIdempotent)
													.append("product_hash",
															asList(productHashs))
													.append("product_type",
															productType)
													.append("product_abstract",
															productAbstract)
													.append("dci", dci)
													.append("creative_date",
															creativeDate)
													.append("creative_address",
															creativeAddress)
													.append("publish_status",
															publishStatus)
													.append("publish_date",
															publishDate)
													.append("publish_address",
															publishAddress)
													.append("creative_purpose",
															creativePurpose)
													.append("creative_process",
															creativeProcess)
													.append("creative_self",
															creativeSelf))));
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
				coprApiService.save(doc, "TxPool");
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
	public String coprQueryData(String params) {
		// TODO Auto-generated method stub
		System.out.println("gettx");
		logger.info("版权家登记信息 查询");
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
			/* 校验厂家是否正确 */
			String agentId = json.getString("agent_id");
			if (!StringUtils.equals("banquanjia", agentId)) {
				result.put("status", 402);
				result.put("err_msg", "厂家不正确");
				return JSON.toJSONString(result);
			}
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

			DBCursor dbs = coprApiService.query("TxPool", agentUserId,
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

}
