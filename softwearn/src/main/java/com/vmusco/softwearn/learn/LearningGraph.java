package com.vmusco.softwearn.learn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.vmusco.softminer.graphs.EdgeIdentity;
import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.graphs.GraphStream;

/**
 * This class is a facade to manage treshold on a graph
 * @author Vincenzo Musco - http://www.vmusco.com
 *
 */
public class LearningGraph {
	HashMap<String, Float> thresholds = new HashMap<String, Float>();
	private Graph g;
	private String[] tests;

	public LearningGraph(Graph g, float init, String[] tests) {
		this.g = g;
		this.tests = tests;

		for(EdgeIdentity edge : g.getEdges()){
			String key = GraphStream.buildDirectedEdgeName(edge.getFrom(), edge.getTo());
			thresholds.put(key, init);
		}
	}

	public Graph getGraph() {
		return g;
	}

	public void setThreshold(String from, String to, float threshold){
		String key = GraphStream.buildDirectedEdgeName(from, to);
		thresholds.put(key, threshold);
	}

	public float getThreshold(String from, String to){
		String key = GraphStream.buildDirectedEdgeName(from, to);
		return thresholds.get(key);
	}

	public void displayWeights() {
		for(String s : thresholds.keySet()){
			if(thresholds.get(s) > 0f){
				System.out.println(s+" => "+thresholds.get(s));
			}
		}
	}

	public String[] getImpactedTests(String changedNode, final float th){
		final List<String> propagationEstimated = new ArrayList<String>();

		for(String t : tests){
			List<String[]> paths = g.getPaths(t, changedNode);
			
			boolean include = true;
			if(paths != null && paths.size() > 0){
				for(String[] p : paths){
					if(!include)	break;
					
					for(EdgeIdentity ei : Graph.getAllEdgesInPath(p)){
						if(!include)	break;
						
						if(getThreshold(ei.getFrom(), ei.getTo()) <= th){
							include = false;
						}
					}
				}
			}else{
				include = false;
			}

			if(include){
				propagationEstimated.add(t);
			}
		}

		return (String[]) propagationEstimated.toArray(new String[0]);
	}
}
