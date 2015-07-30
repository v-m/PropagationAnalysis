package com.vmusco.smf.testing;

public interface TestsExecutionListener {

	void testSuiteExecutionStart(int nbtest, int length, String cmd);
	void testCaseException(int nbtest, String readLine, String[] executedCommand);
	void testCaseExecutionFinished(int cpt, String[] all, String[] fail,
			String[] ignored, String[] hang);
	
	// Each test alert
	void testCaseNewFail(int cpt, String line);
	void testCaseNotPermitted(int cpt, String line);
	void testCaseNewIgnored(int cpt, String line);
	void testCaseEntered(int cpt, String line);			// On test case
	void testCaseUndeterminedTest(int cpt, String line);
	void testCaseOtherCase(int cpt, String line);
	void testCaseNewLoop(int cpt, String line);
	void testCaseFailureInfos(int cpt, String line);
	void testSuiteUnrunnable(int cpt, String aTest, String line);
	void currentTimeout(int timeout);
	void newTimeout(int timeout);
}
