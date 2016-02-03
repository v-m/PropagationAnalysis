package com.vmusco.softwearn.learn.learner.late;

import java.util.List;

import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.MutationStatistics;
import com.vmusco.softminer.graphs.EdgeIdentity;
import com.vmusco.softminer.graphs.GraphVisitorValidator;
import com.vmusco.softwearn.learn.LearningGraph;
import com.vmusco.softwearn.learn.LearningKGraph;

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
	
	@Override
	public List<String[]> getPaths(final LearningGraph g, String test, String point) {
		return g.graph().getPaths(test, point, new GraphVisitorValidator() {
			@Override
			public boolean isNodeAccepted(String from) {
				return true;
			}

			@Override
			public boolean isEdgeAccepted(String arrived, String next) {
				return g.getEdgeThreshold(arrived, next) < 1;
			}
		});
	}
	
	
}
