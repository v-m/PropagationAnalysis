package com.vmusco.softwearn.learn.learner;

import com.vmusco.softwearn.learn.LearningGraph;

/**
 * 
 * @author Vincenzo Musco - http://www.vmusco.com
 */
@Deprecated
public class NoAlgorithm extends ImpactLearner{
	
	/*@Override
	public void postPreparedSet(MutationStatistics ms, MutantIfos[] mutants) {
	}*/

	@Override
	public float defaultInitWeight() {
		return 1;
	}

	@Override
	public void learn(LearningGraph g, String changePoint, String impactedTest) {
		
	}

	@Override
	public void postDeclareAnImpact(String change, String[] tests) {
	}

}
