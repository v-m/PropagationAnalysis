package com.vmusco.softwearn.learn.learner.late;

import java.util.HashMap;
import java.util.Map;

import com.vmusco.softminer.graphs.EdgeIdentity;
import com.vmusco.softwearn.learn.LearningKGraph;

public class DichoLateImpactLearning extends LateImpactLearner {

	private Map<String, Integer> nbTimeTestImpactedByMutation = new HashMap<String, Integer>();
	private Map<String, Integer> nbTimeMutated = new HashMap<String, Integer>();


	@Override
	public void postDeclareAnImpact(String change, String[] tests) {
		newMutation(change);

		for(String i : tests){
			newImpact(change, i);
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

	public float getEmpiricalWeight(String changePoint, String impactedTest){
		String k = getKey(changePoint, impactedTest);

		if(nbTimeTestImpactedByMutation.get(k) == null ||
				nbTimeMutated.get(changePoint) == null)
			return 0;
		
		return (nbTimeTestImpactedByMutation.get(k)*1f)/nbTimeMutated.get(changePoint);
	}

	public void printEmpiricalWeights() {

		System.out.println("nbTimeTestImpactedByMutation");

		for(String s : nbTimeTestImpactedByMutation.keySet()){
			int nb = nbTimeTestImpactedByMutation.get(s);
			System.out.println(s+" => "+nb);
		}

		/*System.out.println("nbTimeMutated");

		for(String s : nbTimeMutated.keySet()){
			int nb = nbTimeMutated.get(s);
		}*/
	}

	public DichoLateImpactLearning(int maxk) {
		super(maxk);
	}

	public DichoLateImpactLearning(int maxk, int kspnr) {
		super(maxk, kspnr);
	}

	@Override
	public void updatePath(LearningKGraph g, EdgeIdentity edge, String test, String point) {
		float weight = getEmpiricalWeight(point, test);

		float nweight = (g.getEdgeThreshold(edge.getFrom(), edge.getTo()) + weight)/2;
		g.setEdgeThreshold(edge.getFrom(), edge.getTo(), nweight);
	}

	@Override
	public float defaultInitWeight() {
		return 0;
	}

}
