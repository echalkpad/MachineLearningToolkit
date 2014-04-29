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
package com.ubhave.mltoolkit.utils;

import java.util.ArrayList;
import java.util.HashMap;

import android.util.Log;

/**
 * Signature defines features used by a classifier, their names and the 
 * index of the class feature. Instances of data (which consist of values) 
 * need to correspond to the signature of the classifier they are used for.
 * Signature exposes a method for checking that compliance. 
 * 
 * @author Veljko Pejovic, University of Birmingham, UK <v.pejovic@cs.bham.ac.uk>
 *
 */
public class Signature {

	private static final String TAG = "Signature";
	
	private ArrayList<Feature> d_features;
	
    private HashMap<String, Feature> d_namesFeatures;
    
    private int d_classIndex;		

	public Signature(ArrayList<Feature> a_features, int a_classIndex){
		d_namesFeatures = new HashMap<String, Feature>();
		d_features = a_features;
		d_classIndex = a_classIndex;
		for (Feature f : a_features) {
			d_namesFeatures.put(f.name(), f);
		}
	}
	
	public Signature(ArrayList<Feature> a_features){
		this(a_features, a_features.size() - 1);
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
		return d_features;
	}
	
	public int size() {
		return d_features.size();
	}
	
	public boolean checkCompliance(Instance a_instance, boolean a_training) {
		Log.d(TAG, "checkInstanceCompliance");
		int checkSize = a_instance.size();
		
		// Instances that are used for training should have the exact same features as the signature.
		// Those that are about to be classified, should have one feature less -- the class feature.
		if (!a_training) {
			checkSize++;
		}
		
		if (checkSize != this.getFeatures().size()){
			return false;
		}
		
		for (int i=0; i<a_instance.size(); i++){
			Log.d(TAG, "instance type: "+a_instance.getValueAtIndex(i).getValueType()+" feature type: "+this.getFeatureAtIndex(i).getFeatureType());
			
			if (this.getFeatureAtIndex(i).getFeatureType() != a_instance.getValueAtIndex(i).getValueType()
					&& (a_instance.getValueAtIndex(i).getValueType() != Value.MISSING_VALUE)){
				return false;
			}
		}
		return true;	
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}
}
