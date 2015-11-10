package com.vmusco.pminer.impact;

import com.vmusco.pminer.exceptions.SpecialEntryPointException;
import com.vmusco.softminer.graphs.EdgeMarkers;
import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.graphs.GraphNodeVisitor;
import com.vmusco.softminer.graphs.NodeMarkers;

/**
 * This class manage subgraphs representing the concequences from one or several node(s).
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public abstract class ConsequencesExplorer{
	final protected Graph base;
	protected Graph last_propa;
	
	public abstract void visit(String[] id) throws SpecialEntryPointException;
	public abstract String[] getLastConsequenceNodes();
	public abstract String[] getLastConsequenceNodesIn(String[] nodes);

	public ConsequencesExplorer(Graph base) {
		this.base = base;
	}
	
	public Graph getLastConcequenceGraph(){
		return last_propa;
	}
	
	protected GraphNodeVisitor populateNew() {
		final Graph newgraph = base.createNewLikeThis();
		last_propa = newgraph;
		
		return new GraphNodeVisitor() {

			@Override
			public void visitNode(String node) {
				newgraph.addNode(node, false);
				SoftMinerPropagationExplorer.propagateMarkersAndTypesForNode(node, newgraph, base);
			}

			@Override
			public void visitEdge(String from, String to) {
				newgraph.addDirectedEdgeAndNodeIfNeeded(from, to,false, false);

				if(base.hasDirectedEdge(from, to)){
					SoftMinerPropagationExplorer.propagateMarkersAndTypesForEdge(from, to, newgraph, base);
				}else{
					for(EdgeMarkers aMarker : base.getEdgeMarkers(from, to)){
						newgraph.markEdge(from, to, aMarker);
					}

					newgraph.setEdgeType(from, to, base.getEdgeType(from, to));
				}
			}

			@Override
			public String[] nextNodesToVisitFrom(String node) {
				return base.getNodesConnectedTo(node);
			}
		};
	}
	
	
	public static void propagateMarkersAndTypesForNode(String node, Graph newGraph, Graph accordingTo){
		for(NodeMarkers aMarker : accordingTo.getNodeMarkers(node)){
			newGraph.markNode(node, aMarker);
		}

		newGraph.setNodeType(node, accordingTo.getNodeType(node));
	}

	public static void propagateMarkersAndTypesForEdge(String from, String to, Graph newGraph, Graph accordingTo){
		for(EdgeMarkers aMarker : accordingTo.getEdgeMarkers(from, to)){
			newGraph.markEdge(from, to, aMarker);
		}

		newGraph.setEdgeType(from, to, accordingTo.getEdgeType(from, to));
	}

	public int getLastNbTestNodes(String id, String[] tests) throws SpecialEntryPointException{
		return getLastConsequenceNodesIn(tests).length;
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