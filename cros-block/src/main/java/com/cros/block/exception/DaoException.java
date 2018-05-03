package com.cros.block.exception;


/**
 * @ClassName: DaoException
 * @Package com.trader.eval.exception
 * @Description:TODO ADD FUNCTION
 * @date: 2016年8月15日 下午1:35:32
 * @author hokuny@foxmail.com
 * @version 
 */
public class DaoException extends RiskException {
	private static final long serialVersionUID = 1L;

	public DaoException() {
		super();
	}

	public DaoException(String message) {
		super(message);
	}

	public DaoException(String message, Throwable cause) {
		super(message, cause);
	}

	public DaoException(Throwable cause) {
		super(cause);
	}
}
