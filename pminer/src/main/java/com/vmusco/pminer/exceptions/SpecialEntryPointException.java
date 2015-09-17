package com.vmusco.pminer.exceptions;

/**
 * This exception is thrown when trying to access a subgraph which has not been generated due to 
 * missing entry point in graph or a special entry point (eg. isolated node)
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class SpecialEntryPointException extends Exception {
	private static final long serialVersionUID = 1L;
	private TYPE type;
	
	public SpecialEntryPointException(TYPE type) {
		this.type = type;
	}
	
	public TYPE getType() {
		return type;
	}
	
	public enum TYPE{
		NOT_FOUND,
		ISOLATED
	}; 
}
