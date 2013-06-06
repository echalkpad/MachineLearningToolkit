package com.ubhave.mltoolkit.classifier;

import java.util.ArrayList;

import com.google.gson.annotations.SerializedName;
import com.ubhave.mltoolkit.utils.Instance;
import com.ubhave.mltoolkit.utils.MLException;
import com.ubhave.mltoolkit.utils.Signature;
import com.ubhave.mltoolkit.utils.Value;

public abstract class Classifier {
	
	protected Signature d_signature;
	
	public Classifier(Signature a_signature) {
		d_signature = a_signature;
	}
	
	public abstract void train(ArrayList<Instance> instances) throws MLException;

	public abstract Value classify(Instance instance);
	
}