package com.vmusco.softminer.tests.cases.testExoObject;

import java.util.Calendar;

public class Main {
	public static void test(){
		Calendar c = Calendar.getInstance();
		
		Foo.biz(c);
		Bar.biz(c);
	}
}
