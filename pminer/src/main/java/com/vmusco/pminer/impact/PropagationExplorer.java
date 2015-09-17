package com.vmusco.pminer.impact;

import com.vmusco.pminer.exceptions.AlreadyGeneratedException;
import com.vmusco.pminer.exceptions.SpecialEntryPointException;
import com.vmusco.softminer.graphs.EdgeIdentity;
import com.vmusco.softminer.graphs.EdgeMarkers;
import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.graphs.GraphNodeVisitor;
import com.vmusco.softminer.graphs.NodeMarkers;

/**
 * This class manage subgraphs representing the propagation from a node.
 * Cache removed due to over memory consumption
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public abstract class PropagationExplorer{
	final protected Graph base;
	//HashMap<String, Graph> cache = new HashMap<String, Graph>();
	protected Graph last_propa;
	protected String last_entryid;
	
	public abstract void visitTo(String id) throws SpecialEntryPointException;
	//public abstract String[] getImpactedNodes(String id) throws NoEntryPointException;
	//public abstract String[] getImpactedTestNodes(String id, String[] tests) throws NoEntryPointException;
	public abstract String[] getLastImpactedNodes();
	public abstract String[] getLastImpactedTestNodes(String[] tests);

	public PropagationExplorer(Graph base) {
		this.base = base;
	}
	
	/*public Graph getPropagationGraph(String id){
		return cache.get(id);
	}

	public boolean hasSubGraph(String id){
		return cache.containsKey(id);
	}*/
	
	public Graph getLastPropagationGraph(){
		return last_propa;
	}
	
	protected GraphNodeVisitor populateNew(String id) throws AlreadyGeneratedException{
		//if(hasSubGraph(id))
		//	throw new AlreadyGeneratedException();
		last_entryid = id;
		final Graph newgraph = base.createNewLikeThis();
		last_propa = newgraph;
		//cache.put(id, newgraph);
		
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

		for(EdgeIdentity nnfe : newGraph.getEdges()){
			propagateMarkersAndTypesForEdge(nnfe.getFrom(), nnfe.getTo(), newGraph, accordingTo);
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


	/*public int getNbTestNodes(String id, String[] tests) throws NoEntryPointException{
		return getImpactedTestNodes(id, tests).length;
	}


	public int getNbNodes(String id) throws NoEntryPointException {
		return getImpactedNodes(id).length;
	}*/
	
	public int getLastNbTestNodes(String id, String[] tests) throws SpecialEntryPointException{
		return getLastImpactedTestNodes(tests).length;
	}


	public int getLastNbNodes(String id) throws SpecialEntryPointException {
		return getLastImpactedNodes().length;
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