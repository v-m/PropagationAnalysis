package com.vmusco.softminer.tests.cases.testAnonymousClass;

public class Test {

	public Test() {
		new Foo() {
			@Override
			public void foo() {
				new Bar() {
					@Override
					public void foo() {
					}
				};
			}
		};
		
		new Bar() {
			@Override
			public void foo() {
				new Foo() {
					@Override
					public void foo() {
					}
				};
			}
		};
	}

}
