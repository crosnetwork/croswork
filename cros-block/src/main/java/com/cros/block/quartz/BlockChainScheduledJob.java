/**
 * Project Name:cros-block
 * File Name:BlockChainScheduledJob.java
 * Package Name:org.weibei.blockchain.quartz
 * Date:2016年11月30日下午3:51:56
 * Copyright (c) 2016, hokuny@foxmail.com All Rights Reserved.
 *
 */

package com.cros.block.quartz;

import java.util.Calendar;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import com.cros.block.api.coprs.CoprApiService;
import com.cros.block.api.impl.WeibeiMsgApiImpl;
import com.cros.block.service.impl.WeibeiBlockChainServiceImpl;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * ClassName:BlockChainScheduledJob <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason: TODO ADD REASON. <br/>
 * Date: 2016年11月30日 下午3:51:56 <br/>
 * 
 * @author hokuny@foxmail.com
 * @version
 * @since JDK 1.6
 * @see
 */
@Component("blockChainScheduledJob")
public class BlockChainScheduledJob {
	private Log logger = LogFactory.getLog(WeibeiMsgApiImpl.class);
	@Resource(name = "weibeiBlockChainService")
	private WeibeiBlockChainServiceImpl weibeiBlockChainService;
	@Resource(name = "coprApiService")
	private CoprApiService coprApiService;

	protected void execute() {
		logger.info("is job for 10 min");
		Calendar cal = Calendar.getInstance();
		DBCursor cor = coprApiService.queryForLock("Lock");
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
					coprApiService.updateForLock(obj, "Lock");
					weibeiBlockChainService.saveToblockChain();
					// 更新锁状态
					obj.put("status", "0");
					logger.info("update status  0");
					coprApiService.updateForLock(obj, "Lock");
				}
			} else {
				logger.info("first write Lock  0");
				DBObject obj = new BasicDBObject();
				obj.put("_id", "1");
				obj.put("time", cal.getTimeInMillis());
				obj.put("status", "1");
				coprApiService.save(obj, "Lock");
				weibeiBlockChainService.saveToblockChain();
				obj.put("status", "0");
				logger.info("update status  first  0");
				coprApiService.updateForLock(obj, "Lock");
			}
		} catch (Exception e) {
			logger.error(e);
		} finally {
			cor.close();
		}

	}

}
