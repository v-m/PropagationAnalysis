package com.vmusco.softminer.tests.cases.testMethodReturnIfceWithGenerics;

public class Foo<T> {
	protected Bar<T> meth1() {
        return new Bar<T>() {
            public boolean evaluate(final T o, String v) {
                return o instanceof String;
            }
        };
    }
}