package com.ubhave.mltoolkit.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


public class Feature {
	
	public static final int NOMINAL = 0;
	public static final int NUMERIC = 1;
	
	private int d_type;
	private String d_name;
    private ArrayList<String> d_categories;
    private HashMap<String,Integer> d_categoryIndex;
    
	public Feature(String fname, int ftype) throws MLException{
		if (ftype == NOMINAL){
			throw new MLException(MLException.INCOMPATIBLE_FEATURE_TYPE,
					"Nominal features require a list of possible categories;" +
					" use Feature(String, int, String[]) instead."); 
		}
		d_type = ftype;
		d_name = fname;
	}
	
	public Feature(String fname, int ftype, String[] fvalues) throws MLException{
		if (ftype == NUMERIC){
			throw new MLException(MLException.INCOMPATIBLE_FEATURE_TYPE,
					"Numeric features do not need a list of possible categories;" +
					" use Feature(String, int) instead."); 
		}
		d_type = ftype;
		d_name = fname;
		d_categories = new ArrayList<String>(Arrays.asList(fvalues));
 		for(int i=0;i<fvalues.length;i++) d_categoryIndex.put(fvalues[i], Integer.valueOf(i));
	}
	
	public Feature(String fname, int ftype, ArrayList<String> fvalues) throws MLException{
		if (ftype == NUMERIC){
			throw new MLException(MLException.INCOMPATIBLE_FEATURE_TYPE,
					"Numeric features do not need a list of possible categories;" +
					" use Feature(String, int) instead."); 
		}
		d_type = ftype;
		d_name = fname;
		d_categories = (ArrayList<String>) fvalues.clone();
 		for(int i=0;i<fvalues.size();i++) d_categoryIndex.put(fvalues.get(i), Integer.valueOf(i));
	}
	
	public int getFeatureType(){
		return d_type;
	}
	
	public ArrayList<String> getValues(){
		return d_categories;
	}
	
	public String name(){
		return d_name;
	}
	
	public int indexOfCategory(String value){
		return d_categoryIndex.get(value);
	}
	
	public String categoryOfIndex(int index){
		return d_categories.get(index);
	}
	
	public int numberOfCategories(){
		if (d_type == NOMINAL) return d_categories.size(); 
		else return 1;
	}
}
