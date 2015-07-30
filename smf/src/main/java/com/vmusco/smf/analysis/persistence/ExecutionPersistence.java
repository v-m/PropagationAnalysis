package com.vmusco.smf.analysis.persistence;

import java.io.File;

/**
 * This abstract class allows to define persistence functions for varios T obkects
 * @author Vincenzo Musco - http://www.vmusco.com
 * @param <T> the type of objects to persist 
 */
public abstract class ExecutionPersistence<T> {
	protected File f;
	
	public ExecutionPersistence(File f) {
		this.f = f;
	}
	
	public abstract T loadState() throws Exception;
	public abstract void loadState(T updateMe) throws Exception;
	public abstract void saveState(T persistObject) throws Exception;
}
