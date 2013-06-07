package com.ubhave.mltoolkit.classifier;

import java.util.ArrayList;

import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.ubhave.mltoolkit.utils.Instance;
import com.ubhave.mltoolkit.utils.MLException;
import com.ubhave.mltoolkit.utils.Signature;
import com.ubhave.mltoolkit.utils.Value;

public abstract class Classifier {
	
	protected Signature d_signature;
	
	protected int d_type;
	
	private static final String TAG = "Classifier";
	
	/*public Classifier(){	
		Log.d(TAG, "Classifier empty constructor");
	}*/
	
	public Classifier(Signature a_signature) {
		d_signature = a_signature;
	}
	
	public abstract void train(ArrayList<Instance> instances) throws MLException;

	public abstract Value classify(Instance instance);
	
	public abstract void printClassifierInfo();
	
}