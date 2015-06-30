package com.vmusco.smf.analysis.persistence;

import java.io.File;
import java.io.IOException;

import com.vmusco.smf.analysis.ProcessStatistics;

public abstract class ExecutionPersistence<T> {
	protected File f;
	
	public ExecutionPersistence(File f) {
		this.f = f;
	}
	
	public abstract T loadState() throws Exception;
	public abstract void loadState(T updateMe) throws Exception;
	public abstract void saveState(T persistObject) throws Exception;
}
