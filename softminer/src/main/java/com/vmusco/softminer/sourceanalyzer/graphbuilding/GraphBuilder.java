package com.vmusco.softminer.sourceanalyzer.graphbuilding;

import java.io.File;

import spoon.compiler.SpoonCompiler;
import spoon.reflect.factory.Factory;
import spoon.reflect.factory.FactoryImpl;
import spoon.support.DefaultCoreFactory;
import spoon.support.StandardEnvironment;
import spoon.support.compiler.jdt.JDTBasedSpoonCompiler;

import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.graphs.Graph.GraphApi;
import com.vmusco.softminer.sourceanalyzer.ProcessorCommunicator;

/**
*
* @author Vincenzo Musco - http://www.vmusco.com
*/
public class GraphBuilder {
	@SuppressWarnings("unused")
	final private String projectName;
	final private String[] inputSources;
	private String[] cp ;
	private long lastBuildingDuration = -1;

	private SpoonCompiler compiler;
	private Factory factory;


	//FACTORIES TO GET GRAPHBUILDER CONFIGURED OBJECTS


	public static GraphBuilder newGraphBuilderOnlyWithDependenciesWithoutInheritence(String projectName, String[] inputSources){
		return newGraphBuilderOnlyWithDependenciesWithoutOverriden(projectName, inputSources, null);
	}
	
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
	 * Get a simple call graph on which overriden methods are totally removed
	 * @param projectName a project name
	 * @param inputSources the list of sources to include in the use graph
	 * @param cp the list of class path entries to build the sources
	 * @return the builder object
	 */
	public static GraphBuilder newGraphBuilderManuallyConfigured(String projectName, String[] inputSources, String[] cp, boolean resolveIfcesAndAbstract, boolean fields, boolean removeOverriden){
		GraphBuilder gb = new GraphBuilder(projectName, inputSources, cp);
		ProcessorCommunicator.resolveInterfacesAndClasses = resolveIfcesAndAbstract;
		ProcessorCommunicator.includesFields = fields;
		ProcessorCommunicator.removeOverridenMethods = removeOverriden;
		return gb;
	}
	
	/**
	 * Get a simple call graph on which overriden methods are totally removed
	 * @param projectName a project name
	 * @param inputSources the list of sources to include in the use graph
	 * @param cp the list of class path entries to build the sources
	 * @return the builder object
	 */
	public static GraphBuilder newGraphBuilderOnlyWithDependenciesWithoutOverriden(String projectName, String[] inputSources, String[] cp){
		GraphBuilder gb = new GraphBuilder(projectName, inputSources, cp);
		ProcessorCommunicator.resolveInterfacesAndClasses = false;
		ProcessorCommunicator.includesFields = false;
		ProcessorCommunicator.removeOverridenMethods = true;
		return gb;
	}
	
	/**
	 * Get a simple call graph
	 * @param projectName a project name
	 * @param inputSources the list of sources to include in the use graph
	 * @param cp the list of class path entries to build the sources
	 * @return the builder object
	 */
	public static GraphBuilder newGraphBuilderOnlyWithDependencies(String projectName, String[] inputSources, String[] cp){
		GraphBuilder gb = new GraphBuilder(projectName, inputSources, cp);
		ProcessorCommunicator.resolveInterfacesAndClasses = false;
		ProcessorCommunicator.includesFields = false;
		ProcessorCommunicator.removeOverridenMethods = false;
		return gb;
	}
	
	/**
	 * Get a call graph with fields
	 * @param projectName a project name
	 * @param inputSources the list of sources to include in the use graph
	 * @param cp the list of class path entries to build the sources
	 * @return the builder object
	 */
	public static GraphBuilder newGraphBuilderWithFields(String projectName, String[] inputSources, String[] cp){
		GraphBuilder gb = new GraphBuilder(projectName, inputSources, cp);
		ProcessorCommunicator.resolveInterfacesAndClasses = false;
		ProcessorCommunicator.includesFields = true;
		ProcessorCommunicator.removeOverridenMethods = false;
		return gb;
	}
	

	/**
	 * Get a call graph with Class Hierarchy Analysis
	 * @param projectName a project name
	 * @param inputSources the list of sources to include in the use graph
	 * @param cp the list of class path entries to build the sources
	 * @return the builder object
	 */
	public static GraphBuilder newGraphBuilderWithInheritence(String projectName, String[] inputSources, String[] cp){
		GraphBuilder gb = new GraphBuilder(projectName, inputSources, cp);
		ProcessorCommunicator.resolveInterfacesAndClasses = true;
		ProcessorCommunicator.includesFields = false;
		ProcessorCommunicator.removeOverridenMethods = false;
		return gb;
	}
	
	/**
	 * Get a call graph with Class Hierarchy Analysis and Fields
	 * @param projectName a project name
	 * @param inputSources the list of sources to include in the use graph
	 * @param cp the list of class path entries to build the sources
	 * @return the builder object
	 */
	public static GraphBuilder newGraphBuilderWithFieldsAndInheritence(String projectName, String[] inputSources, String[] cp){
		GraphBuilder gb = new GraphBuilder(projectName, inputSources, cp);
		ProcessorCommunicator.resolveInterfacesAndClasses = true;
		ProcessorCommunicator.includesFields = true;
		ProcessorCommunicator.removeOverridenMethods = false;
		return gb;
	}
	
	/**
	 * New generator without classpath generation
	 * @param projectName
	 * @param inputSources
	 */
	protected GraphBuilder(String projectName, String[] inputSources) {
		this(projectName, inputSources, null);
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
		else
			this.compiler.getFactory().getEnvironment().setNoClasspath(true);

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

	public void setClassPath(String[] cp){
		this.cp = cp;
	}

	/**
	 * Ask Spoon not to take into consideration the classpath resolution
	 */
	public void setNoClassPath(){
		this.cp = null;
	}
}
