package com.ubhave.mltoolkit.classifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.ubhave.mltoolkit.utils.Feature;
import com.ubhave.mltoolkit.utils.Instance;
import com.ubhave.mltoolkit.utils.MLException;
import com.ubhave.mltoolkit.utils.Signature;
import com.ubhave.mltoolkit.utils.Value;

/**
 * TODO: Nominal attributes are supported for now.
 * TODO: How to handle missing attributes?
 * TODO: Pruning?
 * @author veljko
 *
 */
public class ID3 extends Classifier{

	/*
	ID3 (Examples, Target_Attribute, Attributes)
    Create a root node for the tree
    If all examples are positive, Return the single-node tree Root, with label = +.
    If all examples are negative, Return the single-node tree Root, with label = -.
    If number of predicting attributes is empty, then Return the single node tree Root,
    with label = most common value of the target attribute in the examples.
    Otherwise Begin
        A ← The Attribute that best classifies examples.
        Decision Tree attribute for Root = A.
        For each possible value, v_i, of A,
            Add a new tree branch below Root, corresponding to the test A = v_i.
            Let Examples(v_i) be the subset of examples that have the value v_i for A
            If Examples(v_i) is empty
                Then below this new branch add a leaf node with label = most common target value in the examples
            Else below this new branch add the subtree ID3 (Examples(v_i), Target_Attribute, Attributes – {A})
    End
    Return Root
    */
	
	private Feature d_bestFeature;
	
	private int d_bestFeatureIndex;
	
	private int[] d_candidateFeatures;
	
	private Value d_majorValue;
	
	private boolean d_isLeaf;
	
	private HashMap<Object, ID3> d_subtrees;
	
	private static int[] allFeaturesArray(Signature a_signature) {
		int[] candidateFeatures = new int[a_signature.getFeatures().size()];
		Arrays.fill(candidateFeatures, 1);
		return candidateFeatures;
	}
	public ID3(Signature a_signature) {
		this(a_signature, allFeaturesArray(a_signature));
	}
	
	public ID3(Signature a_signature, int[] a_candidateFeatures) {
		super(a_signature);
		d_subtrees = new HashMap<Object, ID3>();
		d_candidateFeatures = a_candidateFeatures;
		d_isLeaf = true;
		d_majorValue = null;
		d_bestFeature = null;
		d_bestFeatureIndex = 0;
	}

