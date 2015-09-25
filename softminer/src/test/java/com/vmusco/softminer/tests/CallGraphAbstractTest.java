package com.vmusco.softminer.tests;

import org.junit.Ignore;
import org.junit.Test;

/**
*
* @author Vincenzo Musco - http://www.vmusco.com
*/
public abstract class CallGraphAbstractTest {
	private GraphBuilderObtainer gbo = null;
	
	public CallGraphAbstractTest(GraphBuilderObtainer gbo) {
		this.gbo = gbo;
	}

	public void setGraphBuilderObtainer(GraphBuilderObtainer gbo) {
		this.gbo = gbo;
	}
	
	protected GraphBuilderObtainer getGraphBuilderObtainer() {
		return gbo;
	}
	
	/***
	 * This test allows testing the direction of the edge regarding variable get/set.
	 * A setter must go from method to variable, but a reader must go from varable to method (reverse edge)
	 * @throws Exception
	 */
	@Test
	public abstract void testVariableUsage() throws Exception;

	/****
	 * This test is used to check if calling an interface method is represented as a link on the graph node
	 * @throws Exception
	 */
	@Test
	public abstract void testInterfaceAndLinking() throws Exception;
	

	/****
	 * This test illustrates call graphs used in paper
	 * @throws Exception
	 */
	@Test
	public abstract void testPaperCase() throws Exception;

	/**
	 * Test a simple inheritence case
	 * Status: Finished
	 * @throws Exception
	 */
	@Test
	public abstract void testSimpleInheritanceConnectedToAbstract() throws Exception;
	/**
	 * Test a simple inheritence case
	 * Status: Finished
	 * @throws Exception
	 */
	@Test
	public abstract void testSimpleInheritanceConnectedToImplementation() throws Exception;
	
	/**
	 * Test abstract class inheritance and links
	 * Status: Finished
	 * @throws Exception
	 */
	@Test
	public abstract void testAbstractAndInheritanceClasses() throws Exception;
	
	@Test
	public abstract void testInterfaceAndInheritance() throws Exception;
	
	
	@Ignore
	@Test
	public void testImplicitDependency() throws Exception{
		//com.vmusco.softminer.tests.cases.testImplicitDependency.Foo.class
	}

	@Ignore
	@Test
	public void testAnonymousClass() throws Exception{
		//com.vmusco.softminer.tests.cases.testAnonymousClass.Foo.class
	}

	/**
	 * This test allows to explore the constructors dependencies
	 * Status: Finished
	 * @throws Exception
	 */
	@Ignore
	@Test
	public void testConstructors() throws Exception{
		//com.vmusco.softminer.tests.cases.testConstructors.Foo.class
	}


	/**
	 * This test allows to explore a simple graph. The aim is to see if edges are correctly set
	 * in case of method calls inside the same class AND outside.
	 * Status: Finished
	 * @throws Exception
	 */
	@Ignore
	@Test
	public void testSimpleMethodCalls() throws Exception{
		//com.vmusco.softminer.tests.cases.testSimpleMethodCalls.Foo.class
	}

	/***
	 * This test allows to check the signature return for static methods
	 * State: Finished
	 * @throws Exception
	 */
	@Ignore
	@Test
	public void testStaticMethodCalls() throws Exception{
		//com.vmusco.softminer.tests.cases.testStaticMethodCalls.Foo1.class
	}

	@Ignore
	@Test
	public void testGenericParameters() throws Exception{
		//com.vmusco.softminer.tests.cases.testGenericParameters.MyClass.class
	}

	@Ignore
	@Test
	public void testInternalClasses() throws Exception{
		//com.vmusco.softminer.tests.cases.testInternalClasses.Foo.class
	}

	@Ignore
	@Test
	public void testSuperLinks() throws Exception{
		//com.vmusco.softminer.tests.cases.testSuperLinks.Z.class
	}

	@Ignore
	@Test
	public void testExoObject() throws Exception{
		// com.vmusco.softminer.tests.cases.testExoObject
	}

	@Ignore
	@Test
	public void testExoInterface() throws Exception{
		// com.vmusco.softminer.tests.cases.testExoInterface.Test.class
	}

	@Ignore
	@Test
	public void testMethodReturnIfceWithGenerics() throws Exception{
		// com.vmusco.softminer.tests.cases.testMethodReturnIfceWithGenerics.Foo.class
	}

	@Ignore
	@Test
	public void testMethodReturnIfceWithGenericsMixed() throws Exception{
		// com.vmusco.softminer.tests.cases.testMethodReturnIfceWithGenericsMixed.Foo.class
	}
}
