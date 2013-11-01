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
package com.ubhave.mltoolkit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import com.ubhave.mltoolkit.classifier.Classifier;
import com.ubhave.mltoolkit.classifier.ID3;
import com.ubhave.mltoolkit.classifier.NaiveBayes;
import com.ubhave.mltoolkit.classifier.ZeroR;
import com.ubhave.mltoolkit.utils.Constants;
import com.ubhave.mltoolkit.utils.MLException;
import com.ubhave.mltoolkit.utils.Signature;

// TODO: What are some of the settings that we can use here? 
// How should we load them in the manager?

/**
 * Deals with instantiating classifiers, sending training/test data
 * to the right classifier, namely the one that an application has instantiated earlier.
 * Has methods that allow it to save classifiers to a file, and load them from a file. 
 * This should be used when a service that uses the manager is (re)started/destroyed.  
 * 
 * Only one MachineLearningManager exists per context to ensure consistency.  
 * 
 * @author Veljko Pejovic, University of Birmingham, UK <v.pejovic@cs.bham.ac.uk>
 *
 */
public class MachineLearningManager {
	
	private static final String TAG = "MLManager";
	
	private static MachineLearningManager d_manager;
	
	private final ClassifierList d_classifiers;
	
	private static Object d_lock = new Object();
	
	private final Context d_context;
	
	public static MachineLearningManager getMLManager(Context a_context) throws MLException{
		
		if (a_context == null) {
			throw new MLException(MLException.INVALID_PARAMETER, 
					" Invalid parameter, context object passed is null");
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
		// automatic loading if classifiers exist on the device
		if (Arrays.asList(d_context.fileList()).contains(Constants.CLASSIFIER_STORAGE_FILE)){	
			d_classifiers = loadFromPersistent();
		}
		else{
			d_classifiers = new ClassifierList();
		}
	}
	
	public Classifier addClassifier(int a_type, Signature a_signature, String a_name) throws MLException{
		Log.d(TAG, "addClassifier");

		Classifier cls = d_classifiers.getClassifier(a_name);
		
		// TODO: Expose classifier properties so that we can check 
		// if the existing classifier is the same as the one we require.
		if (cls != null) {
			Log.d(TAG, "return existing classifier");
			return cls;
		}
		//throw new MLException(MLException.CLASSIFIER_EXISTS, "Classifier "+a_name+" already exists.");
		Log.d(TAG, "return brand new classifier");
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
	
	public void saveToPersistentExternal(String a_filename) {
		Gson gson = new Gson();
		String JSONstring = gson.toJson(d_classifiers);
	
		try {
			String root = Environment.getExternalStorageDirectory().toString();
			File file = new File(root + a_filename);  			
			FileOutputStream fos = new FileOutputStream(file);		
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
	
	public ClassifierList loadFromExternalPersistent(String a_filename) {
		
		Log.d(TAG, "loadFromExternalPersistent");
		
		StringBuilder JSONstring = new StringBuilder();
		
		try {
			File sdcard = Environment.getExternalStorageDirectory();
			File file = new File(sdcard,a_filename);
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			
			String line;
			while ((line = br.readLine()) != null) {
				JSONstring.append(line);
				Log.d(TAG, "Read "+line);
			}
			br.close();			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Gson gson = new GsonBuilder()
			.registerTypeHierarchyAdapter(Classifier.class, new ClassifierAdapter())
			.create();
		
		ClassifierList list = (ClassifierList) gson.fromJson(JSONstring.toString(), ClassifierList.class);
		return list;
	}
	
	public ClassifierList loadFromPersistent() {

		Log.d(TAG, "loadFromPersistent");
		
		StringBuilder JSONstring = new StringBuilder();
		
		try {
			FileInputStream is = d_context.openFileInput(Constants.CLASSIFIER_STORAGE_FILE);			
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			
			String line;
			while ((line = br.readLine()) != null) {
				JSONstring.append(line);
				Log.d(TAG, "Read "+line);
			}
			br.close();			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Gson gson = new GsonBuilder()
			.registerTypeHierarchyAdapter(Classifier.class, new ClassifierAdapter())
			.create();
		
		ClassifierList list = (ClassifierList) gson.fromJson(JSONstring.toString(), ClassifierList.class);
		return list;
	}
	
	static class ClassifierAdapter implements JsonDeserializer<Classifier> {
		
		Gson gson;		
		
		ClassifierAdapter(){
			GsonBuilder gsonBuilder = new GsonBuilder();
		    gson = gsonBuilder.create();		    
		}
		
		public Classifier deserialize(JsonElement a_elem, Type a_type, JsonDeserializationContext a_context) throws JsonParseException {
			Log.d(TAG, "Deserialize a classifier");
			Classifier result = null;
			
			JsonObject object = a_elem.getAsJsonObject();
			int type = object.get("d_type").getAsInt();
			Log.d(TAG, "Classifier type "+type);
			switch(type){
				case Constants.TYPE_NAIVE_BAYES:
					result = gson.fromJson(a_elem, NaiveBayes.class);					
					break;
				case Constants.TYPE_ID3:
					result = gson.fromJson(a_elem, ID3.class);
					break;
				case Constants.TYPE_ZERO_R:
					result = gson.fromJson(a_elem, ZeroR.class);
					break;
			}
			result.printClassifierInfo();
			return result;
		}
	}
}
