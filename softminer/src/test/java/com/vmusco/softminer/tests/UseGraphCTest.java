package com.vmusco.softminer.tests;

import org.junit.Test;

import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.sourceanalyzer.graphbuilding.GraphBuilder;

public class UseGraphCTest extends UseGraphAbstractTest {
	
	public UseGraphCTest() {
		setGraphBuilderObtainer(DepGraphTestHelper.testPkgAndGenerateBuilderUseGraphCFactory());
	}
	
	@Test
	public void testVariableUsage() throws Exception{
		DepGraphTestHelper dgth = new DepGraphTestHelper(getGraphBuilderObtainer(), com.vmusco.softminer.tests.cases.testVariableUsage.MyClass.class);
		Graph dg = dgth.getGraph();
		
		dgth.fullAssertGraph(0, 0);
	}

	/****
	 * This test is used to check if calling an interface method is represented as a link on the graph node
	 * @throws Exception
	 */
	@Test
	public void testInterfaceAndLinking() throws Exception{
		DepGraphTestHelper dgth = new DepGraphTestHelper(getGraphBuilderObtainer(), com.vmusco.softminer.tests.cases.testInterfaceAndLinking.T.class);
		Graph dg = dgth.getGraph();

		//dgth.executionInspect(dg);
		dgth.fullAssertGraph(7, 6);
		
		String t_biz = dgth.formatAtom("T.biz("+com.vmusco.softminer.tests.cases.testInterfaceAndLinking.A.class.getCanonicalName()+")");
		String u_bar = dgth.formatAtom("U.bar()");
		String a_foo = dgth.formatAtom("A.foo()");
		String b_b = dgth.formatAtom("B()");
		String c_c = dgth.formatAtom("C()");
		String c_foo = dgth.formatAtom("C.foo()");
		String b_foo = dgth.formatAtom("B.foo()");

		dgth.fullAssertNode(
				t_biz, 
				new String[]{u_bar}, 
				new String[]{a_foo});

		dgth.fullAssertNode(
				a_foo, 
				new String[]{t_biz}, 
				new String[]{b_foo, c_foo});

		dgth.fullAssertNode(
				u_bar, 
				new String[]{}, 
				new String[]{c_c, t_biz, b_b});

		dgth.fullAssertNode(
				c_c, 
				new String[]{u_bar}, 
				new String[]{});

		dgth.fullAssertNode(
				b_b, 
				new String[]{u_bar}, 
				new String[]{});

		dgth.fullAssertNode(
				b_foo, 
				new String[]{a_foo}, 
				new String[]{});
		
		dgth.fullAssertNode(
				c_foo, 
				new String[]{a_foo}, 
				new String[]{});
	}
	
	/**
	 * Test abstract class inheritance and links
	 * Status: Finished
	 * @throws Exception
	 */
	@Test
	public void testAbstractAndInheritanceClasses() throws Exception{
		DepGraphTestHelper dgth = new DepGraphTestHelper(getGraphBuilderObtainer(), com.vmusco.softminer.tests.cases.testAbstractAndInheritance.A.class);
		Graph dg = dgth.getGraph();

		//dgth.executionInspect(dg);
		dgth.fullAssertGraph(11, 11);

		String a = dgth.formatAtom("A()");
		String a2 = dgth.formatAtom("A2()");
		String a3 = dgth.formatAtom("A3()");
		String a4 = dgth.formatAtom("A4()");		
		String a2_bar = dgth.formatAtom("A2.bar()");
		String a_bar = dgth.formatAtom("A.bar()");
		String a_biz = dgth.formatAtom("A.biz()");
		String t_foo = dgth.formatAtom("T.foo()");
		String a_foo = dgth.formatAtom("A.foo()");
		String a3_foo = dgth.formatAtom("A3.foo()");
		String a4_foo = dgth.formatAtom("A4.foo()");

		dgth.fullAssertNode(
				a, 
				new String[]{a2}, 
				new String[]{});
		
		dgth.fullAssertNode(
				a2, 
				new String[]{a4, a3}, 
				new String[]{a});

		dgth.fullAssertNode(
				a3, 
				new String[]{t_foo}, 
				new String[]{a2});

		dgth.fullAssertNode(
				a4, 
				new String[]{t_foo}, 
				new String[]{a2});
		
		dgth.fullAssertNode(
				a2_bar, 
				new String[]{a_bar}, 
				new String[]{});
		
		dgth.fullAssertNode(
				a_bar, 
				new String[]{t_foo}, 
				new String[]{a2_bar});

		dgth.fullAssertNode(
				a_biz, 
				new String[]{t_foo}, 
				new String[]{});

		dgth.fullAssertNode(
				t_foo, 
				new String[]{}, 
				new String[]{a4, a_biz, a_bar, a3, a_foo});

		dgth.fullAssertNode(
				a_foo, 
				new String[]{t_foo}, 
				new String[]{a3_foo, a4_foo});
		
		dgth.fullAssertNode(
				a3_foo, 
				new String[]{a_foo}, 
				new String[]{});

		dgth.fullAssertNode(
				a4_foo, 
				new String[]{a_foo}, 
				new String[]{});
	}

	@Test
	public void testInterfaceAndInheritance() throws Exception{
		DepGraphTestHelper dgth = new DepGraphTestHelper(getGraphBuilderObtainer(), com.vmusco.softminer.tests.cases.testInterfaceAndInheritance.Z.class);
		Graph dg = dgth.getGraph();
		
		//dgth.executionInspect();
		//System.out.println(stateAsATestCase());	

		String z_main = dgth.formatAtom("Z.main("+DepGraphTestHelper.STRING_CANONICAL_NAME+"[])");
		String b_bar = dgth.formatAtom("B.bar()");
		String a_foo = dgth.formatAtom("A.foo()");
		String c_bar = dgth.formatAtom("C.bar()");
		String c = dgth.formatAtom("C()");
		String c_foo = dgth.formatAtom("C.foo()");

		dgth.fullAssertGraph(6, 7);

		dgth.fullAssertNode(
				z_main, 
				new String[]{}, 
				new String[]{b_bar, a_foo, c_bar, c, c_foo});

		dgth.fullAssertNode(
				b_bar, 
				new String[]{z_main}, 
				new String[]{c_bar});

		dgth.fullAssertNode(
				a_foo, 
				new String[]{z_main}, 
				new String[]{c_foo});

		dgth.fullAssertNode(
				c_bar, 
				new String[]{z_main, b_bar}, 
				new String[]{});

		dgth.fullAssertNode(
				c, 
				new String[]{z_main}, 
				new String[]{});

		dgth.fullAssertNode(
				c_foo, 
				new String[]{z_main, a_foo}, 
				new String[]{});
	}

	
}
