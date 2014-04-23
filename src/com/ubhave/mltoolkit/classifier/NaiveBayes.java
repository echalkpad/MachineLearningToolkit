/*******************************************************************************
 * Copyright (c) 2013, University of Birmingham, UK
 * Veljko Pejovic,  <v.pejovic@cs.bham.ac.uk>
 * 
 * 
 * This library was developed as part of the EPSRC Ubhave (Ubiquitous and Social
 * Computing for Positive Behaviour Change) Project. For more information, please visit
 * http://www.ubhave.org
 * 
 * Permission to use, copy, modify, and/or distribute this software for any purpose with
 * or without fee is hereby granted, provided that the above copyright notice and this
 * permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 ******************************************************************************/
package com.ubhave.mltoolkit.classifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.ubhave.mltoolkit.utils.ClassifierConfig;
import com.ubhave.mltoolkit.utils.Constants;
import com.ubhave.mltoolkit.utils.Feature;
import com.ubhave.mltoolkit.utils.Instance;
import com.ubhave.mltoolkit.utils.MLException;
import com.ubhave.mltoolkit.utils.Signature;
import com.ubhave.mltoolkit.utils.Value;

// TODO: check if there is a distinction between the class feature and other features
// Do we need such a distinction?

/**
 * Naive Bayesian classifier that supports both nominal and numeric attributes.
 * Numeric features are modelled with a Gaussian distribution. 
 * Laplace smoothing is supported for nominal attributes, so that classes with
 * high preference for a single value do not overfit. 
 * The classifier is an online classifier, i.e. training can happen iteratively. 
 * 
 * @author Veljko Pejovic, University of Birmingham, UK <v.pejovic@cs.bham.ac.uk>
 *
 */
public class NaiveBayes extends Classifier implements OnlineClassifier {

	private static final String TAG = "NaiveBayes";
	
	private static Object d_lock = new Object();
	
	// For each feature we hold the count of occurrences of every class variable value.
	// These are further bisected to the feature values in case of NOMINAL features.
	// For NUMERIC features we keep stats necessary for Gaussian distribution calculation.
	private HashMap<String, HashMap<String, double[]>> d_valueCounts;

    // Holds the number of occurrences of each value that the class variable may take.
	private double[] d_classCounts;
	
    // Fixes the problem of too few occurrences in certain bins.
	private boolean d_LaplaceSmoothing;
	
	public NaiveBayes(Signature a_signature, ClassifierConfig a_config) {
		super(a_signature, a_config);		
		d_type = Constants.TYPE_NAIVE_BAYES;
		
		if (a_config.containsParam(Constants.LAPLACE_SMOOTHING)) {
			d_LaplaceSmoothing = (Boolean) a_config.getParam(Constants.LAPLACE_SMOOTHING);
		} else {
			d_LaplaceSmoothing = Constants.DEFAULT_LAPLACE_SMOOTHING;
		}
		
		initialize();
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
				//Log.d(TAG, "Feature counts put for class value "+classValue);
			}
			String output = "Feature counts put for "+feature.name()+": ";
			for (String key : featureCount.keySet()) {
					String miniOutput = "";
					double classFeatureCountsTest[] = featureCount.get(key);
					for (int i=0; i<classFeatureCountsTest.length; i++) {
						miniOutput += classFeatureCountsTest[i]+",";
					}
				
					output +="{ "+key+", ["+miniOutput+"]},";					
			}
			Log.d(TAG, output);
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
		
