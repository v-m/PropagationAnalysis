package com.vmusco.softminer.graphs;

public interface GraphNodeVisitor {
	void visitNode(String node);
	void visitEdge(String from, String to);
	String[] nextNodesToVisitFrom(String node);
}
