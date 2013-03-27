package com.ubhave.mltoolkit;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import com.ubhave.mltoolkit.classifier.Classifier;
import com.ubhave.mltoolkit.utils.Constants;
import com.ubhave.mltoolkit.utils.MLException;
import com.ubhave.mltoolkit.utils.Signature;

/**
 * Deals with instantiating classifiers, sending training/test data
 * to the right classifier, namely the one that an application has instantiated earlier.
 * Makes sure that the state is preserved throughout the application lifetime and beyond.
 * It has to save the state of the classifier when the service is shut down. 
 * It should also be able to save the classifier state on the persistent storage using GSON. 
 * Finally, this class should enable remote classifier loading and uploading to a server.
 * 
 * 
 */

// TODO: perhaps we need some settings params
// TODO: need to distinguish among classifier types when deserializing.

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
					try {
						d_manager = new MachineLearningManager(a_context);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}				
				}
			}
		}
		return d_manager;
	}
	
	private MachineLearningManager(Context a_context) throws IOException{
		d_context = a_context;
		//if (Arrays.asList(d_context.fileList()).contains(Constants.CLASSIFIER_STORAGE_FILE)){	
		//	d_classifiers = loadFromPersistent();	
		//}
		//else{
			d_classifiers = new ClassifierList();
		//}
		
	}
	
	public Classifier addClassifier(int a_type, Signature a_signature, String a_name) throws MLException{
		Log.d(TAG, "addClassifier");

		if (d_classifiers.getClassifier(a_name) != null) 
			throw new MLException(MLException.CLASSIFIER_EXISTS, "Classifier "+a_name+" already exists.");
		return d_classifiers.addClassifier(a_type, a_signature, a_name);
	}
	
	public void removeClassifier(String a_classifierID){
		d_classifiers.removeClassifier(a_classifierID);
	}
	
	/*public Classifier getClassifier(int a_classifierID){
		return d_classifiers.getClassifier(a_classifierID);		
	}*/
	
	// This will return null in case there is no such classifier
	public Classifier getClassifier(String a_classifierID){
		return d_classifiers.getClassifier(a_classifierID);		
	}
	
	public void saveToPersistent() {
		Gson gson = new Gson();
		String JSONstring = gson.toJson(d_classifiers);
	
		try {
			FileOutputStream fos = d_context.openFileOutput(Constants.CLASSIFIER_STORAGE_FILE, Context.MODE_PRIVATE);		
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			osw.write(JSONstring);
			osw.flush();
			osw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ClassifierList loadFromPersistent() {

		StringBuilder JSONstring = new StringBuilder();
		
		try {
			FileInputStream is = d_context.openFileInput(Constants.CLASSIFIER_STORAGE_FILE);			
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			
			String line;
			while ((line = br.readLine()) != null) {
				JSONstring.append(line);
			}
			br.close();			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Gson gson = new Gson();
		ClassifierList list = (ClassifierList) gson.fromJson(JSONstring.toString(), ClassifierList.class);
		return list;
	}
}
