package com.vmusco.smf.analysis;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.vmusco.smf.exceptions.BadStateException;

/**
 * This class contains all informations related to a run 
 * @author Vincenzo Musco - http://www.vmusco.com
 *
 */
public class TestsExecutionIfos {
	public TestsExecutionIfos() {
	}
	
	/**
	 * Copy constructor -- no deep copying !
	 * @param base
	 */
	public TestsExecutionIfos(TestsExecutionIfos base){
		this.failingTestCases = base.failingTestCases; 
		this.ignoredTestCases = base.ignoredTestCases; 
		this.hangingTestCases = base.hangingTestCases; 
		this.errorOnTestSuite = base.errorOnTestSuite; 
		this.calledNodes = base.calledNodes; 
		this.runTestsTime = base.runTestsTime; 
		this.timeout = base.timeout; 
		this.tests_arr = base.tests_arr; 
	}
	
	/**
	 * Failing test cases for the mutant
	 */
	private String[] failingTestCases = null;
	/**
	 * Ignored test cases for the mutant
	 */
	private String[] ignoredTestCases = null;
	/**
	 * Hanging (infinite-loop) test cases for the mutant
	 */
	private String[] hangingTestCases = null;
	/**
	 * Full test suite failure (eg. static field on init)
	 */
	private String[] errorOnTestSuite = null;

	/**
	 * Called nodes when running a test. Generally are application nodes.
	 * This field may be null if no instrumented analysis has been run 
	 * or if this information has not been read from file
	 */
	private Map<String, String[]> calledNodes = null;

	/**
	 * Stacktrace informations if available
	 */
	//private String[][] stacktraces = null;

	private long runTestsTime = -1;
	private int timeout = -1;
	/**
	 * This content is NOT directly persisted !
	 */
	private String[] tests_arr;

	/**
	 * Take care, DO NOT USE IN COMPARISONS AND STATISTICS.
	 * This method do not takes into consideration the failing cases and the fact that some methods are similar in clean run.
	 * @return
	 */
	public String[] getRawErrorOnTestSuite() {
		return errorOnTestSuite;
	}
	/**
	 * Take care, DO NOT USE IN COMPARISONS AND STATISTICS.
	 * This method do not takes into consideration the failing cases and the fact that some methods are similar in clean run.
	 * To get a coherent and representative set, use {@link ProcessStatistics#getCoherentMutantFailingTestCases(TestsExecutionIfos)}.
	 * @return
	 */
	public String[] getRawFailingTestCases() {
		return failingTestCases;
	}
	/**
	 * Take care, DO NOT USE IN COMPARISONS AND STATISTICS.
	 * This method do not takes into consideration the failing cases and the fact that some methods are similar in clean run.
	 * To get a coherent and representative set, use {@link ProcessStatistics#getCoherentMutantHangingTestCases(TestsExecutionIfos)}.
	 * @return
	 */
	public String[] getRawHangingTestCases() {
		return hangingTestCases;
	}
	/**
	 * Take care, DO NOT USE IN COMPARISONS AND STATISTICS.
	 * This method do not takes into consideration the failing cases and the fact that some methods are similar in clean run.
	 * To get a coherent and representative set, use {@link ProcessStatistics#getCoherentMutantIgnoredTestCases(TestsExecutionIfos)}.
	 * @return
	 */
	public String[] getRawIgnoredTestCases() {
		return ignoredTestCases;
	}
	public long getRunTestsTime(){
		return runTestsTime;
	}
	public void setErrorOnTestSuite(String[] mutantErrorOnTestSuite) {
		this.errorOnTestSuite = mutantErrorOnTestSuite;
	}
	public void setFailingTestCases(String[] mutantFailingTestCases) {
		this.failingTestCases = ProcessStatistics.fixTestSignatures(mutantFailingTestCases);
	}
	public void setHangingTestCases(String[] mutantHangingTestCases) {
		this.hangingTestCases = ProcessStatistics.fixTestSignatures(mutantHangingTestCases);
	}
	public void setIgnoredTestCases(String[] mutantIgnoredTestCases) {
		this.ignoredTestCases = ProcessStatistics.fixTestSignatures(mutantIgnoredTestCases);
	}
	public void setRunTestsTime(long runTestOnMutantTime) {
		this.runTestsTime = runTestOnMutantTime;
	}

	public void addCalledNodeInformation(String concernedTest, String[] concernedCalledNodes){
		if(calledNodes == null)
			calledNodes = new HashMap<>();

		calledNodes.put(concernedTest, concernedCalledNodes);
	}
	
	public void setCalledNodeInformation(Map<String, String[]> calledNodes){
		this.calledNodes = calledNodes;
	}

	public String[] getTestsWithCalledInformations() throws BadStateException{
		if(calledNodes == null)
			throw new BadStateException("No dynamic execution of injected software has been found !");

		return calledNodes.keySet().toArray(new String[calledNodes.keySet().size()]);
	}

	public String[] getCalledNodes(String forTest) throws BadStateException{
		if(calledNodes == null)
			throw new BadStateException("No dynamic execution of injected software has been found !");

		return calledNodes.get(forTest);
	}
	
	public Map<String, String[]> getCalledNodes() {
		return calledNodes;
	}

	public String[] getAllCalledNodesForAllTests() throws BadStateException{
		if(calledNodes == null)
			throw new BadStateException("No dynamic execution of injected software has been found !");

		Set<String> ret = new HashSet<String>();

		for(String k : calledNodes.keySet()){
			for(String v : calledNodes.get(k)){
				ret.add(v);
			}
		}

		return ret.toArray(new String[ret.size()]);
	}
	
	/**
	 * This content is NOT directly persisted !
	 */
	public void setAllRunnedTests(String[] tests_arr) {
		this.tests_arr = tests_arr;
	}

	/**
	 * This content is NOT directly persisted !
	 */
	public String[] getAllRunnedTests() {
		return this.tests_arr;
	}



	public void setTestTimeOut(int timeout) {
		this.timeout = timeout;
	}

	public int getTestTimeOut(){
		return timeout;
	}

	/*public void setStacktraces(String[][] array) {
		this.stacktraces = array;
	}

	public String[][] getStacktraces(){
		return this.stacktraces;
	}*/
}
