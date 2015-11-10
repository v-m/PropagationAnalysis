package com.vmusco.pminer.analyze;

import com.vmusco.smf.analysis.MutantIfos;

/**
 * Abstract class defining events for analyzing mutants
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public abstract class MutantTestAnalyzer {
	
	/***
	 * This method is called once before startng the execution (i.e. to display headers ?)
	 * Default behavior: None
	 */
	public void fireExecutionStarting(){}
	
	/***
	 * This method is called once the full test execution is ended (i.e. to display avg results ?)
	 * Default behavior: None
	 */
	public void fireExecutionEnded(){}

	public abstract void fireIntersectionFound(MutantIfos mi, String[] ais, String[] cis);
	public abstract void fireUnboundedFound(MutantIfos mi);
	public abstract void fireIsolatedFound(MutantIfos mi);
	
	/***
	 * This method can be invoked to order the runner to interrupt its execution and discard remaining set
	 * @return
	 */
	public boolean forceStop(){
		return false;
	}
}
