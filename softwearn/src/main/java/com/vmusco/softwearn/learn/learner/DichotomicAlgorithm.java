package com.vmusco.softwearn.learn.learner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.MutationStatistics;
import com.vmusco.smf.exceptions.MutationNotRunException;
import com.vmusco.softminer.graphs.EdgeIdentity;
import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softwearn.learn.LearningGraph;

public class DichotomicAlgorithm extends ImpactLearner{
	private static final Logger logger = LogManager.getFormatterLogger(DichotomicAlgorithm.class.getSimpleName());

	private Map<String, Integer> nbTimeTestImpactedByMutation = new HashMap<String, Integer>();
	private Map<String, Integer> nbTimeMutated = new HashMap<String, Integer>();

	@Override
	public void postPreparedSet(MutationStatistics ms, MutantIfos[] mis){
		for(MutantIfos mi : mis){
			newMutation(mi.getMutationIn());

			try {
				for(String i : ms.getCoherentMutantFailAndHangTestCases(mi.getExecutedTestsResults())){
					newImpact(mi.getMutationIn(), i.endsWith("()")?i:i+"()");
				}
			} catch (MutationNotRunException e) {
				e.printStackTrace();
			}
		}
	}

	private void newMutation(String changePoint){
		String k = changePoint;
		int nb = 0;

		if(nbTimeMutated.containsKey(k)){
			nb = nbTimeMutated.get(k);
		}

		nbTimeMutated.put(k, nb + 1);
	}

	private static String getKey(String changePoint, String impactedTest){
		return changePoint+"_"+impactedTest;
	}

	private void newImpact(String changePoint, String impactedTest){
		String k = getKey(changePoint, impactedTest);
		int nb = 0;

		if(nbTimeTestImpactedByMutation.containsKey(k)){
			nb = nbTimeTestImpactedByMutation.get(k);
		}

		nbTimeTestImpactedByMutation.put(k, nb + 1);
	}

	private float getEmpiricalWeight(String changePoint, String impactedTest){
		String k = getKey(changePoint, impactedTest);

		return (nbTimeTestImpactedByMutation.get(k)*1f)/nbTimeMutated.get(changePoint);
	}

	public void printEmpiricalWeights() {

		System.out.println("nbTimeTestImpactedByMutation");

		for(String s : nbTimeTestImpactedByMutation.keySet()){
			int nb = nbTimeTestImpactedByMutation.get(s);
			System.out.println(s+" => "+nb);
		}

		System.out.println("nbTimeMutated");

		for(String s : nbTimeMutated.keySet()){
			int nb = nbTimeMutated.get(s);
		}
	}

	@Override
	public float defaultInitWeight() {
		return 0;
	}

	@Override
	public void learn(LearningGraph g, String changePoint, String impactedTest) {
		float weight = getEmpiricalWeight(changePoint, impactedTest);

		if(g.graph().isThereAtLeastOnePath(impactedTest, changePoint)){
			List<String[]> paths = g.graph().getPaths(impactedTest, changePoint);

			for(String[] path : paths){
				for(EdgeIdentity edge : Graph.getAllEdgesInPath(path)){
					float nweight = (g.getEdgeThreshold(edge.getFrom(), edge.getTo()) + weight)/2;
					g.setEdgeThreshold(edge.getFrom(), edge.getTo(), nweight);
				}
			}
		}else{
			logger.trace("No path between "+impactedTest+" and "+changePoint);
		}
	}
}
