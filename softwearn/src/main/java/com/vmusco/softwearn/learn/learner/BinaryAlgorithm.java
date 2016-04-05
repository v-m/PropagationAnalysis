package com.vmusco.softwearn.learn.learner;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vmusco.softminer.graphs.EdgeIdentity;
import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softwearn.learn.LearningGraph;

/**
 * 
 * @author Vincenzo Musco - http://www.vmusco.com
 */
@Deprecated
public class BinaryAlgorithm extends ImpactLearner{
	private static final Logger logger = LogManager.getFormatterLogger(BinaryAlgorithm.class.getSimpleName());

	@Override
	public float defaultInitWeight() {
		return 0;
	}

	@Override
	public void learn(LearningGraph g, String changePoint, String impactedTest) {
		if(g.graph().isThereAtLeastOnePath(impactedTest, changePoint)){
			List<String[]> paths = g.graph().getPaths(impactedTest, changePoint);
			
			for(String[] path : paths){
				for(EdgeIdentity edge : Graph.getAllEdgesInPath(path)){
					g.setEdgeThreshold(edge.getFrom(), edge.getTo(), 1f);
				}
			}
		}else{
			logger.trace("No path between "+impactedTest+" and "+changePoint);
		}
	}

	@Override
	public void postDeclareAnImpact(String change, String[] tests) {
	}
}
