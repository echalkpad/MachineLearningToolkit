package com.ubhave.mltoolkit.classifier;

import com.ubhave.mltoolkit.utils.Instance;
import com.ubhave.mltoolkit.utils.MLException;

public interface OnlineClassifier {

	public void update(Instance a_instance) throws MLException;

}
