package com.vmusco.softminer.tests;

import junit.framework.Assert;

import org.junit.Test;

import com.vmusco.softminer.graphs.CausalityGraph;
import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.sourceanalyzer.ProcessorCommunicator;
import com.vmusco.softminer.sourceanalyzer.graphbuilding.GraphBuilder;
import com.vmusco.softminer.sourceanalyzer.processors.SpecificTags;

public class CausalityGraphExtractingTest extends DepGraphTest {
	/***
	 * This test allows testing the direction of the edge regarding variable get/set.
	 * A setter must go from method to variable, but a reader must go from variable to method (reverse edge)
	 * @throws Exception
	 */
	@Test
	public void testVariableUsage() throws Exception{
		GraphBuilder gb = setTestPkgAndGenerateBuilder(com.vmusco.softminer.tests.cases.testVariableUsage.MyClass.class);
		Graph dg = CausalityGraph.convert(gb.generateDependencyGraph(buildingLogicToUse));
		
		//executionInspect(dg);
		//System.out.println(stateAsATestCase(dg));

		String set_variable = buildingLogicToUse.formatAtom("MyClass.setaVariable("+STRING_CANONICAL_NAME+")");
		String get_variable = buildingLogicToUse.formatAtom("MyClass.getaVariable()");
		String variable_self = buildingLogicToUse.formatAtom("MyClass#aVariable");
		fullAssertGraph(dg, 3, 2);
		
		fullAssertNode(dg, 
		variable_self, 
		new String[]{set_variable, }, 
		new String[]{get_variable});

		fullAssertNode(dg, 
		get_variable, 
		new String[]{variable_self}, 
		new String[]{});

		fullAssertNode(dg, 
		set_variable, 
		new String[]{}, 
		new String[]{variable_self});
	}
	

	/****
	 * This test is used to check if calling an interface method is represented as a link on the graph node
	 * @throws Exception
	 */
	@Test
	public void testInterfaceAndLinking() throws Exception{
		// WithOUT resolution
		GraphBuilder gb = setTestPkgAndGenerateBuilder(com.vmusco.softminer.tests.cases.testInterfaceAndLinking.T.class);
		ProcessorCommunicator.resolveInterfacesAndClasses = false;
		Graph dg = CausalityGraph.convert(gb.generateDependencyGraph(buildingLogicToUse));
		
		//executionInspect(dg);
		//System.out.println(stateAsATestCase(dg));
		
		fullAssertGraph(dg, 5, 4);
		
		String t_biz = "T.biz("+com.vmusco.softminer.tests.cases.testInterfaceAndLinking.A.class.getCanonicalName()+")";
		String u_bar = "U.bar()";
		String a_foo = "A.foo()";
		String b_b = "B.B()";
		String c_c = "C.C()";
		String c_foo = "C.foo()";
		String b_foo = "B.foo()";
		
		
		fullAssertNode(dg, 
		t_biz, 
		new String[]{a_foo}, 
		new String[]{u_bar});
		
		fullAssertNode(dg, 
		a_foo, 
		new String[]{}, 
		new String[]{t_biz});
		
		fullAssertNode(dg, 
		u_bar, 
		new String[]{c_c, t_biz, b_b}, 
		new String[]{});
		
		fullAssertNode(dg, 
		c_c, 
		new String[]{}, 
		new String[]{u_bar});
		
		fullAssertNode(dg, 
		b_b, 
		new String[]{}, 
		new String[]{u_bar});
		
		
		// With resolution
		gb = setTestPkgAndGenerateBuilder(com.vmusco.softminer.tests.cases.testInterfaceAndLinking.T.class);
		ProcessorCommunicator.resolveInterfacesAndClasses = true;
		dg = CausalityGraph.convert(gb.generateDependencyGraph(buildingLogicToUse));
		
		//executionInspect(dg);
		//System.out.println(stateAsATestCase(dg));
		
		fullAssertGraph(dg, 6, 5);

		fullAssertNode(dg, 
		t_biz, 
		new String[]{c_foo, b_foo},
		new String[]{u_bar}); 

		fullAssertNode(dg, 
		b_foo, 
		new String[]{}, 
		new String[]{t_biz});

		fullAssertNode(dg, 
		c_foo, 
		new String[]{}, 
		new String[]{t_biz});

		fullAssertNode(dg, 
		u_bar, 
		new String[]{c_c, t_biz, b_b}, 
		new String[]{});

		fullAssertNode(dg, 
		c_c, 
		new String[]{}, 
		new String[]{u_bar});

		fullAssertNode(dg, 
		b_b, 
		new String[]{}, 
		new String[]{u_bar});
	}
	