		if (classValue.getValueType() == Value.NUMERIC_VALUE &&
				classValue.getValueType() == Value.MISSING_VALUE){
			throw new MLException(MLException.INCOMPATIBLE_FEATURE_TYPE, 
					"Class variable has to be of type NOMINAL.");
		}
		
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
				String output = "Update:"+d_signature.getFeatureAtIndex(i).name()
						+"["+(String) classValue.getValue()+"] = {";
				for (int j=0; j<classFeatureCounts.length; j++) {
					output += classFeatureCounts[j]+",";
				}
				Log.d(TAG, output + "}");
			}
			if (featureValue.getValueType() == Value.NUMERIC_VALUE){
				classFeatureCounts[0] += 1; // count				
				classFeatureCounts[1] += (Double)featureValue.getValue(); // value sum
				classFeatureCounts[2] += Math.pow((Double)featureValue.getValue(),2); // value square sum
				Log.d(TAG, "Update:"+d_signature.getFeatureAtIndex(i).name()
						+"["+(String) classValue.getValue()+"] = "
						+"{"+classFeatureCounts[0]+","+classFeatureCounts[1]+","+classFeatureCounts[2]+"}");
			}
			// Do nothing for a missing value.
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
	
	
	public double[] getDistribution(Instance a_instance) throws MLException {
		
		if (!d_signature.checkInstanceCompliance(a_instance)){
			throw new MLException(MLException.INCOMPATIBLE_INSTANCE, 
					"Instance is not compatible with the dataset used for classifier construction.");					
		}
		
		Feature classFeature = d_signature.getClassFeature(); 
		ArrayList<String> classValues = classFeature.getValues();
		double[] classPriors = new double[classValues.size()];
		double[] classPosteriors = new double[classValues.size()];
		
		int classCountsTotal = 0;
		for (int j=0; j<d_classCounts.length; j++) classCountsTotal += d_classCounts[j];
		for (int j=0; j<classPriors.length; j++) {
			if (classCountsTotal == 0) {
				classPriors[j] = 1.0/classPriors.length; 
			} else {
				classPriors[j] = d_classCounts[j]/classCountsTotal;
			}
			classPosteriors[j] = classPriors[j];
			
		}
		
		Log.d(TAG, "Class values: "+classValues);
		String outputPrior="[";
		for (int i=0; i<classPriors.length;i++) {
			outputPrior += classPriors[i]+",";
		}
		Log.d(TAG, "Class priors: "+outputPrior);
		
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
					/*String printFeatureCounts = "Class feature counts: ";
					for (int j=0; j<classFeatureCounts.length; j++) {
						classFeatureTotal+=classFeatureCounts[j];
						printFeatureCounts += classFeatureCounts[j]+", ";
					}
					Log.d(TAG, printFeatureCounts);
					Log.d(TAG, "Feature value "+featureValue.getValue().toString()+" index: "+featureValueIndex);
					*/
					int featureValueIndex = feature.indexOfCategory((String) featureValue.getValue());	
					
					if (d_LaplaceSmoothing){
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
					double featureValueDouble = (Double)featureValue.getValue();
					
					if (classFeatureCounts[0] > 0) {
						mean = classFeatureCounts[1]/classFeatureCounts[0];
						stdDev = Math.sqrt(classFeatureCounts[2] - Math.pow(mean,2));
						normalProbability = Math.exp(Math.pow(featureValueDouble - mean ,2))/(2*stdDev*Math.sqrt(2*Math.PI));
						// TODO: if the current value equals the mean the normal probability goes to infinity;
						// to prevent this we, cap it to 1.0.
						if (Double.isInfinite(normalProbability)) normalProbability = 1.0;
						
					}
					
					/*Log.d(TAG, "calc for: "+ classValue
							+" and " + feature.name()
							+" resulting probability "+normalProbability+" total "+classPosteriors[indexOfClassValue]);
					*/
					classPosteriors[indexOfClassValue] *= normalProbability;
				}
			}
		}
		
		String outputPosterior="[";
		for (int i=0; i<classPosteriors.length;i++) {
			outputPosterior += classPosteriors[i]+",";
		}
		Log.d(TAG, "Class posteriors: "+outputPosterior);
		
		return classPosteriors;
	}


	@Override
	public Value classify(Instance a_instance) throws MLException {
		synchronized (d_lock)
		{
			double[] classDistribution = getDistribution(a_instance);
			double maxAposteriori = 0;
			int maxAposterioriIndex = -1;
			for (int i=0; i<classDistribution.length; i++){
				if (classDistribution[i] > maxAposteriori){
					maxAposteriori = classDistribution[i];
					maxAposterioriIndex = i;
				}
			}
			
			// When the classifier is not yet trained we return the first class value
			if (maxAposterioriIndex == -1) {
				maxAposterioriIndex = 0;
			}
			
			Value maxClass = new Value(d_signature.getClassFeature().categoryOfIndex(maxAposterioriIndex), 
					Value.NOMINAL_VALUE);
			return maxClass;
		}
	}

	@Override
	public void printClassifierInfo() {
		Log.d(TAG, "Classifer type: "+d_type);
		Log.d(TAG, "Signature: "+d_signature.toString());
		Log.d(TAG, "Class counts: "+d_classCounts);
		Log.d(TAG, "Value counts: "+d_valueCounts);
	}
}