	@Override
	public void train(ArrayList<Instance> instances) throws MLException {
		// Calculate stats such as:
		Feature classFeature = d_signature.getClassFeature(); 
		int[] classCounts = new int[classFeature.numberOfCategories()];	
		Arrays.fill(classCounts, 0);
		
		// if all Instances belong to a single class
		for (Instance i : instances) {
			Value classValue = i.getValueAtIndex(d_signature.getClassIndex());
			int classValueInt = classFeature.indexOfCategory((String) classValue.getValue());
			classCounts[classValueInt] += 1;			
		}
		int NZcounter = 0, nonZeroClassValueIndex = 0;
		for (int i = 0; i < classCounts.length; i ++)
		    if (classCounts[i] > 0) {
		    	NZcounter ++;
		    	nonZeroClassValueIndex = i;
		    }
		if (NZcounter == 1){
			d_isLeaf = true;
			d_majorValue = new Value(d_signature.getClassFeature().categoryOfIndex(nonZeroClassValueIndex), Value.NOMINAL_VALUE);
			return;
		}
		

		// If number of predicting attributes is empty, then Return the single node tree Root,
	    // with label = most common value of the target attribute in the examples.
		int numCandidateFeatures = 0;
		for (int indicator : d_candidateFeatures)
			numCandidateFeatures += indicator;
		if (numCandidateFeatures == 0) {
			d_isLeaf = true;
			int maxClassValueInt = 0;
			int maxClassValueCount = 0;
			for (int i=0; i<classCounts.length; i++){
				if (maxClassValueCount < classCounts[i]) {
					maxClassValueInt = i;
					maxClassValueCount = classCounts[i];
				}
			}
			d_majorValue = new Value(d_signature.getClassFeature().categoryOfIndex(maxClassValueInt), Value.NOMINAL_VALUE);
			return; 
		}
		
		else {
			// Calculate information gain for each attribute
			double IG[] = new double[d_signature.getFeatures().size()];
			double maxIG = -1;
			int maxIGindex = 0;
			HashMap<String, ArrayList<Instance>> maxSubsets = null;
			
			double totalSetEntropy = calculateEntropy(instances);
			// H(S) - sum(p(t)*H(t))_for_attribute_A_the_data_is_split_in_T_sets
			// H(t) = - sum(p(x)log(p(x))) where x in X (set of class values)
			//TODO: There should be a faster way to check if there are any 
			// interesting attributes left, before the entropy is calculated.

			for(int i=0; i<d_signature.getFeatures().size(); i++){
				
				Feature feature = d_signature.getFeatureAtIndex(i);
				
				if (feature.getFeatureType() == Feature.NOMINAL && d_candidateFeatures[i]==1) { 
					
					HashMap<String, ArrayList<Instance>> subsets = new HashMap<String, ArrayList<Instance>>();
					int classFeatureCounts[] = new int[feature.numberOfCategories()];
					Arrays.fill(classFeatureCounts, 0);
					
					for (Instance instance : instances) {
						
						Value featureValue = instance.getValueAtIndex(i);
						String featureValueName = (String) featureValue.getValue();
						int featureValueInt = feature.indexOfCategory(featureValueName);
						
						classFeatureCounts[featureValueInt] += 1;
						
						ArrayList<Instance> subset = subsets.get(featureValueName);
						if (subset == null) {
							subset = new ArrayList<Instance>();
						}
						subset.add(instance);
						subsets.put(featureValueName, subset);
					}
					
					
					Iterator<Entry<String, ArrayList<Instance>>> it = subsets.entrySet().iterator();
					double sumEntropies = 0;
					while (it.hasNext()) {
						Entry<String, ArrayList<Instance>> pairs = it.next();
						sumEntropies += calculateEntropy(pairs.getValue());
					}
					
					double IGvalue = totalSetEntropy - sumEntropies;
					
					if (IGvalue > maxIG) {
						maxIG = IGvalue;
						maxIGindex = i;
						maxSubsets = subsets;
					}
					IG[i] = IGvalue;
					
				}
				
			}
			// Pick the best attribute 
			d_bestFeature = d_signature.getFeatureAtIndex(maxIGindex);
			d_bestFeatureIndex = maxIGindex;
			d_isLeaf = false;
			
			// TODO: what happens if some of the deciding feature values are not observed
			// in the training set?
			
			ArrayList<String> featureValueList = d_bestFeature.getValues();
			int candidateFeatures[] = (int[]) d_candidateFeatures.clone();
			candidateFeatures[maxIGindex] = 0;
			
			for (String value : featureValueList) {
				ID3 subTree = null;
				if (maxSubsets.containsKey(value)) {
					subTree = new ID3(d_signature, candidateFeatures);
					subTree.train(maxSubsets.get(value));

				} else {
					// Shortcut to the majority class leaf.
					Arrays.fill(candidateFeatures, 0);
					subTree = new ID3(d_signature, candidateFeatures);
				}
				d_subtrees.put(value, subTree);
			}
			
		}
		// if no attributes left -> return majority class

	}

	// H(t) = - sum(p(x)log(p(x))) where x in X (set of class values)
	private double calculateEntropy (ArrayList<Instance> a_instances) {
		
		double entropy = 0;
		
		Feature classFeature = d_signature.getClassFeature(); 

		int classCounts[] = new int[classFeature.numberOfCategories()];
		int classCountsTotal = 0;
		Arrays.fill(classCounts, 0);
		
		for (Instance i : a_instances) {
			Value classValue = i.getValueAtIndex(d_signature.getClassIndex());
			int classValueInt = classFeature.indexOfCategory((String) classValue.getValue());
			classCounts[classValueInt] += 1;
		}

		for (int j=0; j<classCounts.length; j++) classCountsTotal += classCounts[j];
		for (int j=0; j<classCounts.length; j++) {
			double probabilityFeatureValue = (double)classCounts[j]/classCountsTotal;
			entropy -= (probabilityFeatureValue)*Math.log(probabilityFeatureValue);
		}
		
		return entropy;
	}
	@Override
	public Value classify(Instance a_instance) {
		
		if (d_isLeaf) {
			return d_majorValue;
		} else {
			Value v = a_instance.getValueAtIndex(d_bestFeatureIndex);
			ID3 nextTree = d_subtrees.get(v.getValue());
			return nextTree.classify(a_instance);
		}
	}

}