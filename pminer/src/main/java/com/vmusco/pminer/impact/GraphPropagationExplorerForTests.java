package com.vmusco.pminer.impact;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.vmusco.pminer.exceptions.SpecialEntryPointException;
import com.vmusco.pminer.exceptions.SpecialEntryPointException.TYPE;
import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.MutationStatistics;
import com.vmusco.softminer.graphs.Graph;

/**
 * Uses softminer exported call graph
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class GraphPropagationExplorerForTests extends GraphPropagationExplorer{
	private String[] tests;

	public GraphPropagationExplorerForTests(Graph base, String[] tests) {
		super(base);
		this.tests = tests;
	}
	
	@Override
	public String[] getLastConsequenceNodes(){
		List<String> ret = new ArrayList<>();
		
		List<String> tests = Arrays.asList(this.tests);
		
		for(String node : super.getLastConsequenceNodes()){
			if(tests.contains(node)){
				ret.add(node);
			}
		}
		
		return ret.toArray(new String[ret.size()]);
	}
}