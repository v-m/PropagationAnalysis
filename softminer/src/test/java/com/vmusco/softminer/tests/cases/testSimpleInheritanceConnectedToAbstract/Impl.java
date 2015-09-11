package com.vmusco.softminer.tests.cases.testSimpleInheritanceConnectedToAbstract;

public class Impl extends Abs{

	@Override
	public void foo() {
		
	}

	public static void fct() {
		Abs i = new Impl();
		i.foo();
	}
	
}
