package com.vmusco.softminer.tests.cases.testSuperLinks;

public class B extends A {
	@Override
	public void bar() {
		super.foo();
	}
	
	@Override
	public void foo() {
		super.foo();
	}
	
}
