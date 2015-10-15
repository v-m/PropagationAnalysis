package hello.you.tests;

import hello.you.Class1;
import hello.you.Class3;

import org.junit.Assert;
import org.junit.Test;

public class Test1{
	@Test
	public void mytest(){
		Class1 c1 = new Class1();
		Class3 c3 = new Class3();

		System.out.println("Hello I am JUNIT 4.x");
		
		Assert.assertTrue(c1.returnTrue());
		System.out.println("I print several");
		Assert.assertFalse(c3.returnFalse());
		System.out.println(" messages along your console ! :)");
	}
	
	public void testFunction(){
		Class1 c1 = new Class1();
		Class3 c3 = new Class3();

		System.out.println("Hello I am JUNIT 3.x");
		Assert.assertTrue(c1.returnTrue());
		Assert.assertFalse(c3.returnFalse());
	}
}
