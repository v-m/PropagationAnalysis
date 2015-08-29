package com.vmusco.softminer.tests;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;

import com.vmusco.softminer.graphs.EdgeTypes;
import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.sourceanalyzer.graphbuilding.GraphBuilder;
import com.vmusco.softminer.sourceanalyzer.graphbuilding.SpoonGraphBuilder;

@Ignore
@Deprecated
public class UseGraphAExtractingTest extends DepGraphTest {

	@Override
	public GraphBuilder getGraphBuilder(Class c) throws Exception {
		return setTestPkgAndGenerateBuilderUseGraphA(c);
	}

	/***
	 * This test allows testing the direction of the edge regarding variable get/set.
	 * A setter must go from method to variable, but a reader must go from varable to method (reverse edge)
	 * @throws Exception
	 */
	@Test
	public void testVariableUsage() throws Exception{
		GraphBuilder gb = getGraphBuilder(com.vmusco.softminer.tests.cases.testVariableUsage.MyClass.class);
		Graph dg = gb.generateDependencyGraph(buildingLogicToUse);

		//executionInspect(dg);
		//System.out.println(stateAsATestCase(dg));

		String set_variable = buildingLogicToUse.formatAtom("MyClass.setaVariable("+STRING_CANONICAL_NAME+")");
		String get_variable = buildingLogicToUse.formatAtom("MyClass.getaVariable()");
		String variable_self = buildingLogicToUse.formatAtom("MyClass#aVariable");

		fullAssertGraph(dg, 3, 2);

		fullAssertNode(dg, 
				variable_self, 
				new String[]{set_variable, get_variable}, 
				new String[]{});

		fullAssertNode(dg, 
				get_variable, 
				new String[]{}, 
				new String[]{variable_self});

		fullAssertNode(dg, 
				set_variable, 
				new String[]{}, 
				new String[]{variable_self});

		if(buildingLogicToUse instanceof SpoonGraphBuilder){
			Assert.assertEquals(dg.getEdgeType(get_variable, variable_self), EdgeTypes.READ_OPERATION);
			Assert.assertEquals(dg.getEdgeType(set_variable, variable_self), EdgeTypes.WRITE_OPERATION);
		}
	}


	/****
	 * This test is used to check if calling an interface method is represented as a link on the graph node
	 * @throws Exception
	 */
	@Test
	public void testInterfaceAndLinking() throws Exception{
		GraphBuilder gb = getGraphBuilder(com.vmusco.softminer.tests.cases.testInterfaceAndLinking.T.class);
		Graph dg = gb.generateDependencyGraph(buildingLogicToUse);

		fullAssertGraph(dg, 5, 4);

		String t_biz = buildingLogicToUse.formatAtom("T.biz("+com.vmusco.softminer.tests.cases.testInterfaceAndLinking.A.class.getCanonicalName()+")");
		String u_bar = buildingLogicToUse.formatAtom("U.bar()");
		String a_foo = buildingLogicToUse.formatAtom("A.foo()");
		String b_b = buildingLogicToUse.formatAtom("B.B()");
		String c_c = buildingLogicToUse.formatAtom("C.C()");
		String c_foo = buildingLogicToUse.formatAtom("C.foo()");
		String b_foo = buildingLogicToUse.formatAtom("B.foo()");


		fullAssertNode(dg, 
				t_biz, 
				new String[]{u_bar}, 
				new String[]{a_foo});

		fullAssertNode(dg, 
				a_foo, 
				new String[]{t_biz}, 
				new String[]{});

		fullAssertNode(dg, 
				u_bar, 
				new String[]{}, 
				new String[]{c_c, t_biz, b_b});

		fullAssertNode(dg, 
				c_c, 
				new String[]{u_bar}, 
				new String[]{});

		fullAssertNode(dg, 
				b_b, 
				new String[]{u_bar}, 
				new String[]{});
	}

