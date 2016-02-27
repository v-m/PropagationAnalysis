package com.vmusco.smf.testing;

import com.vmusco.smf.exceptions.MutantHangsException;

/**
 * Event listener for test execution (mutants exploration)
 * @author Vincenzo Musco - http://www.vmusco.com
 *
 */
public interface TestingNotification extends TestsExecutionListener{
	void mutantStarted(String id);
	void mutantPersisting(String id);
	void mutantSkippedDueToException(String id);
	void mutantEnded(String id);
	void mutantLocked();
	void mutantException(Exception e);
	void mutantAlreadyDone();
	void mutantHangs(String id);
}
