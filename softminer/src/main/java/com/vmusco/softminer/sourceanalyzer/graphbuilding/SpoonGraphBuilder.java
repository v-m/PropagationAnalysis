package com.vmusco.softminer.sourceanalyzer.graphbuilding;

import java.util.ArrayList;
import java.util.List;

import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.sourceanalyzer.ProcessorCommunicator;
import com.vmusco.softminer.sourceanalyzer.processors.SimpleFeaturesProcessor;

import spoon.compiler.SpoonCompiler;
import spoon.processing.Processor;

/**
*
* @author Vincenzo Musco - http://www.vmusco.com
*/
public class SpoonGraphBuilder extends GraphBuildLogic {
	private Processor<?> aProcessor;
	
	public static SpoonGraphBuilder getFeatureGranularityGraphBuilder(){
		return new SpoonGraphBuilder(new SimpleFeaturesProcessor());
	}
	
	public SpoonGraphBuilder(Processor<?> aProcessor) {
		this.aProcessor = aProcessor;
	}
	
	@Override
	public Graph build(SpoonCompiler compiler) {
		List<Processor<?>> arg0 = new ArrayList<>();
		arg0.add(aProcessor);
		compiler.process(arg0);
		return ProcessorCommunicator.outputgraph;
	}

	@Override
	public String formatAtom(String atom) {
		return atom;
	}
}