	/**
	 * Test abstract class inheritance and links
	 * Status: Finished
	 * @throws Exception
	 */
	@Test
	public void testAbstractAndInheritanceClasses() throws Exception{
		GraphBuilder gb = getGraphBuilder(com.vmusco.softminer.tests.cases.testAbstractAndInheritance.A.class);
		Graph dg = gb.generateDependencyGraph(buildingLogicToUse);

		fullAssertGraph(dg, 8, 8);

		String a2_a2 = buildingLogicToUse.formatAtom("A2.A2()");
		String a3_a3 = buildingLogicToUse.formatAtom("A3.A3()");
		String a4_a4 = buildingLogicToUse.formatAtom("A4.A4()");
		String a_a = buildingLogicToUse.formatAtom("A.A()");
		String t_foo = buildingLogicToUse.formatAtom("T.foo()");
		String a_biz = buildingLogicToUse.formatAtom("A.biz()");
		String a_bar = buildingLogicToUse.formatAtom("A.bar()");
		String a_foo = buildingLogicToUse.formatAtom("A.foo()");
		String a2_bar = buildingLogicToUse.formatAtom("A2.bar()");
		String a3_foo = buildingLogicToUse.formatAtom("A3.foo()");
		String a4_foo = buildingLogicToUse.formatAtom("A4.foo()");

		fullAssertNode(dg, 
				a2_a2, 
				new String[]{a3_a3, a4_a4}, 
				new String[]{a_a});

		fullAssertNode(dg, 
				a_a, 
				new String[]{a2_a2}, 
				new String[]{});

		fullAssertNode(dg, 
				a3_a3, 
				new String[]{t_foo}, 
				new String[]{a2_a2});

		fullAssertNode(dg, 
				a4_a4, 
				new String[]{t_foo}, 
				new String[]{a2_a2});

		fullAssertNode(dg, 
				t_foo, 
				new String[]{}, 
				new String[]{a4_a4, a_biz, a_bar, a3_a3, a_foo});

		fullAssertNode(dg, 
				a_biz, 
				new String[]{t_foo}, 
				new String[]{});

		fullAssertNode(dg, 
				a_bar, 
				new String[]{t_foo}, 
				new String[]{});

		fullAssertNode(dg, 
				a_foo, 
				new String[]{t_foo}, 
				new String[]{});
	}

	@Test
	public void testInterfaceAndInheritance() throws Exception{
		// Without resolution
		GraphBuilder gb = getGraphBuilder(com.vmusco.softminer.tests.cases.testInterfaceAndInheritance.Z.class);
		Graph dg = gb.generateDependencyGraph(buildingLogicToUse);

		//executionInspect(dg);
		//System.out.println(stateAsATestCase(dg));	

		String z_main = buildingLogicToUse.formatAtom("Z.main("+STRING_CANONICAL_NAME+"[])");
		String b_bar = buildingLogicToUse.formatAtom("B.bar()");
		String a_foo = buildingLogicToUse.formatAtom("A.foo()");
		String c_bar = buildingLogicToUse.formatAtom("C.bar()");
		String c_c = buildingLogicToUse.formatAtom("C.C()");
		String c_foo = buildingLogicToUse.formatAtom("C.foo()");

		fullAssertGraph(dg, 6, 5);

		fullAssertNode(dg, 
				z_main, 
				new String[]{}, 
				new String[]{b_bar, a_foo, c_bar, c_c, c_foo});

		fullAssertNode(dg, 
				b_bar, 
				new String[]{z_main}, 
				new String[]{});

		fullAssertNode(dg, 
				a_foo, 
				new String[]{z_main}, 
				new String[]{});

		fullAssertNode(dg, 
				c_bar, 
				new String[]{z_main}, 
				new String[]{});

		fullAssertNode(dg, 
				c_c, 
				new String[]{z_main}, 
				new String[]{});

		fullAssertNode(dg, 
				c_foo, 
				new String[]{z_main}, 
				new String[]{});
	}

	@Test
	public void testImplicitDependency() throws Exception{
		GraphBuilder gb = getGraphBuilder(com.vmusco.softminer.tests.cases.testImplicitDependency.Foo.class);
		Graph dg = gb.generateDependencyGraph(buildingLogicToUse);

		//executionInspect(dg);
		//System.out.println(stateAsATestCase(dg));	

		// Nothing to see here as autoboxing is not reported -- pertinence?
		//TODO: Test not implemented yet !
	}

	@Test
	public void testAnonymousClass() throws Exception{
		GraphBuilder gb = getGraphBuilder(com.vmusco.softminer.tests.cases.testAnonymousClass.Foo.class);
		Graph dg = gb.generateDependencyGraph(buildingLogicToUse);

		//executionInspect(dg);
		//System.out.println(stateAsATestCase(dg));
		//TODO: Test not implemented yet !
	}

