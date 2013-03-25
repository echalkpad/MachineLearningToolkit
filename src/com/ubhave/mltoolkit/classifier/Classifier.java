package com.ubhave.mltoolkit.classifier;

import java.io.Serializable;

import com.ubhave.mltoolkit.utils.Constants;
import com.ubhave.mltoolkit.utils.Instance;
import com.ubhave.mltoolkit.utils.MLException;
import com.ubhave.mltoolkit.utils.Signature;
import com.ubhave.mltoolkit.utils.Value;

public abstract class Classifier implements Serializable {
	
	protected Signature d_signature;
	
	private static final long serialVersionUID = 1998880384642330312L;
	
	public Classifier(Signature a_signature) {
		d_signature = a_signature;
	}
	
	public abstract void train(Instance[] instances) throws MLException;

	public abstract Value classify(Instance instance);

	public void kill() {
		// TODO Auto-generated method stub
		
	}
	
}