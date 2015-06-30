package com.vmusco.softminer.tests.cases.testAbstractAndInheritance;

public class T {
	public void foo(){
		// Shows the transparent inheritance process for constructors 
		// and the call to the good method
		A foo = new A3();
		A bar = new A4();
		
		foo.foo();		// Implemented in A3
		foo.bar();		// Implemented in A2
		foo.biz();		// Implemented in A
		bar.foo();		// Implemented in A4
		bar.bar();		// Implemented in A2
		bar.biz();		// Implemented in A
	}
	
	public void bar(){
	}
}
