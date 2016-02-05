package com.vmusco.softminer.tests;

import org.junit.Test;

public class CallGraphTweakingTests {
	@Test
	public void testNonReturnEdgesRemoval() throws Exception {
		GraphBuilderObtainer testPkgAndGenerateBuilderCallGraph = DepGraphTestHelper.testPkgAndGenerateBuilderCallGraphFactory();

		DepGraphTestHelper dgth = new DepGraphTestHelper(testPkgAndGenerateBuilderCallGraph, com.vmusco.softminer.tests.cases.cgtweaking.MyClass.class);

		String MyClass_bar = dgth.formatAtom("MyClass.bar()");
		String MyClass_biz = dgth.formatAtom("MyClass.biz()");
		String MyClass_foo = dgth.formatAtom("MyClass.foo()");
		String MyMain_main = dgth.formatAtom("MyMain.main(java.lang.String[])");
		String MyClass = dgth.formatAtom("MyClass(int)");

		//dgth.getGraph().bestDisplay();

		dgth.fullAssertGraph(5, 4);

		dgth.fullAssertNode(
				MyClass_bar, 
				new String[]{MyClass_biz}, 
				new String[]{});

		dgth.fullAssertNode(
				MyClass_biz, 
				new String[]{}, 
				new String[]{MyClass_bar,MyClass_foo});


		dgth.fullAssertNode(
				MyClass_foo, 
				new String[]{MyClass_biz, MyMain_main}, 
				new String[]{});


		dgth.fullAssertNode(
				MyMain_main, 
				new String[]{}, 
				new String[]{MyClass,MyClass_foo});

		dgth.fullAssertNode(
				MyClass, 
				new String[]{MyMain_main}, 
				new String[]{});
	}
}