	/**
	 * This test allows to explore the constructors dependencies
	 * Status: Finished
	 * @throws Exception
	 */
	@Test
	public void testConstructors() throws Exception{
		GraphBuilder gb = getGraphBuilder(com.vmusco.softminer.tests.cases.testConstructors.Foo.class);
		Graph dg = gb.generateDependencyGraph(buildingLogicToUse);

		//executionInspect(dg);
		//System.out.println(stateAsATestCase(dg));

		String foo_foo_withstring = buildingLogicToUse.formatAtom("Foo.Foo("+STRING_CANONICAL_NAME+")");
		String foo_foo = buildingLogicToUse.formatAtom("Foo.Foo()");
		String foo_foo_withstringandint = buildingLogicToUse.formatAtom("Foo.Foo("+STRING_CANONICAL_NAME+",int)");

		fullAssertGraph(dg, 3, 2);
		fullAssertNode(dg, 
				foo_foo, 
				new String[]{}, 
				new String[]{foo_foo_withstring});

		fullAssertNode(dg, 
				foo_foo_withstring, 
				new String[]{foo_foo}, 
				new String[]{foo_foo_withstringandint});

		fullAssertNode(dg, 
				foo_foo_withstringandint, 
				new String[]{foo_foo_withstring}, 
				new String[]{});
	}


	/**
	 * This test allows to explore a simple graph. The aim is to see if edges are correctly set
	 * in case of method calls inside the same class AND outside.
	 * Status: Finished
	 * @throws Exception
	 */
	@Test
	public void testSimpleMethodCalls() throws Exception{
		GraphBuilder gb = getGraphBuilder(com.vmusco.softminer.tests.cases.testSimpleMethodCalls.Foo.class);
		Graph dg = gb.generateDependencyGraph(buildingLogicToUse);

		//executionInspect(dg);
		//System.out.println(stateAsATestCase(dg));

		fullAssertGraph(dg, 7, 7);

		String goo_bar5 = buildingLogicToUse.formatAtom("Foo.bar5(float)");
		String foo_bar2_withstring = buildingLogicToUse.formatAtom("Foo.bar2("+STRING_CANONICAL_NAME+")");
		String foo_bar1 = buildingLogicToUse.formatAtom("Foo.bar1()");
		String foo_bar2 = buildingLogicToUse.formatAtom("Foo2.bar()");
		String foo2_bar2 = buildingLogicToUse.formatAtom("Foo2.bar2("+STRING_CANONICAL_NAME+")");
		String foo_bar4 = buildingLogicToUse.formatAtom("Foo.bar4(double)");
		String foo_bar3 = buildingLogicToUse.formatAtom("Foo.bar3(int)");

		fullAssertNode(dg, 
				foo_bar1, 
				new String[]{goo_bar5}, 
				new String[]{foo_bar2_withstring});

		fullAssertNode(dg, 
				foo_bar2_withstring, 
				new String[]{foo_bar1}, 
				new String[]{foo_bar4, foo_bar3});

		fullAssertNode(dg, 
				foo_bar4, 
				new String[]{foo_bar2_withstring, foo_bar3}, 
				new String[]{});

		fullAssertNode(dg, 
				foo_bar3, 
				new String[]{foo_bar2_withstring}, 
				new String[]{foo_bar4});

		fullAssertNode(dg, 
				goo_bar5, 
				new String[]{}, 
				new String[]{foo_bar2, foo_bar1, foo2_bar2});

		fullAssertNode(dg, 
				foo_bar2, 
				new String[]{goo_bar5}, 
				new String[]{});

		fullAssertNode(dg, 
				foo2_bar2, 
				new String[]{goo_bar5}, 
				new String[]{});


	}

