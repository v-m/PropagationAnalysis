package com.vmusco.softwearn.learn;

import java.util.HashMap;
import java.util.Map;

public class LearningKGraphStream extends LearningGraphStream implements LearningKGraph{
	protected Map<Integer, Map<Integer, Float>> kthresholds = new HashMap<Integer, Map<Integer,Float>>();
	
	public LearningKGraphStream(float init, int kmax) {
		super(init);
		
		for(int i=0; i<kmax; i++){
			kthresholds.put(i, new HashMap<Integer, Float>());
		}
	}
	
	@Override
	public void setK(int k){
		super.thresholds = kthresholds.get(k);
	}
	
	@Override
	public void addDirectedEdge(String from, String to, float treshold) {
		setK(0);
		super.addDirectedEdge(from, to, treshold);
		
		for(int i=1; i<kthresholds.size(); i++){
			setK(i);
			setEdgeThreshold(from, to, treshold);
		}
	}

	public Map<Integer, Map<Integer, Float>> getAllThresholds() {
		return kthresholds;
	}
	
	public void setAllThresholds(Map<Integer, Map<Integer, Float>> thr) {
		this.kthresholds = thr;
	}
}
