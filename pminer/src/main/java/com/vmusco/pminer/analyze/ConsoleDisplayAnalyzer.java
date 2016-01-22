package com.vmusco.pminer.analyze;

import com.vmusco.smf.analysis.MutantIfos;

/**
 * This class is used to display to the console the impacts
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class ConsoleDisplayAnalyzer extends MutantTestAnalyzer {
	@Override
	public void intersectionFound(MutantIfos mi, String[] ais, String[] cis) {
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

	@Override
	public void unboundedFound(MutantIfos mi) {
		
	}

	@Override
	public void isolatedFound(MutantIfos mi) {
		
	}

}
