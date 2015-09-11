package com.vmusco.softminer.tests;

import org.junit.Test;

/**
*
* @author Vincenzo Musco - http://www.vmusco.com
*/
public class CallGraphFCHATest extends CallGraphAbstractTest {
	public CallGraphFCHATest() {
		super(null);
	}
	
	private GraphBuilderObtainer localHelper() {
		return DepGraphTestHelper.testPkgAndGenerateBuilderFCHACallGraphFactory();
	}
	
	@Test
	public void testVariableUsage() throws Exception{
		// Same as FCG
		CallGraphFTest t = new CallGraphFTest();
		t.setGraphBuilderObtainer(localHelper());
		t.testVariableUsage();
	}

	@Test
	public void testInterfaceAndLinking() throws Exception{
		// Same as CHACG
		CallGraphCHATest t = new CallGraphCHATest();
		t.setGraphBuilderObtainer(localHelper());
		t.testInterfaceAndLinking();
	}
	
	@Test
	public void testAbstractAndInheritanceClasses() throws Exception{
		// Same as CHACG
		CallGraphCHATest t = new CallGraphCHATest();
		t.setGraphBuilderObtainer(localHelper());
		t.testAbstractAndInheritanceClasses();
	}

	@Test
	public void testInterfaceAndInheritance() throws Exception{
		// Same as CHACG
		CallGraphCHATest t = new CallGraphCHATest();
		t.setGraphBuilderObtainer(localHelper());
		t.testInterfaceAndInheritance();
	}

	@Override
	public void testSimpleInheritanceConnectedToAbstract() throws Exception {
		// Same as CHACG
		CallGraphCHATest t = new CallGraphCHATest();
		t.setGraphBuilderObtainer(localHelper());
		t.testSimpleInheritanceConnectedToAbstract();
	}

	@Override
	public void testSimpleInheritanceConnectedToImplementation()
			throws Exception {
		// Same as CHACG
		CallGraphCHATest t = new CallGraphCHATest();
		t.setGraphBuilderObtainer(localHelper());
		t.testSimpleInheritanceConnectedToImplementation();
	}

}
