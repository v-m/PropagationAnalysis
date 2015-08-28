package com.vmusco.pminer;

import java.util.HashSet;
import java.util.Set;

import com.vmusco.softminer.graphs.EdgeMarkers;
import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.graphs.GraphNodeVisitor;
import com.vmusco.softminer.graphs.NodeMarkers;
import com.vmusco.softminer.graphs.Graph.NodesNamesForEdge;

/**
 * This class build a sub graph of the original graph with only nodes implied
 * in the propagation
 * @author Vincenzo Musco - http://www.vmusco.com
 *
 */
public class UseGraph implements GraphNodeVisitor {
	private Graph concernedGraph;
	private Graph newGraph;

	public UseGraph(Graph base) {
		this.concernedGraph = base;
		this.newGraph = Graph.getNewGraph(base.getGraphFamily());
	}

	public void visitNode(String node) {
		newGraph.addNode(node, false);
		propagateMarkersAndTypesForNode(node, this.concernedGraph);
	}

	/*public void visitEdge(String from, String to) {
		newGraph.addDirectedEdgeAndNodeIfNeeded(to, from,false, false);

		if(this.concernedGraph.hasDirectedEdge(to, from)){
			propagateMarkersAndTypesForEdge(to, from, this.concernedGraph);
		}else{
			for(EdgeMarkers aMarker : this.concernedGraph.getEdgeMarkers(from, to)){
				newGraph.markEdge(to, from, aMarker);
			}

			newGraph.setEdgeType(to, from, this.concernedGraph.getEdgeType(from, to));
		}
	}*/

	public void visitEdge(String from, String to) {
		newGraph.addDirectedEdgeAndNodeIfNeeded(from, to,false, false);

		if(this.concernedGraph.hasDirectedEdge(from, to)){
			propagateMarkersAndTypesForEdge(from, to, this.concernedGraph);
		}else{
			for(EdgeMarkers aMarker : this.concernedGraph.getEdgeMarkers(from, to)){
				newGraph.markEdge(from, to, aMarker);
			}

			newGraph.setEdgeType(from, to, this.concernedGraph.getEdgeType(from, to));
		}
	}

	/**
	 * Directly set on the visiting phase...
	 * @param accordingTo
	 */
	@Deprecated
	public void propagateMarkersAndTypes(Graph accordingTo){
		for(String node : newGraph.getNodesNames()){
			propagateMarkersAndTypesForNode(node, accordingTo);
		}

		for(NodesNamesForEdge nnfe : newGraph.getEdges()){
			propagateMarkersAndTypesForEdge(nnfe.from, nnfe.to, accordingTo);
		}
	}

	public void propagateMarkersAndTypesForNode(String node, Graph accordingTo){
		for(NodeMarkers aMarker : accordingTo.getNodeMarkers(node)){
			newGraph.markNode(node, aMarker);
		}

		newGraph.setNodeType(node, accordingTo.getNodeType(node));
	}

	public void propagateMarkersAndTypesForEdge(String from, String to, Graph accordingTo){
		for(EdgeMarkers aMarker : accordingTo.getEdgeMarkers(from, to)){
			newGraph.markEdge(from, to, aMarker);
		}

		newGraph.setEdgeType(from, to, accordingTo.getEdgeType(from, to));
	}

	public String[] getBasinNodes(){
		return this.newGraph.getNodesNames();
	}

	public Graph getBasinGraph(){
		return this.newGraph;
	}

	public int getNbTestNodes(String[] tests){
		return getTestNodes(tests).length;
	}

	public String[] getTestNodes(String[] tests){
		String[] nodesNames = this.newGraph.getNodesNames();
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

	public String[] nextNodesToVisitFrom(String node) {
		return this.concernedGraph.getNodesConnectedTo(node);
	}
}
