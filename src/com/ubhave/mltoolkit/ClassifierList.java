package com.ubhave.mltoolkit;

import java.util.HashMap;
import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import com.ubhave.mltoolkit.classifier.Classifier;
import com.ubhave.mltoolkit.classifier.NaiveBayes;
import com.ubhave.mltoolkit.classifier.ZeroR;
import com.ubhave.mltoolkit.utils.Constants;
import com.ubhave.mltoolkit.utils.Instance;
import com.ubhave.mltoolkit.utils.MLException;
import com.ubhave.mltoolkit.utils.Signature;
import com.ubhave.mltoolkit.utils.Value;

import android.nfc.Tag;
import android.util.Log;
import android.util.SparseArray;

/**
 * Takes care of classifier instantiation and registration.
 * Every classifier that's created has a unique name. 
 *
 */
public class ClassifierList {
	
   /* @SerializedName("classifier_map")	
	private final SparseArray<Classifier> d_classifierMap;*/

	private static final String TAG = "ClassifierList";
	
    @SerializedName("named_classifiers")
	private HashMap<String, Classifier> d_namedClassifiers;

	private transient final Random d_keyGenerator;
	
	public ClassifierList(){
		//d_classifierMap = new SparseArray<Classifier>();
		d_namedClassifiers = new HashMap<String, Classifier>();
		d_keyGenerator = new Random();	
	}
	
	private static Classifier createClassifier(int a_type, Signature a_signature){
		
		Log.d(TAG, "createClassifier");

		switch (a_type) {
			case Constants.TYPE_NAIVE_BAYES:
				Log.d(TAG, "create NaiveBayes");
				// TODO we use laplace smoothing here
				return new NaiveBayes(a_signature, true);
			case Constants.TYPE_ZERO_R:
				Log.d(TAG, "create ZeroR");
				return new ZeroR(a_signature);
			default:
				Log.d(TAG, "createDefault");
				return new NaiveBayes(a_signature);		
		}
	}

	public void removeClassifier(String a_classifierID)
	{
		
		if (d_namedClassifiers.containsKey(a_classifierID))
		{
			d_namedClassifiers.remove(a_classifierID);
			//c.kill();			
			//d_classifierMap.delete(a_classifierID);
		}
	}

	public Classifier getClassifier(String a_classifierID)
	{
		if (d_namedClassifiers.containsKey(a_classifierID)) {
			return d_namedClassifiers.get(a_classifierID);
		} else {
			return null;
		}
			
		//return d_classifierMap.get(a_classifierID);
	}
	
	/*
	public void removeClassifier(int a_classifierID)
	{
		Classifier c = d_classifierMap.get(a_classifierID);
		if (c != null)
		{
			c.kill();
			d_classifierMap.delete(a_classifierID);
		}
	}

	public Classifier getClassifier(int a_classifierID)
	{
		return d_classifierMap.get(a_classifierID);
	}
	*/
	
	public synchronized Classifier addClassifier(int a_type, Signature a_signature, String a_name) throws MLException{
		Log.d(TAG, "addClassifier");
		Classifier classifier = createClassifier(a_type, a_signature);
		//int classifierID = randomKey();
		//d_classifierMap.append(classifierID, classifier);
		d_namedClassifiers.put(a_name, classifier);
		return classifier;
	}
	
	/*
	private int randomKey() throws MLException
	{
		int classifierID = d_keyGenerator.nextInt();
		int loopCount = 0;
		while (d_classifierMap.get(classifierID) != null)
		{
			if (loopCount > 1000)
			{
				throw new MLException(MLException.INVALID_STATE, "Listener map >1000 key conflicts.");
			}
			classifierID = d_keyGenerator.nextInt();
			loopCount++;
		}
		return classifierID;
	}*/
}
