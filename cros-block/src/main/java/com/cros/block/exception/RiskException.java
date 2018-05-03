package com.cros.block.exception;



/**
 * @ClassName: RiskException
 * @Package com.trader.eval.exception
 * @Description:TODO ADD FUNCTION
 * @date: 2016年8月15日 下午1:35:48
 * @author hokuny@foxmail.com
 * @version 
 */
public class RiskException extends Exception {
	private static final long serialVersionUID = 1L;

	public RiskException() {
		super();
	}

	public RiskException(String message) {
		super(message);
	}

	public RiskException(String message, Throwable cause) {
		super(message, cause);
	}

	public RiskException(Throwable cause) {
		super(cause);
	}
}
