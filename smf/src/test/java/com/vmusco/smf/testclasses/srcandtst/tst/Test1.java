package com.vmusco.smf.testclasses.srcandtst.tst;

import org.junit.Assert;
import org.junit.Test;

import com.vmusco.smf.testclasses.srcandtst.src.*;

public class Test1 {
	@Test
	public void mytest(){
		Class1 c1 = new Class1();
		Class3 c3 = new Class3();

		Assert.assertTrue(c1.returnTrue());
		Assert.assertFalse(c3.returnFalse());
	}
}
