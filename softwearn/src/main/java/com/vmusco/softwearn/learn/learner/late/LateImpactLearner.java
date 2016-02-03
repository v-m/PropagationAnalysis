package com.vmusco.softwearn.learn.learner.late;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vmusco.softminer.graphs.EdgeIdentity;
import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.graphs.GraphVisitorValidator;
import com.vmusco.softminer.graphs.algorithms.ShortestPath;
import com.vmusco.softminer.graphs.algorithms.ShortestPath;
import com.vmusco.softwearn.learn.LearningGraph;
import com.vmusco.softwearn.learn.LearningKGraph;
import com.vmusco.softwearn.learn.learner.Learner;

/**
 * This class is an optimized version of the learner intended to compute only one time all pair of POINT + TEST
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public abstract class LateImpactLearner implements Learner {
	protected static final Logger logger = LogManager.getFormatterLogger(LateImpactLearner.class.getSimpleName());

	private int maxk;

	protected Map<String, Map<Integer, Integer>> lateLearner;

	public LateImpactLearner(int maxk) {
		this.maxk = maxk;
		this.lateLearner = new HashMap<String, Map<Integer,Integer>>();
	}

	@Override
	public void learn(LearningGraph g, String point, String[] tests, int k) {
		if(g instanceof LearningKGraph){
			for(String aTest : tests){
				String key = point+"##>"+aTest;

				if(!lateLearner.containsKey(key)){
					lateLearner.put(key, newStructureForKLateLearning(maxk));
				}

				logger.trace("Treating Edge %s", key);

				Map<Integer, Integer> str = lateLearner.get(key);
				lateLearnExceptFor(str, k);
			}
		}else{
			logger.error("The graph should be a LearningKGraph object !");
		}
	}

	public abstract void updatePath(LearningKGraph g, EdgeIdentity edge);

	public void updatePath(LearningKGraph g, EdgeIdentity edge, int k) {
		g.setK(k);
		updatePath(g, edge);
	}

	@Override
	public void learningRoundFinished(LearningGraph g){
		logger.debug("Starting concrete learning phase...");

		if(g instanceof LearningKGraph){
			LearningKGraph lg = (LearningKGraph)g;
			ShortestPath esp = new ShortestPath(g.graph());
			
			for(String key : lateLearner.keySet()){
				String point = key.split("##>")[0];
				String test = key.split("##>")[1];

				logger.trace("Treating %s -> %s", point, test);

				if(g.graph().isThereAtLeastOnePath(test, point)){
					//List<String[]> paths = getPaths(g, test, point);
					//List<String[]> paths = ShortestPath.kShortestPathsYens(g.graph(), test, point, 10);
					List<String[]> paths = esp.yen(test, point, 10);

					for(String[] path : paths){
						EdgeIdentity[] allEdgesInPath = Graph.getAllEdgesInPath(path);
						
						if(allEdgesInPath == null){
							logger.info("Empty path for %s - %s !", test, point);
							continue;
						}
						for(EdgeIdentity edge : allEdgesInPath){
							Map<Integer, Integer> ks = lateLearner.get(key);

							for(int onek : ks.keySet()){
								int nbtime = ks.get(onek);
								logger.trace("Path for %d x%d", onek, nbtime);

								for(int i=0; i<nbtime; i++){
									logger.trace("+1 path for %d !", onek);
									updatePath(lg, edge, onek);
								}
							}
						}
					}
				}else{
					logger.trace("No path between "+test+" and "+point);
				}
			}
		}else{
			logger.error("Graph should be a LearningKGraph instance !");
		}
	}

	public List<String[]> getPaths(final LearningGraph g, String test, String point) {
		return g.graph().getPaths(test, point);
	}
	
	
	
	
	

	private static void lateLearnExceptFor(Map<Integer, Integer> str, int k) {
		for(int i=0; i<str.size(); i++){
			if(i == k)
				continue;

			logger.trace("Update weight for k="+i);
			str.put(i, str.get(i)+1);
		}
	}

	private static Map<Integer, Integer> newStructureForKLateLearning(int maxk) {
		Map<Integer, Integer> ret = new HashMap<Integer, Integer>();

		for(int i=0; i<maxk; i++){
			ret.put(i, 0);
		}

		return ret;
	}
}
