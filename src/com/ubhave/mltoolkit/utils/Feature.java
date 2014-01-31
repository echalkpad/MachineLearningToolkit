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
import java.util.Arrays;
import java.util.HashMap;

import android.util.Log;

/**
 * Features can be nominal or numeric. 
 * 
 * @author Veljko Pejovic, University of Birmingham, UK <v.pejovic@cs.bham.ac.uk>
 *
 */
public class Feature {
	
	private static final String TAG = "Feature";

	public static final int NOMINAL = 0;
	
	public static final int NUMERIC = 1;
	
	private int d_type;
	
	private String d_name;
	
    private ArrayList<String> d_categories;
	
    private HashMap<String,Integer> d_categoryIndex;
    
	public Feature(String fname, int ftype) throws MLException{
		if (ftype == NOMINAL){
			throw new MLException(MLException.INCOMPATIBLE_FEATURE_TYPE,
					"Nominal features require a list of possible categories;" +
					" use Feature(String, int, String[]) instead."); 
		}
		d_type = ftype;
		d_name = fname;
	}
	
	public Feature(String fname, int ftype, String[] fvalues) throws MLException{
		if (ftype == NUMERIC){
			throw new MLException(MLException.INCOMPATIBLE_FEATURE_TYPE,
					"Numeric features do not need a list of possible categories;" +
					" use Feature(String, int) instead."); 
		}
		d_type = ftype;
		d_name = fname;
		d_categories = new ArrayList<String>(Arrays.asList(fvalues));
		d_categoryIndex = new HashMap<String, Integer>();
 		for(int i=0;i<fvalues.length;i++) {
 			d_categoryIndex.put(fvalues[i], Integer.valueOf(i));
 		}
	}
	
	public Feature(String fname, int ftype, ArrayList<String> fvalues) throws MLException{
		if (ftype == NUMERIC){
			throw new MLException(MLException.INCOMPATIBLE_FEATURE_TYPE,
					"Numeric features do not need a list of possible categories;" +
					" use Feature(String, int) instead."); 
		}
		d_type = ftype;
		d_name = fname;
		d_categories = (ArrayList<String>) fvalues.clone();
		d_categoryIndex = new HashMap<String, Integer>();

 		for(int i=0;i<fvalues.size();i++) d_categoryIndex.put(fvalues.get(i), Integer.valueOf(i));
	}
	
	public int getFeatureType(){
		return d_type;
	}
	
	public ArrayList<String> getValues(){
		return d_categories;
	}
	
	public String name(){
		return d_name;
	}
	
	public int indexOfCategory(String value){
		//Log.d(TAG, "feature "+d_name+" going for value "+value);
		return d_categoryIndex.get(value);
		
	}
	
	public String categoryOfIndex(int index){
		return d_categories.get(index);
	}
	
	public int numberOfCategories(){
		if (d_type == NOMINAL) return d_categories.size(); 
		else return 1;
	}
}
