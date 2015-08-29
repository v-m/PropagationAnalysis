package com.vmusco.smf.utils;

public abstract class InterruptionManager {
	private static InterruptionDemander demander = null;
	
	public static void initInterruptibleTask(){
		demander = null;
	}
	
	public static void interruptDemanded(InterruptionDemander id){
		InterruptionManager.demander = id;
	}
	
	public static void notifyLastIterationFinished(){
		InterruptionManager.demander.initShutdown();
	}
	
	public static boolean isInterruptedDemanded(){
		return demander != null;
	}
}
