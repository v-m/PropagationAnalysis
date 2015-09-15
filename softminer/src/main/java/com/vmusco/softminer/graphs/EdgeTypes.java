package com.vmusco.softminer.graphs;

/**
*
* @author Vincenzo Musco - http://www.vmusco.com
*/
public enum EdgeTypes {
	READ_OPERATION, 
	WRITE_OPERATION,
	METHOD_CALL,
	INLINE_CONSTRUCTOR_CALL,
	INTERFACE_IMPLEMENTATION,
	ABSTRACT_METHOD_IMPLEMENTATION
}
