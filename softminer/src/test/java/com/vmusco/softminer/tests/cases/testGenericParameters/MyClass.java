package com.vmusco.softminer.tests.cases.testGenericParameters;

import java.util.concurrent.atomic.AtomicReference;

/***
 * 
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class MyClass {
	public static <L> void bar(Class<L> param1, L param2) {
        return;
    }
	
	public static <L, M, N, O> void bar2(Class<L> param1, M param2, Class<N> param3, O param4, L param5, String param6, O param7) {
        bar(String.class, "");
    }
	
	public static void biz(){
		AtomicReference<String> test = new AtomicReference<String>();
		test.compareAndSet("a", "b");
	}

	public void foo(){
		String test = "";
		
		bar(String.class, test);
	}

	public void foo2(){
		String aString = "";
		String aSecondString = "";
		String aThirdString = "";
		Integer anInt = 1;
		Integer aSecondInt = 2;

		bar2(String.class, aString, Integer.class, anInt, aSecondString, aThirdString, aSecondInt);
	}
	
	public <L> void foo3(L param){
		foo4(param, "hello");
	}
	
	public <L,M> void foo4(L param, M param2){
		biz();
	}


}