	/**
	 * Test abstract class inheritance and links
	 * Status: Finished
	 * @throws Exception
	 */
	@Test
	public void testAbstractAndInheritanceClasses() throws Exception{
		GraphBuilder gb = setTestPkgAndGenerateBuilder(com.vmusco.softminer.tests.cases.testAbstractAndInheritance.A.class);
		
		// Testing with resolving disabled !
		//ProcessorCommunicator.resolveInterfacesAndClasses = false;
		//ProcessorCommunicator.includesFields = true;
		//Graph dg = CausalityGraph.convert(gb.generateDependencyGraph(buildingLogicToUse));
		//Graph dg = gb.generateDependencyGraph(buildingLogicToUse);
		
		//executionInspect(dg);
		//System.out.println(stateAsATestCase(dg));
		
		String a2_a2 = "A2()";
		String a3_a3 = "A3()";
		String a4_a4 = "A4()";
		String a_a = "A()";
		String t_foo = "T.foo()";
		String a_biz = "A.biz()";
		String a_bar = "A.bar()";
		String a_foo = "A.foo()";
		String a2_bar = "A2.bar()";
		String a3_foo = "A3.foo()";
		String a4_foo = "A4.foo()";
		
		/*fullAssertGraph(dg, 8, 8);
		
		
		
		fullAssertNode(dg, 
		a2_a2, 
		new String[]{a_a},
		new String[]{a3_a3, a4_a4}); 
		
		fullAssertNode(dg, 
		a_a, 
		new String[]{}, 
		new String[]{a2_a2});
		
		fullAssertNode(dg, 
		a3_a3, 
		new String[]{a2_a2}, 
		new String[]{t_foo});
		
		fullAssertNode(dg, 
		a4_a4, 
		new String[]{a2_a2}, 
		new String[]{t_foo});
		
		fullAssertNode(dg, 
		t_foo, 
		new String[]{a4_a4, a_biz, a_bar, a3_a3, a_foo}, 
		new String[]{});
		
		fullAssertNode(dg, 
		a_biz, 
		new String[]{}, 
		new String[]{t_foo});
		
		fullAssertNode(dg, 
		a_bar, 
		new String[]{}, 
		new String[]{t_foo});
		
		fullAssertNode(dg, 
		a_foo, 
		new String[]{}, 
		new String[]{t_foo});*/
		
		// Testing with resolving enabled !
		gb = setTestPkgAndGenerateBuilder(com.vmusco.softminer.tests.cases.testAbstractAndInheritance.A.class);
		ProcessorCommunicator.resolveInterfacesAndClasses = true;
		Graph dg = gb.generateDependencyGraph(buildingLogicToUse);
		
		//executionInspect(dg);
		//System.out.println(stateAsATestCase(dg));
		
		fullAssertGraph(dg, 9, 9);

		fullAssertNode(dg, 
		a2_a2, 
		new String[]{a_a}, 
		new String[]{a3_a3, a4_a4});

		fullAssertNode(dg, 
		a_a, 
		new String[]{}, 
		new String[]{a2_a2});

		fullAssertNode(dg, 
		a3_a3, 
		new String[]{a2_a2}, 
		new String[]{t_foo});

		fullAssertNode(dg, 
		a4_a4, 
		new String[]{a2_a2}, 
		new String[]{t_foo});

		fullAssertNode(dg, 
		t_foo, 
		new String[]{a4_a4, a_biz, a2_bar, a3_foo, a4_foo, a3_a3}, 
		new String[]{});

		fullAssertNode(dg, 
		a_biz, 
		new String[]{}, 
		new String[]{t_foo});

		fullAssertNode(dg, 
		a2_bar, 
		new String[]{}, 
		new String[]{t_foo});

		fullAssertNode(dg, 
		a3_foo, 
		new String[]{}, 
		new String[]{t_foo});

		fullAssertNode(dg, 
		a4_foo, 
		new String[]{}, 
		new String[]{t_foo});
	}
	
	/**
	 * Test abstract class inheritance and links
	 * Status: Finished
	 * @throws Exception
	 */
	@Test
	public void testTiti() throws Exception{
		GraphBuilder gb = setTestPkgAndGenerateBuilder(com.vmusco.softminer.tests.cases.titi.All.class);
		
		
		// Testing with resolving enabled !
		gb = setTestPkgAndGenerateBuilder(com.vmusco.softminer.tests.cases.titi.All.class);
		ProcessorCommunicator.resolveInterfacesAndClasses = true;
		ProcessorCommunicator.includesFields = true;
		Graph dg = gb.generateDependencyGraph(buildingLogicToUse);
		
	}

