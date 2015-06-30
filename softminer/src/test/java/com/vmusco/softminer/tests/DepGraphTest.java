package com.vmusco.softminer.tests;

import org.junit.Assert;

import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.sourceanalyzer.ProcessorCommunicator;
import com.vmusco.softminer.sourceanalyzer.ProcessorCommunicator.PatternBehavior;
import com.vmusco.softminer.sourceanalyzer.graphbuilding.GraphBuildLogic;
import com.vmusco.softminer.sourceanalyzer.graphbuilding.GraphBuilder;
import com.vmusco.softminer.sourceanalyzer.graphbuilding.JeantessierGraphBuilder;
import com.vmusco.softminer.sourceanalyzer.graphbuilding.SpoonGraphBuilder;
import com.vmusco.softminer.sourceanalyzer.processors.FeaturesProcessor;
import com.vmusco.softminer.sourceanalyzer.processors.GraphItemRenamer;

public abstract class DepGraphTest {
	protected static String currTestedPakg = "";
	//protected static Class processorToUse = FeatureProcessorFinal.class;
	
	protected static GraphBuildLogic buildingLogicToUse = SpoonGraphBuilder.getFeatureGranularityGraphBuilder();
	//protected static GraphBuildLogic buildingLogicToUse = new JeantessierGraphBuilder();
	
	protected static final String STRING_CANONICAL_NAME = java.lang.String.class.getCanonicalName();
	protected static final String CLASS_CANONICAL_NAME = java.lang.Class.class.getCanonicalName();
	
	public abstract GraphBuilder getGraphBuilder(Class c) throws Exception;
	
	/**
	 * Prepare the test for a specific test package
	 * @param packageInto specify any class into the target package
	 * @return
	 * @throws Exception
	 */
	protected static GraphBuilder setTestPkgAndGenerateBuilderUseGraphA(Class packageInto) throws Exception{
		String[] sources = preSetTestPkgAndGenerateBuilder(packageInto);
		GraphBuilder instForTestCase = GraphBuilder.newGraphBuilderOnlyWithDependencies("test", sources); 
		postSetTestPkgAndGenerateBuilder();
		return instForTestCase;
	}
	
	protected static GraphBuilder setTestPkgAndGenerateBuilderUseGraphB(Class packageInto) throws Exception{
		String[] sources = preSetTestPkgAndGenerateBuilder(packageInto);
		GraphBuilder instForTestCase = GraphBuilder.newGraphBuilderWithFields("test", sources); 
		postSetTestPkgAndGenerateBuilder();
		return instForTestCase;
	}
	
	protected static GraphBuilder setTestPkgAndGenerateBuilderUseGraphC(Class packageInto) throws Exception{
		String[] sources = preSetTestPkgAndGenerateBuilder(packageInto);
		GraphBuilder instForTestCase = GraphBuilder.newGraphBuilderWithInheritence("test", sources); 
		postSetTestPkgAndGenerateBuilder();
		return instForTestCase;
	}
	
	protected static GraphBuilder setTestPkgAndGenerateBuilderUseGraphD(Class packageInto) throws Exception{
		String[] sources = preSetTestPkgAndGenerateBuilder(packageInto);
		GraphBuilder instForTestCase = GraphBuilder.newGraphBuilderWithFieldsAndInheritence("test", sources); 
		postSetTestPkgAndGenerateBuilder();
		return instForTestCase;
	}
	
	protected static String[] preSetTestPkgAndGenerateBuilder(Class packageInto) throws Exception{
		System.out.println("\nCurrent test: "+packageInto.getPackage().getName());
		System.out.println("*************");
		DepGraphTest.currTestedPakg = packageInto.getPackage().getName();
		
		return new String[]{System.getProperty("user.dir")+"/src/test/java/"+DepGraphTest.currTestedPakg.replaceAll("\\.", "/")};
	}
	
