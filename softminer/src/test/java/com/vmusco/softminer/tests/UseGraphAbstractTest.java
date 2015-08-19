package com.vmusco.softminer.tests;

import org.junit.Test;

import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.sourceanalyzer.graphbuilding.GraphBuilder;

public abstract class UseGraphAbstractTest {
	private GraphBuilderObtainer gbo = DepGraphTestHelper.testPkgAndGenerateBuilderUseGraphBFactory();
	
	public void setGraphBuilderObtainer(GraphBuilderObtainer gbo) {
		this.gbo = gbo;
	}
	
	protected GraphBuilderObtainer getGraphBuilderObtaine() {
		return gbo;
	}
	
	/***
	 * This test allows testing the direction of the edge regarding variable get/set.
	 * A setter must go from method to variable, but a reader must go from varable to method (reverse edge)
	 * @throws Exception
	 */
	@Test
	public abstract void testVariableUsage() throws Exception;

	/****
	 * This test is used to check if calling an interface method is represented as a link on the graph node
	 * @throws Exception
	 */
	@Test
	public abstract void testInterfaceAndLinking() throws Exception;
	
	/**
	 * Test abstract class inheritance and links
	 * Status: Finished
	 * @throws Exception
	 */
	@Test
	public abstract void testAbstractAndInheritanceClasses() throws Exception;
	
	@Test
	public abstract void testInterfaceAndInheritance() throws Exception;
}
