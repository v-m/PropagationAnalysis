package com.vmusco.smf.analysis;

public class MutantIfos{
	/**
	 * Describes the method in which the mutation occured (mutant id => method)
	 */
	public String mutationIn;
	/**
	 * Describes the mutation point (before) (mutant id => point)
	 */
	public String mutationFrom;
	/**
	 * Describes the mutation point (after) (mutant id => point)
	 */
	public String mutationTo;
	/**
	 * Describes the mutation viability (mutant id => true (compiles), false (fail))
	 */
	public boolean viable;
	
	/**
	 * The hash of the source generated.
	 * Obtained via {@link Mutation#getHashForMutationSource(java.io.File)}
	 */
	public String hash = null;
	
	public boolean excutedTests = false;
	
	/**
	 * Failing test cases for the mutant
	 */
	public String[] mutantFailingTestCases = null;
	/**
	 * Ignored test cases for the mutant
	 */
	public String[] mutantIgnoredTestCases = null;
	/**
	 * Hanging (infinite-loop) test cases for the mutant
	 */
	public String[] mutantHangingTestCases = null;
	/**
	 * Full test suite failure (eg. static field on init)
	 */
	public String[] mutantErrorOnTestSuite = null;
	public long runTestOnMutantTime = -1;
}