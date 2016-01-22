package com.vmusco.softminer.graphs;

/**
 * Simple adapter. Do not use directly {@link Graph#visitDirectedByGraphNodeVisitor(GraphNodeVisitor, String)} with this adapter as
 * it will result in failing visit.
 * @author Vincenzo Musco - http://www.vmusco.com
 *
 */
public class GraphNodeVisitorAdapter implements GraphNodeVisitor{
	@Override
	public void visitNode(String node) {
	}

	@Override
	public void visitEdge(String from, String to) {
	}

	@Override
	public String[] nextNodesToVisitFrom(String node) {
		return null;
	}

	@Override
	public boolean interruptVisit() {
		return false;
	}
}
