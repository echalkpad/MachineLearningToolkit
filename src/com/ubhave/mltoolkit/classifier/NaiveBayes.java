package com.ubhave.mltoolkit.classifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import com.ubhave.mltoolkit.utils.DataSet;
import com.ubhave.mltoolkit.utils.Feature;
import com.ubhave.mltoolkit.utils.Instance;
import com.ubhave.mltoolkit.utils.MLException;
import com.ubhave.mltoolkit.utils.Value;

public class NaiveBayes extends Classifier implements OnlineClassifier {

	private static final long serialVersionUID = 6727156425109236600L;
		
	private DataSet d_dataSet;
	
	private HashMap<String, HashMap<String, double[]>> d_valueCounts;

	private double[] d_classCounts;
	
	private boolean d_LaplaceSmoothing;
	
	public NaiveBayes(DataSet a_dataSet) {
		initialize(a_dataSet);
	}
	
	public NaiveBayes(DataSet a_dataSet, boolean a_Laplace) {
		this(a_dataSet);
		d_LaplaceSmoothing = a_Laplace;
	}
	
	public void initialize(DataSet a_dataSet) {
		d_dataSet = a_dataSet;
		d_valueCounts = new HashMap<String, HashMap<String, double[]>>();
		ArrayList<Feature> features = d_dataSet.getFeatures();
		Feature classFeature = d_dataSet.getClassFeature(); 
		ArrayList<String> classValues = classFeature.getValues();

		d_classCounts = new double[classFeature.numberOfCategories()];	
		Arrays.fill(d_classCounts, 0.0);
				
		for(Feature feature : features){			
			HashMap<String, double[]> featureCount = new HashMap<String, double[]>();
		
			for (String classValue : classValues){										
				double[] classFeatureCounts;
				if (feature.getFeatureType() == Feature.NOMINAL) 
					classFeatureCounts = new double[feature.numberOfCategories()];
				else 
					// We keep the count and the aggregate value for NUMERIC features
					classFeatureCounts = new double[feature.numberOfCategories()+1];
				
				Arrays.fill(classFeatureCounts, 0.0);
				featureCount.put(classValue, classFeatureCounts);
			}
			d_valueCounts.put(feature.name(), featureCount);
		}
	}


	public void update(Instance a_instance) throws MLException {
		
		if (!d_dataSet.checkInstanceCompliance(a_instance)){
			throw new MLException(MLException.INCOMPATIBLE_INSTANCE, 
					"Instance is not compatible with the dataset used for classifier construction.");					
		}

		Feature classFeature = d_dataSet.getClassFeature();
		Value classValue = a_instance.getValueAtIndex(d_dataSet.getClassIndex());
		if (classValue.getValueType() == Value.MISSING_VALUE){
			//TODO: no class specified
			return;
		}
		if (classValue.getValueType() == Value.NUMERIC_VALUE){
			throw new MLException(MLException.INCOMPATIBLE_FEATURE_TYPE, 
					"Class variable is of type NOMINAL.");
		}
		int classValueInt = (Integer) classValue.getValue();
		
		d_classCounts[classValueInt] += 1;
		
		for (int i=0; i<a_instance.size(); i++){
			double[] classFeatureCounts = (d_valueCounts.get(d_dataSet.getFeatureAtIndex(i).name()))
					.get(classFeature.categoryOfIndex(classValueInt));
			Value featureValue = a_instance.getValueAtIndex(i);
			
			if (featureValue.getValueType() == Value.NOMINAL_VALUE){
				classFeatureCounts[(Integer)featureValue.getValue()] += 1;
			}
			if (featureValue.getValueType() == Value.NUMERIC_VALUE){
				classFeatureCounts[0] += (Integer)featureValue.getValue();
				classFeatureCounts[1] += 1;
			}
			// Do nothing for a missing value
		}
	}

	
	@Override
	public void train(Instance[] a_instances) throws MLException {
		
		for(Instance a_instance : a_instances){
			this.update(a_instance);
		}
	}
	
	
	public double[] getDistribution(Instance a_instance) {
		ArrayList<Feature> features = d_dataSet.getFeatures();
		Feature classFeature = d_dataSet.getClassFeature(); 
		ArrayList<String> classValues = classFeature.getValues();
		double[] classPriors = new double[classValues.size()];
		double[] classPosteriors = new double[classValues.size()];
		
		int classCountsTotal = 0;
		for (int j=0; j<d_classCounts.length; j++) classCountsTotal += d_classCounts[j];
		for (int j=0; j<classPriors.length; j++) classPriors[j] = d_classCounts[j]/classCountsTotal;
		
		for (int i=0; i<a_instance.size(); i++){
			Value featureValue = a_instance.getValueAtIndex(i);
			Feature feature = d_dataSet.getFeatureAtIndex(i);
			// for every feature (a specific value of it) we get a prob of each class
			double[] classFeatureProbs = new double[classValues.size()];
			
			HashMap<String, double[]> featureCounts = d_valueCounts.get(d_dataSet.getFeatureAtIndex(i).name());

			// Assume NOMINAL for now			
			for (String classValue : classValues){
				double[] classFeatureCounts = featureCounts.get(classValue);		
				double classFeatureTotal = 0;
				for (int j=0; j<classFeatureCounts.length; j++) classFeatureTotal+=classFeatureCounts[j];
				int indexOfClassValue = classFeature.indexOfCategory(classValue);
				if (d_LaplaceSmoothing){
					classFeatureProbs[indexOfClassValue]=classFeatureCounts[(Integer)featureValue.getValue()]+1/
							(classFeatureTotal + feature.numberOfCategories());					
				}
				else {
					classFeatureProbs[indexOfClassValue]=classFeatureCounts[(Integer)featureValue.getValue()]/classFeatureTotal;
				}
				classPosteriors[indexOfClassValue] *= classFeatureProbs[indexOfClassValue];
			}
		}
		return classPosteriors;
	}


	@Override
	public Value classify(Instance a_instance) {
		double[] classDistribution = getDistribution(a_instance);
		double maxAposteriori = 0;
		int maxAposterioriIndex = -1;
		for (int i=0; i<classDistribution.length; i++){
			if (classDistribution[i] > maxAposteriori){
				maxAposteriori = classDistribution[i];
				maxAposterioriIndex = i;
			}
		}
		
		Value maxClass = new Value(d_dataSet.getClassFeature().categoryOfIndex(maxAposterioriIndex), 
				Value.NOMINAL_VALUE);
		return maxClass;
	}
	

}
