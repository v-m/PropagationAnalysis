package com.vmusco.smf.exceptions;

/**
* This exception is throws when trying to work with a mutation instance which is not yet run
* @author Vincenzo Musco - http://www.vmusco.com
*/
public class MutationNotRunException extends Exception {
	private static final long serialVersionUID = 1L;

	public MutationNotRunException(String id) {
		super(String.format("Mutation %s is not run !", id));
	}
}
