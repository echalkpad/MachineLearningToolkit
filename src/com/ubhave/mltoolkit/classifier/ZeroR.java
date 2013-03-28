package com.ubhave.mltoolkit.classifier;

import java.util.ArrayList;
import java.util.Arrays;

import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.ubhave.mltoolkit.utils.Feature;
import com.ubhave.mltoolkit.utils.Instance;
import com.ubhave.mltoolkit.utils.MLException;
import com.ubhave.mltoolkit.utils.Signature;
import com.ubhave.mltoolkit.utils.Value;

/**
 * ZeroR classifier is not taking any features into account during the classification.
 * It merely outputs the mean value/most frequent class. 
 * Besides classification, we can use ZeroR for regression.
 * 
 */
public class ZeroR extends Classifier implements OnlineClassifier {

	private static final String TAG = "ZeroR";

    // Holds the number of occurrences of each value that the class variable may take.
    @SerializedName("class_counts")
	private double[] d_classCounts;
    
	private static Object d_lock = new Object();
	
	public ZeroR(Signature a_signature) {
		super(a_signature);
		Feature classFeature = d_signature.getClassFeature(); 
		if (classFeature.getFeatureType() == Feature.NOMINAL)
			d_classCounts = new double[classFeature.numberOfCategories()];		
		else if (classFeature.getFeatureType() == Feature.NUMERIC) 
			d_classCounts = new double[2];
		Arrays.fill(d_classCounts, 0.0);
	}

	@Override
	public void update(Instance a_instance) throws MLException {
		
		if (!d_signature.checkInstanceCompliance(a_instance)){
			throw new MLException(MLException.INCOMPATIBLE_INSTANCE, 
					"Instance is not compatible with the dataset used for classifier construction.");					
		}
		
		Feature classFeature = d_signature.getClassFeature();
		
		Value classValue = a_instance.getValueAtIndex(d_signature.getClassIndex());
		
		if (classFeature.getFeatureType() == Feature.NOMINAL) {
			int classValueInt = classFeature.indexOfCategory((String) classValue.getValue());		
			d_classCounts[classValueInt] += 1;
		} else if (classFeature.getFeatureType() == Feature.NUMERIC) {
			d_classCounts[0] += (Float) classValue.getValue();
			d_classCounts[1] += 1;
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

	@Override
	public Value classify(Instance a_instance) {
		
		if (d_signature.getClassFeature().getFeatureType() == Feature.NOMINAL) {
			double maxCount = 0;
			int maxValueIndex = 0;
			
			for (int i=0; i<d_classCounts.length; i++) {
				Log.d(TAG, "Class value index "+i+" count "+d_classCounts[i]);
				if (d_classCounts[i] > maxCount) {
					maxValueIndex = i;
					maxCount = d_classCounts[i];
				}
			}	
			Value maxClass = new Value(d_signature.getClassFeature().categoryOfIndex(maxValueIndex), 
					Value.NOMINAL_VALUE);
			
			return maxClass;
		} else { //it's NUMERIC
			double mean = d_classCounts[0]/d_classCounts[1];
			Value maxClass = new Value(mean, Value.NUMERIC_VALUE);
			return maxClass;
		}
	}
}