	/***
	 * This test allows to check the signature return for static methods
	 * State: Finished
	 * @throws Exception
	 */
	@Test
	public void testStaticMethodCalls() throws Exception{
		GraphBuilder gb = getGraphBuilder(com.vmusco.softminer.tests.cases.testStaticMethodCalls.Foo1.class);
		Graph dg = gb.generateDependencyGraph(buildingLogicToUse);

		//executionInspect(dg);
		//System.out.println(stateAsATestCase(dg));

		fullAssertGraph(dg, 4, 3);

		String foo1_bar1 = buildingLogicToUse.formatAtom("Foo1.bar1()");
		String foo2_bar1 = buildingLogicToUse.formatAtom("Foo2.bar1()");
		String foo1_bar2 = buildingLogicToUse.formatAtom("Foo1.bar2("+STRING_CANONICAL_NAME+")");
		String foo1_bar3 = buildingLogicToUse.formatAtom("Foo1.bar3(int)");

		fullAssertNode(dg, 
				foo1_bar1, 
				new String[]{}, 
				new String[]{foo1_bar2});

		fullAssertNode(dg, 
				foo1_bar2, 
				new String[]{foo1_bar1}, 
				new String[]{foo1_bar3});

		fullAssertNode(dg, 
				foo1_bar3, 
				new String[]{foo1_bar2}, 
				new String[]{foo2_bar1});

		fullAssertNode(dg, 
				foo2_bar1, 
				new String[]{foo1_bar3}, 
				new String[]{});
	}


	@Test
	public void testGenericParameters() throws Exception{
		GraphBuilder gb = getGraphBuilder(com.vmusco.softminer.tests.cases.testGenericParameters.MyClass.class);
		Graph dg = gb.generateDependencyGraph(buildingLogicToUse);

		//executionInspect(dg);
		//System.out.println(stateAsATestCase(dg));

		fullAssertGraph(dg, 7, 5);

		String myclass_foo = buildingLogicToUse.formatAtom("MyClass.foo()");
		String myclass_bar = buildingLogicToUse.formatAtom("MyClass.bar("+CLASS_CANONICAL_NAME+",L)");
		String myclass_foo2 = buildingLogicToUse.formatAtom("MyClass.foo2()");
		String myclass_bar2 = buildingLogicToUse.formatAtom("MyClass.bar2("+CLASS_CANONICAL_NAME+",M,"+CLASS_CANONICAL_NAME+",O,L,"+STRING_CANONICAL_NAME+",O)");
		String myclass_foo3 = buildingLogicToUse.formatAtom("MyClass.foo3(L)");
		String myclass_foo4 = buildingLogicToUse.formatAtom("MyClass.foo4(L,M)");
		String myclass_biz = buildingLogicToUse.formatAtom("MyClass.biz()");

		fullAssertNode(dg, 
				myclass_bar2, 
				new String[]{myclass_foo2}, 
				new String[]{myclass_bar});

		fullAssertNode(dg, 
				myclass_bar, 
				new String[]{myclass_bar2, myclass_foo}, 
				new String[]{});

		fullAssertNode(dg, 
				myclass_foo4, 
				new String[]{myclass_foo3}, 
				new String[]{myclass_biz});

		fullAssertNode(dg, 
				myclass_biz, 
				new String[]{myclass_foo4}, 
				new String[]{});

		fullAssertNode(dg, 
				myclass_foo3, 
				new String[]{}, 
				new String[]{myclass_foo4});

		fullAssertNode(dg, 
				myclass_foo, 
				new String[]{}, 
				new String[]{myclass_bar});

		fullAssertNode(dg, 
				myclass_foo2, 
				new String[]{}, 
				new String[]{myclass_bar2});
	}

	@Test
	public void testInternalClasses() throws Exception{
		GraphBuilder gb = getGraphBuilder(com.vmusco.softminer.tests.cases.testInternalClasses.Foo.class);
		Graph dg = gb.generateDependencyGraph(buildingLogicToUse);

		//executionInspect(dg);
		//System.out.println(stateAsATestCase(dg));

		fullAssertGraph(dg, 4, 3);

		String foo_foo = buildingLogicToUse.formatAtom("Foo.Foo()");
		String foo_biz_biz = buildingLogicToUse.formatAtom("Foo$Biz.Biz()");
		String foo_bar_bar_biz = buildingLogicToUse.formatAtom("Foo$Bar.Bar("+com.vmusco.softminer.tests.cases.testInternalClasses.Foo.class.getCanonicalName()+"$Biz)");
		String foo_bar_biz_biz = buildingLogicToUse.formatAtom("Foo$Bar$Biz.Biz()");

		fullAssertNode(dg, 
				foo_bar_bar_biz, 
				new String[]{foo_foo}, 
				new String[]{foo_bar_biz_biz});

		fullAssertNode(dg, 
				foo_bar_biz_biz, 
				new String[]{foo_bar_bar_biz}, 
				new String[]{});

		fullAssertNode(dg, 
				foo_foo, 
				new String[]{}, 
				new String[]{foo_bar_bar_biz, foo_biz_biz});

		fullAssertNode(dg, 
				foo_biz_biz, 
				new String[]{foo_foo}, 
				new String[]{});
	}

