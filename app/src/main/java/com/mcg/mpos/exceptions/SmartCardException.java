package com.mcg.mpos.exceptions;


/**
 * 智能卡操作异常类
 */
public class SmartCardException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private SmartCardException() {
		super();
	}

	public SmartCardException(String message) {
		super(message);
	}
}
