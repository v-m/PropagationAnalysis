package com.vmusco.pminer.faultlocalization;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.MutationStatistics;
import com.vmusco.smf.exceptions.BadStateException;
import com.vmusco.smf.exceptions.MutationNotRunException;
import com.vmusco.softminer.graphs.Graph;

/**
 * Class used for containing statistics about fault localization while processing. 
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class FaultLocalizationStatsWithMutantIfos extends FaultLocalizationStats {
	static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getFormatterLogger(FaultLocalizationStatsWithMutantIfos.class.getSimpleName());
	protected MutationStatistics ms;
	protected MutantIfos mi;
	
	public FaultLocalizationStatsWithMutantIfos(MutationStatistics ms) throws MutationNotRunException, BadStateException {
		this(ms, null);
	}
	
	public FaultLocalizationStatsWithMutantIfos(MutationStatistics ms, Graph base) throws MutationNotRunException, BadStateException {
		super(ms.getTestExecutionResult().getAllCalledNodesForAllTests(), 
				ms.getTestCases(), 
				base);
		this.ms = ms;
	}
	
	public void changeMutantIdentity(MutantIfos mi) throws MutationNotRunException{
		this.mi = mi;

		String[] coherentMutantFailAndHangTestCases = ms.getCoherentMutantFailAndHangTestCases(mi.getExecutedTestsResults());
		
		changeMutantIdentity(UUTUsedByTests(mi.getExecutedTestsResults().getCalledNodes()), 
				coherentMutantFailAndHangTestCases);
	}
	
	public String getMutationIn() {
		return mi.getMutationIn();
	}
	
	public static Map<String, Set<String>> UUTUsedByTests(Map<String, String[]> in){
		Map<String, Set<String>> out = new HashMap<>();
		
		for(String test : in.keySet()){
			for(String uut : in.get(test)){
				if(!out.containsKey(uut)){
					out.put(uut, new HashSet<String>());
				}
				
				out.get(uut).add(test);
			}
		}
		
		return out;
	}
}
