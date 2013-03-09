package com.ubhave.mltoolkit.utils;

import java.util.ArrayList;

/**
 * 
 * @author Veljko Pejovic (v.pejovic@cs.bham.ac.uk)
 *
 */

public class Instance {

	private ArrayList<Value> d_values;
	
	private DataSet d_dataset;
	
	public Instance(int a_numFeatures){
		d_values = new ArrayList<Value>(a_numFeatures);
	}
	
	public Instance(){
		this(0);
	}
	
	public Instance(ArrayList<Value> featureValues){
		d_values = featureValues;
	}
	
	public void addValue(Value a_value){
		d_values.add(a_value);		
	}
	
	public Value getValueAtIndex(int a_i){
		return d_values.get(a_i);
	}
	
	public void setValueAtIndex(int a_i, Value a_value) throws IndexOutOfBoundsException {
		d_values.set(a_i, a_value);
	}
	
	public void setDataSet(DataSet a_dataSet){
		d_dataset = a_dataSet;		
	}
	
	public DataSet getDataSet(){
		return d_dataset;
	}
	
	public int size(){
		return d_values.size();
	}
}