package com.vmusco.softminer.tests;

import org.junit.Assert;

import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.sourceanalyzer.ProcessorCommunicator;
import com.vmusco.softminer.sourceanalyzer.ProcessorCommunicator.PatternBehavior;
import com.vmusco.softminer.sourceanalyzer.graphbuilding.GraphBuildLogic;
import com.vmusco.softminer.sourceanalyzer.graphbuilding.GraphBuilder;
import com.vmusco.softminer.sourceanalyzer.graphbuilding.SpoonGraphBuilder;
import com.vmusco.softminer.sourceanalyzer.processors.GraphItemRenamer;

public class DepGraphTestHelper {
	private String currTestedPakg = "";
	private GraphBuildLogic buildingLogicToUse = SpoonGraphBuilder.getFeatureGranularityGraphBuilder();
	private GraphBuilder graphBuilder; 
	
	public static final String STRING_CANONICAL_NAME = java.lang.String.class.getCanonicalName();
	public static final String CLASS_CANONICAL_NAME = java.lang.Class.class.getCanonicalName();
	
	public String formatAtom(String atom){
		return buildingLogicToUse.formatAtom(atom);
	}
	
	public DepGraphTestHelper(GraphBuilderObtainer gbo, Class packageInto) throws Exception {
		currTestedPakg = packageInto.getPackage().getName();
		String[] sources = getFullPath(currTestedPakg); 
		graphBuilder = gbo.getGraphBuilder(sources);
		postSetTestPkgAndGenerateBuilder();
	}
	
	public Graph getGraph() throws Exception{
		return graphBuilder.generateDependencyGraph(buildingLogicToUse);
	}
	
	/**
	 * Prepare the test for a specific test package
	 * @param packageInto specify any class into the target package
	 * @return
	 * @throws Exception
	 */
	public static GraphBuilderObtainer testPkgAndGenerateBuilderUseGraphAFactory(){
		return new GraphBuilderObtainer() {
			@Override
			public GraphBuilder getGraphBuilder(String[] items) {
				return GraphBuilder.newGraphBuilderOnlyWithDependencies("test", items); 
			}
		};
	}
	
	public static GraphBuilderObtainer testPkgAndGenerateBuilderUseGraphBFactory(){
		return new GraphBuilderObtainer() {
			@Override
			public GraphBuilder getGraphBuilder(String[] items) {
				return GraphBuilder.newGraphBuilderWithFields("test", items); 
			}
		};
	}
	
	public static GraphBuilderObtainer testPkgAndGenerateBuilderUseGraphCFactory(){
		return new GraphBuilderObtainer() {
			@Override
			public GraphBuilder getGraphBuilder(String[] items) {
				return GraphBuilder.newGraphBuilderWithInheritence("test", items); 
			}
		};
	}
	
	public static GraphBuilderObtainer testPkgAndGenerateBuilderUseGraphDFactory(){
		return new GraphBuilderObtainer() {
			@Override
			public GraphBuilder getGraphBuilder(String[] items) {
				return GraphBuilder.newGraphBuilderWithFieldsAndInheritence("test", items); 
			}
		};
	}
	
	private static String[] getFullPath(String pt){
		return new String[]{System.getProperty("user.dir")+"/src/test/java/"+pt.replaceAll("\\.", "/")};
	}
	
	private void postSetTestPkgAndGenerateBuilder() throws Exception{
		ProcessorCommunicator.patternBehavior = PatternBehavior.INCLUDE_IF_BOTH;
		ProcessorCommunicator.pattern = getFunction("").replace(".", "\\.")+".*";
		
		ProcessorCommunicator.nodesRenamer = new GraphItemRenamer() {
			public String renamed(String originalName) {
				return popFunction(originalName);
			}
		};
	}
		
	
	protected String getFunction(String name){
		return currTestedPakg + "." + name;
	}
	
	protected String popFunction(String name){
		String start = currTestedPakg + ".";
		if(name.startsWith(start))
			return name.substring(start.length());
		return name;
	}
	
	protected void assertHasEdge(Graph g, String src, String dst){
		//System.out.println(Tests.getFunction(src)+" -> "+Tests.getFunction(dst)+" ?");
		String fsrc = getFunction(src);
		String fdst = getFunction(dst);
		
		if(ProcessorCommunicator.nodesRenamer != null){
			fsrc = ProcessorCommunicator.nodesRenamer.renamed(fsrc);
			fdst = ProcessorCommunicator.nodesRenamer.renamed(fdst);
		}
		
		Assert.assertTrue(g.hasDirectedEdge(fsrc, fdst));
	}
	
	protected void assertHasEdges(Graph g, String src, String[] dsts){
		for(String dst : dsts){
			assertHasEdge(g, src, dst);
		}
	}
	
	protected void assertHasEdges(Graph g, String[] srcs, String dst){
		for(String src : srcs){
			assertHasEdge(g, src, dst);
		}
	}
	
	protected void assertHasOutDegree(Graph g, String node, int degree){
		Assert.assertEquals(degree, g.getOutDegreeFor(node));
	}

	protected void assertHasInDegree(Graph g, String node, int degree){
		Assert.assertEquals(degree, g.getInDegreeFor(node));
	}

	protected void assertHasDegree(Graph g, String node, int degree){
		Assert.assertEquals(degree, g.getDegreeFor(node));
	}

	protected void assertHasDegrees(Graph g, String node, int in_degree, int out_degree){
		assertHasInDegree(g, node, in_degree);
		assertHasOutDegree(g, node, out_degree);
	}
	
	protected void fullAssertGraph(Graph g, int nb_nodes, int nb_edges){
		Assert.assertEquals(nb_nodes, g.getNbNodes());
		Assert.assertEquals(nb_edges, g.getNbEdges());
	}
	
	protected void fullAssertNode(Graph g, String node, String[] srcs, String[] dsts){
		assertHasDegrees(g, node, srcs.length, dsts.length);
		assertHasEdges(g, node, dsts);
		assertHasEdges(g, srcs, node);
	}
	
	protected void executionInspect(Graph aGraph) throws Exception{
		aGraph.bestDisplay();
		
		
		for(String node : aGraph.getNodesNames()){
			System.out.println("\t"+popFunction(node));
		}
		System.out.println("Press any key to continue...");
		System.in.read();
	}
	
	protected String stateAsATestCase(Graph aGraph){
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
