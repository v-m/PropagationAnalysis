package com.vmusco.smf.testing;

public class TestingHelper {
	public static final String STACKTRACELINE = ((char)2)+">STACKTRACE>";
	
	public static void printStackTrace(){
		int i = 0;
		for(StackTraceElement tce : Thread.currentThread().getStackTrace()){
			if(i++ < 2)
				continue;
			System.out.println(STACKTRACELINE+tce.getClassName()+"/"+tce.getMethodName()+"/"+tce.getFileName()+"/"+tce.getLineNumber());
		}
	}
}
