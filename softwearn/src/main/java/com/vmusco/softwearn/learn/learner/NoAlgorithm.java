package com.vmusco.softwearn.learn.learner;

import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.MutationStatistics;
import com.vmusco.smf.analysis.ProcessStatistics;
import com.vmusco.softwearn.learn.LearningGraph;

public class NoAlgorithm extends ImpactLearner{
	
	@Override
	public void postPreparedSet(MutationStatistics ms, MutantIfos[] mutants) {
	}

	@Override
	public float defaultInitWeight() {
		return 1;
	}

	@Override
	public void learn(LearningGraph g, String changePoint, String impactedTest) {
		
	}

}
