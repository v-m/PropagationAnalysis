package com.vmusco.softwearn.tests;

import com.vmusco.softminer.graphs.EdgeIdentity;
import com.vmusco.softwearn.learn.LearningGraph;
import com.vmusco.softwearn.learn.LearningGraphStream;
import com.vmusco.softwearn.learn.learner.ImpactLearner;

public class TestTools {
	public static LearningGraph getATestingGraph(float init){
		LearningGraph g = new LearningGraphStream(init);

		for(EdgeIdentity ei : edgesDataset()){
			g.graph().addDirectedEdgeAndNodeIfNeeded(ei);
		}

		return g;
	}
	
	public static EdgeIdentity[] edgesDataset(){
		return new EdgeIdentity[]{
			new EdgeIdentity("b", "a"),
			new EdgeIdentity("f", "b"),
			new EdgeIdentity("e", "c"),
			new EdgeIdentity("c", "a"),
			new EdgeIdentity("g", "c")
		};
	}
	

	public static LearningGraph runTestFor(ImpactLearner l){
		LearningGraph g = getATestingGraph(l.defaultInitWeight());

		l.learn(g, "a", "f");

		return g;
	}
}
