package com.vmusco.smf.analysis;

import com.vmusco.smf.analysis.persistence.MutantInfoXMLPersisitence;

/**
 * This class bundles all informations relatives to one mutation
 * @author Vincenzo Musco - http://www.vmusco.com
 * @see MutantInfoXMLPersisitence
 */
public class MutantIfos{
	/**
	 * Describes the method in which the mutation occured (mutant id => method)
	 */
	private String mutationIn;
	/**
	 * Describes the mutation point (before) (mutant id => point)
	 */
	private String mutationFrom;
	/**
	 * Describes the mutation point (after) (mutant id => point)
	 */
	private String mutationTo;
	/**
	 * Describes the mutation viability (mutant id => true (compiles), false (fail))
	 */
	private boolean viable;
	
	/**
	 * The hash of the source generated.
	 * Obtained via {@link Mutation#getHashForMutationSource(java.io.File)}
	 */
	private String hash = null;
	
	private Boolean executedTests = null;
	
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
	
	
	
	
	
	public String getMutationIn() {
		return mutationIn;
	}
	
	public void setMutationIn(String mutationIn) {
		this.mutationIn = mutationIn;
	}
	
	public String getMutationFrom() {
		return mutationFrom;
	}
	
	public String getMutationTo() {
		return mutationTo;
	}
	
	public void setMutationFrom(String mutationFrom) {
		this.mutationFrom = mutationFrom;
	}
	
	public void setMutationTo(String mutationTo) {
		this.mutationTo = mutationTo;
	}
	
	public boolean isViable() {
		return viable;
	}
	
	public void setViable(boolean viable) {
		this.viable = viable;
	}
	
	public void setHash(String hash) {
		this.hash = hash;
	}
	
	public String getHash() {
		return hash;
	}
	
	/**
	 * Determine whether the tests has already been run for this mutant.
	 * Note that if the answer is undetermined, the function will also return false.
	 * To ensure the state is known, use {@link MutantIfos#isExecutionKnown()}.
	 * @return true if the test has been run on this mutant, else false
	 */
	public boolean isExecutedTests() {
		return executedTests!=null?executedTests:false;
	}
	
	public void setExecutedTests(boolean excutedTests) {
		this.executedTests = excutedTests;
	}
	
	/**
	 * To ensure compatibility with older versions of the software, this method allows to 
	 * ensure if the xml file contains informations about exeuction. If false, a manual determination
	 * is required (see {@link MutationStatistics#loadResultsForExecutedTestOnMutants(int)} for updating the datastructure
	 * or {@link MutationStatistics#checkIfExecutionExists(String)}.
	 * @return
	 * @see MutationStatistics#loadResultsForExecutedTestOnMutants(int)
	 */
	public boolean isExecutionKnown(){
		return executedTests!=null;
	}
	
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
	public long getRunTestOnMutantTime() {
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