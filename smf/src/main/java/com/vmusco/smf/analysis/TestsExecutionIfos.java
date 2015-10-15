package com.vmusco.smf.analysis;

import java.util.HashSet;
import java.util.Set;

import com.vmusco.smf.exceptions.MutationNotRunException;
import com.vmusco.smf.utils.MutationsSetTools;

/**
 * This class contains all informations related to a run 
 * @author Vincenzo Musco - http://www.vmusco.com
 *
 */
public class TestsExecutionIfos {
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
	 * Stacktrace informations if available
	 */
	private String[][] stacktraces = null;
	
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
	 * To get a coherent and representative set, use {@link TestsExecutionIfos#getCoherentMutantFailingTestCases(ProcessStatistics)} or {@link TestsExecutionIfos#getCoherentMutantFailAndHangTestCases(ProcessStatistics)}
	 * @return
	 */
	public String[] getRawFailingTestCases() {
		return failingTestCases;
	}
	/**
	 * Take care, DO NOT USE IN COMPARISONS AND STATISTICS.
	 * This method do not takes into consideration the failing cases and the fact that some methods are similar in clean run.
	 * To get a coherent and representative set, use {@link TestsExecutionIfos#getCoherentMutantHangingTestCases(ProcessStatistics)} or {@link TestsExecutionIfos#getCoherentMutantFailAndHangTestCases(ProcessStatistics)}
	 * @return
	 */
	public String[] getRawHangingTestCases() {
		return hangingTestCases;
	}
	/**
	 * Take care, DO NOT USE IN COMPARISONS AND STATISTICS.
	 * This method do not takes into consideration the failing cases and the fact that some methods are similar in clean run.
	 * To get a coherent and representative set, use {@link TestsExecutionIfos#getCoherentMutantIgnoredTestCases(ProcessStatistics)}
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
	
	public void setStacktraces(String[][] array) {
		this.stacktraces = array;
	}
	
	public String[][] getStacktraces(){
		return this.stacktraces;
	}
}
