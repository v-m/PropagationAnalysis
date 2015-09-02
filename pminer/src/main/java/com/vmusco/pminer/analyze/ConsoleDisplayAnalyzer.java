package com.vmusco.pminer.analyze;

import com.vmusco.pminer.UseGraph;
import com.vmusco.pminer.compute.CIAEstimationSets;
import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.ProcessStatistics;
import com.vmusco.smf.exceptions.MutationNotRunException;
import com.vmusco.softminer.graphs.Graph;

/**
 * This class is used to display to the console the impacts
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class ConsoleDisplayAnalyzer extends MutantTestAnalyzer {
	@Override
	public void fireIntersectionFound(ProcessStatistics ps, MutantIfos mi, UseGraph graph) throws MutationNotRunException{
		String[] ais = mi.getExecutedTestsResults().getCoherentMutantFailAndHangTestCases(ps); 
		String[] cis = ExploreMutants.getRetrievedTests(graph, ps.getTestCases());
		
		CIAEstimationSets sets = new CIAEstimationSets(cis, ais);

		for(String aTest : sets.getFoundImpactedSet()){
			System.out.println("\u001b[32m"+"\t"+aTest+"\u001b[0m");
		}

		for(String aTest : sets.getFalsePositivesImpactedSet()){
			System.out.println("\u001b[31m"+"\t"+aTest+"\u001b[0m");
		}

		for(String aTest : sets.getDiscoveredImpactedSet()){
			System.out.println("\u001b[90m"+"\t"+aTest+"\u001b[0m");
		}
	}

}
