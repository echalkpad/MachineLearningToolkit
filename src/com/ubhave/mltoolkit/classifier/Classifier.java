package com.ubhave.mltoolkit.classifier;

import java.io.Serializable;
import com.ubhave.mltoolkit.utils.Instance;
import com.ubhave.mltoolkit.utils.MLException;
import com.ubhave.mltoolkit.utils.Value;

public abstract class Classifier implements Serializable {

	private static final long serialVersionUID = 1998880384642330312L;

	public abstract void train(Instance[] instances) throws MLException;

	public abstract Value classify(Instance instance);
	
}