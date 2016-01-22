package com.vmusco.softminer.tests;

import org.junit.Test;

import com.vmusco.softminer.sourceanalyzer.ProcessorCommunicator;

/**
*
* @author Vincenzo Musco - http://www.vmusco.com
*/
public class CallGraphCHATest extends CallGraphAbstractTest {
	
	public CallGraphCHATest() {
		super(DepGraphTestHelper.testPkgAndGenerateBuilderCHACallGraphFactory());
	}
	

	private GraphBuilderObtainer localHelper() {
		return DepGraphTestHelper.testPkgAndGenerateBuilderCHACallGraphFactory();
	}
	
	@Test
	public void testVariableUsage() throws Exception{
		DepGraphTestHelper dgth = new DepGraphTestHelper(getGraphBuilderObtainer(), tweaksDisabler, com.vmusco.softminer.tests.cases.testVariableUsage.MyClass.class);
		
		dgth.fullAssertGraph(0, 0);
	}

	/****
	 * This test is used to check if calling an interface method is represented as a link on the graph node
	 * @throws Exception
	 */
	@Test
	public void testInterfaceAndLinking() throws Exception{
		DepGraphTestHelper dgth = new DepGraphTestHelper(getGraphBuilderObtainer(), tweaksDisabler, com.vmusco.softminer.tests.cases.testInterfaceAndLinking.T.class);

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
		DepGraphTestHelper dgth = new DepGraphTestHelper(getGraphBuilderObtainer(), tweaksDisabler, com.vmusco.softminer.tests.cases.testAbstractAndInheritance.A.class);

		//dgth.getGraph().bestDisplay();
		dgth.fullAssertGraph(17, 19);

		String a = dgth.formatAtom("A()");
		String a2 = dgth.formatAtom("A2()");
		String a3 = dgth.formatAtom("A3()");
		String a4 = dgth.formatAtom("A4()");		
		String a_bar = dgth.formatAtom("A.bar()");
		String a2_bar = dgth.formatAtom("A2.bar()");
		String a3_bar = dgth.formatAtom("A3.bar()");
		String a4_bar = dgth.formatAtom("A4.bar()");
		String a_biz = dgth.formatAtom("A.biz()");
		String a2_biz = dgth.formatAtom("A2.biz()");
		String a3_biz = dgth.formatAtom("A3.biz()");
		String a4_biz = dgth.formatAtom("A4.biz()");
		String t_foo = dgth.formatAtom("T.foo()");
		String a_foo = dgth.formatAtom("A.foo()");
		String a2_foo = dgth.formatAtom("A2.foo()");
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
				new String[]{a3_bar, a4_bar});
		
		dgth.fullAssertNode(
				a3_bar, 
				new String[]{a_bar, a2_bar}, 
				new String[]{});
		
		dgth.fullAssertNode(
				a4_bar, 
				new String[]{a_bar, a2_bar}, 
				new String[]{});
		
		dgth.fullAssertNode(
				a_bar, 
				new String[]{t_foo}, 
				new String[]{a2_bar, a3_bar, a4_bar});

		dgth.fullAssertNode(
				a_biz, 
				new String[]{t_foo}, 
				new String[]{a2_biz, a3_biz, a4_biz});
		
		dgth.fullAssertNode(
				a2_biz, 
				new String[]{a_biz}, 
				new String[]{});
		
		dgth.fullAssertNode(
				a3_biz, 
				new String[]{a_biz}, 
				new String[]{});
		
		dgth.fullAssertNode(
				a4_biz, 
				new String[]{a_biz}, 
				new String[]{});

		dgth.fullAssertNode(
				t_foo, 
				new String[]{}, 
				new String[]{a4, a_biz, a_bar, a3, a_foo});

		dgth.fullAssertNode(
				a_foo, 
				new String[]{t_foo}, 
				new String[]{a2_foo, a3_foo, a4_foo});
		
		dgth.fullAssertNode(
				a_foo, 
				new String[]{t_foo}, 
				new String[]{a2_foo, a3_foo, a4_foo});
		
		dgth.fullAssertNode(
				a2_foo, 
				new String[]{a_foo}, 
				new String[]{});
		
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
		DepGraphTestHelper dgth = new DepGraphTestHelper(getGraphBuilderObtainer(), tweaksDisabler, com.vmusco.softminer.tests.cases.testInterfaceAndInheritance.Z.class);

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

