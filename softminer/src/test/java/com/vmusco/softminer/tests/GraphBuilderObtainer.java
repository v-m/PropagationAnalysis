package com.vmusco.softminer.tests;

import com.vmusco.softminer.sourceanalyzer.graphbuilding.GraphBuilder;

/**
*
* @author Vincenzo Musco - http://www.vmusco.com
*/
public interface GraphBuilderObtainer {
	public GraphBuilder getGraphBuilder(String[] items);
}