	@Test
	public void testSuperLinks() throws Exception{
		GraphBuilder gb = getGraphBuilder(com.vmusco.softminer.tests.cases.testSuperLinks.Z.class);
		Graph dg = gb.generateDependencyGraph(buildingLogicToUse);

		//executionInspect(dg);
		//System.out.println(stateAsATestCase(dg));

		String b_b = buildingLogicToUse.formatAtom("B.B()");
		String z_z = buildingLogicToUse.formatAtom("Z.Z()");
		String a_a = buildingLogicToUse.formatAtom("A.A()");
		String b_bar = buildingLogicToUse.formatAtom("B.bar()");
		String a_foo = buildingLogicToUse.formatAtom("A.foo()");
		String b_foo = buildingLogicToUse.formatAtom("B.foo()");
		String a_bar = buildingLogicToUse.formatAtom("A.bar()");
		String a_biz = buildingLogicToUse.formatAtom("A.biz()");

		fullAssertGraph(dg, 8, 10);

		fullAssertNode(dg, 
				b_b, 
				new String[]{z_z}, 
				new String[]{a_a});

		fullAssertNode(dg, 
				a_a, 
				new String[]{b_b, z_z}, 
				new String[]{});

		fullAssertNode(dg, 
				b_bar, 
				new String[]{z_z}, 
				new String[]{a_foo});

		fullAssertNode(dg, 
				a_foo, 
				new String[]{b_bar, b_foo, z_z}, 
				new String[]{});

		fullAssertNode(dg, 
				b_foo, 
				new String[]{z_z}, 
				new String[]{a_foo});

		fullAssertNode(dg, 
				z_z, 
				new String[]{}, 
				new String[]{a_bar, a_foo, b_foo, b_b, b_bar, a_a, a_biz});

		fullAssertNode(dg, 
				a_bar, 
				new String[]{z_z}, 
				new String[]{});

		fullAssertNode(dg, 
				a_biz, 
				new String[]{z_z}, 
				new String[]{});
	}


	@Test
	public void testExoObject() throws Exception{
		GraphBuilder gb = getGraphBuilder(com.vmusco.softminer.tests.cases.testExoObject.Main.class);
		Graph g = gb.generateDependencyGraph(buildingLogicToUse);

		//executionInspect(dg);
		//System.out.println(stateAsATestCase(dg));
		g.bestDisplay();
	}


	@Test
	public void testExoInterface() throws Exception{
		GraphBuilder gb = getGraphBuilder(com.vmusco.softminer.tests.cases.testExoInterface.Test.class);
		
		Graph g = gb.generateDependencyGraph(buildingLogicToUse);
		//executionInspect(dg);
		//System.out.println(stateAsATestCase(dg));
		g.bestDisplay();
	}

	@Test
	public void testMethodReturnIfceWithGenerics() throws Exception{
		GraphBuilder gb = getGraphBuilder(com.vmusco.softminer.tests.cases.testMethodReturnIfceWithGenerics.Foo.class);
		
		Graph g = gb.generateDependencyGraph(buildingLogicToUse);
		//executionInspect(dg);
		//System.out.println(stateAsATestCase(dg));
		g.bestDisplay();
	}

	@Test
	public void testMethodReturnIfceWithGenericsMixed() throws Exception{
		GraphBuilder gb = getGraphBuilder(com.vmusco.softminer.tests.cases.testMethodReturnIfceWithGenericsMixed.Foo.class);
		
		Graph g = gb.generateDependencyGraph(buildingLogicToUse);
		//executionInspect(dg);
		//System.out.println(stateAsATestCase(dg));
		g.bestDisplay();
	}

}
