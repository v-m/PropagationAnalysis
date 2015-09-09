package com.vmusco.pminer.impact;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.vmusco.pminer.exceptions.AlreadyGeneratedException;
import com.vmusco.pminer.exceptions.NoEntryPointException;
import com.vmusco.softminer.graphs.EdgeMarkers;
import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.graphs.GraphNodeVisitor;
import com.vmusco.softminer.graphs.NodeMarkers;
import com.vmusco.softminer.graphs.Graph.NodesNamesForEdge;

/**
 * This class manage subgraphs representing the propagation from a node.
 * A cache is used by this class in order to increase efficiency.
 * @author Vincenzo Musco - http://www.vmusco.com
 *
 */
public abstract class PropagationExplorer{
	final protected Graph base;
	HashMap<String, Graph> cache = new HashMap<String, Graph>();

	public abstract boolean visitTo(String id);
	public abstract String[] getImpactedNodes(String id) throws NoEntryPointException;
	public abstract String[] getImpactedTestNodes(String id, String[] tests) throws NoEntryPointException;

	public PropagationExplorer(Graph base) {
		this.base = base;
	}
	
	public Graph getPropagationGraph(String id){
		return cache.get(id);
	}

	public boolean hasSubGraph(String id){
		return cache.containsKey(id);
	}
	
	protected GraphNodeVisitor populateNew(String id) throws AlreadyGeneratedException{
		if(hasSubGraph(id))
			throw new AlreadyGeneratedException();
		
		final Graph newgraph = base.createNewLikeThis();
		cache.put(id, newgraph);
		
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
	
	
	/**
	 * Directly set on the visiting phase...
	 * @param accordingTo
	 */
	@Deprecated
	public static void propagateMarkersAndTypes(Graph newGraph, Graph accordingTo){
		for(String node : newGraph.getNodesNames()){
			propagateMarkersAndTypesForNode(node, newGraph, accordingTo);
		}

		for(NodesNamesForEdge nnfe : newGraph.getEdges()){
			propagateMarkersAndTypesForEdge(nnfe.from, nnfe.to, newGraph, accordingTo);
		}
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


	public int getNbTestNodes(String id, String[] tests) throws NoEntryPointException{
		return getImpactedTestNodes(id, tests).length;
	}


	public int getNbNodes(String id) throws NoEntryPointException {
		return getImpactedNodes(id).length;
	}

}