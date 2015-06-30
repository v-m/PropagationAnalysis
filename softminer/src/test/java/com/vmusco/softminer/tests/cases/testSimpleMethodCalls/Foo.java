package com.vmusco.softminer.tests.cases.testSimpleMethodCalls;

public class Foo {
	public void bar1(){
		bar2("Hello");
	}

	public void bar2(String stringParam){
		bar3(1);
		bar4(2);
	}
	
	public void bar3(int intParam){
		bar4(1);
	}
	
	public void bar4(double doubleParam){
		
	}
	
	public void bar5(float floatParam){
		bar1();
		
		Foo2 anObject = null;
		anObject.bar();
		anObject.bar2("foo");
	}
}
