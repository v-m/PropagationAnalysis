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
	private long runTestsTime = -1;
	private int timeout = -1;
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
	
	
	
	
	
	
	
	
	
	
	
	//TODO: the end of this file should be moved for coherency with reusability
	private static String[] includeTestSuiteGlobalFailingCases(ProcessStatistics ps, String[] testsuites, String[] include){
		Set<String> cases = new HashSet<String>();

		if(include != null){
			for(String s : include){
				cases.add(s);
			}
		}

		for(String ts : testsuites){
			for(String s : ps.getTestCases()){
				if(s.startsWith(ts)){
					cases.add(s);
				}
			}
		}

		return cases.toArray(new String[0]);
	}

	/**
	 * This method return the failing function after...
	 *  - removing functions already failing on execution on the unmutated version of the software;
	 *  - adding all functions included in test suite global failing methods.
	 * @param ps The {@link ProcessStatistics} object which describes the execution
	 * @return 
	 * @throws MutationNotRunException
	 */
	public String[] getCoherentMutantFailingTestCases(ProcessStatistics ps) throws MutationNotRunException {
		String[] mutset = includeTestSuiteGlobalFailingCases(ps, getRawErrorOnTestSuite(), getRawFailingTestCases());
		String[] glbset = includeTestSuiteGlobalFailingCases(ps, ps.getErrorOnTestSuite(), ps.getFailingTestCases());

		return MutationsSetTools.setDifference(mutset, glbset);
	}

	/**
	 * This method return the ignored function after removing functions already ignored on execution on the unmutated version of the software;
	 * @param ps The {@link ProcessStatistics} object which describes the execution
	 * @return 
	 * @throws MutationNotRunException
	 */
	public String[] getCoherentMutantIgnoredTestCases(ProcessStatistics ps) throws MutationNotRunException {
		String[] mutset = getRawIgnoredTestCases();
		String[] glbset = ps.getIgnoredTestCases();

		return MutationsSetTools.setDifference(mutset, glbset);
	}

	/**
	 * This method return the hanging function after removing functions already hanging on execution on the unmutated version of the software;
	 * @param ps The {@link ProcessStatistics} object which describes the execution
	 * @return 
	 * @throws MutationNotRunException
	 */
	public String[] getCoherentMutantHangingTestCases(ProcessStatistics ps) throws MutationNotRunException {
		String[] mutset = getRawHangingTestCases();
		String[] glbset = ps.getHangingTestCases();

		return MutationsSetTools.setDifference(mutset, glbset);
	}

	/**
	 * Includes tests hanging when the whole test case fail, the failing and the hanging cases in one shot.
	 * The result do not includes the elements already failing or hanging in the execution of the un mutated version of the code. 
	 * @param ps The {@link ProcessStatistics} object which describes the execution
	 * @return
	 * @throws MutationNotRunException
	 */
	public String[] getCoherentMutantFailAndHangTestCases(ProcessStatistics ps) throws MutationNotRunException {
		Set<String> cases = new HashSet<String>();

		for(String s : includeTestSuiteGlobalFailingCases(ps, getRawErrorOnTestSuite(), null)){
			cases.add(s);
		}
		
		for(String s:getRawHangingTestCases()){
			cases.add(s);
		}

		for(String s:getRawFailingTestCases()){
			cases.add(s);
		}

		return MutationsSetTools.setDifference(cases.toArray(new String[0]), ps.getUnmutatedFailAndHang());
	}
	public void setTestTimeOut(int timeout) {
		this.timeout = timeout;
	}
	
	public int getTestTimeOut(){
		return timeout;
	}
	
	public void setAllRunnedTests(String[] tests_arr) {
		this.tests_arr = tests_arr;
	}
	
	public String[] getAllRunnedTests() {
		return this.tests_arr;
	}
}
