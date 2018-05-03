//package com.cros.block.util;
//
//import java.util.Date;
//
///**
// * @Title: LedpLogger.java
// * @Package com.citroen.ledp.util
// * @Description: TODO(日志接口类)
// * @author weixicai
// * @date 2015年2月6日 上午2:25:54
// * @version V1.0
// */
//public class LedpLogger{
//
//	public enum Operation{
//		create("create"),update("update"),delete("delete"),login("login"),other("other");
//		public String value;
//		private Operation(String value){this.value = value;}
//	}
//	
//	public enum Result{
//		success("success"),failure("failure");
//		public String value;
//		private Result(String value){this.value = value;}
//	}
//	//operator保存当前登陆人;resource操作对象如:(登陆信息发布);operation操作类型如:update;result操作结果如:success;comment操作备注如:登陆成功,id:172.16.108.104
//	public static  void info(User operator,String resource,Operation operation,Result result,String comment){
//		log("info",operator, resource, operation, result,comment);
//	}
//	public static void warn(User operator,String resource,Operation operation,Result result,String comment){
//		log("warn",operator, resource, operation, result,comment);
//	}
//	public static void error(User operator,String resource,Operation operation,Result result,String comment){
//		log("error",operator, resource, operation, result,comment);
//	}
//	
//	public static  void info(Member operator,String resource,Operation operation,Result result,String comment){
//		log("info",operator, resource, operation, result,comment);
//	}
//	public static void error(Member operator,String resource,Operation operation,Result result,String comment){
//		log("error",operator, resource, operation, result,comment);
//	}
//	private static void log(String type,User operator,String resource,Operation operation,Result result,String comment){
//		Log log = new Log();
//		log.setType(type);
//		log.setOperator(operator.getId()==null?0:operator.getId());
//		log.setResource(resource);
//		log.setOperation(operation.name());
//		log.setResult(result.name());
//		log.setSplit("1");
//		log.setComment(comment);
//		log.setDatetime(new Date());
//	}
//	
//	private static void log(String type,Member operator,String resource,Operation operation,Result result,String comment){
//		Log log = new Log();
//		log.setType(type);
//		log.setOperator(operator==null?null:operator.getId());
//		log.setResource(resource);
//		log.setOperation(operation.name());
//		log.setResult(result.name());
//		log.setSplit("2");
//		log.setComment(comment);
//		log.setDatetime(new Date());
//	}
//}
