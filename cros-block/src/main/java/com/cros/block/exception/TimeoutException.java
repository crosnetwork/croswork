package com.cros.block.exception;



/**
 * @ClassName: TimeoutException
 * @Package com.trader.eval.exception
 * @Description:TODO ADD FUNCTION
 * @date: 2016年8月15日 下午1:36:05
 * @author hokuny@foxmail.com
 * @version 
 */
public class TimeoutException extends RiskException {
	private static final long serialVersionUID = 1L;

	public TimeoutException() {
		super();
	}

	public TimeoutException(String message) {
		super(message);
	}

	public TimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

	public TimeoutException(Throwable cause) {
		super(cause);
	}
}
