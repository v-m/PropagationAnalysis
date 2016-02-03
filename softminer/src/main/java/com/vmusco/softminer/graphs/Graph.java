package com.vmusco.softminer.graphs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.graphstream.algorithm.Dijkstra;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;
import org.graphstream.graph.implementations.SingleGraph;

import com.vmusco.smf.utils.SourceReference;
import com.vmusco.softminer.exceptions.IncompatibleTypesException;

/***
 * This interface represent an abstract graph representation
 * @author Vincenzo Musco - http://www.vmusco.com
 *
 */
public abstract class Graph {
	private static final Logger logger = LogManager.getFormatterLogger(Graph.class.getSimpleName());
	protected Object graph;
	private long buildTime = -1;

	/**
	 * Generates a new graph which contain only nodes/edges with specific types and markers
	 * @param node
	 * @return
	 */
	public abstract Graph keepOnly(NodeTypes[] nt, NodeMarkers[] nm, EdgeTypes[] et, EdgeMarkers[] em);

	public abstract Graph createNewLikeThis();
	public abstract void addDirectedEdge(String from, String to);
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
		return getPaths(from, to, new GraphVisitorValidator() {
			@Override
			public boolean isNodeAccepted(String from) {
				return true;
			}

			@Override
			public boolean isEdgeAccepted(String arrived, String next) {
				return true;
			}
		});
	}
	
	public List<String[]> getPaths(String from, String to, GraphVisitorValidator nv) {
		if(!hasNode(from) || !hasNode(to) || !nv.isNodeAccepted(from) || !nv.isNodeAccepted(to))
			return null;

		logger.trace("Starting a reccurssion for graph paths [N=%d;E=%d]", getNbNodes(), getNbEdges());

		return getPathsRecur(from, to, new String[0], nv);
	}

	private List<String[]> getPathsRecur(String arrived, String to, String[] path, GraphVisitorValidator nv){
		List<String[]> paths = new ArrayList<String[]>();
		
		for(String next : getNodesConnectedFrom(arrived)){
			logger.trace("Node %s", next);
			if(nv.isNodeAccepted(next) && nv.isEdgeAccepted(arrived, next)){
				if(next.equals(to)){
					// Arrived :)
					String[] cpy = Arrays.copyOf(path, path.length+2);
					cpy[cpy.length-2] = arrived;
					cpy[cpy.length-1] = next;

					paths.add(cpy);
				}else{

					boolean doesNotExistInPath = true;

					// have we already seen this node
					for(int i=0; i<path.length; i++){
						if(path[i].equals(next))
							doesNotExistInPath = false;
					}

					if(doesNotExistInPath){
						String[] cpy = Arrays.copyOf(path, path.length+1);
						cpy[cpy.length-1] = arrived;

						List<String[]> solved = getPathsRecur(next, to, cpy, nv);
						paths.addAll(solved);
					}
				}
			}
		}

		return paths;
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
		if(path.length == 0)
			return null;
		
		EdgeIdentity[] re = new EdgeIdentity[path.length-1];

		for(int i=1; i<path.length; i++){
			re[i-1] = new EdgeIdentity(path[i-1], path[i]);
		}

		return re;

	}

	public abstract boolean hasNode(String name);
	public abstract boolean hasDirectedEdge(String from, String to);
	public abstract void conformizeWith(Graph g) throws IncompatibleTypesException;
	public abstract void conformizeNodeWith(Graph g, String node) throws IncompatibleTypesException;
	public abstract void conformizeEdgeWith(Graph g, String from, String to) throws IncompatibleTypesException;
	public abstract int getNbNodes();
	public abstract int getNbNodes(NodeTypes t);

	public int getNbEdges() {
		return getEdges().length;
	}

	public int getNbEdges(EdgeTypes t){
		return getEdges(t).length;
	}

	public abstract String[] getNodesConnectedFrom(String node);
	public abstract String[] getNodesConnectedTo(String node);


	public EdgeIdentity[] getEdgesConnectedFrom(String node){
		List<EdgeIdentity> ret = new ArrayList<EdgeIdentity>();

		for(String n : getNodesConnectedFrom(node)){
			ret.add(new EdgeIdentity(node, n));
		}

		return ret.toArray(new EdgeIdentity[ret.size()]);
	}
	public EdgeIdentity[] getEdgesConnectedTo(String node){
		List<EdgeIdentity> ret = new ArrayList<EdgeIdentity>();

		for(String n : getNodesConnectedTo(node)){
			ret.add(new EdgeIdentity(n, node));
		}

		return ret.toArray(new EdgeIdentity[ret.size()]);		
	}
	public abstract String[] getNodesNames();
	public abstract EdgeIdentity[] getEdges();

	public EdgeIdentity[] getEdges(EdgeTypes t){
		List<EdgeIdentity> ret = new ArrayList<EdgeIdentity>();

		for(EdgeIdentity n : getEdges()){
			if(getEdgeType(n.getFrom(), n.getTo()) == t)
				ret.add(n);
		}

		return ret.toArray(new EdgeIdentity[ret.size()]);
	}

	public int getOutDegreeFor(String node) {
		return getNodesConnectedFrom(node).length;
	}

	public int getInDegreeFor(String node) {
		return getNodesConnectedTo(node).length;
	}

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


	public abstract void addDirectedEdgeAndNodeIfNeeded(String from, String to);

	public abstract void addNode(String name);

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

	protected void visit(GraphNodeVisitor aVisitor, String node, boolean from){
		ArrayList<String> nodesToExplore = new ArrayList<String>();
		HashSet<String> visitedNodes = new HashSet<String>();

		if(!hasNode(node))
			return;

		// Explore the implied nodes
		nodesToExplore.add(node);
		visitedNodes.add(node);

		while(!nodesToExplore.isEmpty()){
			String cur = nodesToExplore.remove(0);
			if(!aVisitor.isNodeValid(cur)){
				continue;
			}

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

			if(aVisitor.interruptVisit()){
				break;
			}
		}
	}

	/*public void visitEdgeFrom(GraphEdgeVisitor aVisitor, String node){
		visitEdges(aVisitor, node, true);
	}

	public void visitEdgeTo(GraphEdgeVisitor aVisitor, String node){
		visitEdges(aVisitor, node, false);
	}

	private void visitEdges(GraphEdgeVisitor aVisitor, String node, boolean from){
		ArrayList<EdgeIdentity> edgesToExplore = new ArrayList<EdgeIdentity>();
		HashSet<EdgeIdentity> visitedEdges = new HashSet<EdgeIdentity>();

		if(!hasNode(node))
			return;

		// Explore the implied nodes
		if(from)
			edgesToExplore.addAll(Arrays.asList(getEdgesConnectedFrom(node)));
		else
			edgesToExplore.addAll(Arrays.asList(getEdgesConnectedTo(node)));


		while(!edgesToExplore.isEmpty()){
			EdgeIdentity cur = edgesToExplore.remove(0);
			visitedEdges.add(cur);
			aVisitor.visitEdge(cur);

			EdgeIdentity[] edges;
			if(from)
				edges = getEdgesConnectedFrom(cur.getTo());
			else
				edges = getEdgesConnectedTo(cur.getFrom());

			if(edges == null)
				continue;

			for(EdgeIdentity candidate : edges){
				if(!visitedEdges.contains(candidate)){
					edgesToExplore.add(candidate);
				}
			}
		}
	}*/

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
				//aVisitor.visitEdge(cur, candidate);

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

	public abstract void setNodeFormalTypes(String node, List<String> types);
	public abstract List<String> getNodeFormalTypes(String node);

	public abstract void renameNode(String oldname, String newname);

	public void addDirectedEdgeAndNodeIfNeeded(EdgeIdentity ei) {
		addDirectedEdgeAndNodeIfNeeded(ei.getFrom(), ei.getTo());
	}

	/**
	 * Compute the shortest path from a node to another.
	 * Note that edges are unweighted. Each edge is considered as 1
	 * @param from
	 * @param to
	 * @return
	 */
	public abstract String[] shortestPath(String from, String to);

	public EdgeIdentity[] getNodesConnectedFromAndTo(String n) {
		List<EdgeIdentity> ret = new ArrayList<EdgeIdentity>();
		
		for(String to : getNodesConnectedFrom(n)){
			EdgeIdentity ei = new EdgeIdentity(n, to);
			ret.add(ei);
		}
		
		for(String from : getNodesConnectedTo(n)){
			EdgeIdentity ei = new EdgeIdentity(from, n);
			ret.add(ei);
		}
		
		return ret.toArray(new EdgeIdentity[ret.size()]);
	}
}
