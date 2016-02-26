package com.vmusco.smf.utils;

/**
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class SafeInterruption extends Thread{
	private Object lock = new Object();
	private boolean interruptDemanded = false;
	private boolean disabled = false;
	
	@Override
	public void run() {
		if(disabled)
			return;
		
		synchronized (lock) {

			System.out.println("Received interrupt... Please wait, clean exiting...");
			interruptDemanded = true;
			
			try {
				lock.wait();
			} catch (InterruptedException e) {
				System.out.println("InterruptionDemander: Lock failed due to an interrupt !");
				e.printStackTrace();
			}
		}
	}

	public void shutdown(){
		synchronized (lock) {
			lock.notifyAll();
		}
	}

	public void shutdownProcess(){
		
	}
	
	public boolean isInterruptDemanded() {
		return interruptDemanded;
	}

	public void disableBlocker() {
		disabled  = true;
	}

}
