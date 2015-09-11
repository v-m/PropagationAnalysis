package com.vmusco.smf.exceptions;

/**
 * This exception is a meta exception for error handling while loading/saving state
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class PersistenceException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public Exception underException;
	
	public PersistenceException(Exception under) {
		this.underException = under;
	}
	
	public Exception getUnderException() {
		return underException;
	}
}
