/**
 * Project Name:cros-block
 * File Name:WeibeiBlockChainImpl.java
 * Package Name:org.weibei.blockchain.service.impl
 * Date:2016年12月1日下午2:11:52
 * Copyright (c) 2016, hokuny@foxmail.com All Rights Reserved.
 *
 */

package com.cros.block.service.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;

import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Utils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import com.cros.block.api.dao.mongodb.MongoDBBaseDao;
import com.cros.block.service.IWeibeiBlockChainService;
import com.cros.block.util.MerkleTree;

import com.alibaba.fastjson.JSONArray;
import com.mongodb.BasicDBList;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * ClassName:WeibeiBlockChainImpl <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason: TODO ADD REASON. <br/>
 * Date: 2016年12月1日 下午2:11:52 <br/>
 * 
 * @author hokuny@foxmail.com
 * @version
 * @since JDK 1.6
 * @see
 */

@Service("weibeiBlockChainService")
public class WeibeiBlockChainServiceImpl implements IWeibeiBlockChainService {
	@Resource(name = "mongoDBBaseDao")
	MongoDBBaseDao mongoDBBaseDao;
	private Log logger = LogFactory.getLog(WeibeiBlockChainServiceImpl.class);
	
	@Override
	public void saveToblockChain() {
		/*
		 *  业务逻辑
		 *  Step1： 查询txPool 集中 status=pending的数据   DBTx
		 *  Step2： DBTx count/100  = 0     执行打包
		 *                         = 1    分2次 执行打包 
		 *                         = n  分n+1次 执行打包
		 *      # 打包逻辑
		 *      1. 查询上一个区块的信息  
		 *              校验 txpool区块上的tx值是否有效
		 *              cuurrent       previous
		 *              prev_block   block_hash值
		 *              height       height+1
		 *      2. block heard     
		 *              size         DBTx count
		 *              block_hash   docment 转 hash
		 *              merkleroot   所有被打包docment tx_id值
		 *              bitcoin_tx   目前为空
		 *      3. 签名和hash值
		 *         找到的所有记录写入document的 "tx" 节点
		 *         
		 *      4. 将docment写入 blockchain
		 *      5. 修改txPool数据状态
		 *       
		 */
		
		System.out.println("saveToblockChain -----");
		DBCursor dbs_pending = mongoDBBaseDao.queryForTxHash("TxPool");
		List<ObjectId> ids=new ArrayList<ObjectId>();
		List<String> Tx_hashs=new ArrayList<String>();
		JSONArray jsonArray = new JSONArray();
		try{
		while (dbs_pending.hasNext()) {
			DBObject document = dbs_pending.next();
			document.removeField("_class");

			Map documentMap = document.toMap();

			ObjectId id = (ObjectId) documentMap.get("_id");
			String tx_id = (String) documentMap.get("tx_id");
			document.removeField("_id");
			JSONObject jsonObject = JSONObject.fromObject(document);
			// 校验  tx 是否通过
			if(!verifyTx(new Document(jsonObject))){
				// if 不通过 修改状态
				BasicDBList values = new BasicDBList();
				values.add(id);
				mongoDBBaseDao.updateTxStatusInvalid("TxPool", values);
				continue;
        	}

			// 如果通过 继续执行
			ids.add(id);
			Tx_hashs.add(tx_id);
			jsonArray.add(jsonObject);
			if(Tx_hashs.size()==100){
				//开始打包
				System.out.println("=====满足100条记录开始打包");
				createBlockchain(jsonArray,ids,Tx_hashs);
				// 清理 ids 和Tx_hashs
				ids.clear();
				Tx_hashs.clear();
				jsonArray.clear();
			}
		}
		}finally{
			dbs_pending.close();
		}
		if(Tx_hashs!=null && Tx_hashs.size()>0 && Tx_hashs.size()<=100){
			//开始打包
			System.out.println("=====不满足100条记录开始打包");
			createBlockchain(jsonArray,ids,Tx_hashs);
			// 清理 ids 和Tx_hashs
			ids.clear();
			Tx_hashs.clear();
			jsonArray.clear();
		}
	}
	
	private void createBlockchain(JSONArray jsonArray,List<ObjectId> ids,List<String> Tx_hashs){
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",
				Locale.ENGLISH);
		Calendar cal = Calendar.getInstance();
		// Create a document header
		Document doc = new Document("ver", 1);
		// 
		DBCursor prev_block_Cur = mongoDBBaseDao.queryForPrevBlock("Blockchain");
		try{
		if (prev_block_Cur.hasNext()) {
			DBObject document = (DBObject) prev_block_Cur.next();
			Map map = document.toMap();
			String prev_block =(String) map.get("blockHash");
			int height =(int) map.get("height");
			doc.append("prev_block", prev_block);
			doc.append("height", height+1);
		}else{
			doc.append("prev_block", "0000000000000000000000000000000000000000000000000000000000000000");
			doc.append("height", 1);
		}
		}finally{
			prev_block_Cur.close();
		}
		
		doc.append("size", Tx_hashs.size());
		doc.append("time", format.format(cal.getTime()));
		String merkleroot =  new MerkleTree(Tx_hashs).merkle_tree();
		doc.append("merkleroot", merkleroot);
		doc.append("tx", jsonArray);
		String blockhash = Utils.HEX.encode(Sha256Hash.hashTwice(doc.toJson().getBytes()) );
		doc.append("block_hash", blockhash);
		doc.append("bitcoin_tx", "");
		
//		Iterator<Object> it = jsonArray.iterator();
//		boolean flag=true;
//        while (it.hasNext()) {
//        	JSONObject ob = (JSONObject) it.next();
//        	if(!verifyTx(new Document(ob))){
//        		flag=false;
//        		break;
//        	}
//        	ob.remove("status");
//        }
		// 校验
//		if(flag){
			mongoDBBaseDao.add(doc, "Blockchain");
			//更新txpool中的状态 
			BasicDBList values = new BasicDBList();
			for(ObjectId id:ids){
				values.add(id);
			}
			mongoDBBaseDao.updateTxStatusConfirmed("TxPool", values);
//		}
	}
	
	private boolean verifyTx(Document document){
        String txHash = document.getString("tx_id");
        String scriptSig = document.getString("scriptSig");
        String[] tokens = scriptSig.split(" ");
        String signature = tokens[0];
        String publicKey = tokens[1];
        document.remove("status");
        document.remove("_id");
        document.remove("tx_id");
		String docHash = Utils.HEX.encode(Sha256Hash.hashTwice(document.toJson().getBytes()));
		logger.info("Is hash the same = " + docHash.equals(txHash));
        document.remove("scriptSig");
		try {
			String docHash2 = Utils.HEX.encode(Sha256Hash.hashTwice(document.toJson().getBytes()));
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
