package com.vmusco.softwearn.learn.learner.late;

import com.vmusco.softminer.graphs.EdgeIdentity;
import com.vmusco.softwearn.learn.LearningKGraph;

/**
 * 
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class NoLateImpactLearning extends LateImpactLearner {

	public NoLateImpactLearning(int maxk) {
		super(maxk);
	}

	public NoLateImpactLearning(int maxk, int kspnr) {
		super(maxk, kspnr);
	}
	
	@Override
	public void updatePath(LearningKGraph g, EdgeIdentity edge, String test, String point) {
	}

	@Override
	public float defaultInitWeight() {
		return 1;
	}

	@Override
	public void postDeclareAnImpact(String change, String[] tests) {
		
	}
}
