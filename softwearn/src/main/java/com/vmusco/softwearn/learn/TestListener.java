package com.vmusco.softwearn.learn;

import com.vmusco.smf.analysis.MutantIfos;

/**
 * Notify the end of a mutant analysis against the learning process
 * @author Vincenzo Musco - http://www.vmusco.com
 *
 */
public interface TestListener {
	public void testResult(MutantIfos mi, String[] impactedTests);
	public void oneFoldEnded();
	void allFoldsEnded();
}
