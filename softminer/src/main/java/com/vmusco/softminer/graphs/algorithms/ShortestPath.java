package com.vmusco.softminer.graphs.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vmusco.softminer.graphs.EdgeIdentity;
import com.vmusco.softminer.graphs.Graph;

import edu.ufl.cise.bsmock.graph.Edge;
import edu.ufl.cise.bsmock.graph.Node;
import edu.ufl.cise.bsmock.graph.ksp.Eppstein;
import edu.ufl.cise.bsmock.graph.ksp.KSPAlgorithm;
import edu.ufl.cise.bsmock.graph.ksp.LazyEppstein;
import edu.ufl.cise.bsmock.graph.ksp.Yen;
import edu.ufl.cise.bsmock.graph.util.Path;


public class ShortestPath {
	protected static final Logger logger = LogManager.getFormatterLogger(ShortestPath.class.getSimpleName());

	edu.ufl.cise.bsmock.graph.Graph g = new edu.ufl.cise.bsmock.graph.Graph();
	private Graph currentGraph;
	
	public Graph getCurrentGraph() {
		return currentGraph;
	}
	
	public ShortestPath(Graph gs){
		this.currentGraph = gs;
		
		long time = System.currentTimeMillis();
		
		for(String node : gs.getNodesNames()){
			edu.ufl.cise.bsmock.graph.Node n = new edu.ufl.cise.bsmock.graph.Node();
			g.addNode(node);
		}
		
		for(EdgeIdentity ei : gs.getEdges()){
			edu.ufl.cise.bsmock.graph.Edge e = new edu.ufl.cise.bsmock.graph.Edge(ei.getFrom(), ei.getTo(), 1d);
			g.addEdge(e);
		}
		time = System.currentTimeMillis() - time;
		
		logger.info("Importing the graph: %d ms", time);
	}

	public List<String[]> yen(String from, String to, int k){
		KSPAlgorithm kspalgo = new Yen();
		List<Path> ksp = kspalgo.ksp(g, from, to, k);
		
		return pathsToArray(ksp);
	}
	
	public List<String[]> eppstein(String from, String to, int k){
		KSPAlgorithm kspalgo = new Eppstein();
		List<Path> ksp = kspalgo.ksp(g, from, to, k);
		
		return pathsToArray(ksp);
	}
	
	public List<String[]> lazyEppstein(String from, String to, int k){
		KSPAlgorithm kspalgo = new LazyEppstein();
		List<Path> ksp = kspalgo.ksp(g, from, to, k);
		
		return pathsToArray(ksp);
	}

	private List<String[]> pathsToArray(List<Path> ksp) {
		List<String[]> ret = new ArrayList<String[]>();

		for(Path p : ksp){
			ret.add(p.getNodes().toArray(new String[p.getNodes().size()]));
		}
		
		return ret;
	}

}
