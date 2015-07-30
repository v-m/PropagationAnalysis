package com.vmusco.smf.testing;

public interface TestingNotification extends TestsExecutionListener{
	void mutantStarted(String id);
	void mutantPersisting(String id);
	void mutantSkippedDueToException(String id);
	void mutantEnded(String id);
	void mutantLocked();
	void mutantException(Exception e);
	void mutantAlreadyDone();
}
