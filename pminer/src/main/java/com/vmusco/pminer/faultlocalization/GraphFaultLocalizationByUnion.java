package com.vmusco.pminer.faultlocalization;

import com.vmusco.pminer.exceptions.SpecialEntryPointException;
import com.vmusco.pminer.impact.ConsequencesExplorer;
import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.MutationStatistics;
import com.vmusco.smf.exceptions.MutationNotRunException;
import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.graphs.GraphNodeVisitor;

/**
 * Uses softminer exported call graph
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class GraphFaultLocalizationByUnion extends ConsequencesExplorer{
	
	public GraphFaultLocalizationByUnion(Graph base) {
		super(base);
	}
	
	@Override
	public void visit(String[] tests) {
		
		for(String id : tests){
			if(!base.hasNode(id)){
				System.err.println("Node not found: "+id);
				continue;
			}
			
			base.visitFrom(populate(), id);
			finishedOneVisit();
		}
	}
	

	@Override
	public void visit(MutationStatistics ms, MutantIfos mi) throws SpecialEntryPointException {
		try {
			visit(ms.getCoherentMutantFailAndHangTestCases(mi.getExecutedTestsResults()));
		} catch (MutationNotRunException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	protected void finishedOneVisit() {
	}

	protected GraphNodeVisitor populate() {
		return populateCurrent();
	}

	@Override
	public String[] getLastConsequenceNodes(){
		return getLastConcequenceGraph().getNodesNames();
	}
}