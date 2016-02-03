package com.vmusco.softminer.graphs;

/**
*
* @author Vincenzo Musco - http://www.vmusco.com
*/
public interface GraphNodeVisitor {
	void visitNode(String node);
	void visitEdge(String from, String to);
	boolean isNodeValid(String node);
	String[] nextNodesToVisitFrom(String node);
	boolean interruptVisit();
}
