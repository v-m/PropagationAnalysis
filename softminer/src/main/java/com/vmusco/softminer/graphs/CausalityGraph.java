package com.vmusco.softminer.graphs;

import com.vmusco.softminer.sourceanalyzer.processors.SpecificTags;

public abstract class CausalityGraph {
	public static Graph convert(Graph dependency){
		Graph causalityGraph = Graph.getNewGraph(dependency.getGraphFamily());
		
		// Copy each node...
		for(String node : dependency.getNodesNames()){
			causalityGraph.addNode(node, true);
		}
		
		// Explore edges and add according to the case
		for(Graph.NodesNamesForEdge edge : dependency.getEdges()){
			if(dependency.getEdgeType(edge.from, edge.to).equals(EdgeTypes.READ_OPERATION)){
				causalityGraph.addDirectedEdge(edge.to, edge.from);
			}else{
				causalityGraph.addDirectedEdge(edge.from, edge.to);
			}
		}
		
		return causalityGraph;
	}
}
