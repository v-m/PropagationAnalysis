package com.vmusco.smf.exceptions;

/**
 * Thrown exception if the position object is not correctly built, 
 * (may occurs if the element point to an implicit element of the code, 
 * ie. a super() call in a new object via hierarchy, the returned 
 * position could be negative).
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class MalformedSourcePositionException extends Exception {
	private static final long serialVersionUID = 1L;
}
