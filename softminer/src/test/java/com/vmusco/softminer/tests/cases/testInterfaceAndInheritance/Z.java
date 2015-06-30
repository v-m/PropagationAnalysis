package com.vmusco.softminer.tests.cases.testInterfaceAndInheritance;

public class Z {
	public static void main(String[] args) {
		C foo = new C();
		A bar = (A) foo;
		B biz = (B) foo;
		
		foo.bar();
		foo.foo();
		bar.foo();
		biz.foo();
		biz.bar();
	}
}
