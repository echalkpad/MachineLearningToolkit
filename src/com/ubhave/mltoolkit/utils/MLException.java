package com.ubhave.mltoolkit.utils;

public class MLException extends Exception {

	public static final int INCOMPATIBLE_FEATURE_TYPE = 100;
	public static final int INCOMPATIBLE_INSTANCE = 101;
	public static final int INVALID_PARAMETER = 102;
	public static final int INVALID_STATE = 103;
	
	public static final int CLASSIFIER_EXISTS = 200;
	
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
