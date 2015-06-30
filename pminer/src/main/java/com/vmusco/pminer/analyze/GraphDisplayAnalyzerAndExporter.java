package com.vmusco.pminer.analyze;

import java.io.File;

import com.vmusco.pminer.UseGraph;
import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.MutationStatistics;
import com.vmusco.smf.analysis.ProcessStatistics;
import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.graphs.NodeMarkers;
import com.vmusco.softminer.sourceanalyzer.processors.SpecificTags;

public class GraphDisplayAnalyzerAndExporter extends GraphDisplayAnalyzer{

	private String persistTo;
	
	
	public GraphDisplayAnalyzerAndExporter(Graph makeUpGraph, String persistingName, boolean showLinkDetailsOnConsole, boolean displayWindow) {
		super(makeUpGraph, showLinkDetailsOnConsole, displayWindow);
		
		initMakeuping(makeUpGraph);
		
		this.persistTo = persistingName;
	}
	
	private void initMakeuping(Graph makeUpGraph) {
		for(String node : makeUpGraph.getNodesNames()){
			if(makeUpGraph.isNodeMarked(node, NodeMarkers.USES_REFLEXION)){
				makeUpGraph.shadowNode(node, 2, "yellow");
			}
		}
	}

	@Override
	public void fireIntersectionFound(ProcessStatistics ps, String mutationId, MutantIfos mi, String[] graphDetermined, UseGraph basin, long propatime){
		
		super.makeUp(ps, mi, graphDetermined, basin);
		super.g.persistAsImage(this.persistTo);
		super.fireIntersectionFound(ps, mutationId, mi, graphDetermined, basin, propatime);
		
		if(super.displayWindow)
			super.g.persistAsImage(this.persistTo+"_custom", false);
	}
}
