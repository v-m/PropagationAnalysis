package com.vmusco.smf.testing;


public class TestingInstrumentedCodeHelper {
	/***********************************
	 *  Global behavior configuration  *
	 ***********************************/	
	private static boolean ENABLE_ENTERING = true;
	private static boolean ENABLE_LEAVING = true;
	private static boolean ENABLE_STACKTRACE = false;

	public static void setEnteringPrinting(boolean isYes) {
		ENABLE_ENTERING = isYes;
	}
	
	public static void setLeavingPrinting(boolean isYes) {
		ENABLE_LEAVING = isYes;
	}
	
	public static boolean isEnteringPrinting() {
		return ENABLE_ENTERING;
	}
	
	public static boolean isLeavingPrinting() {
		return ENABLE_LEAVING;
	}

	
	
	
	public static void setStacktracePrinting(boolean isYes) {
		ENABLE_STACKTRACE = isYes;
	}
	
	public static boolean isStacktracePrinting() {
		return ENABLE_STACKTRACE;
	}
	
	
	
	
	
	
	/********************
	 *  Injected logic  *
	 ********************/
	
	/** Stack trace printing **/
	public static final String STACKTRACELINE = ((char)2)+">STACKTRACE>";
	public static final String STACKTRACESTART = ((char)2)+">STACKTRACE>START!";
	public static final String STACKTRACEEND = ((char)2)+">STACKTRACE>END!";

	/** Entering/leaving method **/

	public static final String STARTKEY = ((char)2)+"=EMINSTR=>";
	public static final String ENDKEY = ((char)2)+"=LMINSTR=>";
	public static final String THROWKEY = ((char)2)+"=LMTINSTR=>";
	public static final String RETURNKEY = ((char)2)+"=LMRINSTR=>";

	public static void printStackTrace(){
		if(!ENABLE_STACKTRACE)
			return;
		
		int i = 0;
		System.out.println(STACKTRACESTART);
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		
		
		for(int is = 0; is<stackTrace.length;is++){
			StackTraceElement tce = stackTrace[is];
			if(i++ < 2)
				continue;

			System.out.println(STACKTRACELINE+tce.getClassName()+"/"+tce.getMethodName()+"/"+tce.getFileName()+"/"+tce.getLineNumber());
		}
		System.out.println(STACKTRACEEND);
	}


	public static void printMethodEntering(String methodId){
		if(!ENABLE_ENTERING)
			return;
		
		System.out.println(STARTKEY+methodId);
	}

	public static void printMethodExiting(String methodId){
		if(!ENABLE_LEAVING)
			return;
		
		System.out.println(ENDKEY+methodId);
	}

	public static void printMethodThrow(String methodId){
		if(!ENABLE_LEAVING)
			return;
	
		System.out.println(THROWKEY+methodId);
	}

	public static void printMethodReturn(String methodId){
		if(!ENABLE_LEAVING)
			return;
		
		System.out.println(RETURNKEY+methodId);
	}

}