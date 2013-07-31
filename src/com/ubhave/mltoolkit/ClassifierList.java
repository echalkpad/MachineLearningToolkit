package com.ubhave.mltoolkit;

import java.util.HashMap;
import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import com.ubhave.mltoolkit.classifier.Classifier;
import com.ubhave.mltoolkit.classifier.ID3;
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

	private static final String TAG = "ClassifierList";
	
	private HashMap<String, Classifier> d_namedClassifiers;

	private final Random d_keyGenerator;
	
	public ClassifierList(){
		//d_classifierMap = new SparseArray<Classifier>();
		Log.d(TAG, "ClassifierList empty constructor");
		d_namedClassifiers = new HashMap<String, Classifier>();
		d_keyGenerator = new Random();	
	}
	
	private static Classifier createClassifier(int a_type, Signature a_signature){
		
		Log.d(TAG, "createClassifier");

		switch (a_type) {
			case Constants.TYPE_NAIVE_BAYES:
				Log.d(TAG, "create NaiveBayes");
				return new NaiveBayes(a_signature, true);
			case Constants.TYPE_ID3:
				Log.d(TAG, "create ID3");
				return new ID3(a_signature);			
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
		}
	}

	public Classifier getClassifier(String a_classifierID)
	{
		if (d_namedClassifiers.containsKey(a_classifierID)) {
			return d_namedClassifiers.get(a_classifierID);
		} else {
			return null;
		}
	}	
	
	public synchronized Classifier addClassifier(int a_type, Signature a_signature, String a_name) throws MLException{
		Log.d(TAG, "addClassifier");
		Classifier classifier = createClassifier(a_type, a_signature);
		d_namedClassifiers.put(a_name, classifier);
		return classifier;
	}
		
}
