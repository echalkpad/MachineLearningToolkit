package com.ubhave.mltoolkit.utils;

/**
 * Values can be double for numeric, int for nominal, 
 * and a special NaN for missing values.
 * 
 * @author Veljko Pejovic (v.pejovic@cs.bham.ac.uk)
 *
 */
public class Value {

	public static final int NOMINAL_VALUE=0;
	public static final int NUMERIC_VALUE=1;
	public static final int MISSING_VALUE=2;

	private Object d_value;
	private int d_type;

	public Value(Object a_value, int a_type){
		switch (a_type) {
			case NOMINAL_VALUE: d_value = a_value;
								d_type = NOMINAL_VALUE;
								break;
			case NUMERIC_VALUE: d_value = a_value;
								d_type = NUMERIC_VALUE;
								break;
			case MISSING_VALUE: d_value = Double.NaN;
								d_type = MISSING_VALUE;
								break;			
		}
	}
	
	public int getValueType(){
		return d_type;
	}
	
	public Object getValue(){
		return d_value;
	}
}
