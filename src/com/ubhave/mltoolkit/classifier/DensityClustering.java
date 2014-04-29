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
import java.util.HashMap;
import java.util.Iterator;

import android.util.Log;

import com.ubhave.mltoolkit.utils.ClassifierConfig;
import com.ubhave.mltoolkit.utils.Constants;
import com.ubhave.mltoolkit.utils.Feature;
import com.ubhave.mltoolkit.utils.Instance;
import com.ubhave.mltoolkit.utils.MLException;
import com.ubhave.mltoolkit.utils.Signature;
import com.ubhave.mltoolkit.utils.Value;

/**
 * This classifier calculates centroids of labelled, clustered data instances.
 * The classifier first removes cluster outliers using the density method,
 * i.e. if less than a given percentage of other data instances are in the 
 * epsilon environment of a point, the point is removed as an outlier. 
 * Cluster centroids are then calculated. The classifier is not an online 
 * classifier, and is batch trained from given instances. 
 * 
 * At the classification time, an instance is given a label that corresponds
 * to the closest cluster centroid. The distance used for density and closeness
 * calculation is euclidean distance. In case only two nominal features exist, 
 * the classifier assumes that GPS coordinates are given, thus it adjusts the 
 * distance calculation according to the harvesine formula.
 * 
 * The classifier can only be instantiated with a nominal class feature and one 
 * or more numeric attribute features. 
 * 
 * LIMITATION: If no data are present for a label, the assigned centroid will 
 * have all zeros coordinates.
 * 
 * @author Veljko Pejovic, University of Birmingham, UK <v.pejovic@cs.bham.ac.uk>
 *
 */
public class DensityClustering extends Classifier {

	private static final String TAG = "DensityClustering";
	
	private HashMap<String,double[]> d_centroids;
	
	private HashMap<String,Integer> d_numTrains;
	
	private double d_maxDistance;
	
	private double d_minInclusionPct;	
	
	private static double toRad(double a_degree) {
		return Math.PI*a_degree/180.0;
	}
	
	public static double distance(final double[] a_coordsA,final double[] a_coordsB) throws MLException {
		
		if (a_coordsA.length != a_coordsB.length) {
			throw new MLException(MLException.INCOMPATIBLE_INSTANCE, 
					"Instance is not compatible with the dataset used for classifier construction.");					
		}
		
		// We assume GPS coordinates if vectors of size two are given
		if (a_coordsA.length == 2) {
			
			double lat1 = a_coordsA[0];
			double lon1 = a_coordsA[1];
			double lat2 = a_coordsB[0];
			double lon2 = a_coordsB[1];			
			double R = 6371.0;
			double dLat = toRad(lat2 - lat1);
			double dLon = toRad(lon2 - lon1);
			double radLat1 = toRad(lat1);
			double radLat2 = toRad(lat2);
			
			double a = Math.sin(dLat/2.0) * Math.sin(dLat/2.0) + 
					Math.sin(dLon/2.0) * Math.sin(dLon/2.0) * Math.cos(radLat1) * Math.cos(radLat2);
			double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
			
			return R * c * 1000.0;		
		} 
		// Otherwise Euclidean distance
		else {
			double sqrSum = 0;			
			for(int i=0; i<a_coordsA.length; i++) {
				sqrSum += Math.pow(a_coordsA[i] - a_coordsB[i], 2);				
			}
			return Math.sqrt(sqrSum);
		}		
	}
	
	public DensityClustering(Signature a_signature, ClassifierConfig a_config){
		
		super(a_signature, a_config);
		
		d_type = Constants.TYPE_DENSITY_CLUSTER;
		
		if (a_config.containsParam(Constants.MAX_CLUSTER_DISTANCE)) {
			d_maxDistance = (Double) a_config.getParam(Constants.MAX_CLUSTER_DISTANCE);
		} else {
			d_maxDistance = Constants.DEFAULT_MAX_CLUSTER_DISTANCE;
		}
		if (a_config.containsParam(Constants.MIN_INCLUSION_PERCENT)) {
			d_minInclusionPct = (Double) a_config.getParam(Constants.MIN_INCLUSION_PERCENT);
		} else {
			d_minInclusionPct = Constants.DEFAULT_MIN_INCLUSION_PERCENT;
		}
		
		Feature classFeature = a_signature.getClassFeature();
		ArrayList<String> classValues = classFeature.getValues();
		
		d_numTrains = new HashMap<String, Integer>();
		d_centroids = new HashMap<String, double[]>();
		for (String classValue : classValues) {
			d_centroids.put(classValue, new double[d_signature.size()-1]);
			d_numTrains.put(classValue, 0);
		}

	}

