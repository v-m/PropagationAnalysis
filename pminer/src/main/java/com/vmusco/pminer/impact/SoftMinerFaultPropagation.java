package com.vmusco.pminer.impact;

import java.util.HashSet;
import java.util.Set;

import com.vmusco.pminer.exceptions.SpecialEntryPointException;
import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.graphs.GraphTools;

/**
 * Uses softminer exported call graph
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class SoftMinerFaultPropagation extends ConsequencesExplorer{
	protected Graph final_union = null;
	private int hasFaulty = 0;
	private String faultyNode;
	private boolean intersect;
	
	public SoftMinerFaultPropagation(Graph base, String faultyNode, boolean intersect) {
		super(base);
		this.faultyNode = faultyNode;
		this.intersect = intersect;
	}
	
	@Override
	public void visit(String[] tests) throws SpecialEntryPointException {
		
		for(String id : tests){
			if(!base.hasNode(id)){
				//System.err.println("Node not found: "+id);
				continue;
			}
			
			if(base.getOutDegreeFor(id) <= 0 && base.getInDegreeFor(id) <= 0){
				//System.err.println("Isolated node: "+id);
				continue;
			}
			
			base.visitFrom(populateNew(), id);
			
			if(final_union == null){
				final_union = last_propa;
			}else{
				if(intersect)
					final_union = GraphTools.intersect(final_union, last_propa);
				else
					final_union = GraphTools.union(final_union, last_propa);
			}

			
			if(last_propa.hasNode(faultyNode))
				hasFaulty++;
		}
	}
	
	public int getNbHasFaulty() {
		return hasFaulty;
	}
	
	@Override
	public Graph getLastConcequenceGraph() {
		return final_union;
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