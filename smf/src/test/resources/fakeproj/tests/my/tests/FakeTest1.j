package my.tests;

import org.junit.Assert;
import org.junit.Test;
import org.junit.Ignore;

public class FakeTest1 {
	
	@Test
	public void failingTest(){
		Assert.fail();
	}

	@Test
	public void passingTest1(){
	}
	
	@Test
	public void passingTest2(){
	}
	
	@Test
	public void passingTest3(){
	}

	@Test
	public void hangingTest(){
		while(true){}
	}

	@Test
	public void exceptionTest(){
		throw new Error();
	}
	
	@Test@Ignore
	public void ignoredTest(){
	}
}
