package com.vmusco.pminer.faultlocalization;

import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.graphs.GraphNodeVisitor;
import com.vmusco.softminer.graphs.GraphTools;

/**
 * Uses softminer exported call graph
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class GraphFaultLocalizationByIntersection extends GraphFaultLocalizationByUnion{
	protected Graph final_intersect = null;

	public GraphFaultLocalizationByIntersection(Graph base) {
		super(base);
	}
	
	@Override
	protected void finishedOneVisit() {
		if(final_intersect == null){
			final_intersect = last_propa;
		}else{
			final_intersect = GraphTools.intersect(final_intersect, last_propa);
		}
	}
	
	@Override
	protected GraphNodeVisitor populate() {
		return populateNew();
	}

	@Override
	public Graph getLastConcequenceGraph() {
		return final_intersect;
	}
}