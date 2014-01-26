package com.ubhave.mltoolkit.utils;

import java.util.HashMap;
import java.util.Set;

public class ClassifierConfig {

	private HashMap<String, Object> d_params;
		
	public ClassifierConfig() {
		
		d_params = new HashMap<String, Object>();
		
	}
	
	public void addParam(String a_param, Object a_value){
		d_params.put(a_param, a_value);
	}
	
	public Object getParam(String a_param) {
		if (d_params.containsKey(a_param)) {
			return d_params.get(a_param);
		}
		return null;
	}
	
	public boolean containsParam(String a_param) {
		
		return d_params.containsKey(a_param);
	}
	
	public Set<String> getAllParams() {
		return d_params.keySet();
	}
}
