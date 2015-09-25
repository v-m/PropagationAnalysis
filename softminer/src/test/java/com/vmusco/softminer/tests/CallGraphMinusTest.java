package com.vmusco.softminer.tests;

import org.junit.Test;

/**
*
* @author Vincenzo Musco - http://www.vmusco.com
*/
public class CallGraphMinusTest extends CallGraphAbstractTest {

	private GraphBuilderObtainer localHelper() {
		return DepGraphTestHelper.testPkgAndGenerateBuilderMinusCallGraphFactory();
	}
	
	public CallGraphMinusTest() {
		super(DepGraphTestHelper.testPkgAndGenerateBuilderMinusCallGraphFactory());
	}
	
	@Test
	public void testVariableUsage() throws Exception{

	}

	@Test
	public void testInterfaceAndLinking() throws Exception{

	}

	@Test
	public void testAbstractAndInheritanceClasses() throws Exception {

	}

	@Test
	public void testInterfaceAndInheritance() throws Exception {

	}

	@Override
	public void testSimpleInheritanceConnectedToAbstract() throws Exception {
		CallGraphFTest t = new CallGraphFTest();
		t.setGraphBuilderObtainer(localHelper());
		t.testSimpleInheritanceConnectedToAbstract();
	}

	@Override
	public void testSimpleInheritanceConnectedToImplementation() throws Exception {
		DepGraphTestHelper dgth = new DepGraphTestHelper(getGraphBuilderObtainer(), com.vmusco.softminer.tests.cases.testSimpleInheritanceConnectedToImplementation.Impl.class);
		
		String abs = dgth.formatAtom("Abs()");
		String impl = dgth.formatAtom("Impl()");
		String impl_fct = dgth.formatAtom("Impl.fct()");
		
		dgth.fullAssertGraph(3, 2);
		
		dgth.fullAssertNode(
				impl_fct, 
				new String[]{}, 
				new String[]{impl});

		dgth.fullAssertNode(
				impl, 
				new String[]{impl_fct}, 
				new String[]{abs});
		

		dgth.fullAssertNode(
				abs, 
				new String[]{impl}, 
				new String[]{});
	}
	
	@Override
	public void testPaperCase() throws Exception {
		DepGraphTestHelper dgth = new DepGraphTestHelper(getGraphBuilderObtainer(), com.vmusco.softminer.tests.cases.testPaperCase.B.class);
		
		String a = dgth.formatAtom("A()");
		String b = dgth.formatAtom("B()");
		String c_biz1 = dgth.formatAtom("C.biz1()");
		String c_biz2 = dgth.formatAtom("C.biz2()");
		String a_foo = dgth.formatAtom("A.foo()");
		
		//dgth.getGraph().bestDisplay();
		dgth.fullAssertGraph(5, 4);

		dgth.fullAssertNode(
				a, 
				new String[]{b}, 
				new String[]{});

		dgth.fullAssertNode(
				b, 
				new String[]{c_biz1, c_biz2}, 
				new String[]{a});

		dgth.fullAssertNode(
				c_biz1, 
				new String[]{}, 
				new String[]{b});

		dgth.fullAssertNode(
				c_biz2, 
				new String[]{}, 
				new String[]{b, a_foo});
	}
}
