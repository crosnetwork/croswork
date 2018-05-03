package com.cros.block.exception;



/**
 * @ClassName: SecurityException
 * @Package com.trader.eval.exception
 * @Description:TODO ADD FUNCTION
 * @date: 2016年8月15日 下午1:35:59
 * @author hokuny@foxmail.com
 * @version 
 */
public class SecurityException extends RiskException {
	private static final long serialVersionUID = 1L;

	public SecurityException() {
		super();
	}

	public SecurityException(String message) {
		super(message);
	}

	public SecurityException(String message, Throwable cause) {
		super(message, cause);
	}

	public SecurityException(Throwable cause) {
		super(cause);
	}
}
