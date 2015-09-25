package com.vmusco.softminer.tests.cases.testPaperCase;

public class C {
	int bar = 0;

	public void biz1(){
		B aB = new B();
		bar = 1;
		aB.foo();
	}

	public void biz2(){
		A aA = new B();
		if(bar > 0)
			aA.foo();
	}
}