	@Override
	public void testSimpleInheritanceConnectedToAbstract() throws Exception {
		DepGraphTestHelper dgth = new DepGraphTestHelper(getGraphBuilderObtainer(), tweaksDisabler, com.vmusco.softminer.tests.cases.testSimpleInheritanceConnectedToAbstract.Impl.class);
		
		String abs = dgth.formatAtom("Abs()");
		String impl = dgth.formatAtom("Impl()");
		String impl_fct = dgth.formatAtom("Impl.fct()");
		String abs_foo = dgth.formatAtom("Abs.foo()");
		String impl_foo = dgth.formatAtom("Impl.foo()");
		
		dgth.fullAssertGraph(5, 4);
		dgth.fullAssertNode(
				abs_foo, 
				new String[]{impl_fct}, 
				new String[]{impl_foo});

		dgth.fullAssertNode(
				impl_foo, 
				new String[]{abs_foo}, 
				new String[]{});
		
		dgth.fullAssertNode(
				impl_fct, 
				new String[]{}, 
				new String[]{abs_foo, impl});

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
	public void testSimpleInheritanceConnectedToImplementation() throws Exception {
		DepGraphTestHelper dgth = new DepGraphTestHelper(getGraphBuilderObtainer(), tweaksDisabler, com.vmusco.softminer.tests.cases.testSimpleInheritanceConnectedToImplementation.Impl.class);
		
		String abs = dgth.formatAtom("Abs()");
		String impl = dgth.formatAtom("Impl()");
		String impl_fct = dgth.formatAtom("Impl.fct()");
		String abs_foo = dgth.formatAtom("Abs.foo()");
		String impl_foo = dgth.formatAtom("Impl.foo()");
		
		//dgth.getGraph().bestDisplay();
		
		dgth.fullAssertGraph(5, 4);
		dgth.fullAssertNode(
				impl_foo, 
				new String[]{abs_foo,impl_fct}, 
				new String[]{});
		
		dgth.fullAssertNode(
				abs_foo, 
				new String[]{}, 
				new String[]{impl_foo});
		
		dgth.fullAssertNode(
				impl_fct, 
				new String[]{}, 
				new String[]{impl_foo, impl});

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
		DepGraphTestHelper dgth = new DepGraphTestHelper(getGraphBuilderObtainer(), tweaksDisabler, com.vmusco.softminer.tests.cases.testPaperCase.C.class);
		

		String a = dgth.formatAtom("A()");
		String b = dgth.formatAtom("B()");
		String c_biz1 = dgth.formatAtom("C.biz1()");
		String c_biz2 = dgth.formatAtom("C.biz2()");
		String a_foo = dgth.formatAtom("A.foo()");
		String b_foo = dgth.formatAtom("B.foo()");
		
		//dgth.getGraph().bestDisplay();
		dgth.fullAssertGraph(6, 6);

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
				new String[]{a_foo, c_biz1}, 
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
		DepGraphTestHelper dgth = new DepGraphTestHelper(getGraphBuilderObtainer(), tweaksDisabler, com.vmusco.softminer.tests.cases.testSimpleInheritence.Class1.class);

		String c1 = dgth.formatAtom("Class1()");
		String c2 = dgth.formatAtom("Class2()");
		String c3 = dgth.formatAtom("Class3()");

		String c1_foo = dgth.formatAtom("Class1.foo()");
		String c2_foo = dgth.formatAtom("Class2.foo()");
		String c3_foo = dgth.formatAtom("Class3.foo()");
		
		//dgth.getGraph().bestDisplay();
		dgth.fullAssertGraph(6, 4);
		
		dgth.fullAssertNode(
				c3, 
				new String[]{}, 
				new String[]{c2});
		dgth.fullAssertNode(
				c2, 
				new String[]{c3}, 
				new String[]{c1});
		dgth.fullAssertNode(
				c1, 
				new String[]{c2}, 
				new String[]{});
		
		dgth.fullAssertNode(
				c1_foo, 
				new String[]{}, 
				new String[]{c2_foo, c3_foo});
		dgth.fullAssertNode(
				c2_foo, 
				new String[]{c1_foo}, 
				new String[]{});
		dgth.fullAssertNode(
				c3_foo, 
				new String[]{c1_foo}, 
				new String[]{});
		
	}

	@Override
	public void testSimpleMethodCalls() throws Exception {
		// Same as CG
		CallGraphTest t = new CallGraphTest();
		t.setGraphBuilderObtainer(localHelper());
		t.testSimpleMethodCalls();
	}
}
