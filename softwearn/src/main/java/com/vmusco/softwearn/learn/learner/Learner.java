package com.vmusco.softwearn.learn.learner;

import com.vmusco.softwearn.learn.LearningGraph;

/**
 * 
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public interface Learner {
	public static final String FAULT_PATH_SEPARATOR = "##>";
	
	void learn(LearningGraph g, String point, String[] tests, int k);
	//public void postPreparedSet(MutationStatistics ms, MutantIfos[] mutants);
	public void postDeclareAnImpact(String change, String[] tests);
	public float defaultInitWeight();
	void learningRoundFinished(LearningGraph g);
	
	// Used for statistics...
	public void setChangeId(String id);
	public int getLearnedPathForChange(String id);
}
