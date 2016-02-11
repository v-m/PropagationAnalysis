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

		c1.recursiveMethod(100);
		Assert.assertTrue(c1.returnTrue());
		Assert.assertFalse(c3.returnFalse());
	}
	
	public void testFunction(){
		Class1 c1 = new Class1();
		Class3 c3 = new Class3();

		Assert.assertTrue(c1.returnTrue());
		Assert.assertFalse(c3.returnFalse());
	}
}
