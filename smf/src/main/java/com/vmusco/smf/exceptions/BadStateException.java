package com.vmusco.smf.exceptions;

/**
 * This exception is thrown if the user invoke a method when the object is in a bad state for the operation
 * Eg. if the user ask to instrument after having compiled.
 * @author Vincenzo Musco - http://www.vmusco.com
 *
 */
public class BadStateException extends Exception {

}
