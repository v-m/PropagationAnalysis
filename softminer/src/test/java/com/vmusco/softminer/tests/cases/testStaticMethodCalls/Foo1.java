package com.vmusco.softminer.tests.cases.testStaticMethodCalls;

public class Foo1 {
	public void bar1(){
		Foo1.bar2("Hello");
	}

	public static void bar2(String stringParam){
		Foo1.bar3(1);
	}

	private static void bar3(int nbParam){
		Foo2.bar1();
	}
}