	protected static void postSetTestPkgAndGenerateBuilder() throws Exception{
		ProcessorCommunicator.patternBehavior = PatternBehavior.INCLUDE_IF_BOTH;
		ProcessorCommunicator.pattern = DepGraphTest.getFunction("").replace(".", "\\.")+".*";
		
		ProcessorCommunicator.nodesRenamer = new GraphItemRenamer() {
			public String renamed(String originalName) {
				return DepGraphTest.popFunction(originalName);
			}
		};
	}
		
	
	protected static String getFunction(String name){
		return currTestedPakg + "." + name;
	}
	
	protected static String popFunction(String name){
		String start = currTestedPakg + ".";
		if(name.startsWith(start))
			return name.substring(start.length());
		return name;
	}
	
	protected static void assertHasEdge(Graph g, String src, String dst){
		//System.out.println(Tests.getFunction(src)+" -> "+Tests.getFunction(dst)+" ?");
		String fsrc = getFunction(src);
		String fdst = getFunction(dst);
		
		if(ProcessorCommunicator.nodesRenamer != null){
			fsrc = ProcessorCommunicator.nodesRenamer.renamed(fsrc);
			fdst = ProcessorCommunicator.nodesRenamer.renamed(fdst);
		}
		
		Assert.assertTrue(g.hasDirectedEdge(fsrc, fdst));
	}
	
	protected static void assertHasEdges(Graph g, String src, String[] dsts){
		for(String dst : dsts){
			assertHasEdge(g, src, dst);
		}
	}
	
	protected static void assertHasEdges(Graph g, String[] srcs, String dst){
		for(String src : srcs){
			assertHasEdge(g, src, dst);
		}
	}
	
	protected static void assertHasOutDegree(Graph g, String node, int degree){
		Assert.assertEquals(degree, g.getOutDegreeFor(node));
	}

	protected static void assertHasInDegree(Graph g, String node, int degree){
		Assert.assertEquals(degree, g.getInDegreeFor(node));
	}

	protected static void assertHasDegree(Graph g, String node, int degree){
		Assert.assertEquals(degree, g.getDegreeFor(node));
	}

	protected static void assertHasDegrees(Graph g, String node, int in_degree, int out_degree){
		assertHasInDegree(g, node, in_degree);
		assertHasOutDegree(g, node, out_degree);
	}
	
	protected static void fullAssertGraph(Graph g, int nb_nodes, int nb_edges){
		Assert.assertEquals(nb_nodes, g.getNbNodes());
		Assert.assertEquals(nb_edges, g.getNbEdges());
	}
	
	protected static void fullAssertNode(Graph g, String node, String[] srcs, String[] dsts){
		assertHasDegrees(g, node, srcs.length, dsts.length);
		assertHasEdges(g, node, dsts);
		assertHasEdges(g, srcs, node);
	}
	
	protected static void executionInspect(Graph aGraph) throws Exception{
		aGraph.bestDisplay();
		
		
		for(String node : aGraph.getNodesNames()){
			System.out.println("\t"+popFunction(node));
		}
		System.out.println("Press any key to continue...");
		System.in.read();
	}
	
	protected static String stateAsATestCase(Graph aGraph){
		String out = "fullAssertGraph(dg, "+aGraph.getNbNodes()+", "+aGraph.getNbEdges()+");\n\n";
		
		for(String node : aGraph.getNodesNames()){
			String innodeslist = "";
			for(String innode : aGraph.getNodesConnectedTo(node)){
				innodeslist += ((innodeslist.length()>0)?", ":"")+"\""+innode+"\"";
			}

			String outnodeslist = "";
			for(String outnode : aGraph.getNodesConnectedFrom(node)){
				outnodeslist += ((outnodeslist.length()>0)?", ":"")+"\""+outnode+"\"";
			}
			
			String thisnode = "fullAssertNode(dg, \n\""+node+"\", \nnew String[]{"+innodeslist+"}, \nnew String[]{"+outnodeslist+"});";
			out += thisnode+"\n\n";
		}
		
		
		return out;
	}
}
