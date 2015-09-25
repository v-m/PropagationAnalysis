package com.vmusco.softminer.tests;

import org.junit.Test;

/**
*
* @author Vincenzo Musco - http://www.vmusco.com
*/
public class CallGraphFCHATest extends CallGraphAbstractTest {
	public CallGraphFCHATest() {
		super(DepGraphTestHelper.testPkgAndGenerateBuilderFCHACallGraphFactory());
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

	@Override
	public void testPaperCase() throws Exception {
		DepGraphTestHelper dgth = new DepGraphTestHelper(getGraphBuilderObtainer(), com.vmusco.softminer.tests.cases.testPaperCase.C.class);

		String a = dgth.formatAtom("A()");
		String b = dgth.formatAtom("B()");
		String c_biz1 = dgth.formatAtom("C.biz1()");
		String c_biz2 = dgth.formatAtom("C.biz2()");
		String a_foo = dgth.formatAtom("A.foo()");
		String b_foo = dgth.formatAtom("B.foo()");
		String c_bar = dgth.formatAtom("C#bar");
		
		//dgth.getGraph().bestDisplay();
		dgth.fullAssertGraph(7, 8);
		
		dgth.fullAssertNode(
				b, 
				new String[]{c_biz1, c_biz2}, 
				new String[]{a});
		

		dgth.fullAssertNode(
				a, 
				new String[]{b}, 
				new String[]{});
		

		dgth.fullAssertNode(
				a_foo, 
				new String[]{c_biz2}, 
				new String[]{b_foo});
		

		dgth.fullAssertNode(
				b_foo, 
				new String[]{c_biz1, a_foo}, 
				new String[]{});
		

		dgth.fullAssertNode(
				c_biz1, 
				new String[]{c_bar}, 
				new String[]{b_foo, b});
	

		dgth.fullAssertNode(
				c_biz2, 
				new String[]{}, 
				new String[]{a_foo, b, c_bar});
		

		dgth.fullAssertNode(
				c_bar, 
				new String[]{c_biz2}, 
				new String[]{c_biz1});
		
		// Paper Figure (no constructors)
		dgth.getGraph().removeNode(a);
		dgth.getGraph().removeNode(b);
		//dgth.getGraph().bestDisplay();
	}
}
