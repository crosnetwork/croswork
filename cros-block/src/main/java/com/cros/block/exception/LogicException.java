package com.cros.block.exception;



/**
 * @ClassName: LogicException
 * @Package com.trader.eval.exception
 * @Description:TODO ADD FUNCTION
 * @date: 2016年8月15日 下午1:35:43
 * @author hokuny@foxmail.com
 * @version 
 */
public class LogicException extends RiskException {
	private static final long serialVersionUID = 1L;

	public LogicException() {
		super();
	}

	public LogicException(String message) {
		super(message);
	}

	public LogicException(String message, Throwable cause) {
		super(message, cause);
	}

	public LogicException(Throwable cause) {
		super(cause);
	}
}
