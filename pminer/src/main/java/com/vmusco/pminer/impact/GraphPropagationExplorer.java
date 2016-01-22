package com.vmusco.pminer.impact;

import java.util.ArrayList;
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
public class GraphPropagationExplorer extends ConsequencesExplorer{
	public GraphPropagationExplorer(Graph base) {
		super(base);
	}
	
	public void visit(String id) throws SpecialEntryPointException {
		visit(new String[]{ id });
	}

	@Override
	public void visit(MutationStatistics ms, MutantIfos mi) throws SpecialEntryPointException {
		visit(mi.getMutationIn());
	}
	
	/**
	 * Takes into consideration only the fist node for propagation estimation
	 */
	@Override
	public void visit(String[] ids) throws SpecialEntryPointException {
		String id = ids[0];

		if(!base.hasNode(id)){
			throw new SpecialEntryPointException(TYPE.NOT_FOUND);
		}
		
		if(base.getOutDegreeFor(id) <= 0 && base.getInDegreeFor(id) <= 0){
			throw new SpecialEntryPointException(TYPE.ISOLATED);
		}
		
		base.visitTo(populateNew(), id);
	}

	@Override
	public String[] getLastConsequenceNodes() {
		return getLastConcequenceGraph().getNodesNames();
	}
}