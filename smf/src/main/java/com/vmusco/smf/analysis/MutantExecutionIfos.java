package com.vmusco.smf.analysis;

import java.util.HashSet;
import java.util.Set;

import com.vmusco.smf.exceptions.MutationNotRunException;
import com.vmusco.smf.utils.MutationsSetTools;

/**
 * This class contains all informations related to a mutant run 
 * @author Vincenzo Musco - http://www.vmusco.com
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
	
	/**
	 * Take care, DO NOT USE IN COMPARISONS AND STATISTICS.
	 * This method do not takes into consideration the failing cases and the fact that some methods are similar in clean run.
	 * @return
	 */
	public String[] getRawMutantErrorOnTestSuite() {
		return mutantErrorOnTestSuite;
	}
	/**
	 * Take care, DO NOT USE IN COMPARISONS AND STATISTICS.
	 * This method do not takes into consideration the failing cases and the fact that some methods are similar in clean run.
	 * To get a coherent and representative set, use {@link MutantExecutionIfos#getCoherentMutantFailingTestCases(ProcessStatistics)} or {@link MutantExecutionIfos#getCoherentMutantFailAndHangTestCases(ProcessStatistics)}
	 * @return
	 */
	public String[] getRawMutantFailingTestCases() {
		return mutantFailingTestCases;
	}
	/**
	 * Take care, DO NOT USE IN COMPARISONS AND STATISTICS.
	 * This method do not takes into consideration the failing cases and the fact that some methods are similar in clean run.
	 * To get a coherent and representative set, use {@link MutantExecutionIfos#getCoherentMutantHangingTestCases(ProcessStatistics)} or {@link MutantExecutionIfos#getCoherentMutantFailAndHangTestCases(ProcessStatistics)}
	 * @return
	 */
	public String[] getRawMutantHangingTestCases() {
		return mutantHangingTestCases;
	}
	/**
	 * Take care, DO NOT USE IN COMPARISONS AND STATISTICS.
	 * This method do not takes into consideration the failing cases and the fact that some methods are similar in clean run.
	 * To get a coherent and representative set, use {@link MutantExecutionIfos#getCoherentMutantIgnoredTestCases(ProcessStatistics)}
	 * @return
	 */
	public String[] getRawMutantIgnoredTestCases() {
		return mutantIgnoredTestCases;
	}
	public long getRunTestOnMutantTime(){
		return runTestOnMutantTime;
	}
	public void setMutantErrorOnTestSuite(String[] mutantErrorOnTestSuite) {
		this.mutantErrorOnTestSuite = mutantErrorOnTestSuite;
	}
	public void setMutantFailingTestCases(String[] mutantFailingTestCases) {
		this.mutantFailingTestCases = ProcessStatistics.fixTestSignatures(mutantFailingTestCases);
	}
	public void setMutantHangingTestCases(String[] mutantHangingTestCases) {
		this.mutantHangingTestCases = ProcessStatistics.fixTestSignatures(mutantHangingTestCases);
	}
	public void setMutantIgnoredTestCases(String[] mutantIgnoredTestCases) {
		this.mutantIgnoredTestCases = ProcessStatistics.fixTestSignatures(mutantIgnoredTestCases);
	}
	public void setRunTestOnMutantTime(long runTestOnMutantTime) {
		this.runTestOnMutantTime = runTestOnMutantTime;
	}
	
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
		String[] mutset = includeTestSuiteGlobalFailingCases(ps, getRawMutantErrorOnTestSuite(), getRawMutantFailingTestCases());
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
		String[] mutset = getRawMutantIgnoredTestCases();
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
		String[] mutset = getRawMutantHangingTestCases();
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

		for(String s : includeTestSuiteGlobalFailingCases(ps, getRawMutantErrorOnTestSuite(), null)){
			cases.add(s);
		}
		
		for(String s:getRawMutantHangingTestCases()){
			cases.add(s);
		}

		for(String s:getRawMutantFailingTestCases()){
			cases.add(s);
		}

		return MutationsSetTools.setDifference(cases.toArray(new String[0]), ps.getUnmutatedFailAndHang());
	}
}
