package com.ubhave.mltoolkit.classifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.ubhave.mltoolkit.utils.Feature;
import com.ubhave.mltoolkit.utils.Instance;
import com.ubhave.mltoolkit.utils.MLException;
import com.ubhave.mltoolkit.utils.Signature;
import com.ubhave.mltoolkit.utils.Value;

public class NaiveBayes extends Classifier implements OnlineClassifier {

	private static final String TAG = "NaiveBayes";
	
	private static Object d_lock = new Object();
	
    @SerializedName("value_counts")
	private HashMap<String, HashMap<String, double[]>> d_valueCounts;

    @SerializedName("class_counts")
	private double[] d_classCounts;
	
    @SerializedName("laplace_smoothing")
	private boolean d_LaplaceSmoothing;
	
	
	public NaiveBayes(Signature a_signature) {
		super(a_signature);
		initialize();
	}
	
	public NaiveBayes(Signature a_signature, boolean a_Laplace) {
		this(a_signature);
		d_LaplaceSmoothing = a_Laplace;
	}
	
	public void initialize() {
		
		d_valueCounts = new HashMap<String, HashMap<String, double[]>>();
		ArrayList<Feature> features = d_signature.getFeatures();
		Feature classFeature = d_signature.getClassFeature(); 
		ArrayList<String> classValues = classFeature.getValues();

		d_classCounts = new double[classFeature.numberOfCategories()];	
		Arrays.fill(d_classCounts, 0.0);

		for(Feature feature : features){			
			HashMap<String, double[]> featureCount = new HashMap<String, double[]>();
		
			for (String classValue : classValues){										
				double[] classFeatureCounts = null;
				if (feature.getFeatureType() == Feature.NOMINAL) { 
					classFeatureCounts = new double[feature.numberOfCategories()];
				}
				else if (feature.getFeatureType() == Feature.NUMERIC) {
					// For NUMERIC values we have to keep:
					// - count
					// - sum of values
					// - sum of square values 
					// so that we can get the normal distribution in the end
					classFeatureCounts = new double[3];
				}else{
					Log.d(TAG, "Empty value. Skipped");
				}
				
				Arrays.fill(classFeatureCounts, 0.0);
				featureCount.put(classValue, classFeatureCounts);
				Log.d(TAG, "Feature counts put for class value "+classValue);
			}
			Log.d(TAG, "Feature counts put for "+feature.name());
			d_valueCounts.put(feature.name(), featureCount);
		}
	}


