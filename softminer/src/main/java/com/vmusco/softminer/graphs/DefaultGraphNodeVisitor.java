package com.vmusco.softminer.graphs;

public class DefaultGraphNodeVisitor implements GraphNodeVisitor {

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

	@Override
	public boolean isNodeValid(String node) {
		return true;
	}
}