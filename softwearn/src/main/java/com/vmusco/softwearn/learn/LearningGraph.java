package com.vmusco.softwearn.learn;

import com.vmusco.softminer.graphs.EdgeIdentity;
import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.graphs.GraphNodeVisitor;

/**
 * Interface which defines a graph with weight layer on top of edges
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public interface LearningGraph {
	void setEdgeThreshold(String from, String to, float threshold);
	float getEdgeThreshold(String from, String to);
	void displayWeights();
	Graph getPrunedGraph(float treshold);
	float getDefaultTreshold();
	void setDefaultTreshold(float treshold);
	
	/**
	 * Method allowing access to direct Graph object (casting or access)
	 * @return
	 */
	Graph graph();
	
	void addDirectedEdge(String from, String to);
	void addDirectedEdge(String from, String to, float treshold);
	boolean hasDirectedEdge(String from, String to, float th);
	
	EdgeIdentity[] getEdges(float treshold);
	void visitFrom(final GraphNodeVisitor aVisitor, final String node, final float treshold);
	void visitTo(final GraphNodeVisitor aVisitor, final String node, final float treshold);
	
	public abstract String[] getNodesConnectedFrom(String node, float th);
	public abstract String[] getNodesConnectedTo(String node, float th);
	
	/**
	 * This threshold is used by edge accessing methods.
	 * If an edge weight is not in the right range, then the graph object
	 * consider the edge is not there... 
	 * @return
	 */
	public float getThreshold();
	/**
	 * This threshold is used by edge accessing methods.
	 * If an edge weight is not in the right range, then the graph object
	 * consider the edge is not there... 
	 * Do not use a threshold on learning phase as it will result in unpredicted and bad results.
	 * @return
	 */
	public void setThreshold(float threshold);

	public void switchToLearningPhase();
	boolean isLearningPhrase();
	
	/*
	 * Overriding default edges access using the "current threshold"
	 */
	
	public EdgeIdentity[] getEdges();
	public void visitFrom(GraphNodeVisitor aVisitor, String node);
	public void visitTo(GraphNodeVisitor aVisitor, String node);
	
	/**
	 * Set all edges to default edge value (instantiate for a new learning cycle)
	 * Switch to learning phase
	 */
	void resetLearnedInformations();
}
