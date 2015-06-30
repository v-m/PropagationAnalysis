package com.vmusco.softminer.tests;

import java.io.File;

import junit.framework.Assert;

import org.junit.Test;

import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.graphs.Graph.NodeSize;
import com.vmusco.softminer.sourceanalyzer.ProcessorCommunicator;
import com.vmusco.softminer.sourceanalyzer.graphbuilding.GraphBuilder;
import com.vmusco.softminer.sourceanalyzer.graphbuilding.SpoonGraphBuilder;
import com.vmusco.softminer.sourceanalyzer.processors.SpecificTags;

public class ConcreteDependencyGraphExtracting extends DepGraphTest {
	
	@Test
	public void testPracticalApacheCommonLang3TestSmtp() throws Exception{
		GraphBuilder gb = setTestPkgAndGenerateBuilder(com.vmusco.softminer.tests.cases.real.testPracticalApacheCommonLang3TestSMTP.DateFormatUtilsTest.class);
		ProcessorCommunicator.resolveInterfacesAndClasses = true;
		Graph dg = gb.generateDependencyGraph(buildingLogicToUse);
		dg.persistAsImage("/tmp/graph.png");
		
		for(String n : dg.getNodesNames())
			System.out.println(n);

		dg.colorNode("DateFormatUtilsTest.testSMTP()", "red");
		dg.sizeNode("DateFormatUtilsTest.testSMTP()", NodeSize.LARGE);
		dg.colorNode("FastDateFormat.parseToken(java.lang.String,int[])", "blue");
		dg.sizeNode("FastDateFormat.parseToken(java.lang.String,int[])", NodeSize.LARGE);

		dg.bestDisplay();
		//executionInspect(dg);
		//System.out.println(stateAsATestCase(dg));

		Assert.fail("Checking not implemented yet !");
	}
	
	@Test
	public void testPracticalApacheCommonLang3StrSubstitutor() throws Exception{
		GraphBuilder gb = setTestPkgAndGenerateBuilder(com.vmusco.softminer.tests.cases.real.testPracticalApacheCommonLang3StrSubstitutor.StrSubstitutorTest.class);
		ProcessorCommunicator.resolveInterfacesAndClasses = true;
		Graph dg = gb.generateDependencyGraph(buildingLogicToUse);
		dg.persistAsImage("/tmp/graph.png");
		
		for(String n : dg.getNodesNames())
			System.out.println(n);

		/*dg.colorNode("DateFormatUtilsTest.testSMTP()", "red");
		dg.sizeNode("DateFormatUtilsTest.testSMTP()", NodeSize.LARGE);
		dg.colorNode("FastDateFormat.parseToken(java.lang.String,int[])", "blue");
		dg.sizeNode("FastDateFormat.parseToken(java.lang.String,int[])", NodeSize.LARGE);*/

		dg.bestDisplay();
		//executionInspect(dg);
		//System.out.println(stateAsATestCase(dg));

		Assert.fail("Checking not implemented yet !");
	}
	
}
