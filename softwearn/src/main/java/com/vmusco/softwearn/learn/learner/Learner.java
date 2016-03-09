package com.vmusco.softwearn.learn.learner;

import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.MutationStatistics;
import com.vmusco.softwearn.learn.LearningGraph;

public interface Learner {
	public static final String FAULT_PATH_SEPARATOR = "##>";
	
	void learn(LearningGraph g, String point, String[] tests, int k);
	public void postPreparedSet(MutationStatistics ms, MutantIfos[] mutants);
	public float defaultInitWeight();
	void learningRoundFinished(LearningGraph g);
	
	// Used for statistics...
	public void setChangeId(String id);
	public int getLearnedPathForChange(String id);
}
