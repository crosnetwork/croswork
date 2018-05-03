package mongoTest;

import java.util.Date;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.cros.block.api.dao.mongodb.MongoDBBaseDao;
import com.cros.block.util.SpringContextUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;


@ContextConfiguration(locations={"classpath*:spring/spring-dispatcher.xml,spring/spring-api.xml,spring/spring-mvc.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class mongo {

	@Resource(name = "mongoDBBaseDao")
	MongoDBBaseDao mongoDBBaseDao;
	
	@Test
	public void testCRUD(){
		DBObject obj = new BasicDBObject();
		obj.put("username", "CQ001");
		obj.put("blockhash", "0x15we5446442211edwed12e3e1223");
		obj.put("createTime", new Date());
		mongoDBBaseDao.save("User", obj);
		
	}
}
