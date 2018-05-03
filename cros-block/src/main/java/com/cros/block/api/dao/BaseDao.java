package com.cros.block.api.dao;

import java.util.List;

import org.bson.types.ObjectId;

import com.cros.block.model.User;
import com.cros.block.util.PageModel;
import com.mongodb.BasicDBList;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public interface BaseDao {

	<T> T findById(Class<T> entityClass, String id);

	<T> List<T> findAll(Class<T> entityClass);

	void remove(Object obj);

	void add(Object obj,String collectionName);

	void updateForLock(DBObject obj,String collectionName);
	
	DBCursor queryByUsername(String collectionName,String username);
	
	DBCursor queryByConfig(String collection,String agentUserId,String copyright_id,String tx_id,String[] product_hash,int pageNumber,int pageSize);
	
	DBCursor queryForPrevBlock(String collection);
	
	DBCursor queryForTxHash(String collection);
	
	DBCursor queryForUid(String collection,String agentUserId);
	
	DBCursor queryForLock(String collection);
	
	void updateTxStatusConfirmed(String collection,BasicDBList Id);
	
	void updateTxStatusInvalid(String collection,BasicDBList Id);
	
	DBObject queryForId(String collection,ObjectId id);
	
	int updatePwd(String collection,String username,String password);
	
	List<User> findByPage(PageModel<User> page, DBObject queryObject,String collectionName);

	DBCursor queryForAgentUserId(String collection, String agentUserId);
}
