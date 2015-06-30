package com.vmusco.softminer.tests.cases.testExoInterface;

import java.awt.event.ActionListener;

import javax.swing.AbstractAction;

public class Test {
	public void test(){
		ActionListener foo = new Foo();
		ActionListener bar = new Bar();
		
		foo.actionPerformed(null);
		bar.actionPerformed(null);

		AbstractAction foo2 = new Foo2();
		AbstractAction bar2 = new Bar2();
		
		foo2.actionPerformed(null);
		bar2.actionPerformed(null);
	}
}
