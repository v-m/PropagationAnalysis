package com.vmusco.softwearn.learn;

import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.ProcessStatistics;

public interface ImpactLearner {
	public void learn(LearningGraph g, String changePoint, String impactedTest);
	public void postPreparedSet(ProcessStatistics ps, MutantIfos[] mutants);
	public float defaultInitWeight();
}
