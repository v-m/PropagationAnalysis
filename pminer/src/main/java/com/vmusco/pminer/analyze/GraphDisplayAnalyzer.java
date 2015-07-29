package com.vmusco.pminer.analyze;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.vmusco.pminer.UseGraph;
import com.vmusco.pminer.run.PropagationEstimer;
import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.MutationStatistics;
import com.vmusco.smf.analysis.ProcessStatistics;
import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.graphs.Graph.NodeShape;
import com.vmusco.softminer.graphs.Graph.NodeSize;

public class GraphDisplayAnalyzer extends MutantTestAnalyzer {
	private static final String BUGGY_NODE_COLOR = "red";
	private static final String INTERSECTED_NODES_COLOR = "blue";
	private static final String NODES_ONLY_WITH_GRAPH_COLOR = "orange";
	private static final String NODES_ONLY_WITH_EXEC_COLOR = "purple";
	
	private static final Graph.NodeShape BUGGY_NODE_SHAPE = NodeShape.CROSS;
	private static final Graph.NodeShape INTERSECTED_NODES_SHAPE = NodeShape.DIAMOND;
	private static final Graph.NodeShape NODES_ONLY_WITH_GRAPH_SHAPE = NodeShape.BOX;
	private static final Graph.NodeShape NODES_ONLY_WITH_EXEC_SHAPE = NodeShape.ROUNDED_BOX;
	
	protected boolean requireResave = false;
	protected Graph g;
	private String buggyNode = null;
	private Graph originalGraph = null;
	protected boolean showLinkDetailsOnConsole = false;
	protected boolean displayWindow;
	
	public GraphDisplayAnalyzer(Graph makeUpGraph, boolean showLinkDetailsOnConsole, boolean displayWindow) {
		this.g = makeUpGraph;
		this.showLinkDetailsOnConsole = showLinkDetailsOnConsole;
		this.displayWindow = displayWindow;
	}

	protected void makeUp(ProcessStatistics ps,
			/*String mutationOperator,
			String mutationId,*/
			MutantIfos mi,
			String[] graphDetermined, UseGraph basin) {
		String[] mutationDetermined = ExploreMutants.purifyFailAndHangResultSetForMutant(ps, mi); 
		//String mutationInsertionPosition = ps.mutations.get(mutationOperator).mutationIn.get(mutationId);
		String mutationInsertionPosition = mi.getMutationIn();
		
		// PRELEMINARY STATITSTICS
		Set<String> relevant = new HashSet<String>();
		for(String s : mutationDetermined){
			relevant.add(s);
		}
				
		Set<String> retrieved = new HashSet<String>();
		for(String s : graphDetermined){
			retrieved.add(s);
		}
		
		Set<String> inter = intersection(retrieved, relevant);
		// FALSE POSITIVES (only detected using graphs)
		List<String> tempList = new ArrayList<String>();
		tempList.addAll(retrieved);
		tempList.removeAll(inter);
		// FALSE NEGATIVES (only detected using mutation)
		List<String> tempList2 = new ArrayList<String>();
		tempList2.addAll(relevant);
		tempList2.removeAll(inter);
		
		// STYLE THE BUG NODE
    	g.colorNode(mutationInsertionPosition, BUGGY_NODE_COLOR);
		g.shapeNode(mutationInsertionPosition, BUGGY_NODE_SHAPE);
		g.sizeNode(mutationInsertionPosition, NodeSize.LARGE);
		
		// MATCHING CASES
		for(String aTest : inter){
        	g.colorNode(aTest, INTERSECTED_NODES_COLOR);
        	g.shapeNode(aTest, INTERSECTED_NODES_SHAPE);
		}
		
		for(String aTest : tempList.toArray(new String[tempList.size()])){
        	g.colorNode(aTest, NODES_ONLY_WITH_GRAPH_COLOR);
        	g.shapeNode(aTest, NODES_ONLY_WITH_GRAPH_SHAPE);
		}
		
		for(String aTest : tempList2.toArray(new String[tempList2.size()])){
			g.addNode(aTest, false);
			g.colorNode(aTest, NODES_ONLY_WITH_EXEC_COLOR);
			g.shapeNode(aTest, NODES_ONLY_WITH_EXEC_SHAPE);
		}
	}
	@Override
	public void fireIntersectionFound(ProcessStatistics ps, String mutationId, MutantIfos mi, String[] graphDetermined, UseGraph basin, long propatime){
		
		makeUp(ps, mi, graphDetermined, basin);
		
		if(displayWindow)
			basin.getBasinGraph().bestDisplay();
		
		if(showLinkDetailsOnConsole){
			String[] mutationDetermined = ExploreMutants.purifyFailAndHangResultSetForMutant(ps, mi); 
			
			// PRELEMINARY STATITSTICS
			Set<String> relevant = new HashSet<String>();
			for(String s : mutationDetermined){
				relevant.add(s);
			}
					
			Set<String> retrieved = new HashSet<String>();
			for(String s : graphDetermined){
				retrieved.add(s);
			}
			
			Set<String> inter = intersection(retrieved, relevant);
			// FALSE POSITIVES (only detected using graphs)
			List<String> tempList = new ArrayList<String>();
			tempList.addAll(retrieved);
			tempList.removeAll(inter);
			// FALSE NEGATIVES (only detected using mutation)
			List<String> tempList2 = new ArrayList<String>();
			tempList2.addAll(relevant);
			tempList2.removeAll(inter);
			
			for(String aTest : inter){
					System.out.println("\u001b[32m"+"\t"+aTest+"\u001b[0m");
			}
			
			for(String aTest : inter){
				System.out.println("\u001b[32m"+"\t"+aTest+"\u001b[0m");
			}
			
			for(String aTest : tempList.toArray(new String[tempList.size()])){
				System.out.println("\u001b[31m"+"\t"+aTest+"\u001b[0m");
			}
			
			for(String aTest : tempList2.toArray(new String[tempList2.size()])){
				System.out.println("\u001b[90m"+"\t"+aTest+"\u001b[0m");
			}
		}
	}

	public void setBuggyNodeAndOriginalGraph(String buggyNode, Graph originalGraph) {
		this.buggyNode = buggyNode;
		this.originalGraph = originalGraph;
	}

}
