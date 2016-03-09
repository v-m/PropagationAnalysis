package com.vmusco.softwearn.learn.learner;

import com.vmusco.softwearn.learn.LearningGraph;

public abstract class ImpactLearner implements Learner{
	public abstract void learn(LearningGraph g, String changePoint, String impactedTest);

	@Override
	public void learn(LearningGraph g, String point, String[] tests, int k) {
		for(String aTest : tests){
			learn(g, point, aTest);
		}
	}


	@Override
	public void learningRoundFinished(LearningGraph g) {
	}
	
	public void setChangeId(String id){
		// Nothing to do here..
		// Not implemented in this version
	}
	
	public int getLearnedPathForChange(String changeid){
		return -1;
		// Not implemented in this version
	}
}
