package com.vmusco.softwearn.learn.learner;

import com.vmusco.softwearn.learn.LearningGraph;

/**
 * 
 * @author Vincenzo Musco - http://www.vmusco.com
 */
@Deprecated
public abstract class ImpactLearner implements Learner{
	private long time = -1;

	public abstract void learn(LearningGraph g, String changePoint, String impactedTest);

	@Override
	public void learn(LearningGraph g, String point, String[] tests, int k) {
		time = System.currentTimeMillis();
		
		for(String aTest : tests){
			learn(g, point, aTest);
		}
		
		time = System.currentTimeMillis() - time;
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
	
	@Override
	public long getLastLearningTime() {
		return time;
	}
}
