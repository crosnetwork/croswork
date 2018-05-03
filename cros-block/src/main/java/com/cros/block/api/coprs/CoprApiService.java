package com.cros.block.api.coprs;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;
import com.cros.block.api.dao.mongodb.MongoDBBaseDao;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;




/**
 * @Title: LeadsService.java
 * @Package com.citroen.ledp.api.leads
 * @Description: TODO(用一句话描述该文件做什么)
 * @author 游红昆
 * @date 2016年11月7日 下午6:54:50
 * @version V1.0
 */
@Component("coprApiService")
public class CoprApiService {
	@Resource(name = "mongoDBBaseDao")
	MongoDBBaseDao mongoDBBaseDao;
	
	public void save(Object obj,String collectionName) throws Exception{
		mongoDBBaseDao.add(obj,collectionName);
	}
	
	public DBCursor query(String collectionName,String agentUserId,String agent_idempotent,String tx_id, String[] product_hash,int pageNumber,int pageSize) throws Exception{
		return mongoDBBaseDao.queryByConfig(collectionName, agentUserId, agent_idempotent, tx_id, product_hash,pageNumber,pageSize);
	}
	
	public DBCursor queryForUid(String agentUserId,String collectionName){
		return mongoDBBaseDao.queryForUid(collectionName, agentUserId);
	}
	
	public DBCursor queryForLock(String collectionName){
		return mongoDBBaseDao.queryForLock(collectionName);
	}
	
	public void updateForLock(DBObject obj,String collectionName){
		mongoDBBaseDao.updateForLock(obj, collectionName);
	}
}
