package my.tests;
//package com.vmusco.smf;

import org.junit.Test;

public class FakeTest2 {
	FakeTest2Crasher crasher = new FakeTest2Crasher();

	@Test
	public void test1(){

	}

	@Test
	public void test2(){

	}
	
	private class FakeTest2Crasher{
		public FakeTest2Crasher() {
			throw new Error();
		}
	}
}
