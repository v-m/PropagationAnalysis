package com.vmusco.softminer.sourceanalyzer.graphbuilding;

import com.vmusco.softminer.graphs.Graph;

import spoon.compiler.SpoonCompiler;

/**
*
* @author Vincenzo Musco - http://www.vmusco.com
*/
public abstract class GraphBuildLogic {
	public abstract Graph build(SpoonCompiler compiler);
	public abstract String formatAtom(String atom);
}
