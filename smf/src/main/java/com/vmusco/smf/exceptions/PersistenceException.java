package com.vmusco.smf.exceptions;

import java.io.File;

/**
 * This exception is a meta exception for error handling while loading/saving state
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class PersistenceException extends Exception {
	private static final long serialVersionUID = 1L;
	private File f;
	
	public Exception underException;
	
	public PersistenceException(Exception under) {
		this.underException = under;
	}
	
	public PersistenceException(String msg) {
		super(msg);
	}

	public Exception getUnderException() {
		return underException;
	}
	
	public void setFile(File f) {
		this.f = f;
	}
	
	public File getFile() {
		return f;
	}
}
