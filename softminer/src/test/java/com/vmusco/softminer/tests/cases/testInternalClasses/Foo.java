package com.vmusco.softminer.tests.cases.testInternalClasses;

public class Foo {
	class Biz{
		public Biz() {
		}
	}
	
	class Bar{
		Foo.Biz externalBiz;
		Biz internalBiz; 
		
		public Bar(Foo.Biz aBiz) {
			new Biz();
		}
		
		class Biz{
			public Biz() {
			}
		}
	}
	
	public Foo() {
		new Foo.Bar(new Foo.Biz());
	}
}
