package com.vmusco.softminer.tests;

import org.junit.Test;

/**
*
* @author Vincenzo Musco - http://www.vmusco.com
*/
public class CallGraphTest extends CallGraphAbstractTest {

	public CallGraphTest() {
		super(DepGraphTestHelper.testPkgAndGenerateBuilderCallGraphFactory());
	}
	
	private GraphBuilderObtainer localHelper() {
		return DepGraphTestHelper.testPkgAndGenerateBuilderCallGraphFactory();
	}
	
	@Test
	public void testVariableUsage() throws Exception{
		// Same as CHACG
		CallGraphCHATest t = new CallGraphCHATest();
		t.setGraphBuilderObtainer(localHelper());
		t.testVariableUsage();
	}

	@Test
	public void testInterfaceAndLinking() throws Exception{
		// Same as FCG
		CallGraphFTest t = new CallGraphFTest();
		t.setGraphBuilderObtainer(localHelper());
		t.testInterfaceAndLinking();
	}

	@Test
	public void testAbstractAndInheritanceClasses() throws Exception {
		// Same as FCG
		CallGraphFTest t = new CallGraphFTest();
		t.setGraphBuilderObtainer(localHelper());
		t.testAbstractAndInheritanceClasses();
	}

	@Test
	public void testInterfaceAndInheritance() throws Exception {
		// Same as FCG
		CallGraphFTest t = new CallGraphFTest();
		t.setGraphBuilderObtainer(localHelper());
		t.testInterfaceAndInheritance();
	}

	@Override
	public void testSimpleInheritanceConnectedToAbstract() throws Exception {
		// Same as FCG
		CallGraphFTest t = new CallGraphFTest();
		t.setGraphBuilderObtainer(localHelper());
		t.testSimpleInheritanceConnectedToAbstract();
	}

	@Override
	public void testSimpleInheritanceConnectedToImplementation() throws Exception {
		// Same as FCG
		CallGraphFTest t = new CallGraphFTest();
		t.setGraphBuilderObtainer(localHelper());
		t.testSimpleInheritanceConnectedToImplementation();
	}
	
	@Override
	public void testPaperCase() throws Exception {
		DepGraphTestHelper dgth = new DepGraphTestHelper(getGraphBuilderObtainer(), tweaksDisabler, com.vmusco.softminer.tests.cases.testPaperCase.C.class);
		
		String a = dgth.formatAtom("A()");
		String b = dgth.formatAtom("B()");
		String c_biz1 = dgth.formatAtom("C.biz1()");
		String c_biz2 = dgth.formatAtom("C.biz2()");
		String a_foo = dgth.formatAtom("A.foo()");
		String b_foo = dgth.formatAtom("B.foo()");
		
		//dgth.getGraph().bestDisplay();
		dgth.fullAssertGraph(6, 5);

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
				new String[]{});
		

		dgth.fullAssertNode(
				b_foo, 
				new String[]{c_biz1}, 
				new String[]{});
		

		dgth.fullAssertNode(
				c_biz1, 
				new String[]{}, 
				new String[]{b_foo, b});
	

		dgth.fullAssertNode(
				c_biz2, 
				new String[]{}, 
				new String[]{a_foo, b});
		
		// Paper Figure (no constructors)
		dgth.getGraph().removeNode(a);
		dgth.getGraph().removeNode(b);
		//dgth.getGraph().bestDisplay();
	}

	@Override
	public void testSimpleInheritance() throws Exception {
		// Same as FCG
		CallGraphFTest t = new CallGraphFTest();
		t.setGraphBuilderObtainer(localHelper());
		t.testSimpleInheritance();
	}
	
	@Override
	public void testSimpleMethodCalls() throws Exception {
		DepGraphTestHelper dgth = new DepGraphTestHelper(getGraphBuilderObtainer(), tweaksDisabler, com.vmusco.softminer.tests.cases.testSimpleMethodCalls.Foo.class);
		
		String foo2_bar = dgth.formatAtom("Foo2.bar()");
		String foo2_bar2 = dgth.formatAtom("Foo2.bar2(java.lang.String)");
		
		String foo_bar1 = dgth.formatAtom("Foo.bar1()");
		String foo_bar2 = dgth.formatAtom("Foo.bar2(java.lang.String)");
		String foo_bar3 = dgth.formatAtom("Foo.bar3(int)");
		String foo_bar4 = dgth.formatAtom("Foo.bar4(double)");
		String foo_bar5 = dgth.formatAtom("Foo.bar5(float)");

		//dgth.getGraph().bestDisplay();
		
		dgth.fullAssertGraph(7, 7);

		dgth.fullAssertNode(
				foo2_bar, 
				new String[]{foo_bar5}, 
				new String[]{});

		dgth.fullAssertNode(
				foo2_bar2, 
				new String[]{foo_bar5}, 
				new String[]{});
		

		dgth.fullAssertNode(
				foo_bar1, 
				new String[]{foo_bar5}, 
				new String[]{foo_bar2});
		

		dgth.fullAssertNode(
				foo_bar2, 
				new String[]{foo_bar1}, 
				new String[]{foo_bar3, foo_bar4});

		dgth.fullAssertNode(
				foo_bar3, 
				new String[]{foo_bar2}, 
				new String[]{foo_bar4});


		dgth.fullAssertNode(
				foo_bar4, 
				new String[]{foo_bar2, foo_bar3}, 
				new String[]{});


		dgth.fullAssertNode(
				foo_bar5, 
				new String[]{}, 
				new String[]{foo_bar1, foo2_bar, foo2_bar2});
	}
	
}
