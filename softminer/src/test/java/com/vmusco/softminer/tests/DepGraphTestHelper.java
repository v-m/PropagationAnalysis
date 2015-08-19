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
	private Graph builtGraph;
	
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
		builtGraph = graphBuilder.generateDependencyGraph(buildingLogicToUse); 
	}
	
	public Graph getGraph() throws Exception{
		return builtGraph;
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
	
	public void assertHasEdge(String src, String dst){
		//System.out.println(Tests.getFunction(src)+" -> "+Tests.getFunction(dst)+" ?");
		String fsrc = getFunction(src);
		String fdst = getFunction(dst);
		
		if(ProcessorCommunicator.nodesRenamer != null){
			fsrc = ProcessorCommunicator.nodesRenamer.renamed(fsrc);
			fdst = ProcessorCommunicator.nodesRenamer.renamed(fdst);
		}
		
		Assert.assertTrue(builtGraph.hasDirectedEdge(fsrc, fdst));
	}
	
	public void assertHasEdges(String src, String[] dsts){
		for(String dst : dsts){
			assertHasEdge(src, dst);
		}
	}
	
	protected void assertHasEdges(String[] srcs, String dst){
		for(String src : srcs){
			assertHasEdge(src, dst);
		}
	}
	
	protected void assertHasOutDegree(String node, int degree){
		Assert.assertEquals(degree, builtGraph.getOutDegreeFor(node));
	}

	protected void assertHasInDegree(String node, int degree){
		Assert.assertEquals(degree, builtGraph.getInDegreeFor(node));
	}

	protected void assertHasDegree(String node, int degree){
		Assert.assertEquals(degree, builtGraph.getDegreeFor(node));
	}

	protected void assertHasDegrees(String node, int in_degree, int out_degree){
		assertHasInDegree(node, in_degree);
		assertHasOutDegree(node, out_degree);
	}
	
	protected void fullAssertGraph(int nb_nodes, int nb_edges){
		Assert.assertEquals(nb_nodes, builtGraph.getNbNodes());
		Assert.assertEquals(nb_edges, builtGraph.getNbEdges());
	}
	
	protected void fullAssertNode(String node, String[] srcs, String[] dsts){
		assertHasDegrees(node, srcs.length, dsts.length);
		assertHasEdges(node, dsts);
		assertHasEdges(srcs, node);
	}
	
	protected void executionInspect() throws Exception{
		builtGraph.bestDisplay();
		
		
		for(String node : builtGraph.getNodesNames()){
			System.out.println("\t"+popFunction(node));
		}
		System.out.println("Press any key to continue...");
		System.in.read();
	}
	
	protected String stateAsATestCase(){
		String out = "fullAssertGraph(dg, "+builtGraph.getNbNodes()+", "+builtGraph.getNbEdges()+");\n\n";
		
		for(String node : builtGraph.getNodesNames()){
			String innodeslist = "";
			for(String innode : builtGraph.getNodesConnectedTo(node)){
				innodeslist += ((innodeslist.length()>0)?", ":"")+"\""+innode+"\"";
			}

			String outnodeslist = "";
			for(String outnode : builtGraph.getNodesConnectedFrom(node)){
				outnodeslist += ((outnodeslist.length()>0)?", ":"")+"\""+outnode+"\"";
			}
			
			String thisnode = "fullAssertNode(dg, \n\""+node+"\", \nnew String[]{"+innodeslist+"}, \nnew String[]{"+outnodeslist+"});";
			out += thisnode+"\n\n";
		}
		
		
		return out;
	}
}
