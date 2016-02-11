package hello.you;


public class Class1 extends AClass{
	public Class1() {
		Class2 c = new Class2();
		c.doNotReturn();
		c.arithmeticTest();
	}

	public void recursiveMethod(int i) {
		if(i > 0)
			recursiveMethod(i-1);
	}
}
