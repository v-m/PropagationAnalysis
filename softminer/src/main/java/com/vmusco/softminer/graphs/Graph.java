package com.vmusco.softminer.graphs;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

/***
 * This interface represent an abstract graph representation
 * @author Vincenzo Musco - vincenzo.musco@inria.fr
 *
 */
public abstract class Graph {
	protected Object graph;
	private long buildTime = -1;
	
	/**
	 * Generates a new graph which contain only nodes/edges with specific types and markers
	 * @param node
	 * @return
	 */
	public abstract Graph keepOnly(NodeTypes[] nt, NodeMarkers[] nm, EdgeTypes[] et, EdgeMarkers[] em);
	
	public abstract void addNode(String name, boolean displayLabel);
	public abstract void addDirectedEdge(String from, String to, boolean displayLabel);
	public abstract void bestDisplay();
	public abstract boolean hasNode(String name);
	public abstract boolean hasDirectedEdge(String from, String to);
	public abstract void addDirectedEdgeAndNodeIfNeeded(String from, String to, boolean nodesLabel, boolean edgesLabel);
	public abstract int getNbNodes();
	public abstract int getNbNodes(NodeTypes t);
	public abstract int getNbEdges();
	public abstract int getNbEdges(EdgeTypes t);
	public abstract String[] getNodesConnectedFrom(String node);
	public abstract String[] getNodesConnectedTo(String node);
	public abstract String[] getNodesNames();
	public abstract NodesNamesForEdge[] getEdges();
	public abstract int getOutDegreeFor(String node);
	public abstract int getInDegreeFor(String node);
	public int getDegreeFor(String node){
		return getOutDegreeFor(node) + getInDegreeFor(node);
	}
	
	public long getBuildTime() {
		return buildTime;
	}
	public void setBuildTime(long buildTime) {
		this.buildTime = buildTime;
	}

	public abstract void markEdge(String from, String to, EdgeMarkers aMarker);
	public abstract void setEdgeType(String from, String to, EdgeTypes aType);
	public abstract boolean isEdgeMarked(String from, String to, EdgeMarkers aMarker);
	public abstract EdgeTypes getEdgeType(String from, String to);
	public abstract void markNode(String node, NodeMarkers aMarker);
	public abstract void setNodeType(String node, NodeTypes aType);
	public abstract boolean isNodeMarked(String node, NodeMarkers aMarker);
	public abstract NodeTypes getNodeType(String node);
	public abstract NodeMarkers[] getNodeMarkers(String node);
	public abstract EdgeMarkers[] getEdgeMarkers(String from, String to);
		
	public abstract GraphApi getGraphFamily();
	public abstract String[] computeShortestPath(String from, String to);

	public abstract boolean colorNode(String name, String color);
	public abstract boolean sizeNode(String name, NodeSize size);
	public abstract boolean shapeNode(String name, NodeShape shape);
	public abstract boolean shadowNode(String name, int shadowSize, String shadowColor);

	public int nbNodesMarkedAs(NodeMarkers aMarker){
		int cpt = 0;
		
		for(String node : getNodesNames()){
			cpt += isNodeMarked(node, aMarker)?1:0;
		}
		
		return cpt;
	}
	
	public int nbNodesOfType(NodeTypes aType){
		int cpt = 0;
		
		for(String node : getNodesNames()){
			cpt += getNodeType(node).equals(aType)?1:0;
		}
		
		return cpt;
	}
	
	public static Graph getNewGraph(GraphApi anApi){
		Graph g = null;

		switch(anApi){
		case GRAPH_STREAM:
			g = GraphStream.instantiate();
		}

		return g;
	}


	public void addDirectedEdgeAndNodeIfNeeded(String from, String to){
		this.addDirectedEdgeAndNodeIfNeeded(from, to, false, false);
	}

	public void addDirectedEdge(String from, String to){
		this.addDirectedEdge(from, to, false);
	}

	public void addNode(String name){
		this.addNode(name, false);
	}

	public enum GraphApi{
		GRAPH_STREAM
	}

	public enum NodeSize{
		SMALL, NORMAL, LARGE
	}

	public enum NodeShape{
		CIRCLE, BOX, ROUNDED_BOX, DIAMOND, CROSS, PIES
	}

	public Object getGraph() {
		return graph;
	}

	public class NodesNamesForEdge{
		public String from;
		public String to;
	}

	public void visitAllNodes(GraphNodeVisitor aVisitor){

	}

	/***
	 * This method visit each out degree of node and to all its children 
	 * recursively (do not invoke twice when node already visited)
	 * On impact analysis, use this method in case of reversed graph
	 * @param aVisitor
	 * @param node the target node from which we start the visiting
	 */
	public void visitFrom(GraphNodeVisitor aVisitor, String node){
		visit(aVisitor, node, true);
	}

	/***
	 * This method visit each in-degree (entering) of node and to all its children 
	 * recursively (do not invoke twice when node already visited)
	 * On impact analysis, use this method in case of normal use graph
	 * @param aVisitor
	 * @param node the target node from which we start the visiting
	 */
	public void visitTo(GraphNodeVisitor aVisitor, String node){
		visit(aVisitor, node, false);
	}
	
	private void visit(GraphNodeVisitor aVisitor, String node, boolean from){
		ArrayList<String> nodesToExplore = new ArrayList<String>();
		HashSet<String> visitedNodes = new HashSet<String>();

		if(!hasNode(node))
			return;

		// Explore the implied nodes
		nodesToExplore.add(node);
		visitedNodes.add(node);

		while(!nodesToExplore.isEmpty()){
			String cur = nodesToExplore.remove(0);
			aVisitor.visitNode(cur);

			String[] nodes;
			if(from)
				nodes = getNodesConnectedFrom(cur);
			else
				nodes = getNodesConnectedTo(cur);

			if(nodes == null)
				continue;

			for(String candidate : nodes){
				if(from)
					aVisitor.visitEdge(cur, candidate);
				else
					aVisitor.visitEdge(candidate, cur);
				
				if(!visitedNodes.contains(candidate)){
					visitedNodes.add(candidate);
					nodesToExplore.add(candidate);
				}
			}
		}
	}

	public long visitDirectedByGraphNodeVisitor(GraphNodeVisitor aVisitor, String node){
		ArrayList<String> nodesToExplore = new ArrayList<String>();
		HashSet<String> visitedNodes = new HashSet<String>();

		long start_time = System.nanoTime();
		if(!hasNode(node)){
			return -1;
		}

		// Explore the implied nodes
		nodesToExplore.add(node);
		visitedNodes.add(node);

		while(!nodesToExplore.isEmpty()){
			String cur = nodesToExplore.remove(0);
			//System.out.println(cur);
			aVisitor.visitNode(cur);

			String[] nodes;
			nodes = getNodesConnectedTo(cur);
			nodes = aVisitor.nextNodesToVisitFrom(cur);

			if(nodes == null)
				continue;

			for(String candidate : nodes){
				aVisitor.visitEdge(cur, candidate);
				
				if(!visitedNodes.contains(candidate)){
					visitedNodes.add(candidate);
					nodesToExplore.add(candidate);
				}
			}
		}
		
		long end_time = System.nanoTime();
		
		return end_time - start_time;
	}

	public abstract void persistAsImage(String persistTo, boolean shake);
	public void persistAsImage(String persistTo){
		persistAsImage(persistTo, true);
	}
}
