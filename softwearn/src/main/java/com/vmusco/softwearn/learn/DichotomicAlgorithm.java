package com.vmusco.softwearn.learn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.ProcessStatistics;
import com.vmusco.smf.exceptions.MutationNotRunException;
import com.vmusco.softminer.graphs.EdgeIdentity;
import com.vmusco.softminer.graphs.Graph;

public class DichotomicAlgorithm implements ImpactLearner{
	private static final Logger logger = LogManager.getFormatterLogger(MutationGraphKFold.class.getSimpleName());

	private Map<String, Integer> nbTimeTestImpactedByMutation = new HashMap<String, Integer>();
	private Map<String, Integer> nbTimeMutated = new HashMap<String, Integer>();

	public void postPreparedSet(ProcessStatistics ps, MutantIfos[] mis){
		for(MutantIfos mi : mis){
			newMutation(mi.getMutationIn());

			try {
				for(String i : ps.getCoherentMutantFailAndHangTestCases(mi.getExecutedTestsResults())){
					newImpact(mi.getMutationIn(), i+"()");
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

	public void learn(LearningGraph g, String changePoint, String impactedTest) {
		float weight = getEmpiricalWeight(changePoint, impactedTest);
		
		if(g.getGraph().isThereAtLeastOnePath(impactedTest, changePoint)){
			List<String[]> paths = g.getGraph().getPaths(impactedTest, changePoint);

			for(String[] path : paths){
				for(EdgeIdentity edge : Graph.getAllEdgesInPath(path)){
					float nweight = (g.getThreshold(edge.getFrom(), edge.getTo()) + weight)/2;
					g.setThreshold(edge.getFrom(), edge.getTo(), nweight);
				}
			}
		}else{
			logger.info("No path between "+impactedTest+" and "+changePoint);
		}
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

}
