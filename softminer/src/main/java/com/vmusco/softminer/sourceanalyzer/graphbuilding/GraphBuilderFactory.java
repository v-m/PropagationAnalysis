package com.vmusco.softminer.sourceanalyzer.graphbuilding;

import java.io.File;
import java.util.logging.Logger;

import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.graphs.GraphStream;
import com.vmusco.softminer.graphs.Graph.GraphApi;
import com.vmusco.softminer.sourceanalyzer.ProcessorCommunicator;
import com.vmusco.softminer.sourceanalyzer.processors.ClassesProcessor;

import spoon.compiler.SpoonCompiler;
import spoon.reflect.factory.Factory;
import spoon.reflect.factory.FactoryImpl;
import spoon.support.DefaultCoreFactory;
import spoon.support.StandardEnvironment;
import spoon.support.compiler.jdt.JDTBasedSpoonCompiler;

/*@Deprecated
public class GraphBuilderFactory {
	private SpoonCompiler compiler;
	private Factory factory;
	private boolean hasBeenPrepared = false;
	
	private GraphBuilderFactory() {
	}
	
	public static GraphBuilderFactory instantiateFor(String[] inputSources, ClassPathObtainer classpath) throws Exception{
		return instantiateFor(inputSources, classpath.obtainClassPath());
	}
	
	protected static GraphBuilderFactory instantiateFor(String[] inputSources, String[] classpath) throws Exception{
		ProcessorCommunicator.reset();
		ProcessorCommunicator.outputgraph = Graph.getNewGraph(GraphApi.GRAPH_STREAM);
		
		GraphBuilderFactory gb = new GraphBuilderFactory();
		
		gb.factory = new FactoryImpl(new DefaultCoreFactory(), new StandardEnvironment());
		gb.compiler = new JDTBasedSpoonCompiler(gb.factory);
		
		for(String source : inputSources){
			gb.compiler.addInputSource(new File(source));
		}
		
		if(classpath != null)
			gb.compiler.setSourceClasspath(classpath);
		
		gb.compiler.build();
		gb.hasBeenPrepared = true;
		
		return gb;
	}
	
	public Graph generateDependencyGraph(GraphBuildLogic logic){
		if(this.hasBeenPrepared){
			return logic.build(this.compiler);
		}
		
		return null;
	}
}*/
