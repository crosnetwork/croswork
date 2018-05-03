package com.cros.block.exception;


/**
 * @ClassName: ArgumentException
 * @Package com.trader.eval.exception
 * @Description:TODO ADD FUNCTION
 * @date: 2016年8月15日 上午9:13:20
 * @author hokuny@foxmail.com
 * @version 
 */
public class ArgumentException extends RiskException {
	private static final long serialVersionUID = 1L;

	public ArgumentException() {
		super();
	}

	public ArgumentException(String message) {
		super(message);
	}

	public ArgumentException(String message, Throwable cause) {
		super(message, cause);
	}

	public ArgumentException(Throwable cause) {
		super(cause);
	}
}
