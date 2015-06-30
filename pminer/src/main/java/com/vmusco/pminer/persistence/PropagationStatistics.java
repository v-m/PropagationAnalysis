package com.vmusco.pminer.persistence;

import java.util.HashMap;

import com.vmusco.pminer.UseGraph;
import com.vmusco.pminer.analyze.UseGraphMutantStats;

public class PropagationStatistics {
	private HashMap<String, UseGraph> sprop = new HashMap<String, UseGraph>();
	private HashMap<String, UseGraphMutantStats> stats = new HashMap<String, UseGraphMutantStats>();

	public void addPropagationInMutation(String mutid, UseGraph u, UseGraphMutantStats ms){
		sprop.put(mutid, u);
		stats.put(mutid, ms);
	}

	public UseGraph getPropagationInMutation(String mutid){
		return sprop.get(mutid);
	}
	
	public UseGraphMutantStats getPropagationStatsticsInMutation(String mutid){
		return stats.get(mutid);
	}

	public String[] getAllPropagationsMutantsIds() {
		return sprop.keySet().toArray(new String[0]);
	}
}
