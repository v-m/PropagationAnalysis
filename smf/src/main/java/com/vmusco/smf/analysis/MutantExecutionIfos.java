package com.vmusco.smf.analysis;

/**
 * This class contains all informations related to a mutant run 
 * @author Vincenzo Musco - vincenzo.musco@inria.fr
 *
 */
public class MutantExecutionIfos {
	/**
	 * Failing test cases for the mutant
	 */
	private String[] mutantFailingTestCases = null;
	/**
	 * Ignored test cases for the mutant
	 */
	private String[] mutantIgnoredTestCases = null;
	/**
	 * Hanging (infinite-loop) test cases for the mutant
	 */
	private String[] mutantHangingTestCases = null;
	/**
	 * Full test suite failure (eg. static field on init)
	 */
	private String[] mutantErrorOnTestSuite = null;
	private long runTestOnMutantTime = -1;
	
	public String[] getMutantErrorOnTestSuite() {
		return mutantErrorOnTestSuite;
	}
	
	public String[] getMutantFailingTestCases() {
		return mutantFailingTestCases;
	}
	public String[] getMutantHangingTestCases() {
		return mutantHangingTestCases;
	}
	public String[] getMutantIgnoredTestCases() {
		return mutantIgnoredTestCases;
	}
	public long getRunTestOnMutantTime(){
		return runTestOnMutantTime;
	}
	public void setMutantErrorOnTestSuite(String[] mutantErrorOnTestSuite) {
		this.mutantErrorOnTestSuite = mutantErrorOnTestSuite;
	}
	public void setMutantFailingTestCases(String[] mutantFailingTestCases) {
		this.mutantFailingTestCases = mutantFailingTestCases;
	}
	public void setMutantHangingTestCases(String[] mutantHangingTestCases) {
		this.mutantHangingTestCases = mutantHangingTestCases;
	}
	public void setMutantIgnoredTestCases(String[] mutantIgnoredTestCases) {
		this.mutantIgnoredTestCases = mutantIgnoredTestCases;
	}
	public void setRunTestOnMutantTime(long runTestOnMutantTime) {
		this.runTestOnMutantTime = runTestOnMutantTime;
	}
}
