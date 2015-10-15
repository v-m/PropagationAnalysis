package com.vmusco.pminer.analyze;

import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.ProcessStatistics;
import com.vmusco.smf.exceptions.MutationNotRunException;

/**
 * This class is used to display to the console the impacts
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class ConsoleDisplayAnalyzer extends MutantTestAnalyzer {
	@Override
	public void fireIntersectionFound(ProcessStatistics ps, MutantIfos mi, String[] impactedNodes, String[] impactedTests) throws MutationNotRunException{
		String[] ais = ps.getCoherentMutantFailAndHangTestCases(mi.getExecutedTestsResults()); 
		String[] cis = impactedTests;
		
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
