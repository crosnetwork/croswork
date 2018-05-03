package com.cros.block.api.dao.mongodb;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import com.cros.block.api.dao.BaseDao;
import com.cros.block.model.User;
import com.cros.block.util.PageModel;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import com.sun.org.apache.bcel.internal.generic.NEW;


/**
 * @ClassName: MongoDBBaseDao
 * @Package org.weibei.blockchain.api.dao.mongodb
 * @Description:mongodb数据泛型dao类
 * @date: 2016年11月19日 上午11:15:14
 * @author hokuny@foxmail.com
 * @version 
 */
@Repository(value = "mongoDBBaseDao")  
public class MongoDBBaseDao implements BaseDao {
	@Autowired
	@Qualifier("mongoTemplate")
	protected MongoTemplate mongoTemplate;

	/**
	 * 根据主键id返回对象
	 * 
	 * @param id
	 *            唯一标识
	 * @return T 对象
	 */
	public <T> T findById(Class<T> entityClass, String id) {
		return this.mongoTemplate.findById(id, entityClass);
	}

	/**
	 * 根据类获取全部的对象列表
	 * 
	 * @param entityClass
	 *            返回类型
	 * @return List<T> 返回对象列表
	 */
	public <T> List<T> findAll(Class<T> entityClass) {
		return this.mongoTemplate.findAll(entityClass);
	}

	/**
	 * 删除一个对象
	 * 
	 * @param obj
	 *            要删除的Mongo对象
	 */
	public void remove(Object obj) {
		this.mongoTemplate.remove(obj);
	}

	/**
	 * 添加对象
	 * 
	 * @param obj
	 *            要添加的Mongo对象
	 */
	public void add(Object obj,String collectionName) {
		if(collectionName!=null){
			this.mongoTemplate.insert(obj, collectionName);
		}else{
			this.mongoTemplate.insert(obj);
		}


	}

	/**
	 * 修改对象
	 * 
	 * @param obj
	 *            要修改的Mongo对象
	 */
	public void updateForLock(DBObject obj,String collectionName) {
		this.mongoTemplate.getCollection(collectionName).update(new BasicDBObject("_id","1"), obj);
	}
	
	public void save(String collection,DBObject doc){
		this.mongoTemplate.getCollection(collection).insert(doc);
	}
	
	public DBCursor queryByConfig(String collection,String agentUserId,String agent_idempotent,String tx_id,String[] product_hash,int pageNumber,int pageSize){	
		BasicDBObject basicDBObject = new BasicDBObject();
		DBObject orderBy =new BasicDBObject();
		orderBy.put("tx_time", -1);
		if(agentUserId!=null){
			basicDBObject.put("out.data.agent_user_id",agentUserId);
		}
		if(agent_idempotent!=null){
			basicDBObject.put("out.data.agent_idempotent", agent_idempotent);
		}
		if(tx_id!=null){
			basicDBObject.put("tx_hash",tx_id);
		}
		if(product_hash!=null){
			basicDBObject.put("out.data.product_hash", new BasicDBObject("$all",product_hash));
		}
		if(basicDBObject.size()>0){
			return this.mongoTemplate.getCollection(collection).find(basicDBObject).sort(orderBy).skip((pageNumber-1)*pageSize).limit(pageSize);
		}else{
			return null;
		}
		
	}
	
	public DBCursor queryById(String collection,String agent_user_id){		
		return this.mongoTemplate.getCollection(collection).find(new BasicDBObject("out.data.agent_user_id",agent_user_id));
	}

	@Override
	public DBCursor queryForPrevBlock(String collection) {
	      DBCursor dbCursor=mongoTemplate.getCollection(collection).find();
	      //排序
	      DBObject sortDBObject=new BasicDBObject();
	      sortDBObject.put("height",-1);
	      return dbCursor.sort(sortDBObject).limit(1);
	}

	@Override
	public DBCursor queryForTxHash(String collection) {
		return this.mongoTemplate.getCollection(collection).find(new BasicDBObject("status","pending"));
	}
	
	@Override
	public void updateTxStatusConfirmed(String collection,BasicDBList ids) {		
	       Criteria criteria = Criteria.where("_id").in(ids);  
	        Query query = new Query(criteria);  
			Update update = Update.update("status","confirmed"); 
		this.mongoTemplate.updateMulti(query, update, collection);
	}

	@Override
	public DBCursor queryForUid(String collection, String agentUserId) {
		// TODO Auto-generated method stub
		return this.mongoTemplate.getCollection(collection).find(new BasicDBObject("agentUserId",agentUserId));
	}

	@Override
	public DBCursor queryForLock(String collection) {
		
		// TODO Auto-generated method stub
		return this.mongoTemplate.getCollection(collection).find();
	}

	@Override
	public void updateTxStatusInvalid(String collection, BasicDBList ids) {
		 Criteria criteria = Criteria.where("_id").in(ids);  
	        Query query = new Query(criteria);  
			Update update = Update.update("status","invalid"); 
		this.mongoTemplate.updateMulti(query, update, collection);
		// TODO Auto-generated method stub
		
	}

	@Override
	public DBCursor queryByUsername(String collectionName, String username) {
		// TODO Auto-generated method stub
		return this.mongoTemplate.getCollection(collectionName).find(new BasicDBObject("username",username));
	}

	@Override
	public DBObject queryForId(String collection, ObjectId id) {
		// TODO Auto-generated method stub
		Criteria criteria = Criteria.where("_id").is(id);
		Query query = new Query(criteria);  
		return  this.mongoTemplate.getCollection(collection).findOne(id);
	}

	@Override
	public int updatePwd(String collection, String username,String password) {
		// TODO Auto-generated method stub
		Criteria criteria = Criteria.where("username").is(username);
		Query query = new Query(criteria);  
		Update update = Update.update("password",password);
		WriteResult result = mongoTemplate.updateMulti(query, update, "User");
		return result.getN();
	}

	@Override
	public List<User> findByPage(PageModel<User> page, DBObject queryObject, String collectionName) {
		// TODO Auto-generated method stub
		 Query query=new BasicQuery(queryObject);  
		   //查询总数  
		   int count=(int) mongoTemplate.count(query,User.class);  
		   page.setRowCount(count);  
		    
		   //排序  
		      query.with(new Sort(Direction.ASC, "onumber"));  
		     query.skip(page.getSkip()).limit(page.getPageSize());  
		   List<User>datas=mongoTemplate.find(query,User.class);  
		   page.setDatas(datas);
		return null;
	}
	
	@Override
	public DBCursor queryForAgentUserId(String collection, String agentUserId) {
		return this.mongoTemplate.getCollection(collection).find(new BasicDBObject("agentUserId",agentUserId));
	}
	
	public <T> List<T> findAll(Class<T> entityClass,String collectionName){
	    return mongoTemplate.findAll(entityClass, collectionName);
	}
	
	public DBCollection createCollection(String collectionName){
		 return	mongoTemplate.createCollection(collectionName);
	}
	
}
