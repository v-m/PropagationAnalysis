package com.vmusco.softminer.graphs;

import com.vmusco.smf.utils.SignatureTools;

public final class GraphTools {
	private GraphTools() {
	}
	
	public static Graph intersect(Graph g1, Graph g2){
		Graph ret = g1.createNewLikeThis();
		
		for(String node : g1.getNodesNames()){
			if(g2.hasNode(node)){
				ret.addNode(node);
			}
		}
		
		for(EdgeIdentity ei : g1.getEdges()){
			if(ret.hasNode(ei.getFrom()) && ret.hasNode(ei.getTo())){
				ret.addDirectedEdge(ei.getFrom(), ei.getTo());
			}
		}
		
		for(EdgeIdentity ei : g2.getEdges()){
			if(ret.hasNode(ei.getFrom()) 
					&& ret.hasNode(ei.getTo()) 
					&& !ret.hasDirectedEdge(ei.getFrom(), ei.getTo())){
				ret.addDirectedEdge(ei.getFrom(), ei.getTo());
			}
		}
		
		return ret;
	}
	
	public static Graph union(Graph g1, Graph g2){
		Graph ret = g1.createNewLikeThis();
		
		for(String node : g1.getNodesNames()){
			if(!ret.hasNode(node))
				ret.addNode(node);
		}
		for(String node : g2.getNodesNames()){
			if(!ret.hasNode(node))
				ret.addNode(node);
		}
		
		for(EdgeIdentity ei : g1.getEdges()){
			if(!ret.hasDirectedEdge(ei.getFrom(), ei.getTo()))
				ret.addDirectedEdge(ei.getFrom(), ei.getTo());
			
		}
		
		for(EdgeIdentity ei : g1.getEdges()){
			if(!ret.hasDirectedEdge(ei.getFrom(), ei.getTo()))
				ret.addDirectedEdge(ei.getFrom(), ei.getTo());
		}
		
		return ret;
	}
	
	public static void fastInsertion(Graph g, String line){
		for(String instr : line.split(";")){
			String prev = null;
			
			for(String link : instr.split("->")){
				if(prev != null){
					g.addDirectedEdgeAndNodeIfNeeded(prev, link);
				}
				prev = link;
			}
		}
	}
	
	public static Graph exportClassDependencyGraph(Graph callgraph){
		Graph ng = new GraphStream();
		
		for(EdgeIdentity ei : callgraph.getEdges()){
			String from = SignatureTools.getClassOf(ei.getFrom());
			String to = SignatureTools.getClassOf(ei.getTo());

			if(from.equals(to))
				continue;

			ng.addDirectedEdgeAndNodeIfNeeded(from, to);
		}
		
		return ng;
	}
	
	public static Graph exportPackageDependencyGraph(Graph callgraph){
		Graph ng = new GraphStream();
		
		for(EdgeIdentity ei : callgraph.getEdges()){
			String from = SignatureTools.getPackageOf(ei.getFrom());
			String to = SignatureTools.getPackageOf(ei.getTo());

			if(from.equals(to))
				continue;

			ng.addDirectedEdgeAndNodeIfNeeded(from, to);
		}
		
		return ng;
	}
}
