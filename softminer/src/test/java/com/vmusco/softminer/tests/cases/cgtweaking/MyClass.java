package com.vmusco.softminer.tests.cases.cgtweaking;

public class MyClass {
	private int val;
	
	public MyClass(int param) {
		val = param;
	}
	
	public int foo(){
		val += 1;
		return val;
	}
	
	public boolean bar(){
		return true;
	}
	
	public void biz(){
		if(bar()){
			foo();
		}
		
		if(foo() > 0){
			System.out.println("Hello world !");
		}
	}
}
