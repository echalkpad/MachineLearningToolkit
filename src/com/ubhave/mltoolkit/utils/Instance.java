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

/**
 * Instance is merely a list of Values. It should be used, and comply with, 
 * the signature of the classifier.
 * 
 * @author Veljko Pejovic, University of Birmingham, UK <v.pejovic@cs.bham.ac.uk>
 *
 */
public class Instance {

	private ArrayList<Value> d_values;
	
	public Instance(int a_numFeatures){
		d_values = new ArrayList<Value>(a_numFeatures);
	}
	
	public Instance(){
		this(0);
	}
	
	public Instance(ArrayList<Value> featureValues){
		d_values = featureValues;
	}
	
	public void addValue(Value a_value){
		d_values.add(a_value);		
	}
	
	public Value getValueAtIndex(int a_i){
		return d_values.get(a_i);
	}
	
	public void setValueAtIndex(int a_i, Value a_value) throws IndexOutOfBoundsException {
		d_values.set(a_i, a_value);
	}
	
	
	public int size(){
		return d_values.size();
	}
}
