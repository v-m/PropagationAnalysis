package com.vmusco.pminer.tests.faultloc;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.vmusco.pminer.faultlocalization.FaultLocalizationStats;
import com.vmusco.smf.exceptions.BadStateException;
import com.vmusco.smf.exceptions.MutationNotRunException;
import com.vmusco.softminer.graphs.Graph;

public final class FaultLocatorTester {
	private FaultLocalizationStats stats;
	private String[] interNodesList;

	
	public FaultLocatorTester(Graph g, String[] testcases, String changePoint, Map<String, Set<String>> calledNodes, String[] fails) throws MutationNotRunException, BadStateException {
		this(g, testcases, changePoint, calledNodes, fails, g.getNodesNames());
	}

	/**
	 * 
	 * @param g the software graph
	 * @param testcases the list of tests defined in the program
	 * @param changePoint the point (UUT) under consideration
	 * @param calledNodes the list of tests which are really run by the UUTs (key = UUTS, value = List of strings). This item defines the E and N set ! 
	 * @param fails the list of failing tests when running. This item defines the X_p and X_f sets (where X is T, E and N).
	 * @param UUTs consdered UUTs
	 * @throws MutationNotRunException 
	 * @throws BadStateException 
	 */
	public FaultLocatorTester(Graph g, String[] testcases, String changePoint, Map<String, Set<String>> calledNodes, String[] fails, String[] UUTs) throws MutationNotRunException, BadStateException {
		stats = new FaultLocalizationStats(UUTs, testcases, g);

		stats.changeMutantIdentity(calledNodes, fails);
		stats.changeTestingNode(changePoint);

		Graph ig = stats.getLastIntersectedGraph();

		interNodesList = new String[0];
		if(ig != null)
			interNodesList = ig.getNodesNames();
	}

	public String getChangePoint() {
		return stats.getCurrentTestingNode();
	}
	
	public String[] getInterNodesList() {
		return interNodesList;
	}
	
	public FaultLocalizationStats getStats() {
		return stats;
	}
	
	public static class ScoreAndWastedEffort{
		double score;
		int wastedeffort;
		public Map<String, Double> allscores;
		
		@Override
		public String toString() {
			String ret = String.format("Score: %f. Wastedeffort: %d.\n", score, wastedeffort);
			
			for(String k : allscores.keySet()){
				ret += String.format("\t%s -> %f\n", k, allscores.get(k));
			}
			
			return ret;
		}
	}

}
