package com.vmusco.softminer.sourceanalyzer.graphbuilding;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.graphstream.stream.gephi.JSONSender;

import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.graphs.GraphStream;
import com.vmusco.softminer.graphs.Graph.GraphApi;
import com.vmusco.softminer.graphs.persistance.GraphPersistFileCategory;
import com.vmusco.softminer.graphs.persistance.GraphPersistanceDirector;
import com.vmusco.softminer.sourceanalyzer.ProcessorCommunicator;

import spoon.compiler.SpoonCompiler;
import spoon.reflect.factory.Factory;
import spoon.reflect.factory.FactoryImpl;
import spoon.support.DefaultCoreFactory;
import spoon.support.StandardEnvironment;
import spoon.support.compiler.jdt.JDTBasedSpoonCompiler;

public class GraphBuilder {
	final private String projectName;
	final private ClassPathObtainer cpo;
	final private String[] inputSources;
	final private String[] cp;
	private long lastBuildingDuration = -1;

	private SpoonCompiler compiler;
	private Factory factory;
	
	public GraphBuilder(String projectName, String[] inputSources) {
		this.projectName = projectName;
		this.cpo = null;
		this.cp = null;
		this.inputSources = inputSources;
		
		ProcessorCommunicator.reset();
	}
	
	public GraphBuilder(String projectName, String[] inputSources, ClassPathObtainer cpo) {
		this.projectName = projectName;
		this.cpo = cpo;
		this.cp = null;
		this.inputSources = inputSources;
		
		ProcessorCommunicator.reset();
	}
	
	public GraphBuilder(String projectName, String[] inputSources, String[] cp) {
		this.projectName = projectName;
		this.cp = cp;
		this.cpo = null;
		this.inputSources = inputSources;
		
		ProcessorCommunicator.reset();
	}
	
	public Graph generateDependencyGraph(GraphBuildLogic logic) throws Exception{
		ProcessorCommunicator.outputgraph = Graph.getNewGraph(GraphApi.GRAPH_STREAM);
		afterGraphInstatiation();
		
		/**
		 * If No persistance, 
		 * Generate it ...
		 */
		this.factory = new FactoryImpl(new DefaultCoreFactory(), new StandardEnvironment());
		this.compiler = new JDTBasedSpoonCompiler(this.factory);
		
		for(String source : inputSources){
			this.compiler.addInputSource(new File(source));
		}
		
		if(cpo != null)
			this.compiler.setSourceClasspath(cpo.obtainClassPath());
		else if(cp != null)
			this.compiler.setSourceClasspath(cp);

		long start = System.currentTimeMillis();
		this.compiler.build();
		Graph g = logic.build(this.compiler);
		long end = System.currentTimeMillis();
		this.lastBuildingDuration = end-start;
		g.setBuildTime(this.lastBuildingDuration);
		
		return g;
	}
	
	public long getLastBuildingDuration() {
		return lastBuildingDuration;
	}

	public void afterGraphInstatiation(){
		// Override this method to apply special treatment to the create graph (before generation)
	}
	

	public void afterGraphGeneration(){
		// Override this method to apply special treatment to the create graph (after generation)
	}
}
