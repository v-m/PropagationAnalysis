package com.vmusco.softminer.sourceanalyzer.graphbuilding;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.graphstream.stream.gephi.JSONSender;

import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.graphs.GraphStream;
import com.vmusco.softminer.graphs.Graph.GraphApi;
import com.vmusco.softminer.sourceanalyzer.ProcessorCommunicator;

import spoon.compiler.SpoonCompiler;
import spoon.reflect.factory.Factory;
import spoon.reflect.factory.FactoryImpl;
import spoon.support.DefaultCoreFactory;
import spoon.support.StandardEnvironment;
import spoon.support.compiler.jdt.JDTBasedSpoonCompiler;

public class GraphBuilder {
	final private String projectName;
	final private String[] inputSources;
	final private String[] cp;
	private long lastBuildingDuration = -1;

	private SpoonCompiler compiler;
	private Factory factory;


	//FACTORIES TO GET GRAPHBUILDER CONFIGURED OBJECTS

	/**
	 * Get a use graph type A without class path
	 * @see GraphBuilder#newGraphBuilderOnlyWithDependencies(String, String[])
	 */	
	public static GraphBuilder newGraphBuilderOnlyWithDependencies(String projectName, String[] inputSources){
		return newGraphBuilderOnlyWithDependencies(projectName, inputSources, null);
	}
	

	/**
	 * Get a use graph type B without class path
	 * @see GraphBuilder#newGraphBuilderWithFields(String, String[])
	 */	
	public static GraphBuilder newGraphBuilderWithFields(String projectName, String[] inputSources){
		return newGraphBuilderWithFields(projectName, inputSources, null);
	}
	

	/**
	 * Get a use graph type C without class path
	 * @see GraphBuilder#newGraphBuilderWithInheritence(String, String[])
	 */
	public static GraphBuilder newGraphBuilderWithInheritence(String projectName, String[] inputSources){
		return newGraphBuilderWithInheritence(projectName, inputSources, null);
	}
	
	/**
	 * Get a use graph type D without class path
	 * @see GraphBuilder#newGraphBuilderWithFieldsAndInheritence(String, String[])
	 */
	public static GraphBuilder newGraphBuilderWithFieldsAndInheritence(String projectName, String[] inputSources){
		return newGraphBuilderWithFieldsAndInheritence(projectName, inputSources, null);
	}
	
	
	
	/**
	 * Get a use graph type A
	 * @param projectName a project name
	 * @param inputSources the list of sources to include in the use graph
	 * @param cp the list of class path entries to build the sources
	 * @return the builder object
	 */
	public static GraphBuilder newGraphBuilderOnlyWithDependencies(String projectName, String[] inputSources, String[] cp){
		GraphBuilder gb = new GraphBuilder(projectName, inputSources, cp);
		return gb;
	}
	
	/**
	 * Get a use graph type B
	 * @param projectName a project name
	 * @param inputSources the list of sources to include in the use graph
	 * @param cp the list of class path entries to build the sources
	 * @return the builder object
	 */
	public static GraphBuilder newGraphBuilderWithFields(String projectName, String[] inputSources, String[] cp){
		GraphBuilder gb = new GraphBuilder(projectName, inputSources, cp);
		ProcessorCommunicator.includesFields = true;
		return gb;
	}
	

	/**
	 * Get a use graph type C
	 * @param projectName a project name
	 * @param inputSources the list of sources to include in the use graph
	 * @param cp the list of class path entries to build the sources
	 * @return the builder object
	 */
	public static GraphBuilder newGraphBuilderWithInheritence(String projectName, String[] inputSources, String[] cp){
		GraphBuilder gb = new GraphBuilder(projectName, inputSources, cp);
		ProcessorCommunicator.resolveInterfacesAndClasses = true;
		return gb;
	}
	
	/**
	 * Get a use graph type D
	 * @param projectName a project name
	 * @param inputSources the list of sources to include in the use graph
	 * @param cp the list of class path entries to build the sources
	 * @return the builder object
	 */
	public static GraphBuilder newGraphBuilderWithFieldsAndInheritence(String projectName, String[] inputSources, String[] cp){
		GraphBuilder gb = new GraphBuilder(projectName, inputSources, cp);
		ProcessorCommunicator.resolveInterfacesAndClasses = true;
		ProcessorCommunicator.includesFields = true;
		return gb;
	}
	
	
	protected GraphBuilder(String projectName, String[] inputSources) {
		this.projectName = projectName;
		this.cp = null;
		this.inputSources = inputSources;
		
		ProcessorCommunicator.reset();
	}
	
	protected GraphBuilder(String projectName, String[] inputSources, String[] cp) {
		this.projectName = projectName;
		this.cp = cp;
		this.inputSources = inputSources;
		
		ProcessorCommunicator.reset();
	}
	
	public Graph generateDependencyGraph(GraphBuildLogic logic) throws Exception{
		ProcessorCommunicator.outputgraph = Graph.getNewGraph(GraphApi.GRAPH_STREAM);
		afterGraphInstatiation();
		
		this.factory = new FactoryImpl(new DefaultCoreFactory(), new StandardEnvironment());
		this.compiler = new JDTBasedSpoonCompiler(this.factory);
		
		for(String source : inputSources){
			this.compiler.addInputSource(new File(source));
		}
		
		if(cp != null)
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
		// Override this method to apply special treatment to the created graph (before generation)
	}
	

	public void afterGraphGeneration(){
		// Override this method to apply special treatment to the created graph (after generation)
	}
}
