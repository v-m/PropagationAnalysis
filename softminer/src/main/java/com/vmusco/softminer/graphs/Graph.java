package com.vmusco.softminer.graphs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import com.vmusco.smf.utils.SourceReference;

/***
 * This interface represent an abstract graph representation
 * @author Vincenzo Musco - http://www.vmusco.com
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

	public abstract Graph createNewLikeThis();
	public abstract void addNode(String name, boolean displayLabel);
	public abstract void addDirectedEdge(String from, String to, boolean displayLabel);
	public abstract void removeDirectedEdge(String from, String to);
	public abstract void removeNode(String id);
	public abstract void bestDisplay();

	/**
	 * Returns all path from <from> to <to>
	 * @param from
	 * @param to
	 * @return a list of strings which each describes a path or null if from or to are not in the graph
	 */
	public List<String[]> getPaths(String from, String to) {
		if(!hasNode(from) || !hasNode(to))
			return null;

		return getPathsRecur(from, to, new String[0]);
	}

	private List<String[]> getPathsRecur(String arrived, String to, String[] visited){
		List<String[]> paths = new ArrayList<String[]>();

		for(String next : getNodesConnectedFrom(arrived)){
			if(next.equals(to)){
				// Arrived :)
				String[] cpy = Arrays.copyOf(visited, visited.length+2);
				cpy[cpy.length-2] = arrived;
				cpy[cpy.length-1] = next;

				paths.add(cpy);
			}else{
				boolean exec = true;
				for(int i=0; i<visited.length; i++){
					if(visited[i].equals(next))
						exec = false;
				}

				if(exec){
					String[] cpy = Arrays.copyOf(visited, visited.length+1);
					cpy[cpy.length-1] = arrived;

					List<String[]> solved = getPathsRecur(next, to, cpy);
					if(solved.size() > 0){
						paths.addAll(solved);
					}
				}
			}
		}

		return paths;
	}

	/**
	 * This methods do the same job that {@link Graph#visitFrom(GraphNodeVisitor, String)} but consider ALL possibles paths.
	 * This has a performance cose, thus if you just need to explore once the graph, prefer using {@link Graph#visitFrom(GraphNodeVisitor, String)} 
	 * @param visitor
	 * @param node
	 */
	public void visitAllPathsFrom(GraphNodeVisitor visitor, String node){
		visitAllPathRec(node, new String[0], visitor, true);
	}

	/**
	 * This methods do the same job that {@link Graph#visitTo(GraphNodeVisitor, String)} but consider ALL possibles paths.
	 * This has a performance cose, thus if you just need to explore once the graph, prefer using {@link Graph#visitTo(GraphNodeVisitor, String)} 
	 * @param visitor
	 * @param node
	 */
	public void visitAllPathsTo(GraphNodeVisitor visitor, String node){
		visitAllPathRec(node, new String[0], visitor, false);
	}

	/**
	 * Visit all nodes and paths possibles in the graph as long as {@link GraphNodeVisitor#interruptVisit()} is false.
	 * Ensure to update the value after each {@link GraphNodeVisitor#visitEdge(String, String)} call in order to get 
	 * the correct state for the last exploration.
	 * Recursive method.
	 * @param arrived the current tested node
	 * @param visited the already visited nodes
	 * @param aVisitor the visitor to notify of visiting edge/node
	 * @param from
	 */
	private void visitAllPathRec(String arrived, String[] visited, GraphNodeVisitor aVisitor, boolean from){
		String[] nodes;

		aVisitor.visitNode(arrived);

		if(from)
			nodes = getNodesConnectedFrom(arrived);
		else
			nodes = getNodesConnectedTo(arrived);

		if(nodes == null)
			return;

		for(String next : nodes){
			for(String candidate : nodes){
				if(from)
					aVisitor.visitEdge(arrived, candidate);
				else
					aVisitor.visitEdge(candidate, arrived);

				if(!aVisitor.interruptVisit()){
					boolean exec = true;
					for(int i=0; i<visited.length; i++){
						if(visited[i].equals(next))
							exec = false;
					}

					if(exec){
						String[] cpy = Arrays.copyOf(visited, visited.length+1);
						cpy[cpy.length-1] = arrived;

						visitAllPathRec(next, cpy, aVisitor, from);
					}
				}
			}
		}
	}

	/**
	 * Determine whether there is at least one path from a node to another
	 * @param from starting node
	 * @param to finish node
	 * @return true if there is at least a path, false otherwise
	 */
	public boolean isThereAtLeastOnePath(final String from, final String to){
		if(!hasNode(from) || !hasNode(to))
			return false;

		GraphNodeVisitor graphNodeVisitor = new DefaultGraphNodeVisitor() {
			boolean found = false;

			@Override
			public void visitNode(String node) {
				if(node.equals(to))
					found = true;
			}

			@Override
			public boolean interruptVisit() {
				return found;
			}

		};
		visitFrom(graphNodeVisitor, from);

		return graphNodeVisitor.interruptVisit();
	}
	public static EdgeIdentity[] getAllEdgesInPath(String[] path){
		EdgeIdentity[] re = new EdgeIdentity[path.length-1];

		for(int i=1; i<path.length; i++){
			re[i-1] = new EdgeIdentity(path[i-1], path[i]);
		}

		return re;

	}

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
	public abstract EdgeIdentity[] getEdges();
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
	public abstract void bindEdgeToSourcePosition(String from, String to, SourceReference sp);
	public abstract SourceReference[] getSourcePositionForEdge(String from, String to);
	public abstract void bindNodeToSourcePosition(String n, SourceReference sp);
	public abstract SourceReference[] getSourcePositionForNode(String n);
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
	public abstract boolean colorEdge(String from, String to, String color);
	public abstract boolean sizeNode(String name, NodeSize size);
	public abstract boolean sizeEdge(String from, String to, int size);
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
			g = new GraphStream();
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

	public abstract boolean labelEdge(String from, String to, String label);
	public abstract boolean labelNode(String name, String label);
	public abstract boolean appendLabelEdge(String from, String to, String label);
	public abstract boolean appendLabelNode(String name, String label);
}
