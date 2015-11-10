package com.vmusco.softwearn.learn;

import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.ProcessStatistics;

public class NoAlgorithm implements ImpactLearner{
	
	public void learn(LearningGraph g, String changePoint, String impactedTest) {
		
	}

	@Override
	public void postPreparedSet(ProcessStatistics ps, MutantIfos[] mutants) {
	}

	@Override
	public float defaultInitWeight() {
		return 1;
	}

}
