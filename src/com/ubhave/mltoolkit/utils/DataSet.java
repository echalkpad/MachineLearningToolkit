package com.ubhave.mltoolkit.utils;

import java.util.ArrayList;

public class DataSet {
	
	private ArrayList<Feature> d_features;
	
	private int d_classIndex;
	
	private ArrayList<Instance> d_instances;
	
	public DataSet(ArrayList<Feature> a_features, int a_classIndex){
		d_features = a_features;
		d_classIndex = a_classIndex;
	}
	
	public DataSet(ArrayList<Feature> a_features){
		this(a_features, a_features.size() - 1);
	}
	
	public void addInstance (Instance a_instance) throws MLException {
		if (!checkInstanceCompliance(a_instance)){
			throw new MLException(MLException.INCOMPATIBLE_INSTANCE, "Instance is not compatible with the dataset.");
		}
		d_instances.add(a_instance);
	}

	public void addInstances(Instance[] a_instances) throws MLException{
		for (Instance a_instance : a_instances){
			addInstance(a_instance);
		}
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
		return new ArrayList<Feature>();
	}
	
	public boolean checkInstanceCompliance(Instance a_instance) {
		if (a_instance.size() != this.getFeatures().size()){
			return false;
		}
		for (int i=0; i<a_instance.size(); i++){
			if (this.getFeatureAtIndex(i).getFeatureType() != a_instance.getValueAtIndex(i).getValueType()
					&& (a_instance.getValueAtIndex(i).getValueType() != Value.MISSING_VALUE)){
				return false;
			}
		}
		return true;	
	}
}
