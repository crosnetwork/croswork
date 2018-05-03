
package com.cros.block.quartz;

import java.util.Calendar;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import com.cros.block.api.coprs.CoprApiService;
import com.cros.block.api.dao.mongodb.MongoDBBaseDao;
import com.cros.block.api.impl.WeibeiMsgApiImpl;
import com.cros.block.model.BlockInfo;
import com.cros.block.service.impl.CrosBlockServiceChainImpl;
import com.cros.block.service.impl.WeibeiBlockChainServiceImpl;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

@Component("crosBlockChainScheduledJob")
public class CrosBlockChainScheduledJob {
	private Log logger = LogFactory.getLog(WeibeiMsgApiImpl.class);
	@Resource(name = "crosBlockChainService")
	private CrosBlockServiceChainImpl crosBlockChainService;
	@Resource(name = "coprApiService")
	private CoprApiService coprApiService;
	@Resource(name = "mongoDBBaseDao")
	MongoDBBaseDao mongoDBBaseDao;
	
	private List<BlockInfo> findAll(){
		return 	mongoDBBaseDao.findAll(BlockInfo.class,"BlockInformation");
	}
	
	protected void execute() {
	List<BlockInfo> blockInfos = findAll();
	BlockInfo blockInfo2 = new BlockInfo();
	blockInfo2.set_id(null);
	for(BlockInfo blockInfo:blockInfos){
		String blockInfoId = blockInfo.get_id().toString();
		logger.info("is job for 10 min");
		Calendar cal = Calendar.getInstance();
		DBCursor cor = coprApiService.queryForLock("Lock"+blockInfoId);
		try {
			if (cor.hasNext()) {
				DBObject userObj = cor.next();
				String status = userObj.get("status").toString();
				long time = (long) userObj.get("time");
				long difference = cal.getTimeInMillis() - time;
				if ("0".equals(status) && difference/(60*1000) > 10) {
					DBObject obj = new BasicDBObject();
					obj.put("time", cal.getTimeInMillis());
					obj.put("status", "1");
					logger.info("update status  1");
					coprApiService.updateForLock(obj, "Lock"+blockInfoId);
					crosBlockChainService.saveToblockChain(blockInfoId);
					// 更新锁状态
					obj.put("status", "0");
					logger.info("update status  0");
					coprApiService.updateForLock(obj, "Lock"+blockInfoId);
				}
			} else {
				logger.info("first write Lock  0");
				DBObject obj = new BasicDBObject();
				obj.put("_id", "1");
				obj.put("time", cal.getTimeInMillis());
				obj.put("status", "1");
				coprApiService.save(obj, "Lock"+blockInfoId);
				crosBlockChainService.saveToblockChain(blockInfoId);
				obj.put("status", "0");
				logger.info("update status  first  0");
				coprApiService.updateForLock(obj, "Lock"+blockInfoId);
			}
		} catch (Exception e) {
			logger.error(e);
		} finally {
			cor.close();
		}
	}
   
   }

}
