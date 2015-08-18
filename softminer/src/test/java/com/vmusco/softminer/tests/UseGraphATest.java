package com.vmusco.softminer.tests;

import org.junit.Test;

public class UseGraphATest {

	private GraphBuilderObtainer localHelper() {
		return DepGraphTestHelper.testPkgAndGenerateBuilderUseGraphAFactory();
	}
	
	/***
	 * This test allows testing the direction of the edge regarding variable get/set.
	 * A setter must go from method to variable, but a reader must go from varable to method (reverse edge)
	 * @throws Exception
	 */
	@Test
	public void testVariableUsage() throws Exception{
		// Same as C
		UseGraphCTest t = new UseGraphCTest();
		t.setGraphBuilderObtainer(localHelper());
		t.testVariableUsage();
	}


	/****
	 * This test is used to check if calling an interface method is represented as a link on the graph node
	 * @throws Exception
	 */
	@Test
	public void testInterfaceAndLinking() throws Exception{
		// Same as B
		UseGraphBTest t = new UseGraphBTest();
		t.setGraphBuilderObtainer(localHelper());
		t.testInterfaceAndLinking();
	}

}
