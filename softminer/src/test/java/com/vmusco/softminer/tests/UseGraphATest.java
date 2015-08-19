package com.vmusco.softminer.tests;

import org.junit.Test;

public class UseGraphATest extends UseGraphAbstractTest {

	private GraphBuilderObtainer localHelper() {
		return DepGraphTestHelper.testPkgAndGenerateBuilderUseGraphAFactory();
	}
	
	@Test
	public void testVariableUsage() throws Exception{
		// Same as C
		UseGraphCTest t = new UseGraphCTest();
		t.setGraphBuilderObtainer(localHelper());
		t.testVariableUsage();
	}

	@Test
	public void testInterfaceAndLinking() throws Exception{
		// Same as B
		UseGraphBTest t = new UseGraphBTest();
		t.setGraphBuilderObtainer(localHelper());
		t.testInterfaceAndLinking();
	}

	@Test
	public void testAbstractAndInheritanceClasses() throws Exception {
		// Same as B
		UseGraphBTest t = new UseGraphBTest();
		t.setGraphBuilderObtainer(localHelper());
		t.testAbstractAndInheritanceClasses();
	}

	@Test
	public void testInterfaceAndInheritance() throws Exception {
		// Same as B
		UseGraphBTest t = new UseGraphBTest();
		t.setGraphBuilderObtainer(localHelper());
		t.testInterfaceAndInheritance();
	}
}
