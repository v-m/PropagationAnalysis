package com.vmusco.softwearn.learn.learner.late;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vmusco.softminer.graphs.EdgeIdentity;
import com.vmusco.softminer.graphs.Graph;
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
	private int kspnr;
	
	// Statistics structures and methods
	// Used to gather statistics about the learning
	private HashMap<String, Integer> nbOccurenceStats = new HashMap<>();
	private HashMap<String, Set<String>> pathsForMutantsStats = new HashMap<>();
	private String currentMutant = null;
	
	protected Map<String, Map<Integer, Integer>> lateLearner;
	private long time = -1;

			
	public void setChangeId(String currentMutant) {
		this.currentMutant = currentMutant;
	}
	
	public HashMap<String, Integer> getNbOccurencesStat() {
		return nbOccurenceStats;
	}
	
	public HashMap<String, Set<String>> getPathsForMutantsStats() {
		return pathsForMutantsStats;
	}
	
	public int getLearnedPathForChange(String mutid){
		int nbocc = 0;
		
		for(String key : pathsForMutantsStats.keySet()){
			if(pathsForMutantsStats.get(key).contains(mutid)){
				nbocc += nbOccurenceStats.get(key);
			}
		}
		
		return nbocc;
	}
	// ----------
	
	public LateImpactLearner(int maxk) {
		this(maxk, 10);
	}
	
	/**
	 * @param maxk the number of k-fold to consider
	 * @param kspnr the number of shortest path to compute
	 */
	public LateImpactLearner(int maxk, int kspnr) {
		this.maxk = maxk;
		this.kspnr = kspnr;
		this.lateLearner = new HashMap<String, Map<Integer,Integer>>();
	}

	@Override
	public void learn(LearningGraph g, String point, String[] tests, int k) {
		if(g instanceof LearningKGraph){
			for(String aTest : tests){
				String key = point+FAULT_PATH_SEPARATOR+aTest;

				if(!lateLearner.containsKey(key)){
					lateLearner.put(key, newStructureForKLateLearning(maxk));
				}

				logger.trace("Treating Edge %s", key);

				Map<Integer, Integer> str = lateLearner.get(key);
				lateLearnExceptFor(str, k);
				
				if(currentMutant != null){
					if(!pathsForMutantsStats.containsKey(key)){
						pathsForMutantsStats.put(key, new HashSet<String>());
					}
					
					pathsForMutantsStats.get(key).add(currentMutant);
				}
			}
		}else{
			logger.error("The graph should be a LearningKGraph object !");
		}
	}

	public abstract void updatePath(LearningKGraph g, EdgeIdentity edge, String test, String point);

	public void updatePath(LearningKGraph g, EdgeIdentity edge, int k, String test, String point) {
		g.setK(k);
		updatePath(g, edge, test, point);
	}

	@Override
	public void learningRoundFinished(LearningGraph g){
		logger.debug("Starting concrete learning phase...");

		if(g instanceof LearningKGraph){
			LearningKGraph lg = (LearningKGraph)g;
			ShortestPath esp = new ShortestPath(g.graph());
			
			time = System.currentTimeMillis();
			
			for(String key : lateLearner.keySet()){
				int learnedEdgeWithThisPair = 0;
				
				String point = key.split(FAULT_PATH_SEPARATOR)[0];
				String test = key.split(FAULT_PATH_SEPARATOR)[1];

				logger.trace("Treating %s -> %s", point, test);

				if(g.graph().isThereAtLeastOnePath(test, point)){
					//List<String[]> paths = ShortestPath.kShortestPathsYens(g.graph(), test, point, 10);
					
					List<String[]> paths = null;
					
					if(kspnr <= 0){
						paths = g.graph().getPaths(test, point);
					}else{
						paths = esp.yen(test, point, kspnr);
					}

					for(String[] path : paths){
						EdgeIdentity[] allEdgesInPath = Graph.getAllEdgesInPath(path);
						
						if(allEdgesInPath == null){
							logger.info("Empty path for %s - %s !", test, point);
							continue;
						}
						
						learnedEdgeWithThisPair += allEdgesInPath.length;
						
						for(EdgeIdentity edge : allEdgesInPath){
							Map<Integer, Integer> ks = lateLearner.get(key);

							for(int onek : ks.keySet()){
								int nbtime = ks.get(onek);
								logger.trace("Path for %d x%d", onek, nbtime);

								for(int i=0; i<nbtime; i++){
									logger.trace("+1 path for %d !", onek);
									updatePath(lg, edge, onek, test, point);
								}
							}
						}
					}
				}else{
					logger.trace("No path between "+test+" and "+point);
				}
				
				nbOccurenceStats.put(key, learnedEdgeWithThisPair);
			}
			
			time = System.currentTimeMillis() - time;
		}else{
			logger.error("Graph should be a LearningKGraph instance !");
		}
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
	
	public int getKspNr() {
		return kspnr;
	}
	
	@Override
	public long getLastLearningTime(){
		return time;
	}
}
