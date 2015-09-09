package com.vmusco.pminer.analyze;

import java.util.Map;

import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.ProcessStatistics;
import com.vmusco.smf.exceptions.MutationNotRunException;
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
	public void fireExecutionEnded() {
		g.bestDisplay();
	}

	protected void makeUp(ProcessStatistics ps, MutantIfos mi, String[] impactedNodes, String[] impactedTests) throws MutationNotRunException {
		String[] mutationDetermined = mi.getExecutedTestsResults().getCoherentMutantFailAndHangTestCases(ps);
		String[] graphDetermined = impactedTests;
		
		String mutationInsertionPosition = mi.getMutationIn();

		CIAEstimationSets sets = new CIAEstimationSets(graphDetermined, mutationDetermined);
		
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
	
	@Override
	public void fireIntersectionFound(ProcessStatistics ps, MutantIfos mi, String[] impactedNodes, String[] impactedTests) throws MutationNotRunException{
		makeUp(ps, mi, impactedNodes, impactedTests);
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
}
