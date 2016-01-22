package com.vmusco.pminer.impact;

import com.vmusco.pminer.exceptions.SpecialEntryPointException;
import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.MutationStatistics;
import com.vmusco.softminer.exceptions.IncompatibleTypesException;
import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.graphs.GraphNodeVisitor;

/**
 * This class manage subgraphs representing the consequences from one or several node(s).
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public abstract class ConsequencesExplorer{
	final protected Graph base;
	protected Graph last_propa;
	
	public abstract void visit(String[] id) throws SpecialEntryPointException;
	public abstract void visit(MutationStatistics ms, MutantIfos mi) throws SpecialEntryPointException;
	public abstract String[] getLastConsequenceNodes();
	//public abstract String[] getLastConsequenceNodesIn(String[] nodes);

	public ConsequencesExplorer(Graph base) {
		this.base = base;
		last_propa = base.createNewLikeThis();
	}
	
	public Graph getLastConcequenceGraph(){
		return last_propa;
	}
	
	protected GraphNodeVisitor populateNew() {
		final Graph newgraph = base.createNewLikeThis();
		last_propa = newgraph;
		
		return populateCurrent();
	}
	
	protected GraphNodeVisitor populateCurrent() {
		final Graph newgraph = last_propa;
		
		return new GraphNodeVisitor() {

			@Override
			public void visitNode(String node) {
				newgraph.addNode(node);
				try {
					newgraph.conformizeNodeWith(base, node);
				} catch (IncompatibleTypesException e) {
					//TODO: proper logging
					e.printStackTrace();
				}
			}

			@Override
			public void visitEdge(String from, String to) {
				newgraph.addDirectedEdgeAndNodeIfNeeded(from, to);
				try {
					newgraph.conformizeEdgeWith(base, from, to);
				} catch (IncompatibleTypesException e) {
					//TODO: proper logging
					e.printStackTrace();
				}
			}

			@Override
			public String[] nextNodesToVisitFrom(String node) {
				return base.getNodesConnectedTo(node);
			}

			@Override
			public boolean interruptVisit() {
				return false;
			}
		};
	}

	public int getLastNbNodes(String id) throws SpecialEntryPointException {
		return getLastConsequenceNodes().length;
	}
	
	public Graph getBaseGraph() {
		return base;
	}

	public int getBaseGraphNodesCount(){
		return base.getNbNodes();
	}
	
	public int getBaseGraphEdgesCount(){
		return base.getNbEdges();
	}
}