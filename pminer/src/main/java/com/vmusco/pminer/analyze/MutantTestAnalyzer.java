package com.vmusco.pminer.analyze;

/**
 * Abstract class defining events for analyzing mutants
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public abstract class MutantTestAnalyzer {
	
	/***
	 * This method is called once before startng the execution (i.e. to display headers ?)
	 * Default behavior: None
	 */
	public void executionStarting(){}
	
	/***
	 * This method is called once the full test execution is ended (i.e. to display avg results ?)
	 * Default behavior: None
	 */
	public void executionEnded(){}

	public abstract void intersectionFound(String id, String in, String[] ais, String[] cis);
	public abstract void unboundedFound(String id, String in);
	public abstract void isolatedFound(String id, String in);

	/***
	 * This method can be invoked to order the runner to interrupt its execution and discard remaining set
	 * @return
	 */
	public boolean forceStop(){
		return false;
	}
}