	@Test
	public void testInterfaceAndInheritance() throws Exception{
		// Without resolution
		GraphBuilder gb = setTestPkgAndGenerateBuilder(com.vmusco.softminer.tests.cases.testInterfaceAndInheritance.Z.class);
		ProcessorCommunicator.resolveInterfacesAndClasses = false;
		Graph dg = CausalityGraph.convert(gb.generateDependencyGraph(buildingLogicToUse));
		
		//executionInspect(dg);
		//System.out.println(stateAsATestCase(dg));	
		
		String z_main = "Z.main("+STRING_CANONICAL_NAME+"[])";
		String b_bar = "B.bar()";
		String a_foo = "A.foo()";
		String c_bar = "C.bar()";
		String c_c = "C.C()";
		String c_foo = "C.foo()";

		fullAssertGraph(dg, 6, 5);
		
		fullAssertNode(dg, 
		z_main, 
		new String[]{b_bar, a_foo, c_bar, c_c, c_foo}, 
		new String[]{});
		
		fullAssertNode(dg, 
		b_bar, 
		new String[]{}, 
		new String[]{z_main});
		
		fullAssertNode(dg, 
		a_foo, 
		new String[]{}, 
		new String[]{z_main});
		
		fullAssertNode(dg, 
		c_bar, 
		new String[]{}, 
		new String[]{z_main});
		
		fullAssertNode(dg, 
		c_c, 
		new String[]{}, 
		new String[]{z_main});
		
		fullAssertNode(dg, 
		c_foo, 
		new String[]{}, 
		new String[]{z_main});
		

		
		// With resolution
		gb = setTestPkgAndGenerateBuilder(com.vmusco.softminer.tests.cases.testInterfaceAndInheritance.Z.class);
		ProcessorCommunicator.resolveInterfacesAndClasses = true;
		dg = CausalityGraph.convert(gb.generateDependencyGraph(buildingLogicToUse));
		
		//executionInspect(dg);
		//System.out.println(stateAsATestCase(dg));	

		fullAssertGraph(dg, 4, 3);
		
		fullAssertNode(dg, 
		z_main, 
		new String[]{c_bar, c_foo, c_c}, 
		new String[]{});
		
		fullAssertNode(dg, 
		c_bar, 
		new String[]{}, 
		new String[]{z_main});
		
		fullAssertNode(dg, 
		c_foo, 
		new String[]{}, 
		new String[]{z_main});
		
		fullAssertNode(dg, 
		c_c, 
		new String[]{}, 
		new String[]{z_main});
	}
	
	@Test
	public void testImplicitDependency() throws Exception{
		GraphBuilder gb = setTestPkgAndGenerateBuilder(com.vmusco.softminer.tests.cases.testImplicitDependency.Foo.class);
		Graph dg = CausalityGraph.convert(gb.generateDependencyGraph(buildingLogicToUse));
		
		//executionInspect(dg);
		//System.out.println(stateAsATestCase(dg));	
		
		// Nothing to see here as autoboxing is not reported -- pertinence?
		//TODO: Test not implemented yet !
	}

