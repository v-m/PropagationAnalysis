package com.vmusco.softwearn.learn.learner.late;

import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.MutationStatistics;
import com.vmusco.softminer.graphs.EdgeIdentity;
import com.vmusco.softwearn.learn.LearningGraph;
import com.vmusco.softwearn.learn.LearningKGraph;
import com.vmusco.softwearn.learn.LearningKGraphStream;

public class BinaryLateImpactLearning extends LateImpactLearner {

	public BinaryLateImpactLearning(int maxk) {
		super(maxk);
	}
	
	@Override
	public void updatePath(LearningKGraph g, EdgeIdentity edge) {
		g.setEdgeThreshold(edge.getFrom(), edge.getTo(), 1f);
	}

	@Override
	public void postPreparedSet(MutationStatistics ms, MutantIfos[] mutants) {
	}

	@Override
	public float defaultInitWeight() {
		return 0;
	}
}
