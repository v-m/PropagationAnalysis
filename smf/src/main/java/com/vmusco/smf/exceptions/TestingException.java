package com.vmusco.smf.exceptions;

public class TestingException extends Exception{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Exception thrownException;

	
	public TestingException() {
		super();
	}
	
	public TestingException(String message) {
		super(message);
	}
	
	public TestingException(Exception thrown) {
		this.thrownException = thrown;
	}
	
	public Exception getThrownException() {
		return thrownException;
	}
	
	@Override
	public String getMessage() {
		if(this.thrownException != null){
			return String.format("Thrown exception: %s", thrownException.getMessage());
		}else{
			return super.getMessage();
		}
	}
}
