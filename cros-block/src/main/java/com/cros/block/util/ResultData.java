package com.cros.block.util;

import java.io.Serializable;

/**
 * 返回结果的工具类
 *
 */
public class ResultData implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8967147927300560419L;

	/**
	 * 请求状态（是否成功）
	 */
	private boolean success = true;
	
	/**
	 * 请求的提示信息
	 */
	private String message = "";
	
	/**
	 * 请求的数据
	 */
	private Object data;
	
	public ResultData(){}
	
	/**
	 * 
	 * @param success -
	 * @param message -
	 */
	public ResultData(boolean success, String message){
		this(success, message ,null);
	}
	
	/**
	 * 
	 * @param success -
	 * @param message -
	 * @param data -
	 */
	public ResultData(boolean success, String message, Object data) {
		super();
		this.success = success;
		this.message = message;
		this.data = data;
	}
	
	/**
	 * 
	 * @return
	 */
	public static ResultData buildSuccessResult(){
		return buildSuccessResult("");
	}
	
	/**
	 * 
	 * @param message -
	 * @return
	 */
	public static ResultData buildSuccessResult(String message){
		return buildSuccessResult(message, null);
	}
	
	/**
	 * 
	 * @param message -
	 * @param data -
	 * @return
	 */
	public static ResultData buildSuccessResult(String message, Object data){
		return new ResultData(true, message, data);
	}
	
	/**
	 * 
	 * @return
	 */
	public static ResultData buildFailureResult(){
		return buildFailureResult("");
	}
	
	/**
	 * 
	 * @param message -
	 * @return
	 */
	public static ResultData buildFailureResult(String message){
		return buildFailureResult(message, null);
	}
	
	/**
	 *
	 * @param message -
	 * @param data -
	 * @return
	 */
	public static ResultData buildFailureResult(String message, Object data){
		return new ResultData(false, message, data);
	}
	
	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
	
}
