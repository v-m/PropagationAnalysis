package com.vmusco.pminer.analyze;

import java.util.Map;

import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.softminer.graphs.EdgeIdentity;
import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.graphs.Graph.NodeShape;
import com.vmusco.softminer.graphs.Graph.NodeSize;

/**
 * This class is used to display the impacts as a sub graph visualization
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class GraphDisplayAnalyzer extends MutantTestAnalyzer {
	private static final String BUGGY_NODE_COLOR = "blue";
	private static final String INTERSECTED_NODES_COLOR = "green";
	private static final String NODES_ONLY_WITH_GRAPH_COLOR = "red";
	private static final String NODES_ONLY_WITH_EXEC_COLOR = "purple";
	
	private static final Graph.NodeShape BUGGY_NODE_SHAPE = NodeShape.CROSS;
	private static final Graph.NodeShape INTERSECTED_NODES_SHAPE = NodeShape.BOX;
	private static final Graph.NodeShape NODES_ONLY_WITH_GRAPH_SHAPE = NodeShape.DIAMOND;
	private static final Graph.NodeShape NODES_ONLY_WITH_EXEC_SHAPE = NodeShape.ROUNDED_BOX;
	
	protected Graph g;
	
	public GraphDisplayAnalyzer(Graph makeUpGraph) {
		this.g = makeUpGraph;
	}
	
	@Override
	public void executionEnded() {
		g.bestDisplay();
	}

	protected void makeUp(String id, String in, String[] ais, String[] cis) {
		//String[] mutationDetermined = ps.getCoherentMutantFailAndHangTestCases(mi.getExecutedTestsResults());
		//String[] graphDetermined = cis;
		
		String mutationInsertionPosition = in;

		CIAEstimationSets sets = new CIAEstimationSets(cis, ais);
		
		// STYLE THE BUG NODE
    	g.colorNode(mutationInsertionPosition, BUGGY_NODE_COLOR);
		g.shapeNode(mutationInsertionPosition, BUGGY_NODE_SHAPE);
		g.sizeNode(mutationInsertionPosition, NodeSize.LARGE);
		
		// MATCHING CASES
		for(String aTest : sets.getFoundImpactedSet()){
        	g.colorNode(aTest, INTERSECTED_NODES_COLOR);
        	g.shapeNode(aTest, INTERSECTED_NODES_SHAPE);
		}
		
		for(String aTest : sets.getFalsePositivesImpactedSet()){
        	g.colorNode(aTest, NODES_ONLY_WITH_GRAPH_COLOR);
        	g.shapeNode(aTest, NODES_ONLY_WITH_GRAPH_SHAPE);
		}
		
		for(String aTest : sets.getDiscoveredImpactedSet()){
			//g.addNode(aTest, false);
			g.colorNode(aTest, NODES_ONLY_WITH_EXEC_COLOR);
			g.shapeNode(aTest, NODES_ONLY_WITH_EXEC_SHAPE);
		}
	}
	
	/**
	 * Example:
	 * 
	 * Map<EdgeIdentity, Float> edgesw = new HashMap<>();
	 * edgesw.put(new EdgeIdentity("src", "dst"), 0.36850484606864875f);
	 * (...)
	 * gda.changeEdgesWeights(edgesw);
	 * 
	 * @param weights
	 */
	public void changeEdgesWeights(Map<EdgeIdentity, Float> weights){
		for(EdgeIdentity k : weights.keySet()){
			float w = weights.get(k);
			int ww = ((int)((w/2)*10) ) + 2;
			System.out.println(ww);
			g.sizeEdge(k.getFrom(), k.getTo(), ww);
			g.appendLabelEdge(k.getFrom(), k.getTo(), Float.toString(w));
			
			if(w < 0.2)
				g.colorEdge(k.getFrom(), k.getTo(), "red");
			else
				g.colorEdge(k.getFrom(), k.getTo(), "green");
		}
	}

	@Override
	public void intersectionFound(String id, String in, String[] ais, String[] cis) {
		makeUp(id, in, ais, cis);
	}

	@Override
	public void unboundedFound(String id, String in) {
		
	}

	@Override
	public void isolatedFound(String id, String in) {
		
	}
}
