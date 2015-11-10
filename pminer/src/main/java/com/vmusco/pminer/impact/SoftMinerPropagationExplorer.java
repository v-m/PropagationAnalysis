package com.vmusco.pminer.impact;

import java.util.HashSet;
import java.util.Set;

import com.vmusco.pminer.exceptions.SpecialEntryPointException;
import com.vmusco.pminer.exceptions.SpecialEntryPointException.TYPE;
import com.vmusco.softminer.graphs.Graph;

/**
 * Uses softminer exported call graph
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class SoftMinerPropagationExplorer extends ConsequencesExplorer{
	public SoftMinerPropagationExplorer(Graph base) {
		super(base);
	}
	
	public void visit(String id) throws SpecialEntryPointException {
		visit(new String[]{ id });
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
	public String[] getLastConsequenceNodes(){
		return getLastConcequenceGraph().getNodesNames();
	}

	@Override
	public String[] getLastConsequenceNodesIn(String[] tests){
		String[] nodesNames = getLastConcequenceGraph().getNodesNames();
		Set<String> r = new HashSet<String>();

		for(String n : nodesNames){
			for(String t : tests){
				if(n.startsWith(t)){
					r.add(n);
					break;
				}
			}
		}

		return r.toArray(new String[0]);
	}
}