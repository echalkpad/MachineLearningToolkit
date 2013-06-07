package com.ubhave.mltoolkit.utils;

import java.util.ArrayList;
import java.util.HashMap;

import android.util.Log;

public class Signature {

	private static final String TAG = "Signature";
	
	private ArrayList<Feature> d_features;
	
    private HashMap<String, Feature> d_namesFeatures;
    
    private int d_classIndex;		

	public Signature(ArrayList<Feature> a_features, int a_classIndex){
		d_namesFeatures = new HashMap<String, Feature>();
		d_features = a_features;
		d_classIndex = a_classIndex;
		for (Feature f : a_features) {
			d_namesFeatures.put(f.name(), f);
		}
	}
	
	public Signature(ArrayList<Feature> a_features){
		this(a_features, a_features.size() - 1);
	}
	
	public void setClassIndex(int a_classIndex){
		d_classIndex = a_classIndex;
	}
	
	public int getClassIndex(){
		return d_classIndex;
	}
	
	public Feature getClassFeature(){
		return getFeatureAtIndex(getClassIndex());
	}
	
	public Feature getFeatureAtIndex(int a_i){
		return d_features.get(a_i);
	}
	
	public ArrayList<Feature> getFeatures(){
		return d_features;
	}
	
	public boolean checkInstanceCompliance(Instance a_instance) {
		Log.d(TAG, "checkInstanceCompliance");
		Log.d(TAG, "size instance: "+a_instance.size()+" size features: "+this.getFeatures().size());
		
		if (a_instance.size() != this.getFeatures().size()){
			return false;
		}
		
		for (int i=0; i<a_instance.size(); i++){
			Log.d(TAG, "instance type: "+a_instance.getValueAtIndex(i).getValueType()+" feature type: "+this.getFeatureAtIndex(i).getFeatureType());
			
			if (this.getFeatureAtIndex(i).getFeatureType() != a_instance.getValueAtIndex(i).getValueType()
					&& (a_instance.getValueAtIndex(i).getValueType() != Value.MISSING_VALUE)){
				return false;
			}
		}
		return true;	
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}
}
