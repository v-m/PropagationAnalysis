package com.vmusco.softwearn.learn;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.ProcessStatistics;
import com.vmusco.softminer.graphs.EdgeIdentity;
import com.vmusco.softminer.graphs.Graph;

public class BinaryAlgorithm implements ImpactLearner{
	private static final Logger logger = LogManager.getFormatterLogger(MutationGraphKFold.class.getSimpleName());
	
	public void learn(LearningGraph g, String changePoint, String impactedTest) {
		if(g.getGraph().isThereAtLeastOnePath(impactedTest, changePoint)){
			List<String[]> paths = g.getGraph().getPaths(impactedTest, changePoint);
			
			for(String[] path : paths){
				for(EdgeIdentity edge : Graph.getAllEdgesInPath(path)){
					g.setThreshold(edge.getFrom(), edge.getTo(), 1f);
				}
			}
		}else{
			logger.info("No path between "+impactedTest+" and "+changePoint);
		}
	}

	@Override
	public void postPreparedSet(ProcessStatistics ps, MutantIfos[] mutants) {
	}

	@Override
	public float defaultInitWeight() {
		return 1;
	}

}
