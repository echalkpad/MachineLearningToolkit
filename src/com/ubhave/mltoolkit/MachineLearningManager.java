package com.ubhave.mltoolkit;

import com.ubhave.mltoolkit.classifier.Classifier;
import com.ubhave.mltoolkit.utils.MLException;
import com.ubhave.mltoolkit.utils.Signature;


import android.content.Context;
/**
 * Takes care of running classifiers as a service.
 * Deals with instantiating classifiers, sending training/test data
 * to the right classifier, namely the one that an application has instantiated earlier.
 * Makes sure that the state is preserved throughout the application lifetime and beyond.
 * It has to save the state of the classifier when the service is shut down. 
 * It should also be able to save the classifier state on the persistent storage. 
 * Finally, this class should enable remote classifier loading and uploading to a server.
 * 
 * @author Veljko Pejovic (v.pejovic@cs.bham.ac.uk)
 *
 */

// TODO: perhaps we need some settings params
public class MachineLearningManager {
	
	private static final String TAG = "MLManager";
	
	private static MachineLearningManager d_manager;
	
	private final ClassifierList d_classifiers;

	private static Object d_lock = new Object();
	
	private final Context d_context;
	
	public static MachineLearningManager getMLManager(Context a_context) throws MLException{
		
		if (a_context == null) {
			throw new MLException(MLException.INVALID_PARAMETER, " Invalid parameter, context object passed is null");
		}
		if (d_manager == null)
		{
			synchronized (d_lock)
			{
				if (d_manager == null)
				{
					d_manager = new MachineLearningManager(a_context);
				}
			}
		}
		return d_manager;
	}
	
	private MachineLearningManager(Context a_context){
		d_context = a_context;
		d_classifiers = new ClassifierList();
	}
	
	public int addClassifier(int a_type, Signature a_signature) throws MLException{
		Classifier classifier = ClassifierList.createClassifier(a_type, a_signature);
		return d_classifiers.addClassifier(classifier);
	}
	
	public void removeClassifier(int a_classifierID){
		d_classifiers.removeClassifier(a_classifierID);
	}
	
	public Classifier getClassifier(int a_classifierID){
		return d_classifiers.getClassifier(a_classifierID);		
	}
}
