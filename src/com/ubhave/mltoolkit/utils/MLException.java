package com.ubhave.mltoolkit.utils;

public class MLException extends Exception {

	private static final long serialVersionUID = -5918433829252926042L;

	public static final int INCOMPATIBLE_FEATURE_TYPE = 0;
	public static final int INCOMPATIBLE_INSTANCE = 0;
	
	private int errorCode;
	private String message;
	
	public MLException(int errorCode, String message) {
		super(message);
		this.message = message;
		this.errorCode = errorCode;
	}
	
	public int getErrorCode()
	{
		return errorCode;
	}

	public String getMessage()
	{
		return message;
	}

}
