package com.vmusco.smf.testing;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

public class TestExec implements Runnable {
	public static String[] c;
	
	@Override
	public void run() {
		Class[] cc = new Class[c.length];
		int i = 0;
		
		for(String s : c){
			try {
				cc[i++] = Class.forName(s);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				return;
			}
		}
		
		Result result = JUnitCore.runClasses(cc);
	}
}
