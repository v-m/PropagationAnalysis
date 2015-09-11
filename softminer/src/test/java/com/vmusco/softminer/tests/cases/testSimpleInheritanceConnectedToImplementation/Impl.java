package com.vmusco.softminer.tests.cases.testSimpleInheritanceConnectedToImplementation;

public class Impl extends Abs{

	@Override
	public void foo() {
		
	}

	public static void fct() {
		Impl i = new Impl();
		i.foo();
	}
	
}