	@Override
	public void train(ArrayList<Instance> instances) throws MLException {
		//Log.d(TAG, "train with "+instances.size()+" instances");
		
		// Remove outliers (density based)
		String curLabel;
		double curCoordValues[] = new double[d_signature.size()-1];
		
		for (Iterator<Instance> curIter = instances.iterator(); curIter.hasNext();) {
		
			Instance curInstance = curIter.next();
			
			if (!d_signature.checkCompliance(curInstance, true)){
				throw new MLException(MLException.INCOMPATIBLE_INSTANCE, 
						"Instance is not compatible with the dataset used for classifier construction.");					
			}
			
			curLabel = (String) curInstance.getValueAtIndex(d_signature.getClassIndex()).getValue();
			
			for(int i=0; i<d_signature.size()-1; i++) {
				curCoordValues[i] = (Double) curInstance.getValueAtIndex(i).getValue();
			}
			
			String otherLabel;
			double otherCoordValues[] = new double[d_signature.size()-1];
			
			int total = 0;
    		int totalInside = 0;
    		
			for (Iterator<Instance> otherIter = instances.iterator(); otherIter.hasNext();) {
				
				Instance otherInstance = otherIter.next();
				
				if (otherInstance != curInstance) {
					
					if (!d_signature.checkCompliance(otherInstance, true)){
						throw new MLException(MLException.INCOMPATIBLE_INSTANCE, 
								"Instance is not compatible with the dataset used for classifier construction.");					
					}
					
					otherLabel = (String) otherInstance.getValueAtIndex(d_signature.getClassIndex()).getValue();
					
					if (otherLabel.equals(curLabel)) {
					
						for(int i=0; i<d_signature.size()-1; i++) {
							otherCoordValues[i] = (Double) otherInstance.getValueAtIndex(i).getValue();
						}
						
						double distance = distance(curCoordValues, otherCoordValues);
						
						total++;
						
						if (distance<d_maxDistance) totalInside++;						
					}
				}
			}
			
			//Log.d(TAG, "Points: "+totalInside+"/"+total+" vs "+d_minInclusionPct+"/100");
			if (total > 0) {
				if (totalInside/(double)total < (d_minInclusionPct/100.0)){
					//Log.d(TAG, "Remove instance");
					curIter.remove();					
	    		}
			}
			
		}
		// At this point only those instances that are tightly packed are in d_instanceQ
		//Log.d(TAG, "Outliers removed. "+instances.size()+" instances left.");
		
		// Find cluster centroids
		double centroidCoords[];
		for (Instance curInstance : instances) {
			
			curLabel = (String) curInstance.getValueAtIndex(d_signature.getClassIndex()).getValue();
			centroidCoords = d_centroids.get(curLabel);
			
			//Log.d(TAG, "Current instance label "+curLabel);
			
			for(int i=0; i<d_signature.size()-1; i++) {
				//Log.d(TAG, "added coord "+i+ " with value "+(Double) curInstance.getValueAtIndex(i).getValue());
				centroidCoords[i] += (Double) curInstance.getValueAtIndex(i).getValue();
			}
			
			d_numTrains.put(curLabel, d_numTrains.get(curLabel)+1);

		}
		
		int numTrains;
		for (String classValue : d_centroids.keySet()) {
			centroidCoords = d_centroids.get(classValue);
			numTrains = d_numTrains.get(classValue);
			
			//Log.d(TAG, "Centroid with label "+classValue+" contains " +numTrains+ " points.");
			
			for (int i=0; i<d_signature.size()-1; i++) {
				if (numTrains > 0)
					centroidCoords[i] =  centroidCoords[i]/numTrains;
				// otherwise keep them to zero
			}
			
			d_centroids.put(classValue, centroidCoords); 
		}
		
	}

	@Override
	public Value classify(Instance instance) throws MLException {

		if (!d_signature.checkCompliance(instance, false)){
			throw new MLException(MLException.INCOMPATIBLE_INSTANCE, 
					"Instance is not compatible with the dataset used for classifier construction.");					
		}
		
		// Calculate the centroid that is the closest 
		double minDistance = Double.MAX_VALUE;
		
		// if not yet trained, we return the first label
		String minStringValue = d_signature.getClassFeature().getValues().get(0);
		double centroidCoords[];
		double curCoordValues[] = new double[d_signature.size()-1];
		for(int i=0; i<d_signature.size()-1; i++) {
			curCoordValues[i] = (Double) instance.getValueAtIndex(i).getValue();
		}
		
		for (String classValue : d_centroids.keySet()) {
			centroidCoords = d_centroids.get(classValue);
			double curDistance = Double.MAX_VALUE;
			try {
				curDistance = distance(curCoordValues, centroidCoords);
			} catch (MLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			if (curDistance < minDistance){
				minStringValue = classValue;
				minDistance = curDistance;
			}
		}
		
		return new Value(minStringValue, Value.NOMINAL_VALUE);
	}

	@Override
	public void printClassifierInfo() {
		Log.d(TAG, "Classifer type: "+d_type);
		Log.d(TAG, "Signature: "+d_signature.toString());
		for(String classValue : d_centroids.keySet()) {
			double centroidCoords[] = d_centroids.get(classValue);
			String coords = "[";
			for (int i=0; i<centroidCoords.length-1;i++) {
				coords += (centroidCoords[i]+",");
			}
			coords += (centroidCoords[centroidCoords.length-1]+"]");
			Log.d(TAG, classValue+"\t"+coords+"\t"+d_numTrains.get(classValue));			
		}		
	}
}
