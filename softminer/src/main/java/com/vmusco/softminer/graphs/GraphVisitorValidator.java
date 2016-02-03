package com.vmusco.softminer.graphs;

public interface GraphVisitorValidator {
	boolean isNodeAccepted(String from);
	boolean isEdgeAccepted(String arrived, String next);
}
