package com.vmusco.pminer.analyze;

import java.util.HashSet;
import java.util.Set;

import com.vmusco.pminer.impact.PropagationExplorer;
import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.ProcessStatistics;
import com.vmusco.smf.exceptions.MutationNotRunException;


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

	public abstract void fireIntersectionFound(ProcessStatistics ps, MutantIfos mi, String[] impactedNodes, String[] impactedTests) throws MutationNotRunException;
	
	/***
	 * This method can be invoked to order the runner to interrupt its execution and discard remaining set
	 * @return
	 */
	public boolean forceStop(){
		return false;
	}
}