	@Test
	public void testAnonymousClass() throws Exception{
		GraphBuilder gb = setTestPkgAndGenerateBuilder(com.vmusco.softminer.tests.cases.testAnonymousClass.Foo.class);
		Graph dg = CausalityGraph.convert(gb.generateDependencyGraph(buildingLogicToUse));
		
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
		GraphBuilder gb = setTestPkgAndGenerateBuilder(com.vmusco.softminer.tests.cases.testConstructors.Foo.class);
		Graph dg = CausalityGraph.convert(gb.generateDependencyGraph(buildingLogicToUse));

		//executionInspect(dg);
		//System.out.println(stateAsATestCase(dg));
		
		String foo_foo_withstring = "Foo.Foo("+STRING_CANONICAL_NAME+")";
		String foo_foo = "Foo.Foo()";
		String foo_foo_withstringandint = "Foo.Foo("+STRING_CANONICAL_NAME+",int)";
		
		fullAssertGraph(dg, 3, 2);
		fullAssertNode(dg, 
			foo_foo, 
			new String[]{foo_foo_withstring}, 
			new String[]{});

		fullAssertNode(dg, 
			foo_foo_withstring, 
			new String[]{foo_foo_withstringandint}, 
			new String[]{foo_foo});

		fullAssertNode(dg, 
			foo_foo_withstringandint, 
			new String[]{}, 
			new String[]{foo_foo_withstring});
	}
	
	
	/**
	 * This test allows to explore a simple graph. The aim is to see if edges are correctly set
	 * in case of method calls inside the same class AND outside.
	 * Status: Finished
	 * @throws Exception
	 */
	@Test
	public void testSimpleMethodCalls() throws Exception{
		GraphBuilder gb = setTestPkgAndGenerateBuilder(com.vmusco.softminer.tests.cases.testSimpleMethodCalls.Foo.class);
		Graph dg = CausalityGraph.convert(gb.generateDependencyGraph(buildingLogicToUse));

		//executionInspect(dg);
		//System.out.println(stateAsATestCase(dg));
		
		fullAssertGraph(dg, 7, 7);
		
		String foo_bar2_withstring = "Foo.bar2("+STRING_CANONICAL_NAME+")";
		String foo_bar1 = "Foo.bar1()";
		String foo_bar2 = "Foo2.bar()";
		String foo2_bar2 = "Foo2.bar2("+STRING_CANONICAL_NAME+")";
		String foo_bar3 = "Foo.bar3(int)";
		String foo_bar4 = "Foo.bar4(double)";
		String foo_bar5 = "Foo.bar5(float)";
		
		fullAssertNode(dg, 
				foo_bar1, 
				new String[]{foo_bar2_withstring}, 
				new String[]{foo_bar5});
			
		fullAssertNode(dg, 
				foo_bar2_withstring, 
				new String[]{foo_bar4, foo_bar3}, 
				new String[]{foo_bar1});
		
		fullAssertNode(dg, 
				foo_bar4, 
				new String[]{}, 
				new String[]{foo_bar2_withstring, foo_bar3});
		
		fullAssertNode(dg, 
			foo_bar3, 
			new String[]{foo_bar4}, 
			new String[]{foo_bar2_withstring});
		
		fullAssertNode(dg, 
			foo_bar5, 
			new String[]{foo_bar2, foo_bar1, foo2_bar2}, 
			new String[]{});
		
		fullAssertNode(dg, 
			foo_bar2, 
			new String[]{}, 
			new String[]{foo_bar5});
		
		fullAssertNode(dg, 
			foo2_bar2, 
			new String[]{}, 
			new String[]{foo_bar5});
		

	}

	/***
	 * This test allows to check the signature return for static methods
	 * State: Finished
	 * @throws Exception
	 */
	@Test
	public void testStaticMethodCalls() throws Exception{
		GraphBuilder gb = setTestPkgAndGenerateBuilder(com.vmusco.softminer.tests.cases.testStaticMethodCalls.Foo1.class);
		Graph dg = CausalityGraph.convert(gb.generateDependencyGraph(buildingLogicToUse));

		//executionInspect(dg);
		//System.out.println(stateAsATestCase(dg));

		fullAssertGraph(dg, 4, 3);
		
		String foo1_bar1 = "Foo1.bar1()";
		String foo2_bar1 = "Foo2.bar1()";
		String foo1_bar2 = "Foo1.bar2("+STRING_CANONICAL_NAME+")";
		String foo1_bar3 = "Foo1.bar3(int)";
		
		fullAssertNode(dg, 
			foo1_bar1, 
			new String[]{foo1_bar2}, 
			new String[]{});
		
		fullAssertNode(dg, 
			foo1_bar2, 
			new String[]{foo1_bar3}, 
			new String[]{foo1_bar1});
		
		fullAssertNode(dg, 
			foo1_bar3, 
			new String[]{foo2_bar1}, 
			new String[]{foo1_bar2});
		
		fullAssertNode(dg, 
			foo2_bar1, 
			new String[]{}, 
			new String[]{foo1_bar3});

	}
	

