package com.ubhave.mltoolkit;

import java.util.Random;

import com.ubhave.mltoolkit.classifier.Classifier;
import com.ubhave.mltoolkit.classifier.NaiveBayes;
import com.ubhave.mltoolkit.utils.Constants;
import com.ubhave.mltoolkit.utils.Instance;
import com.ubhave.mltoolkit.utils.MLException;
import com.ubhave.mltoolkit.utils.Signature;
import com.ubhave.mltoolkit.utils.Value;

import android.util.SparseArray;

/**
 * Takes care of classifier instantiation and registration.
 * 
 * @author Veljko Pejovic (v.pejovic@cs.bham.ac.uk)
 *
 */
public class ClassifierList {
	private final SparseArray<Classifier> d_classifierList;
	private final Random d_keyGenerator;
	
	public ClassifierList(){
		d_classifierList = new SparseArray<Classifier>();
		d_keyGenerator = new Random();
	}
	
	public static Classifier createClassifier(int a_type, Signature a_signature){
		switch (a_type) {
			case Constants.TYPE_NAIVE_BAYES:
				return new NaiveBayes(a_signature);				
			default:
				return new NaiveBayes(a_signature);		
		}
	}
	
	public void removeClassifier(int a_classifierID)
	{
		Classifier c = d_classifierList.get(a_classifierID);
		if (c != null)
		{
			c.kill();
			d_classifierList.delete(a_classifierID);
		}
	}

	public Classifier getClassifier(int a_classifierID)
	{
		return d_classifierList.get(a_classifierID);
	}

	
	public synchronized int addClassifier(Classifier a_classifier) throws MLException{
		int classifierID = randomKey();
		d_classifierList.append(classifierID, a_classifier);
		return classifierID;
		
	}
	
	private int randomKey() throws MLException
	{
		int classifierID = d_keyGenerator.nextInt();
		int loopCount = 0;
		while (d_classifierList.get(classifierID) != null)
		{
			if (loopCount > 1000)
			{
				throw new MLException(MLException.INVALID_STATE, "Listener map >1000 key conflicts.");
			}
			classifierID = d_keyGenerator.nextInt();
			loopCount++;
		}
		return classifierID;
	}
}
