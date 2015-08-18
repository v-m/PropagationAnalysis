package com.vmusco.softminer.tests;

import org.junit.Test;

import com.vmusco.softminer.graphs.Graph;

public class UseGraphCTest {
	private GraphBuilderObtainer gbo = DepGraphTestHelper.testPkgAndGenerateBuilderUseGraphCFactory();
	
	public void setGraphBuilderObtainer(GraphBuilderObtainer gbo) {
		this.gbo = gbo;
	}
	
	/***
	 * This test allows testing the direction of the edge regarding variable get/set.
	 * A setter must go from method to variable, but a reader must go from varable to method (reverse edge)
	 * @throws Exception
	 */
	@Test
	public void testVariableUsage() throws Exception{
		System.out.println(gbo);
		DepGraphTestHelper dgth = new DepGraphTestHelper(gbo, com.vmusco.softminer.tests.cases.testVariableUsage.MyClass.class);
		Graph dg = dgth.getGraph();
		
		dgth.fullAssertGraph(dg, 0, 0);
	}

	/****
	 * This test is used to check if calling an interface method is represented as a link on the graph node
	 * @throws Exception
	 */
	@Test
	public void testInterfaceAndLinking() throws Exception{
		DepGraphTestHelper dgth = new DepGraphTestHelper(gbo, com.vmusco.softminer.tests.cases.testInterfaceAndLinking.T.class);
		Graph dg = dgth.getGraph();

		//dgth.executionInspect(dg);
		dgth.fullAssertGraph(dg, 7, 6);
		
		String t_biz = dgth.formatAtom("T.biz("+com.vmusco.softminer.tests.cases.testInterfaceAndLinking.A.class.getCanonicalName()+")");
		String u_bar = dgth.formatAtom("U.bar()");
		String a_foo = dgth.formatAtom("A.foo()");
		String b_b = dgth.formatAtom("B()");
		String c_c = dgth.formatAtom("C()");
		String c_foo = dgth.formatAtom("C.foo()");
		String b_foo = dgth.formatAtom("B.foo()");

		dgth.fullAssertNode(dg, 
				t_biz, 
				new String[]{u_bar}, 
				new String[]{a_foo});

		dgth.fullAssertNode(dg, 
				a_foo, 
				new String[]{t_biz}, 
				new String[]{b_foo, c_foo});

		dgth.fullAssertNode(dg, 
				u_bar, 
				new String[]{}, 
				new String[]{c_c, t_biz, b_b});

		dgth.fullAssertNode(dg, 
				c_c, 
				new String[]{u_bar}, 
				new String[]{});

		dgth.fullAssertNode(dg, 
				b_b, 
				new String[]{u_bar}, 
				new String[]{});

		dgth.fullAssertNode(dg, 
				b_foo, 
				new String[]{a_foo}, 
				new String[]{});
		
		dgth.fullAssertNode(dg, 
				c_foo, 
				new String[]{a_foo}, 
				new String[]{});
	}

}