	@Test
	public void testGenericParameters() throws Exception{
		GraphBuilder gb = setTestPkgAndGenerateBuilder(com.vmusco.softminer.tests.cases.testGenericParameters.MyClass.class);
		Graph dg = CausalityGraph.convert(gb.generateDependencyGraph(buildingLogicToUse));
		
		//executionInspect(dg);
		//System.out.println(stateAsATestCase(dg));
		
		fullAssertGraph(dg, 7, 5);

		String myclass_foo = "MyClass.foo()";
		String myclass_bar = "MyClass.bar("+CLASS_CANONICAL_NAME+",L)";
		String myclass_foo2 = "MyClass.foo2()";
		String myclass_bar2 = "MyClass.bar2("+CLASS_CANONICAL_NAME+",M,"+CLASS_CANONICAL_NAME+",O,L,"+STRING_CANONICAL_NAME+",O)";
		String myclass_foo3 = "MyClass.foo3(L)";
		String myclass_foo4 = "MyClass.foo4(L,M)";
		String myclass_biz = "MyClass.biz()";
		
		fullAssertNode(dg, 
		myclass_bar2, 
		new String[]{myclass_bar}, 
		new String[]{myclass_foo2});

		fullAssertNode(dg, 
		myclass_bar, 
		new String[]{}, 
		new String[]{myclass_bar2, myclass_foo});

		fullAssertNode(dg, 
		myclass_foo4, 
		new String[]{myclass_biz}, 
		new String[]{myclass_foo3});

		fullAssertNode(dg, 
		myclass_biz, 
		new String[]{}, 
		new String[]{myclass_foo4});

		fullAssertNode(dg, 
		myclass_foo3, 
		new String[]{myclass_foo4}, 
		new String[]{});

		fullAssertNode(dg, 
		myclass_foo, 
		new String[]{myclass_bar}, 
		new String[]{});

		fullAssertNode(dg, 
		myclass_foo2, 
		new String[]{myclass_bar2}, 
		new String[]{});
	}

	@Test
	public void testInternalClasses() throws Exception{
		GraphBuilder gb = setTestPkgAndGenerateBuilder(com.vmusco.softminer.tests.cases.testInternalClasses.Foo.class);
		Graph dg = CausalityGraph.convert(gb.generateDependencyGraph(buildingLogicToUse));
		
		//executionInspect(dg);
		//System.out.println(stateAsATestCase(dg));

		fullAssertGraph(dg, 4, 3);
		
		String foo_foo = "Foo.Foo()";
		String foo_biz_biz = "Foo$Biz.Biz()";
		String foo_bar_bar_biz = "Foo$Bar.Bar("+com.vmusco.softminer.tests.cases.testInternalClasses.Foo.class.getCanonicalName()+"$Biz)";
		String foo_bar_biz_biz = "Foo$Bar$Biz.Biz()";
		
		fullAssertNode(dg, 
		foo_bar_bar_biz, 
		new String[]{foo_bar_biz_biz}, 
		new String[]{foo_foo});
		
		fullAssertNode(dg, 
		foo_bar_biz_biz, 
		new String[]{}, 
		new String[]{foo_bar_bar_biz});
		
		fullAssertNode(dg, 
		foo_foo, 
		new String[]{foo_bar_bar_biz, foo_biz_biz}, 
		new String[]{});
		
		fullAssertNode(dg, 
		foo_biz_biz, 
		new String[]{}, 
		new String[]{foo_foo});
	}
	
	@Test
	public void testSuperLinks() throws Exception{
		GraphBuilder gb = setTestPkgAndGenerateBuilder(com.vmusco.softminer.tests.cases.testSuperLinks.Z.class);
		Graph dg = CausalityGraph.convert(gb.generateDependencyGraph(buildingLogicToUse));
		
		//executionInspect(dg);
		//System.out.println(stateAsATestCase(dg));
		
		String b_b = "B.B()";
		String z_z = "Z.Z()";
		String a_a = "A.A()";
		String b_bar = "B.bar()";
		String a_foo = "A.foo()";
		String b_foo = "B.foo()";
		String a_bar = "A.bar()";
		String a_biz = "A.biz()";
		
		fullAssertGraph(dg, 8, 10);

		fullAssertNode(dg, 
		b_b, 
		new String[]{a_a}, 
		new String[]{z_z});

		fullAssertNode(dg, 
		a_a, 
		new String[]{}, 
		new String[]{b_b, z_z});

		fullAssertNode(dg, 
		b_bar, 
		new String[]{a_foo}, 
		new String[]{z_z});

		fullAssertNode(dg, 
		a_foo, 
		new String[]{}, 
		new String[]{b_bar, b_foo, z_z});

		fullAssertNode(dg, 
		b_foo, 
		new String[]{a_foo}, 
		new String[]{z_z});

		fullAssertNode(dg, 
		z_z, 
		new String[]{a_bar, a_foo, b_foo, b_b, b_bar, a_a, a_biz}, 
		new String[]{});

		fullAssertNode(dg, 
		a_bar, 
		new String[]{}, 
		new String[]{z_z});

		fullAssertNode(dg, 
		a_biz, 
		new String[]{}, 
		new String[]{z_z});


	}
	
}