	public void update(Instance a_instance) throws MLException {
				
		if (!d_signature.checkInstanceCompliance(a_instance)){
			throw new MLException(MLException.INCOMPATIBLE_INSTANCE, 
					"Instance is not compatible with the dataset used for classifier construction.");					
		}

		Feature classFeature = d_signature.getClassFeature();
		
		Value classValue = a_instance.getValueAtIndex(d_signature.getClassIndex());
		
		if (classValue.getValueType() == Value.MISSING_VALUE){
			//TODO: no class specified
			return;
		}
		if (classValue.getValueType() == Value.NUMERIC_VALUE){
			throw new MLException(MLException.INCOMPATIBLE_FEATURE_TYPE, 
					"Class variable has to be of type NOMINAL.");
		}
		
		Log.d(TAG, "Class value: "+(String) classValue.getValue());
		Log.d(TAG, "Available values: "+classFeature.getValues());
		int classValueInt = classFeature.indexOfCategory((String) classValue.getValue());
		
		d_classCounts[classValueInt] += 1;
		
		for (int i=0; i<a_instance.size(); i++){
			
			double[] classFeatureCounts = (d_valueCounts.get(d_signature.getFeatureAtIndex(i).name()))
					.get(classFeature.categoryOfIndex(classValueInt));
			Value featureValue = a_instance.getValueAtIndex(i);
			
			if (featureValue.getValueType() == Value.NOMINAL_VALUE){
				Feature currentFeature = d_signature.getFeatureAtIndex(i); 
				int featureValueCat = currentFeature.indexOfCategory((String) featureValue.getValue());				
				classFeatureCounts[featureValueCat] += 1;
			}
			if (featureValue.getValueType() == Value.NUMERIC_VALUE){
				classFeatureCounts[0] += 1; // count				
				classFeatureCounts[1] += (Float)featureValue.getValue(); // value sum
				classFeatureCounts[2] += Math.pow((Float)featureValue.getValue(),2); // value square sum
			}
			// Do nothing for a missing value
			Log.d(TAG, "Update for: "+(String) classValue.getValue()
					+" and " + d_signature.getFeatureAtIndex(i).name()
					+" to "+classFeatureCounts[0]+" and "+classFeatureCounts[1]);
		}
	}

	
	@Override
	public void train(ArrayList<Instance> a_instances) throws MLException {
		
		for(Instance a_instance : a_instances){
			synchronized (d_lock) {
				this.update(a_instance);
			}
		}
	}
	
	
	public double[] getDistribution(Instance a_instance) {
		
		Feature classFeature = d_signature.getClassFeature(); 
		ArrayList<String> classValues = classFeature.getValues();
		double[] classPriors = new double[classValues.size()];
		double[] classPosteriors = new double[classValues.size()];
		
		Log.d(TAG, "Class values: "+classValues);
		
		int classCountsTotal = 0;
		for (int j=0; j<d_classCounts.length; j++) classCountsTotal += d_classCounts[j];
		for (int j=0; j<classPriors.length; j++) {
			classPriors[j] = d_classCounts[j]/classCountsTotal;
			classPosteriors[j] = classPriors[j];
			Log.d(TAG, "Class priors: "+classPriors[j]);
		}
		
		for (int i=0; i<a_instance.size(); i++){
			Value featureValue = a_instance.getValueAtIndex(i);
			Feature feature = d_signature.getFeatureAtIndex(i);
			// for every feature (a specific value of it) we get a prob of each class
			double[] classFeatureProbs = new double[classValues.size()];
			
			// A map of all class values -> feature counts
			HashMap<String, double[]> featureCounts = d_valueCounts.get(feature.name());

		
			for (String classValue : classValues){
				
				double[] classFeatureCounts = featureCounts.get(classValue);		
				int indexOfClassValue = classFeature.indexOfCategory(classValue);
				double classFeatureTotal = 0;
				
				if (featureValue.getValueType() == Value.NOMINAL_VALUE) {
					for (int j=0; j<classFeatureCounts.length; j++) {
						classFeatureTotal+=classFeatureCounts[j];
						Log.d(TAG, "Class feature counts: "+classFeatureCounts[j]);
					}
					
					Log.d(TAG, "Feature value: "+((Float)featureValue.getValue()).toString());
					// In case we haven't trained for large values we need to cap this.
					
					// TODO: check if this is OK for nominal, perhaps we need to do reverse lookup
					int featureValueIndex = (Integer)featureValue.getValue();					
					
					Log.d(TAG, "Feature value index: "+featureValueIndex);
							
					if (d_LaplaceSmoothing){
						// TODO: fix this for numerical features!
						classFeatureProbs[indexOfClassValue]=classFeatureCounts[featureValueIndex]+1/
								(classFeatureTotal + feature.numberOfCategories());					
					}
					else {
						if (classFeatureTotal > 0){
							classFeatureProbs[indexOfClassValue]=classFeatureCounts[featureValueIndex]/classFeatureTotal;
						}
					}
					
					classPosteriors[indexOfClassValue] *= classFeatureProbs[indexOfClassValue];
					
				} else if (featureValue.getValueType() == Value.NUMERIC_VALUE) {
					
					double mean = 0;
					double stdDev = 0;
					double normalProbability = 0;
					double featureValueFloat = (Float)featureValue.getValue();
					
					if (classFeatureCounts[0] > 0) {
						mean = classFeatureCounts[1]/classFeatureCounts[0];
						stdDev = Math.sqrt(classFeatureCounts[2] - Math.pow(mean,2));
						normalProbability = Math.exp(Math.pow(featureValueFloat - mean ,2))/(2*stdDev*Math.sqrt(2*Math.PI));
					}
					
					Log.d(TAG, "calc for: "+ classValue
							+" and " + feature.name()
							+" resulting probability "+normalProbability+" total "+classPosteriors[indexOfClassValue]);
					
					classPosteriors[indexOfClassValue] *= normalProbability;
				}
			}
		}
		return classPosteriors;
	}


	@Override
	public Value classify(Instance a_instance) {
		synchronized (d_lock)
		{
			double[] classDistribution = getDistribution(a_instance);
			double maxAposteriori = 0;
			int maxAposterioriIndex = -1;
			for (int i=0; i<classDistribution.length; i++){
				Log.d(TAG, "Class distribution: "+classDistribution[i]);
				if (classDistribution[i] > maxAposteriori){
					maxAposteriori = classDistribution[i];
					maxAposterioriIndex = i;
				}
			}
			
			Value maxClass = new Value(d_signature.getClassFeature().categoryOfIndex(maxAposterioriIndex), 
					Value.NOMINAL_VALUE);
			return maxClass;
		}
	}
	

}
