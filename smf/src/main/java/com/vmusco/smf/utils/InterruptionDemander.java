package com.vmusco.smf.utils;

public class InterruptionDemander extends Thread {
	private static Object lock = new Object();

	@Override
	public void run() {
		synchronized (lock) {

			System.out.println("Interruption demanded... Please wait, clean exiting...");
			InterruptionManager.interruptDemanded(this);
			try {
				lock.wait();
			} catch (InterruptedException e) {
				System.out.println("InterruptionDemander: Lock failed due to an interrupt !");
				e.printStackTrace();
			}
		}
	}

	public void initShutdown(){
		synchronized (lock) {
			lock.notify();
		}
	}

	public void shutdownProcess(){
		
	}
}
