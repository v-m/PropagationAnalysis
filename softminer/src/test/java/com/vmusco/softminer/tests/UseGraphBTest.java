package com.vmusco.softminer.tests;

import junit.framework.Assert;

import org.junit.Test;

import com.vmusco.softminer.graphs.EdgeTypes;
import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.sourceanalyzer.graphbuilding.GraphBuilder;

public class UseGraphBTest extends UseGraphAbstractTest{
	
	public UseGraphBTest() {
		setGraphBuilderObtainer(DepGraphTestHelper.testPkgAndGenerateBuilderUseGraphBFactory());
	}
	
	@Test
	public void testVariableUsage() throws Exception{
		DepGraphTestHelper dgth = new DepGraphTestHelper(getGraphBuilderObtainer(), com.vmusco.softminer.tests.cases.testVariableUsage.MyClass.class);
		
		//dgth.executionInspect(dg);
		//System.out.println(dgth.stateAsATestCase(dg));

		String set_variable = dgth.formatAtom("MyClass.setaVariable("+DepGraphTestHelper.STRING_CANONICAL_NAME+")");
		String get_variable = dgth.formatAtom("MyClass.getaVariable()");
		String variable_self = dgth.formatAtom("MyClass#aVariable");

		dgth.fullAssertGraph(3, 2);

		dgth.fullAssertNode( 
				variable_self, 
				new String[]{get_variable}, 
				new String[]{set_variable});

		dgth.fullAssertNode( 
				get_variable, 
				new String[]{}, 
				new String[]{variable_self});

		dgth.fullAssertNode( 
				set_variable, 
				new String[]{variable_self}, 
				new String[]{});

		Assert.assertEquals(dgth.getGraph().getEdgeType(get_variable, variable_self), EdgeTypes.READ_OPERATION);
		Assert.assertEquals(dgth.getGraph().getEdgeType(variable_self, set_variable), EdgeTypes.WRITE_OPERATION);
	}

	@Test
	public void testInterfaceAndLinking() throws Exception{
		DepGraphTestHelper dgth = new DepGraphTestHelper(getGraphBuilderObtainer(), com.vmusco.softminer.tests.cases.testInterfaceAndLinking.T.class);
		Graph dg = dgth.getGraph();
		
		//dgth.executionInspect(dg);
		dgth.fullAssertGraph(5, 4);

		String t_biz = dgth.formatAtom("T.biz("+com.vmusco.softminer.tests.cases.testInterfaceAndLinking.A.class.getCanonicalName()+")");
		String u_bar = dgth.formatAtom("U.bar()");
		String a_foo = dgth.formatAtom("A.foo()");
		String b_b = dgth.formatAtom("B()");
		String c_c = dgth.formatAtom("C()");

		dgth.fullAssertNode(
				t_biz, 
				new String[]{u_bar}, 
				new String[]{a_foo});

		dgth.fullAssertNode(
				a_foo, 
				new String[]{t_biz}, 
				new String[]{});

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
	}

	@Test
	public void testAbstractAndInheritanceClasses() throws Exception{
		DepGraphTestHelper dgth = new DepGraphTestHelper(getGraphBuilderObtainer(), com.vmusco.softminer.tests.cases.testAbstractAndInheritance.A.class);
		Graph dg = dgth.getGraph();

		//dgth.executionInspect();
		dgth.fullAssertGraph(8, 8);

		String a2 = dgth.formatAtom("A2()");
		String a3 = dgth.formatAtom("A3()");
		String a4 = dgth.formatAtom("A4()");
		String a = dgth.formatAtom("A()");
		String t_foo = dgth.formatAtom("T.foo()");
		String a_biz = dgth.formatAtom("A.biz()");
		String a_bar = dgth.formatAtom("A.bar()");
		String a_foo = dgth.formatAtom("A.foo()");

		dgth.fullAssertNode(
				a2, 
				new String[]{a3, a4}, 
				new String[]{a});

		dgth.fullAssertNode(
				a, 
				new String[]{a2}, 
				new String[]{});

		dgth.fullAssertNode(
				a3, 
				new String[]{t_foo}, 
				new String[]{a2});

		dgth.fullAssertNode(
				a4, 
				new String[]{t_foo}, 
				new String[]{a2});

		dgth.fullAssertNode(
				t_foo, 
				new String[]{}, 
				new String[]{a4, a_biz, a_bar, a3, a_foo});

		dgth.fullAssertNode(
				a_biz, 
				new String[]{t_foo}, 
				new String[]{});

		dgth.fullAssertNode(
				a_bar, 
				new String[]{t_foo}, 
				new String[]{});

		dgth.fullAssertNode(
				a_foo, 
				new String[]{t_foo}, 
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

		dgth.fullAssertGraph(6, 5);

		dgth.fullAssertNode(
				z_main, 
				new String[]{}, 
				new String[]{b_bar, a_foo, c_bar, c, c_foo});

		dgth.fullAssertNode(
				b_bar, 
				new String[]{z_main}, 
				new String[]{});

		dgth.fullAssertNode(
				a_foo, 
				new String[]{z_main}, 
				new String[]{});

		dgth.fullAssertNode(
				c_bar, 
				new String[]{z_main}, 
				new String[]{});

		dgth.fullAssertNode(
				c, 
				new String[]{z_main}, 
				new String[]{});

		dgth.fullAssertNode(
				c_foo, 
				new String[]{z_main}, 
				new String[]{});
	}
}
