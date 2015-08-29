package com.vmusco.smf.analysis.persistence;

import java.io.File;
import java.io.IOException;

import com.vmusco.smf.exceptions.PersistenceException;

/**
 * This abstract class allows to define persistence functions for various T objects
 * It is implementation independent
 * @author Vincenzo Musco - http://www.vmusco.com
 * @param <T> the type of objects to persist 
 */
public abstract class ExecutionPersistence<T> {	
	public abstract T loadState() throws PersistenceException;
	public abstract void loadState(T updateMe) throws PersistenceException;
	public abstract void saveState(T persistObject) throws PersistenceException;
}
