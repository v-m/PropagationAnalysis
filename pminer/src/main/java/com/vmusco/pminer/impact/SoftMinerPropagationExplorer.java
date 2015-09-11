package com.vmusco.pminer.impact;

import java.util.HashSet;
import java.util.Set;

import com.vmusco.pminer.exceptions.AlreadyGeneratedException;
import com.vmusco.pminer.exceptions.NoEntryPointException;
import com.vmusco.softminer.graphs.Graph;

/**
 * Uses softminer exported call graph
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class SoftMinerPropagationExplorer extends PropagationExplorer{
	public SoftMinerPropagationExplorer(Graph base) {
		super(base);
	}

	@Override
	public void visitTo(String id) throws NoEntryPointException {
		try {
			if(!base.hasNode(id)){
				throw new NoEntryPointException();
			}
			
			base.visitTo(populateNew(id), id);
		} catch (AlreadyGeneratedException e) {
			// If already generated, nothing to do...
		}
	}

	
	
	@Override
	public String[] getLastImpactedNodes() throws NoEntryPointException{
		return getLastPropagationGraph().getNodesNames();
	}

	@Override
	public String[] getLastImpactedTestNodes(String[] tests) throws NoEntryPointException{
		String[] nodesNames = getLastPropagationGraph().getNodesNames();
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