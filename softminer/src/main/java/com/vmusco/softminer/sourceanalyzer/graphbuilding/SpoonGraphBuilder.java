package com.vmusco.softminer.sourceanalyzer.graphbuilding;

import java.util.ArrayList;
import java.util.List;

import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.sourceanalyzer.ProcessorCommunicator;
import com.vmusco.softminer.sourceanalyzer.processors.SimpleFeaturesProcessor;

import spoon.compiler.SpoonCompiler;

public class SpoonGraphBuilder extends GraphBuildLogic {
	private Class<?> aProcessor;
	
	public static SpoonGraphBuilder getFeatureGranularityGraphBuilder(){
		return new SpoonGraphBuilder(SimpleFeaturesProcessor.class);
	}
	
	public SpoonGraphBuilder(Class<?> aProcessor) {
		this.aProcessor = aProcessor;
	}
	
	@Override
	public Graph build(SpoonCompiler compiler) {
		List<String> arg0 = new ArrayList<String>();
		arg0.add(aProcessor.getName());
		compiler.process(arg0);
		return ProcessorCommunicator.outputgraph;
	}

	@Override
	public String formatAtom(String atom) {
		return atom;
	}
	
	private Class<?> getProcessor() {
		return aProcessor;
	}
}
